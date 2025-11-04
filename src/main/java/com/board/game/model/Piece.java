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

    public Piece(String id, PlayerColor color, PieceType type, Position position) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.position = position;
        this.captured = false;
    }
}
