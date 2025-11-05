package com.board.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private String id;
    private PlayerColor color;
    private PieceType type;
    private Position position;
    private boolean captured;
    private boolean revealed;  // 척후병에게 잡혔을 때 적에게 노출됨

    public Piece(String id, PlayerColor color, PieceType type, Position position) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.position = position;
        this.captured = false;
        this.revealed = false;
    }
}
