package com.board.game.dto;

import com.board.game.model.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequest {
    private String roomId;
    private String playerId;
    private Position from;
    private Position to;
}
