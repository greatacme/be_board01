package com.board.game.model;

import lombok.Data;
import java.util.*;

@Data
public class GameRoom {
    private String roomId;
    private String redPlayer;
    private String bluePlayer;
    private Board board;
    private GameStatus status;
    private boolean redPlayerReady;
    private boolean bluePlayerReady;
    private Date createdAt;

    public GameRoom(String roomId) {
        this.roomId = roomId;
        this.board = new Board(true);  // Create empty board for setup
        this.status = GameStatus.WAITING;
        this.redPlayerReady = false;
        this.bluePlayerReady = false;
        this.createdAt = new Date();
    }

    public boolean addPlayer(String playerId) {
        // 이미 참가한 플레이어인 경우 true 반환
        if (playerId.equals(redPlayer) || playerId.equals(bluePlayer)) {
            return true;
        }

        if (redPlayer == null) {
            redPlayer = playerId;
            if (bluePlayer != null) {
                status = GameStatus.SETUP;  // Both players joined, start setup
            }
            return true;
        } else if (bluePlayer == null) {
            bluePlayer = playerId;
            if (redPlayer != null) {
                status = GameStatus.SETUP;  // Both players joined, start setup
            }
            return true;
        }
        return false;
    }

    public boolean isPlayerReady(String playerId) {
        if (playerId.equals(redPlayer)) {
            return redPlayerReady;
        } else if (playerId.equals(bluePlayer)) {
            return bluePlayerReady;
        }
        return false;
    }

    public void setPlayerReady(String playerId, boolean ready) {
        if (playerId.equals(redPlayer)) {
            redPlayerReady = ready;
        } else if (playerId.equals(bluePlayer)) {
            bluePlayerReady = ready;
        }

        // Both players ready, start game
        if (redPlayerReady && bluePlayerReady && status == GameStatus.SETUP) {
            status = GameStatus.PLAYING;
        }
    }

    public boolean areBothPlayersReady() {
        return redPlayerReady && bluePlayerReady;
    }

    public boolean removePlayer(String playerId) {
        if (playerId.equals(redPlayer)) {
            redPlayer = null;
            status = GameStatus.FINISHED;
            return true;
        } else if (playerId.equals(bluePlayer)) {
            bluePlayer = null;
            status = GameStatus.FINISHED;
            return true;
        }
        return false;
    }

    public boolean isFull() {
        return redPlayer != null && bluePlayer != null;
    }

    public PlayerColor getPlayerColor(String playerId) {
        if (playerId.equals(redPlayer)) {
            return PlayerColor.RED;
        } else if (playerId.equals(bluePlayer)) {
            return PlayerColor.BLUE;
        }
        return null;
    }
}
