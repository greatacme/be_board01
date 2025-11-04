package com.board.game.model;

public enum GameStatus {
    WAITING,    // Waiting for players
    SETUP,      // Both players joined, setting up pieces
    PLAYING,    // Game in progress
    FINISHED    // Game finished
}
