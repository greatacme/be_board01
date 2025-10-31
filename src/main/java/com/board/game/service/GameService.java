package com.board.game.service;

import com.board.game.dto.GameStateResponse;
import com.board.game.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GameService {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public String createRoom() {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        GameRoom room = new GameRoom(roomId);
        rooms.put(roomId, room);
        log.info("Created room: {}", roomId);
        return roomId;
    }

    public String createRoom(String playerId) {
        String roomId = createRoom();
        joinRoom(roomId, playerId);
        return roomId;
    }

    public GameRoom joinRoom(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            log.warn("Room not found: {}", roomId);
            return null;
        }

        if (room.addPlayer(playerId)) {
            log.info("Player {} joined room {}", playerId, roomId);
            return room;
        }

        log.warn("Room {} is full", roomId);
        return null;
    }

    public GameRoom findOrCreateRoom(String playerId) {
        // Find waiting room
        Optional<GameRoom> waitingRoom = rooms.values().stream()
                .filter(room -> room.getStatus() == GameStatus.WAITING && !room.isFull())
                .findFirst();

        if (waitingRoom.isPresent()) {
            GameRoom room = waitingRoom.get();
            room.addPlayer(playerId);
            log.info("Player {} joined existing room {}", playerId, room.getRoomId());
            return room;
        }

        // Create new room
        String roomId = createRoom();
        return joinRoom(roomId, playerId);
    }

    public boolean movePiece(String roomId, String playerId, Position from, Position to) {
        GameRoom room = rooms.get(roomId);
        if (room == null || room.getStatus() != GameStatus.PLAYING) {
            return false;
        }

        PlayerColor playerColor = room.getPlayerColor(playerId);
        if (playerColor == null || room.getBoard().getCurrentTurn() != playerColor) {
            log.warn("Invalid turn for player {} in room {}", playerId, roomId);
            return false;
        }

        boolean moved = room.getBoard().movePiece(from, to);
        if (moved) {
            log.info("Piece moved in room {}: {} -> {}", roomId, from, to);

            // Check game over
            if (room.getBoard().isGameOver()) {
                room.setStatus(GameStatus.FINISHED);
                log.info("Game over in room {}. Winner: {}", roomId, room.getBoard().getWinner());
            }
        }

        return moved;
    }

    public GameStateResponse getGameState(String roomId) {
        return getGameState(roomId, null);
    }

    public GameStateResponse getGameState(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }

        GameStateResponse response = new GameStateResponse();
        response.setRoomId(roomId);
        response.setPieces(room.getBoard().getPieces());
        response.setCurrentTurn(room.getBoard().getCurrentTurn());
        response.setStatus(room.getStatus());
        response.setWinner(room.getBoard().getWinner());

        if (playerId != null) {
            response.setPlayerColor(room.getPlayerColor(playerId));
        }

        return response;
    }

    public void leaveRoom(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.removePlayer(playerId);
            log.info("Player {} left room {}", playerId, roomId);
        }
    }

    public List<String> getAvailableRooms() {
        return rooms.values().stream()
                .filter(room -> room.getStatus() == GameStatus.WAITING && !room.isFull())
                .map(GameRoom::getRoomId)
                .toList();
    }
}
