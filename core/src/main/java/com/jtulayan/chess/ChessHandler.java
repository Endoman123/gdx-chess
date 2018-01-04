package com.jtulayan.chess;

/**
 * Game Caretaker, in context of Memento Pattern.
 *
 * This handles making and validating chess moves, along with
 * keeping track of the game state.
 *
 * More or less, this will actually handle the backend of the game.
 * No GUI is drawn in this class.
 */
public class ChessHandler {
    public static void main(String[] args) {
        Board b = new Board();

        Board.Memento m = b.createMemento();

        b.restore(m);
    }
}
