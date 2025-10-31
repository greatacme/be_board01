package com.board.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private double x;  // column (0-13 with 6.5 for crossing points)
    private double y;  // row (0-6 with 1.5, 4.5 for crossing points)

    public boolean isValid() {
        // Valid positions: RED side (0-5), BLUE side (8-13), or diagonal crossing points
        if (y < 0 || y > 6) {
            return false;
        }

        // Left and right boards (integer coordinates)
        if ((x >= 0 && x <= 5 && y == Math.floor(y)) ||
            (x >= 8 && x <= 13 && y == Math.floor(y))) {
            return true;
        }

        // Diagonal crossing points: (6.5, 1.5) and (6.5, 4.5)
        if (x == 6.5 && (y == 1.5 || y == 4.5)) {
            return true;
        }

        return false;
    }

    public boolean isRedSide() {
        return x >= 0 && x <= 5;  // Red side is left (x: 0-5)
    }

    public boolean isBlueSide() {
        return x >= 8 && x <= 13;  // Blue side is right (x: 8-13)
    }

    public boolean isInPalace() {
        // Palace (궁성) is 3x3 area at each side
        // Red palace (left): x: 1-3, y: 2-4
        // Blue palace (right): x: 9-11, y: 2-4
        return (x >= 1 && x <= 3 && y >= 2 && y <= 4) ||
               (x >= 9 && x <= 11 && y >= 2 && y <= 4);
    }

    public boolean isInRedPalace() {
        return x >= 1 && x <= 3 && y >= 2 && y <= 4;
    }

    public boolean isInBluePalace() {
        return x >= 9 && x <= 11 && y >= 2 && y <= 4;
    }
}
