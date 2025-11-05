# WebSocket 구독 가이드

## 변경 사항

플레이어별 맞춤형 게임 상태를 제공하기 위해 WebSocket 토픽 구조가 변경되었습니다.

## 이전 방식 (작동하지 않음)
```javascript
// ❌ 모든 플레이어가 동일한 토픽 구독
stompClient.subscribe('/topic/game.' + roomId, callback);
```

## 새로운 방식 (필수)

### 1. 방 참가 시 색상 정보 저장

```javascript
// POST /api/game/join 또는 POST /api/game/rooms/{roomId}/join
const response = await fetch('/api/game/join', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ playerId: myPlayerId })
});

const data = await response.json();
const roomId = data.roomId;
const playerColor = data.playerColor;  // "RED" 또는 "BLUE"

// 색상 정보 저장
localStorage.setItem('playerColor', playerColor);
localStorage.setItem('roomId', roomId);
```

### 2. 색상별 토픽 구독

```javascript
// ✅ 자신의 색상에 맞는 토픽만 구독
const roomId = localStorage.getItem('roomId');
const playerColor = localStorage.getItem('playerColor');

// RED 플레이어는 /topic/game.{roomId}.RED 구독
// BLUE 플레이어는 /topic/game.{roomId}.BLUE 구독
const topic = `/topic/game.${roomId}.${playerColor}`;

stompClient.subscribe(topic, (message) => {
  const gameState = JSON.parse(message.body);
  updateGameBoard(gameState);
});
```

### 3. 완전한 예제

```javascript
// WebSocket 연결 및 구독
function connectWebSocket(roomId, playerColor) {
  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('WebSocket connected');

    // 자신의 색상 토픽 구독
    const topic = `/topic/game.${roomId}.${playerColor}`;
    console.log('Subscribing to:', topic);

    stompClient.subscribe(topic, (message) => {
      const gameState = JSON.parse(message.body);
      console.log('Received game state:', gameState);

      // pieces 배열에서:
      // - 자신의 말: type 정보 있음
      // - 상대 말 (노출 안됨): type = null
      // - 상대 말 (척후병에게 노출됨): type 정보 있음, revealed = true

      updateGameBoard(gameState);
    });
  });

  return stompClient;
}

// 방 참가 및 WebSocket 연결
async function joinGame(playerId) {
  // 1. REST API로 방 참가
  const response = await fetch('/api/game/join', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ playerId })
  });

  const data = await response.json();
  const roomId = data.roomId;
  const playerColor = data.playerColor;

  console.log(`Joined room ${roomId} as ${playerColor}`);

  // 2. WebSocket 연결
  const stompClient = connectWebSocket(roomId, playerColor);

  return { roomId, playerColor, stompClient };
}
```

## 게임 상태 응답 구조

```javascript
{
  "roomId": "abc123",
  "currentTurn": "RED",
  "status": "PLAYING",
  "playerColor": "RED",  // 이 플레이어의 색상
  "pieces": [
    {
      "id": "R1",
      "color": "RED",
      "type": "MAJOR_GENERAL",  // 자신의 말은 항상 type 있음
      "position": { "x": 4, "y": 3 },
      "captured": false,
      "revealed": false
    },
    {
      "id": "B1",
      "color": "BLUE",
      "type": null,  // 상대 말(노출 안됨) - 뒤집어서 표시
      "position": { "x": 8, "y": 3 },
      "captured": false,
      "revealed": false
    },
    {
      "id": "B2",
      "color": "BLUE",
      "type": "SCOUT",  // 상대 말(척후병에게 노출됨) - 앞면 보임!
      "position": { "x": 9, "y": 3 },
      "captured": false,
      "revealed": true  // 척후병에게 노출됨
    }
  ]
}
```

## 말 렌더링 로직

```javascript
function renderPiece(piece, myColor) {
  if (piece.captured) {
    return null;  // 제거된 말은 표시 안 함
  }

  if (piece.color === myColor) {
    // 내 말 - 항상 앞면
    return renderPieceFront(piece.type);
  } else {
    // 상대 말
    if (piece.revealed || piece.type !== null) {
      // 노출됨 - 앞면 표시
      return renderPieceFront(piece.type);
    } else {
      // 노출 안됨 - 뒷면 표시
      return renderPieceBack(piece.color);
    }
  }
}
```

## 주의사항

1. **반드시 자신의 색상 토픽만 구독하세요**
   - RED: `/topic/game.{roomId}.RED`
   - BLUE: `/topic/game.{roomId}.BLUE`

2. **playerColor는 방 참가 시 서버에서 할당됩니다**
   - 먼저 참가한 플레이어: RED
   - 두 번째 플레이어: BLUE

3. **척후병 노출 규칙**
   - 척후병을 이긴 말은 `revealed: true`로 설정됨
   - 상대방 화면에서 해당 말의 `type`이 보임

4. **말 이동 시 WebSocket 메시지 전송**
```javascript
stompClient.send('/app/game.move', {}, JSON.stringify({
  roomId: roomId,
  playerId: playerId,
  from: { x: 4, y: 3 },
  to: { x: 5, y: 3 }
}));
```

## 디버깅

브라우저 콘솔에서 확인:
```javascript
console.log('My color:', playerColor);
console.log('Subscribed topic:', `/topic/game.${roomId}.${playerColor}`);
```

서버 로그에서 확인:
```
Broadcasting to RED player via /topic/game.abc123.RED
Broadcasting to BLUE player via /topic/game.abc123.BLUE
```
