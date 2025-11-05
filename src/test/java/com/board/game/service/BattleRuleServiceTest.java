package com.board.game.service;

import com.board.game.model.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleRuleServiceTest {

    private BattleRuleService battleRuleService;

    @BeforeEach
    void setUp() {
        battleRuleService = new BattleRuleService();
        battleRuleService.init();
    }

    @Test
    void testGeneralDefeatsLowerRanks() {
        // 대장이 중장을 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.GENERAL, PieceType.LIEUTENANT_GENERAL));

        // 대장이 소장을 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.GENERAL, PieceType.MAJOR_GENERAL));

        // 대장이 병사를 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.GENERAL, PieceType.PRIVATE));
    }

    @Test
    void testMineDefeatsAlmostEveryone() {
        // 지뢰가 대장을 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.MINE, PieceType.GENERAL));

        // 지뢰가 탱크를 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.MINE, PieceType.TANK));
    }

    @Test
    void testEngineerDefeatsMine() {
        // 공병이 지뢰를 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.ENGINEER, PieceType.MINE));
    }

    @Test
    void testScoutLosesToEveryone() {
        // 척후병은 일병에게도 짐
        assertEquals(-1, battleRuleService.resolveBattle(PieceType.SCOUT, PieceType.PRIVATE));

        // 척후병은 대장에게도 짐
        assertEquals(-1, battleRuleService.resolveBattle(PieceType.SCOUT, PieceType.GENERAL));
    }

    @Test
    void testFlagDefeatsLowerRanksButNotGenerals() {
        // 군기가 대장에게 이김 (규칙 파일 확인)
        assertEquals(1, battleRuleService.resolveBattle(PieceType.FLAG, PieceType.GENERAL));

        // 군기가 병사를 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.FLAG, PieceType.PRIVATE));
    }

    @Test
    void testMineCannotMove() {
        // 지뢰는 이동 불가
        assertFalse(battleRuleService.canMove(PieceType.MINE));
    }

    @Test
    void testOtherPiecesCanMove() {
        // 다른 말들은 이동 가능
        assertTrue(battleRuleService.canMove(PieceType.GENERAL));
        assertTrue(battleRuleService.canMove(PieceType.SCOUT));
        assertTrue(battleRuleService.canMove(PieceType.TANK));
        assertTrue(battleRuleService.canMove(PieceType.ENGINEER));
    }

    @Test
    void testScoutDetection() {
        // 척후병 확인
        assertTrue(battleRuleService.isScout(PieceType.SCOUT));
        assertFalse(battleRuleService.isScout(PieceType.GENERAL));
        assertFalse(battleRuleService.isScout(PieceType.ENGINEER));
    }

    @Test
    void testDrawBattle() {
        // 같은 계급끼리는 무승부 (둘 다 죽음)
        // 예: 대장 vs 대장
        assertEquals(0, battleRuleService.resolveBattle(PieceType.GENERAL, PieceType.GENERAL));

        // 예: 병사 vs 병사
        assertEquals(0, battleRuleService.resolveBattle(PieceType.PRIVATE, PieceType.PRIVATE));
    }

    @Test
    void testAirplaneVsTank() {
        // 비행기가 탱크를 이김 (규칙 파일 확인)
        assertEquals(1, battleRuleService.resolveBattle(PieceType.AIRPLANE, PieceType.TANK));
    }

    @Test
    void testAntiAircraftVsAirplane() {
        // 고사포가 비행기를 이김
        assertEquals(1, battleRuleService.resolveBattle(PieceType.ANTI_AIRCRAFT, PieceType.AIRPLANE));
    }
}
