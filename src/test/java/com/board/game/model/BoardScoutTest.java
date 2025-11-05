package com.board.game.model;

import com.board.game.service.BattleRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardScoutTest {

    private Board board;
    private BattleRuleService battleRuleService;

    @BeforeEach
    void setUp() {
        board = new Board(true);  // Empty board
        battleRuleService = new BattleRuleService();
        battleRuleService.init();
        board.setBattleRuleService(battleRuleService);
    }

    @Test
    void testScoutLosesAndRevealsWinner_AttackerWins() {
        // 시나리오: 대장이 척후병을 공격하고 이김
        Piece general = new Piece("R1", PlayerColor.RED, PieceType.GENERAL, new Position(4, 3));
        Piece scout = new Piece("B1", PlayerColor.BLUE, PieceType.SCOUT, new Position(5, 3));

        board.getPieces().add(general);
        board.getPieces().add(scout);
        board.setCurrentTurn(PlayerColor.RED);

        // 대장이 척후병을 공격
        boolean moved = board.movePiece(new Position(4, 3), new Position(5, 3));

        assertTrue(moved, "Move should succeed");
        assertTrue(scout.isCaptured(), "Scout should be captured");
        assertTrue(general.isRevealed(), "General should be revealed after defeating scout");
        assertFalse(general.isCaptured(), "General should not be captured");
        assertEquals(5, general.getPosition().getX(), "General should move to scout's position");
    }

    @Test
    void testScoutLosesAndRevealsWinner_DefenderWins() {
        // 시나리오: 척후병이 대장을 공격하고 짐 (대장이 이김)
        Piece scout = new Piece("R1", PlayerColor.RED, PieceType.SCOUT, new Position(4, 3));
        Piece general = new Piece("B1", PlayerColor.BLUE, PieceType.GENERAL, new Position(5, 3));

        board.getPieces().add(scout);
        board.getPieces().add(general);
        board.setCurrentTurn(PlayerColor.RED);

        // 척후병이 대장을 공격
        boolean moved = board.movePiece(new Position(4, 3), new Position(5, 3));

        assertTrue(moved, "Move should succeed");
        assertTrue(scout.isCaptured(), "Scout should be captured");
        assertTrue(general.isRevealed(), "General should be revealed after defeating scout");
        assertFalse(general.isCaptured(), "General should not be captured");
        assertEquals(5, general.getPosition().getX(), "General should stay at position");
    }

    @Test
    void testNonScoutBattle_NoRevelation() {
        // 시나리오: 대장이 병사를 공격 (척후병 아님)
        Piece general = new Piece("R1", PlayerColor.RED, PieceType.GENERAL, new Position(4, 3));
        Piece private_ = new Piece("B1", PlayerColor.BLUE, PieceType.PRIVATE, new Position(5, 3));

        board.getPieces().add(general);
        board.getPieces().add(private_);
        board.setCurrentTurn(PlayerColor.RED);

        // 대장이 병사를 공격
        boolean moved = board.movePiece(new Position(4, 3), new Position(5, 3));

        assertTrue(moved, "Move should succeed");
        assertTrue(private_.isCaptured(), "Private should be captured");
        assertFalse(general.isRevealed(), "General should NOT be revealed (not fighting scout)");
    }

    @Test
    void testScoutVsScout_BothDie() {
        // 시나리오: 척후병 vs 척후병 (무승부)
        Piece scout1 = new Piece("R1", PlayerColor.RED, PieceType.SCOUT, new Position(4, 3));
        Piece scout2 = new Piece("B1", PlayerColor.BLUE, PieceType.SCOUT, new Position(5, 3));

        board.getPieces().add(scout1);
        board.getPieces().add(scout2);
        board.setCurrentTurn(PlayerColor.RED);

        // 척후병이 척후병을 공격
        boolean moved = board.movePiece(new Position(4, 3), new Position(5, 3));

        assertTrue(moved, "Move should succeed");
        assertTrue(scout1.isCaptured(), "Both scouts should be captured");
        assertTrue(scout2.isCaptured(), "Both scouts should be captured");
    }

    @Test
    void testMineCannotMove() {
        // 시나리오: 지뢰는 이동할 수 없음
        Piece mine = new Piece("R1", PlayerColor.RED, PieceType.MINE, new Position(4, 3));

        board.getPieces().add(mine);
        board.setCurrentTurn(PlayerColor.RED);

        // 지뢰 이동 시도
        boolean moved = board.movePiece(new Position(4, 3), new Position(5, 3));

        assertFalse(moved, "Mine should not be able to move");
        assertEquals(4, mine.getPosition().getX(), "Mine should stay at original position");
    }
}
