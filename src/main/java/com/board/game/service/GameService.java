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
    private final BattleRuleService battleRuleService;

    public GameService(BattleRuleService battleRuleService) {
        this.battleRuleService = battleRuleService;
    }

    public String createRoom() {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        GameRoom room = new GameRoom(roomId);
        room.getBoard().setBattleRuleService(battleRuleService);
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

        PlayerColor playerColor = playerId != null ? room.getPlayerColor(playerId) : null;

        GameStateResponse response = new GameStateResponse();
        response.setRoomId(roomId);
        response.setCurrentTurn(room.getBoard().getCurrentTurn());
        response.setStatus(room.getStatus());
        response.setWinner(room.getBoard().getWinner());
        response.setRedPlayerReady(room.isRedPlayerReady());
        response.setBluePlayerReady(room.isBluePlayerReady());
        response.setRedPlayer(room.getRedPlayer());
        response.setBluePlayer(room.getBluePlayer());

        if (playerId != null) {
            response.setPlayerColor(playerColor);
        }

        // Filter pieces based on game status
        List<Piece> pieces = room.getBoard().getPieces();
        if (room.getStatus() == GameStatus.SETUP && playerColor != null) {
            // During SETUP: Only show own pieces
            List<Piece> filteredPieces = pieces.stream()
                    .filter(p -> p.getColor() == playerColor)
                    .toList();
            response.setPieces(filteredPieces);
        } else if (room.getStatus() == GameStatus.PLAYING && playerColor != null) {
            // During PLAYING: Hide opponent piece types (unless revealed by scout)
            List<Piece> maskedPieces = pieces.stream()
                    .map(p -> {
                        if (p.getColor() != playerColor && !p.isCaptured()) {
                            // Show revealed pieces, hide others
                            if (p.isRevealed()) {
                                // Show revealed piece with full info - create explicit copy
                                log.debug("Player {} sees revealed opponent piece: {} ({})",
                                    playerId, p.getId(), p.getType().getKoreanName());
                                Piece revealed = new Piece(p.getId(), p.getColor(), p.getType(), p.getPosition());
                                revealed.setCaptured(false);
                                revealed.setRevealed(true);
                                return revealed;
                            } else {
                                // Create a copy with hidden type for opponent pieces
                                Piece masked = new Piece(p.getId(), p.getColor(), null, p.getPosition());
                                masked.setCaptured(false);
                                masked.setRevealed(false);
                                return masked;
                            }
                        }
                        // Player's own pieces - return as is
                        return p;
                    })
                    .toList();
            response.setPieces(maskedPieces);
        } else {
            response.setPieces(pieces);
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

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public List<String> getAvailableRooms() {
        return rooms.values().stream()
                .filter(room -> room.getStatus() == GameStatus.WAITING && !room.isFull())
                .map(GameRoom::getRoomId)
                .toList();
    }

    public List<Piece> getInitialPieces(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }

        PlayerColor color = room.getPlayerColor(playerId);
        if (color == null) {
            return null;
        }

        return room.getBoard().getInitialPieces(color);
    }

    public boolean placePiece(String roomId, String playerId, String pieceId, Position position) {
        GameRoom room = rooms.get(roomId);
        if (room == null || room.getStatus() != GameStatus.SETUP) {
            return false;
        }

        PlayerColor playerColor = room.getPlayerColor(playerId);
        if (playerColor == null) {
            return false;
        }

        // If position is null, return piece to inventory
        if (position == null) {
            Piece piece = room.getBoard().getPieces().stream()
                    .filter(p -> p.getId().equals(pieceId))
                    .findFirst()
                    .orElse(null);

            if (piece == null) {
                // Get from initial pieces
                List<Piece> initialPieces = room.getBoard().getInitialPieces(playerColor);
                piece = initialPieces.stream()
                        .filter(p -> p.getId().equals(pieceId))
                        .findFirst()
                        .orElse(null);

                if (piece != null) {
                    room.getBoard().getPieces().add(piece);
                }
            }

            if (piece != null) {
                piece.setPosition(null);
                log.info("Piece {} returned to inventory in room {}", pieceId, roomId);
                return true;
            }

            return false;
        }

        // Validate position is in player's own camp during SETUP
        if (!isValidPlacementPosition(position, playerColor)) {
            log.warn("Invalid placement position {} for player color {} in room {}", position, playerColor, roomId);
            return false;
        }

        // Add piece to board if not exists
        Piece piece = room.getBoard().getPieces().stream()
                .filter(p -> p.getId().equals(pieceId))
                .findFirst()
                .orElse(null);

        if (piece == null) {
            // Get initial piece
            List<Piece> initialPieces = room.getBoard().getInitialPieces(playerColor);
            piece = initialPieces.stream()
                    .filter(p -> p.getId().equals(pieceId))
                    .findFirst()
                    .orElse(null);

            if (piece != null) {
                room.getBoard().getPieces().add(piece);
            }
        }

        if (piece != null) {
            piece.setPosition(position);
            log.info("Piece {} placed at {} in room {}", pieceId, position, roomId);
            return true;
        }

        return false;
    }

    private boolean isValidPlacementPosition(Position position, PlayerColor playerColor) {
        // Validate position is valid
        if (!position.isValid()) {
            return false;
        }

        // RED players can only place pieces on their side (x: 0-5)
        if (playerColor == PlayerColor.RED && !position.isRedSide()) {
            return false;
        }

        // BLUE players can only place pieces on their side (x: 8-13)
        if (playerColor == PlayerColor.BLUE && !position.isBlueSide()) {
            return false;
        }

        // Cannot place on front line (x=5 for RED, x=8 for BLUE)
        if (playerColor == PlayerColor.RED && position.getX() == 5) {
            return false;
        }

        if (playerColor == PlayerColor.BLUE && position.getX() == 8) {
            return false;
        }

        return true;
    }

    public boolean setPlayerReady(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);
        if (room == null || room.getStatus() != GameStatus.SETUP) {
            return false;
        }

        room.setPlayerReady(playerId, true);
        log.info("Player {} ready in room {}. Both ready: {}", playerId, roomId, room.areBothPlayersReady());
        return true;
    }
}
