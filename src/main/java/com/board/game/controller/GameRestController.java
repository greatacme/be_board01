package com.board.game.controller;

import com.board.game.dto.GameStateResponse;
import com.board.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameRestController {

    private final GameService gameService;

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
            return ResponseEntity.badRequest()
                    .body(new GameStateResponse(null, null, null, null, null, "Room not found or full", null));
        }

        GameStateResponse response = gameService.getGameState(roomId);
        response.setMessage("Player joined: " + playerId);
        response.setPlayerColor(room.getPlayerColor(playerId));
        return ResponseEntity.ok(response);
    }
}
