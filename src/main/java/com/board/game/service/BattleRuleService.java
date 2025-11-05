package com.board.game.service;

import com.board.game.model.PieceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class BattleRuleService {

    private Map<String, Set<String>> winRules;

    @PostConstruct
    public void init() {
        loadRules();
    }

    /**
     * 승부 규칙을 소스 코드 내에서 정의합니다.
     * 각 말이 이길 수 있는 상대 목록을 Map으로 관리합니다.
     */
    private void loadRules() {
        winRules = new HashMap<>();

        // 군기
        winRules.put("군기", new HashSet<>(Arrays.asList(
            "대장", "중장", "중령", "소령", "중위", "소위", "준위",
            "상사", "중사", "하사", "병장", "상병", "일병", "이병",
            "공병", "척후병", "고사포", "레이더"
        )));

        // 지뢰
        winRules.put("지뢰", new HashSet<>(Arrays.asList(
            "군기", "대장", "중장", "소장", "준장", "대령", "중령", "소령",
            "대위", "중위", "소위", "준위", "상사", "중사", "하사",
            "병장", "상병", "일병", "이병", "척후병", "탱크", "고사포", "레이더"
        )));

        // 대장
        winRules.put("대장", new HashSet<>(Arrays.asList(
            "중장", "소장", "준장", "대령", "중령", "소령", "대위",
            "중위", "소위", "준위", "상사", "중사", "하사", "병장",
            "상병", "일병", "이병", "공병", "척후병", "비행기", "탱크",
            "미사일", "고사포", "레이더"
        )));

        // 중장
        winRules.put("중장", new HashSet<>(Arrays.asList(
            "소장", "준장", "대령", "중령", "소령", "대위", "중위",
            "소위", "준위", "상사", "중사", "하사", "병장", "상병",
            "일병", "이병", "공병", "척후병", "미사일", "고사포", "레이더"
        )));

        // 소장
        winRules.put("소장", new HashSet<>(Arrays.asList(
            "군기", "준장", "대령", "중령", "소령", "대위", "중위",
            "소위", "준위", "상사", "중사", "하사", "병장", "상병",
            "일병", "이병", "공병", "척후병", "미사일", "고사포", "레이더"
        )));

        // 준장
        winRules.put("준장", new HashSet<>(Arrays.asList(
            "군기", "대령", "중령", "소령", "대위", "중위", "소위",
            "준위", "상사", "중사", "하사", "병장", "상병", "일병",
            "이병", "공병", "척후병", "비행기", "탱크", "미사일",
            "고사포", "레이더"
        )));

        // 대령
        winRules.put("대령", new HashSet<>(Arrays.asList(
            "군기", "중령", "소령", "대위", "중위", "소위", "준위",
            "상사", "중사", "하사", "병장", "상병", "일병", "이병",
            "공병", "척후병", "미사일", "고사포", "레이더"
        )));

        // 중령
        winRules.put("중령", new HashSet<>(Arrays.asList(
            "소령", "대위", "중위", "소위", "준위", "상사", "중사",
            "하사", "병장", "상병", "일병", "이병", "공병", "척후병",
            "고사포", "레이더"
        )));

        // 소령
        winRules.put("소령", new HashSet<>(Arrays.asList(
            "대위", "중위", "소위", "준위", "상사", "중사", "하사",
            "병장", "상병", "일병", "이병", "공병", "척후병", "고사포", "레이더"
        )));

        // 대위
        winRules.put("대위", new HashSet<>(Arrays.asList(
            "군기", "중위", "소위", "준위", "상사", "중사", "하사",
            "병장", "상병", "일병", "이병", "공병", "척후병", "고사포", "레이더"
        )));

        // 중위
        winRules.put("중위", new HashSet<>(Arrays.asList(
            "소위", "준위", "상사", "중사", "하사", "병장", "상병",
            "일병", "이병", "공병", "척후병", "고사포", "레이더"
        )));

        // 소위
        winRules.put("소위", new HashSet<>(Arrays.asList(
            "준위", "상사", "중사", "하사", "병장", "상병", "일병",
            "이병", "공병", "척후병", "고사포", "레이더"
        )));

        // 준위
        winRules.put("준위", new HashSet<>(Arrays.asList(
            "상사", "중사", "하사", "병장", "상병", "일병", "이병",
            "공병", "척후병", "고사포", "레이더"
        )));

        // 상사
        winRules.put("상사", new HashSet<>(Arrays.asList(
            "중사", "하사", "병장", "상병", "일병", "이병", "공병", "척후병"
        )));

        // 중사
        winRules.put("중사", new HashSet<>(Arrays.asList(
            "하사", "병장", "상병", "일병", "이병", "공병", "척후병"
        )));

        // 하사
        winRules.put("하사", new HashSet<>(Arrays.asList(
            "병장", "상병", "일병", "이병", "공병", "척후병"
        )));

        // 병장
        winRules.put("병장", new HashSet<>(Arrays.asList(
            "상병", "일병", "이병", "공병", "척후병"
        )));

        // 상병
        winRules.put("상병", new HashSet<>(Arrays.asList(
            "일병", "이병", "공병", "척후병"
        )));

        // 일병
        winRules.put("일병", new HashSet<>(Arrays.asList(
            "이병", "공병", "척후병"
        )));

        // 이병
        winRules.put("이병", new HashSet<>(Arrays.asList(
            "공병", "척후병"
        )));

        // 공병
        winRules.put("공병", new HashSet<>(Arrays.asList(
            "지뢰", "척후병", "탱크", "미사일", "고사포", "레이더"
        )));

        // 척후병 - 아무도 이기지 못함
        winRules.put("척후병", new HashSet<>());

        // 비행기
        winRules.put("비행기", new HashSet<>(Arrays.asList(
            "군기", "지뢰", "대장", "중장", "소장", "대령", "중령",
            "소령", "대위", "중위", "소위", "준위", "상사", "중사",
            "하사", "병장", "상병", "일병", "이병", "공병", "척후병", "탱크"
        )));

        // 탱크
        winRules.put("탱크", new HashSet<>(Arrays.asList(
            "군기", "대장", "중장", "소장", "대령", "중령", "소령",
            "대위", "중위", "소위", "준위", "상사", "중사", "하사",
            "병장", "상병", "일병", "이병", "척후병", "미사일", "레이더"
        )));

        // 미사일
        winRules.put("미사일", new HashSet<>(Arrays.asList(
            "군기", "지뢰", "중령", "소령", "대위", "중위", "소위",
            "준위", "상사", "중사", "하사", "병장", "상병", "일병",
            "이병", "척후병", "비행기", "탱크", "레이더"
        )));

        // 고사포
        winRules.put("고사포", new HashSet<>(Arrays.asList(
            "상사", "중사", "하사", "병장", "상병", "일병", "이병",
            "척후병", "비행기", "레이더"
        )));

        // 레이더
        winRules.put("레이더", new HashSet<>(Arrays.asList(
            "상사", "중사", "하사", "병장", "상병", "일병", "이병",
            "척후병", "비행기", "탱크", "미사일"
        )));

        log.info("Loaded {} battle rules from source code", winRules.size());
    }

    /**
     * 두 말이 충돌했을 때 승부를 판정합니다.
     *
     * @param attacker 공격하는 말의 타입
     * @param defender 방어하는 말의 타입
     * @return 1: attacker wins, -1: defender wins, 0: draw (both die)
     */
    public int resolveBattle(PieceType attacker, PieceType defender) {
        if (attacker == null || defender == null) {
            return 0;
        }

        String attackerName = attacker.getKoreanName();
        String defenderName = defender.getKoreanName();

        // 동일한 말끼리는 무승부 (둘 다 패배)
        if (attackerName.equals(defenderName)) {
            log.debug("Battle: {} vs {} - Draw (same piece)", attackerName, defenderName);
            return 0;
        }

        boolean attackerWins = canDefeat(attackerName, defenderName);
        boolean defenderWins = canDefeat(defenderName, attackerName);

        if (attackerWins && !defenderWins) {
            log.debug("Battle: {} defeats {}", attackerName, defenderName);
            return 1;  // Attacker wins
        } else if (!attackerWins && defenderWins) {
            log.debug("Battle: {} defeats {}", defenderName, attackerName);
            return -1; // Defender wins
        } else {
            // 둘 다 이기지 못하는 경우도 무승부
            log.debug("Battle: {} and {} both die", attackerName, defenderName);
            return 0;  // Draw - both die
        }
    }

    /**
     * 특정 말이 다른 말을 이길 수 있는지 확인합니다.
     */
    private boolean canDefeat(String attacker, String defender) {
        Set<String> defeatable = winRules.get(attacker);
        return defeatable != null && defeatable.contains(defender);
    }

    /**
     * 특정 말이 이동 가능한지 확인합니다.
     * 지뢰는 이동할 수 없습니다.
     */
    public boolean canMove(PieceType pieceType) {
        if (pieceType == null) {
            return false;
        }

        // 지뢰는 이동 불가
        return pieceType != PieceType.MINE;
    }

    /**
     * 척후병인지 확인합니다.
     */
    public boolean isScout(PieceType pieceType) {
        return pieceType == PieceType.SCOUT;
    }
}
