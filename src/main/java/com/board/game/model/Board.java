package com.board.game.model;

import com.board.game.service.BattleRuleService;
import lombok.Data;
import java.util.*;

@Data
public class Board {
    private List<Piece> pieces;
    private PlayerColor currentTurn;
    private BattleRuleService battleRuleService;

    public Board() {
        this(false);
    }

    public Board(boolean empty) {
        this.pieces = new ArrayList<>();
        this.currentTurn = PlayerColor.RED;
        if (!empty) {
            initializeBoard();
        }
    }

    public void setBattleRuleService(BattleRuleService battleRuleService) {
        this.battleRuleService = battleRuleService;
    }

    public List<Piece> getInitialPieces(PlayerColor color) {
        List<Piece> initialPieces = new ArrayList<>();

        // Define piece layout for each row (y: 0-6)
        PieceType[][] layout = {
            {PieceType.MINE, PieceType.GENERAL, PieceType.FLAG, PieceType.LIEUTENANT_GENERAL,
             PieceType.MAJOR_GENERAL, PieceType.BRIGADIER_GENERAL, PieceType.MINE},
            {PieceType.COLONEL, PieceType.LIEUTENANT_COLONEL, PieceType.MAJOR, PieceType.CAPTAIN,
             PieceType.FIRST_LIEUTENANT, PieceType.SECOND_LIEUTENANT, PieceType.WARRANT_OFFICER},
            {PieceType.MASTER_SERGEANT, PieceType.SERGEANT_FIRST_CLASS, PieceType.SERGEANT, PieceType.STAFF_SERGEANT,
             PieceType.CORPORAL, PieceType.PRIVATE_FIRST_CLASS, PieceType.PRIVATE},
            {PieceType.ENGINEER, PieceType.ENGINEER, PieceType.SCOUT, PieceType.SCOUT,
             PieceType.AIRPLANE, PieceType.AIRPLANE, PieceType.TANK},
            {PieceType.TANK, PieceType.MISSILE, PieceType.MISSILE, PieceType.ANTI_AIRCRAFT,
             PieceType.ANTI_AIRCRAFT, PieceType.RADAR, PieceType.RADAR}
        };

        int count = 0;
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 6; y++) {
                String id = (color == PlayerColor.RED ? "R" : "B") + (++count);
                PieceType type = layout[x][y];
                // Pieces start in inventory (position null or special marker)
                initialPieces.add(new Piece(id, color, type, null));
            }
        }

        return initialPieces;
    }

    public void placePiece(String pieceId, Position position) {
        Piece piece = pieces.stream()
                .filter(p -> p.getId().equals(pieceId))
                .findFirst()
                .orElse(null);

        if (piece != null) {
            piece.setPosition(position);
        }
    }

    private void initializeBoard() {
        // Define piece layout for each row (y: 0-6)
        PieceType[][] layout = {
            // x=0 (back row): 7 pieces
            {PieceType.MINE, PieceType.GENERAL, PieceType.FLAG, PieceType.LIEUTENANT_GENERAL,
             PieceType.MAJOR_GENERAL, PieceType.BRIGADIER_GENERAL, PieceType.MINE},

            // x=1: 7 pieces
            {PieceType.COLONEL, PieceType.LIEUTENANT_COLONEL, PieceType.MAJOR, PieceType.CAPTAIN,
             PieceType.FIRST_LIEUTENANT, PieceType.SECOND_LIEUTENANT, PieceType.WARRANT_OFFICER},

            // x=2: 7 pieces
            {PieceType.MASTER_SERGEANT, PieceType.SERGEANT_FIRST_CLASS, PieceType.SERGEANT, PieceType.STAFF_SERGEANT,
             PieceType.CORPORAL, PieceType.PRIVATE_FIRST_CLASS, PieceType.PRIVATE},

            // x=3: 7 pieces
            {PieceType.ENGINEER, PieceType.ENGINEER, PieceType.SCOUT, PieceType.SCOUT,
             PieceType.AIRPLANE, PieceType.AIRPLANE, PieceType.TANK},

            // x=4 (front row): 7 pieces
            {PieceType.TANK, PieceType.MISSILE, PieceType.MISSILE, PieceType.ANTI_AIRCRAFT,
             PieceType.ANTI_AIRCRAFT, PieceType.RADAR, PieceType.RADAR}
        };

        // Initialize RED pieces (left side, x: 0-4, y: 0-6)
        int redPieceCount = 0;
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 6; y++) {
                String id = "R" + (++redPieceCount);
                PieceType type = layout[x][y];
                pieces.add(new Piece(id, PlayerColor.RED, type, new Position(x, y)));
            }
        }

        // Initialize BLUE pieces (right side, x: 9-13, y: 0-6)
        int bluePieceCount = 0;
        for (int x = 9; x <= 13; x++) {
            for (int y = 0; y <= 6; y++) {
                String id = "B" + (++bluePieceCount);
                PieceType type = layout[x - 9][y];  // Mirror the same layout
                pieces.add(new Piece(id, PlayerColor.BLUE, type, new Position(x, y)));
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

        // Check if piece can move (지뢰는 이동 불가)
        if (battleRuleService != null && !battleRuleService.canMove(piece.getType())) {
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

        Piece attacker = getPieceAt(from);
        Piece defender = getPieceAt(to);

        // If destination is empty, just move
        if (defender == null) {
            attacker.setPosition(to);
        } else {
            // Battle resolution
            if (battleRuleService != null) {
                int result = battleRuleService.resolveBattle(attacker.getType(), defender.getType());

                if (result == 1) {
                    // Attacker wins
                    defender.setCaptured(true);
                    attacker.setPosition(to);

                    // 척후병 특수 규칙: 척후병을 제거한 말은 적에게 노출됨
                    if (battleRuleService.isScout(defender.getType())) {
                        attacker.setRevealed(true);
                        System.out.println("DEBUG: " + attacker.getType().getKoreanName() +
                            " (" + attacker.getId() + ") defeated scout - NOW REVEALED");
                    }
                } else if (result == -1) {
                    // Defender wins
                    attacker.setCaptured(true);

                    // 척후병 특수 규칙: 척후병을 공격한 말은 적에게 노출됨
                    if (battleRuleService.isScout(attacker.getType())) {
                        defender.setRevealed(true);
                        System.out.println("DEBUG: " + defender.getType().getKoreanName() +
                            " (" + defender.getId() + ") defeated scout - NOW REVEALED");
                    }
                } else {
                    // Draw - both pieces are captured
                    attacker.setCaptured(true);
                    defender.setCaptured(true);
                }
            } else {
                // Fallback: simple capture without battle rules
                defender.setCaptured(true);
                attacker.setPosition(to);
            }
        }

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
