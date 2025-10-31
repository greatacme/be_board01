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
}
