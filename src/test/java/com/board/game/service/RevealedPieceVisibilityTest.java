package com.board.game.service;

import com.board.game.dto.GameStateResponse;
import com.board.game.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 척후병에게 노출된 말이 상대방 화면에서 보이는지 테스트
 */
class RevealedPieceVisibilityTest {

    private GameService gameService;
    private BattleRuleService battleRuleService;

    @BeforeEach
    void setUp() {
        battleRuleService = new BattleRuleService();
        battleRuleService.init();
        gameService = new GameService(battleRuleService);
    }

    @Test
    void testRevealedPieceVisibleToOpponent() throws Exception {
        // 1. 방 생성 및 플레이어 참가
        String roomId = gameService.createRoom();
        String redPlayerId = "redPlayer";
        String bluePlayerId = "bluePlayer";

        gameService.joinRoom(roomId, redPlayerId);
        gameService.joinRoom(roomId, bluePlayerId);

        // 2. GameRoom 직접 접근 (Reflection 사용)
        GameRoom room = getRoomByReflection(roomId);
        assertNotNull(room);

        // 3. 보드를 PLAYING 상태로 변경
        room.setStatus(GameStatus.PLAYING);
        room.getBoard().setBattleRuleService(battleRuleService);

        // 4. 말 배치
        // RED의 소장을 (4, 3)에 배치
        Piece redMajorGeneral = new Piece("R1", PlayerColor.RED, PieceType.MAJOR_GENERAL, new Position(4, 3));
        // BLUE의 척후병을 (5, 3)에 배치
        Piece blueScout = new Piece("B1", PlayerColor.BLUE, PieceType.SCOUT, new Position(5, 3));
        // BLUE의 일반 병사 (노출되지 않음)
        Piece bluePrivate = new Piece("B2", PlayerColor.BLUE, PieceType.PRIVATE, new Position(8, 3));

        room.getBoard().getPieces().clear();
        room.getBoard().getPieces().add(redMajorGeneral);
        room.getBoard().getPieces().add(blueScout);
        room.getBoard().getPieces().add(bluePrivate);
        room.getBoard().setCurrentTurn(PlayerColor.RED);

        // 5. RED의 소장이 BLUE의 척후병을 공격
        boolean moved = room.getBoard().movePiece(new Position(4, 3), new Position(5, 3));
        assertTrue(moved);
        assertTrue(blueScout.isCaptured());
        assertTrue(redMajorGeneral.isRevealed(), "Red major general should be revealed");

        // 6. BLUE 플레이어 관점에서 게임 상태 가져오기
        GameStateResponse blueView = gameService.getGameState(roomId, bluePlayerId);
        assertNotNull(blueView);

        // 7. BLUE 플레이어가 보는 RED 말 확인
        Piece redPieceInBlueView = blueView.getPieces().stream()
            .filter(p -> p.getId().equals("R1"))
            .findFirst()
            .orElse(null);

        assertNotNull(redPieceInBlueView, "Red major general should be visible to blue player");
        assertTrue(redPieceInBlueView.isRevealed(), "Red major general should be marked as revealed");
        assertEquals(PieceType.MAJOR_GENERAL, redPieceInBlueView.getType(),
            "BLUE player should SEE the type of revealed RED piece (소장)");

        // 8. RED 플레이어 관점에서 게임 상태 가져오기
        GameStateResponse redView = gameService.getGameState(roomId, redPlayerId);
        assertNotNull(redView);

        // 9. RED 플레이어가 보는 BLUE 병사는 타입이 숨겨져야 함
        Piece bluePieceInRedView = redView.getPieces().stream()
            .filter(p -> p.getId().equals("B2") && !p.isCaptured())
            .findFirst()
            .orElse(null);

        assertNotNull(bluePieceInRedView, "Blue private should exist");
        assertFalse(bluePieceInRedView.isRevealed(), "Blue private should not be revealed");
        assertNull(bluePieceInRedView.getType(),
            "RED player should NOT see the type of unrevealed BLUE piece");
    }

    @Test
    void testUnrevealedPieceHiddenFromOpponent() throws Exception {
        // 노출되지 않은 말은 상대방에게 타입이 숨겨져야 함
        String roomId = gameService.createRoom();
        String redPlayerId = "redPlayer";
        String bluePlayerId = "bluePlayer";

        gameService.joinRoom(roomId, redPlayerId);
        gameService.joinRoom(roomId, bluePlayerId);

        GameRoom room = getRoomByReflection(roomId);
        room.setStatus(GameStatus.PLAYING);
        room.getBoard().setBattleRuleService(battleRuleService);

        // RED 대장 (노출 안 됨)
        Piece redGeneral = new Piece("R1", PlayerColor.RED, PieceType.GENERAL, new Position(4, 3));
        redGeneral.setRevealed(false);

        room.getBoard().getPieces().clear();
        room.getBoard().getPieces().add(redGeneral);

        // BLUE 플레이어 관점
        GameStateResponse blueView = gameService.getGameState(roomId, bluePlayerId);
        Piece redPieceInBlueView = blueView.getPieces().stream()
            .filter(p -> p.getId().equals("R1"))
            .findFirst()
            .orElse(null);

        assertNotNull(redPieceInBlueView);
        assertFalse(redPieceInBlueView.isRevealed());
        assertNull(redPieceInBlueView.getType(),
            "BLUE player should NOT see the type of unrevealed RED piece");
    }

    /**
     * Reflection을 사용하여 GameService에서 GameRoom 가져오기
     */
    private GameRoom getRoomByReflection(String roomId) throws Exception {
        Field roomsField = GameService.class.getDeclaredField("rooms");
        roomsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, GameRoom> rooms = (Map<String, GameRoom>) roomsField.get(gameService);
        return rooms.get(roomId);
    }
}
