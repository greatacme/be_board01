package com.board.game.model;

import lombok.Data;
import java.util.*;

@Data
public class Board {
    private List<Piece> pieces;
    private PlayerColor currentTurn;

    public Board() {
        this.pieces = new ArrayList<>();
        this.currentTurn = PlayerColor.RED;
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize RED pieces (left side, 6x7 board)
        // Place pieces from back (x: 0-4), leave front line empty (x: 5)
        // 5 rows × 7 columns = 35 pieces
        int redPieceCount = 0;
        for (int x = 0; x <= 4; x++) {  // Back 5 rows
            for (int y = 0; y <= 6; y++) {
                String id = "R" + (++redPieceCount);
                pieces.add(new Piece(id, PlayerColor.RED, new Position(x, y)));
            }
        }

        // Initialize BLUE pieces (right side, 6x7 board)
        // Place pieces from back (x: 9-13), leave front line empty (x: 8)
        // 5 rows × 7 columns = 35 pieces
        int bluePieceCount = 0;
        for (int x = 9; x <= 13; x++) {  // Back 5 rows
            for (int y = 0; y <= 6; y++) {
                String id = "B" + (++bluePieceCount);
                pieces.add(new Piece(id, PlayerColor.BLUE, new Position(x, y)));
            }
        }
    }

    public Piece getPieceAt(Position position) {
        return pieces.stream()
                .filter(p -> !p.isCaptured() &&
                           Math.abs(p.getPosition().getX() - position.getX()) < 0.01 &&
                           Math.abs(p.getPosition().getY() - position.getY()) < 0.01)
                .findFirst()
                .orElse(null);
    }

    public boolean isValidMove(Position from, Position to) {
        Piece piece = getPieceAt(from);
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }

        // Check if destination is valid
        if (!to.isValid()) {
            return false;
        }

        double fx = from.getX();
        double fy = from.getY();
        double tx = to.getX();
        double ty = to.getY();
        double dx = tx - fx;
        double dy = ty - fy;
        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);

        boolean isValidMove = false;

        // Normal orthogonal movement (4 directions: up, down, left, right)
        if ((absDx == 1 && absDy == 0) || (absDx == 0 && absDy == 1)) {
            isValidMove = true;
        }

        // Diagonal movement to/from crossing points
        if (isDiagonalCrossingMove(fx, fy, tx, ty)) {
            isValidMove = true;
        }

        if (!isValidMove) {
            return false;
        }

        // Check if destination is empty or has opponent piece
        Piece destPiece = getPieceAt(to);
        return destPiece == null || destPiece.getColor() != piece.getColor();
    }

    private boolean isDiagonalCrossingMove(double fx, double fy, double tx, double ty) {
        // Crossing point 1: (6.5, 1.5)
        // Connected to: (5,1), (5,2), (8,1), (8,2)
        if ((fx == 6.5 && fy == 1.5) || (tx == 6.5 && ty == 1.5)) {
            return ((fx == 5 && fy == 1) || (fx == 5 && fy == 2) ||
                    (fx == 8 && fy == 1) || (fx == 8 && fy == 2) ||
                    (fx == 6.5 && fy == 1.5)) &&
                   ((tx == 5 && ty == 1) || (tx == 5 && ty == 2) ||
                    (tx == 8 && ty == 1) || (tx == 8 && ty == 2) ||
                    (tx == 6.5 && ty == 1.5));
        }

        // Crossing point 2: (6.5, 4.5)
        // Connected to: (5,4), (5,5), (8,4), (8,5)
        if ((fx == 6.5 && fy == 4.5) || (tx == 6.5 && ty == 4.5)) {
            return ((fx == 5 && fy == 4) || (fx == 5 && fy == 5) ||
                    (fx == 8 && fy == 4) || (fx == 8 && fy == 5) ||
                    (fx == 6.5 && fy == 4.5)) &&
                   ((tx == 5 && ty == 4) || (tx == 5 && ty == 5) ||
                    (tx == 8 && ty == 4) || (tx == 8 && ty == 5) ||
                    (tx == 6.5 && ty == 4.5));
        }

        return false;
    }

    public boolean movePiece(Position from, Position to) {
        if (!isValidMove(from, to)) {
            return false;
        }

        Piece piece = getPieceAt(from);
        Piece destPiece = getPieceAt(to);

        // Capture opponent piece if present
        if (destPiece != null) {
            destPiece.setCaptured(true);
        }

        // Move piece
        piece.setPosition(to);

        // Switch turn
        currentTurn = (currentTurn == PlayerColor.RED) ? PlayerColor.BLUE : PlayerColor.RED;

        return true;
    }

    public boolean isGameOver() {
        // Game is over if one player has no pieces left
        long redCount = pieces.stream()
                .filter(p -> !p.isCaptured() && p.getColor() == PlayerColor.RED)
                .count();
        long blueCount = pieces.stream()
                .filter(p -> !p.isCaptured() && p.getColor() == PlayerColor.BLUE)
                .count();

        return redCount == 0 || blueCount == 0;
    }

    public PlayerColor getWinner() {
        if (!isGameOver()) {
            return null;
        }

        long redCount = pieces.stream()
                .filter(p -> !p.isCaptured() && p.getColor() == PlayerColor.RED)
                .count();

        return redCount > 0 ? PlayerColor.RED : PlayerColor.BLUE;
    }
}
