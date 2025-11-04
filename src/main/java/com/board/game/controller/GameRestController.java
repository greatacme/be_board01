package com.board.game.controller;

import com.board.game.dto.GameStateResponse;
import com.board.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameRestController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/rooms")
    public ResponseEntity<List<String>> getAvailableRooms() {
        return ResponseEntity.ok(gameService.getAvailableRooms());
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<GameStateResponse> getRoomState(@PathVariable String roomId) {
        GameStateResponse state = gameService.getGameState(roomId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }

    @PostMapping("/rooms")
    public ResponseEntity<GameStateResponse> createRoom(@RequestBody Map<String, String> payload) {
        String playerId = payload.get("playerId");
        String roomId = gameService.createRoom(playerId);

        GameStateResponse response = gameService.getGameState(roomId, playerId);
        response.setMessage("Room created: " + roomId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<GameStateResponse> joinGame(@RequestBody Map<String, String> payload) {
        String playerId = payload.get("playerId");
        com.board.game.model.GameRoom room = gameService.findOrCreateRoom(playerId);

        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        GameStateResponse response = gameService.getGameState(room.getRoomId());
        response.setMessage("Player joined: " + playerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<GameStateResponse> joinRoom(
            @PathVariable String roomId,
            @RequestBody Map<String, String> payload) {
        String playerId = payload.get("playerId");

        com.board.game.model.GameRoom room = gameService.joinRoom(roomId, playerId);

        if (room == null) {
            GameStateResponse errorResponse = new GameStateResponse();
            errorResponse.setMessage("Room not found or full");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        GameStateResponse response = gameService.getGameState(roomId);
        response.setMessage("Player joined: " + playerId);
        response.setPlayerColor(room.getPlayerColor(playerId));

        // Broadcast to all players in the room via WebSocket
        messagingTemplate.convertAndSend("/topic/game." + roomId, response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/{roomId}/initial-pieces")
    public ResponseEntity<?> getInitialPieces(
            @PathVariable String roomId,
            @RequestParam String playerId) {
        List<com.board.game.model.Piece> pieces = gameService.getInitialPieces(roomId, playerId);

        if (pieces == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Room not found or player not in room"));
        }

        return ResponseEntity.ok(pieces);
    }

    @PostMapping("/rooms/{roomId}/place-piece")
    public ResponseEntity<GameStateResponse> placePiece(
            @PathVariable String roomId,
            @RequestBody Map<String, Object> payload) {
        String playerId = (String) payload.get("playerId");
        String pieceId = (String) payload.get("pieceId");
        Map<String, Object> posData = (Map<String, Object>) payload.get("position");

        com.board.game.model.Position position = null;
        if (posData != null) {
            double x = ((Number) posData.get("x")).doubleValue();
            double y = ((Number) posData.get("y")).doubleValue();
            position = new com.board.game.model.Position(x, y);
        }

        boolean success = gameService.placePiece(roomId, playerId, pieceId, position);

        if (!success) {
            GameStateResponse errorResponse = new GameStateResponse();
            errorResponse.setMessage("Failed to place piece");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        GameStateResponse response = gameService.getGameState(roomId, playerId);

        // Broadcast to all players in the room via WebSocket
        messagingTemplate.convertAndSend("/topic/game." + roomId, response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/{roomId}/ready")
    public ResponseEntity<GameStateResponse> setPlayerReady(
            @PathVariable String roomId,
            @RequestBody Map<String, String> payload) {
        String playerId = payload.get("playerId");

        boolean success = gameService.setPlayerReady(roomId, playerId);

        if (!success) {
            GameStateResponse errorResponse = new GameStateResponse();
            errorResponse.setMessage("Failed to set ready");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        GameStateResponse response = gameService.getGameState(roomId, playerId);
        response.setMessage("Player ready");

        // Broadcast to all players in the room via WebSocket
        messagingTemplate.convertAndSend("/topic/game." + roomId, response);

        return ResponseEntity.ok(response);
    }
}
