package com.jtulayan.chess.player;

/**
 * General interface for any object that can call out moves to make on the chessboard.
 */
public interface Player {

    /**
     * Method to get the move from this player.
     * @return a move, notated as "<origin_space><dest_space>"
     */
    String createMove();
}