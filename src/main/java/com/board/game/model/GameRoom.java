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
    private Date createdAt;

    public GameRoom(String roomId) {
        this.roomId = roomId;
        this.board = new Board();
        this.status = GameStatus.WAITING;
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
                status = GameStatus.PLAYING;
            }
            return true;
        } else if (bluePlayer == null) {
            bluePlayer = playerId;
            if (redPlayer != null) {
                status = GameStatus.PLAYING;
            }
            return true;
        }
        return false;
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
