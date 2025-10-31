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
            GameStateResponse response = gameService.getGameState(room.getRoomId());
            response.setMessage("Player joined: " + playerId);

            // Send to the joining player first (so they know the roomId)
            messagingTemplate.convertAndSendToUser(playerId, "/queue/reply", response);

            // Then broadcast to room
            messagingTemplate.convertAndSend("/topic/game." + room.getRoomId(), response);
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

        GameStateResponse response = gameService.getGameState(request.getRoomId());
        if (moved) {
            response.setMessage("Move successful");
        } else {
            response.setMessage("Invalid move");
        }

        // Broadcast to all players in room
        messagingTemplate.convertAndSend("/topic/game." + request.getRoomId(), response);
    }

    @MessageMapping("/game.leave")
    public void leaveGame(@Payload Map<String, String> payload) {
        String playerId = payload.get("playerId");
        String roomId = payload.get("roomId");
        log.info("Player {} leaving room {}", playerId, roomId);

        gameService.leaveRoom(roomId, playerId);

        GameStateResponse response = gameService.getGameState(roomId);
        if (response != null) {
            response.setMessage("Player left: " + playerId);
            messagingTemplate.convertAndSend("/topic/game." + roomId, response);
        }
    }
}
