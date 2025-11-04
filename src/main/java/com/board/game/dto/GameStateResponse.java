package com.board.game.dto;

import com.board.game.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private String roomId;
    private List<Piece> pieces;
    private PlayerColor currentTurn;
    private GameStatus status;
    private PlayerColor winner;
    private String message;
    private PlayerColor playerColor;  // 접속한 플레이어의 색상
    private Boolean redPlayerReady;   // 레드 플레이어 준비 상태
    private Boolean bluePlayerReady;  // 블루 플레이어 준비 상태
    private String redPlayer;         // 레드 플레이어 ID
    private String bluePlayer;        // 블루 플레이어 ID
}
