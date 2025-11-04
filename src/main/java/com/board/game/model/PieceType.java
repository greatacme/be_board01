package com.board.game.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PieceType {
    // 특수
    FLAG("군기", "⚑"),           // 1개
    MINE("지뢰", "💣"),          // 2개

    // 장성급 (4개)
    GENERAL("대장", "★★★★"),        // 최고 계급
    LIEUTENANT_GENERAL("중장", "★★★"),
    MAJOR_GENERAL("소장", "★★"),
    BRIGADIER_GENERAL("준장", "★"),

    // 영관급 (3개)
    COLONEL("대령", "▣"),
    LIEUTENANT_COLONEL("중령", "▤"),
    MAJOR("소령", "▥"),

    // 위관급 (4개)
    CAPTAIN("대위", "◆"),
    FIRST_LIEUTENANT("중위", "◇"),
    SECOND_LIEUTENANT("소위", "◈"),
    WARRANT_OFFICER("준위", "◉"),

    // 부사관 (3개)
    MASTER_SERGEANT("상사", "▲"),
    SERGEANT_FIRST_CLASS("중사", "△"),
    SERGEANT("하사", "▽"),

    // 병사 (4개)
    STAFF_SERGEANT("병장", "●"),
    CORPORAL("상병", "◐"),
    PRIVATE_FIRST_CLASS("일병", "◑"),
    PRIVATE("이병", "○"),

    // 특수부대 (14개)
    ENGINEER("공병", "⚒"),       // 2개
    SCOUT("척후병", "👁"),        // 2개
    AIRPLANE("비행기", "✈"),     // 2개
    TANK("탱크", "▮"),           // 2개
    MISSILE("미사일", "🚀"),      // 2개
    ANTI_AIRCRAFT("고사포", "⚡"),// 2개
    RADAR("레이더", "📡");        // 2개

    private final String koreanName;
    private final String symbol;

    PieceType(String koreanName, String symbol) {
        this.koreanName = koreanName;
        this.symbol = symbol;
    }

    public String getName() {
        return this.name();
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getSymbol() {
        return symbol;
    }
}
