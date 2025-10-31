# Board Game Backend (be_board01)

체스와 유사한 2인 대국 보드게임의 백엔드 서버입니다.

## 기술 스택
- Spring Boot 3.2.0
- Java 17
- Gradle 8.4
- WebSocket (STOMP)
- Lombok

## 게임 규칙
- 2인 대국 게임
- 각 플레이어는 35개의 말을 보유
- 말은 5각형 모양 (일본장기 스타일)
- 이동: 전후좌우 한 칸씩
- 선 교차점에 말 배치 (바둑/장기 스타일)

## 프로젝트 구조
```
src/main/java/com/board/game/
├── config/           # WebSocket, CORS 설정
├── controller/       # WebSocket & REST 컨트롤러
├── dto/             # 데이터 전송 객체
├── model/           # 게임 모델 (Board, Piece, GameRoom)
└── service/         # 게임 로직 서비스
```

## 실행 방법

### 개발 모드
```bash
./gradlew bootRun
```

### 빌드
```bash
./gradlew build
```

### 실행 (JAR)
```bash
java -jar build/libs/be_board01-0.0.1-SNAPSHOT.jar
```

## API 엔드포인트

### REST API
- `GET /api/game/rooms` - 대기 중인 방 목록 조회
- `GET /api/game/rooms/{roomId}` - 방 상태 조회
- `POST /api/game/rooms` - 새 방 생성

### WebSocket
- 연결: `ws://localhost:7184/ws`
- 게임 참가: `/app/game.join`
- 말 이동: `/app/game.move`
- 게임 나가기: `/app/game.leave`
- 구독: `/topic/game.{roomId}`

## 포트
- 7184

## 로그 레벨
- `com.board.game`: DEBUG
