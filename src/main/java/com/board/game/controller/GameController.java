package com.board.game.controller;

import com.board.game.dto.GameStateResponse;
import com.board.game.dto.MoveRequest;
import com.board.game.model.GameRoom;
import com.board.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game.join")
    public void joinGame(@Payload Map<String, String> payload) {
        String playerId = payload.get("playerId");
        log.info("Player {} joining game", playerId);

        GameRoom room = gameService.findOrCreateRoom(playerId);
        if (room != null) {
            GameStateResponse response = gameService.getGameState(room.getRoomId(), playerId);
            response.setMessage("Player joined: " + playerId);

            // Send to the joining player first (so they know the roomId)
            messagingTemplate.convertAndSendToUser(playerId, "/queue/reply", response);

            // Then broadcast to all players with their respective views
            broadcastGameStateToRoom(room.getRoomId());
        }
    }

    @MessageMapping("/game.move")
    public void makeMove(@Payload MoveRequest request) {
        log.info("Move request: {} -> {} by {} in room {}",
                request.getFrom(), request.getTo(), request.getPlayerId(), request.getRoomId());

        boolean moved = gameService.movePiece(
                request.getRoomId(),
                request.getPlayerId(),
                request.getFrom(),
                request.getTo()
        );

        String message = moved ? "Move successful" : "Invalid move";

        // Broadcast to all players with their respective views
        broadcastGameStateToRoom(request.getRoomId(), message);
    }

    @MessageMapping("/game.leave")
    public void leaveGame(@Payload Map<String, String> payload) {
        String playerId = payload.get("playerId");
        String roomId = payload.get("roomId");
        log.info("Player {} leaving room {}", playerId, roomId);

        gameService.leaveRoom(roomId, playerId);

        // Broadcast to remaining players
        broadcastGameStateToRoom(roomId, "Player left: " + playerId);
    }

    /**
     * 각 플레이어에게 맞춤형 게임 상태를 전송합니다.
     * RED 플레이어는 /topic/game.{roomId}.RED 를 구독
     * BLUE 플레이어는 /topic/game.{roomId}.BLUE 를 구독
     */
    private void broadcastGameStateToRoom(String roomId) {
        broadcastGameStateToRoom(roomId, null);
    }

    private void broadcastGameStateToRoom(String roomId, String message) {
        GameRoom room = gameService.getRoom(roomId);
        if (room == null) {
            return;
        }

        // RED 플레이어 전용 토픽으로 전송
        if (room.getRedPlayer() != null) {
            GameStateResponse redView = gameService.getGameState(roomId, room.getRedPlayer());
            if (message != null) {
                redView.setMessage(message);
            }
            log.debug("Broadcasting to RED player via /topic/game.{}.RED", roomId);
            messagingTemplate.convertAndSend("/topic/game." + roomId + ".RED", redView);
        }

        // BLUE 플레이어 전용 토픽으로 전송
        if (room.getBluePlayer() != null) {
            GameStateResponse blueView = gameService.getGameState(roomId, room.getBluePlayer());
            if (message != null) {
                blueView.setMessage(message);
            }
            log.debug("Broadcasting to BLUE player via /topic/game.{}.BLUE", roomId);
            messagingTemplate.convertAndSend("/topic/game." + roomId + ".BLUE", blueView);
        }
    }
}
