package com.jtulayan.chess;

import java.util.Arrays;

/**
 * Code representation of the game board.
 * In the context of the Memento Pattern, this is the Originator.
 *
 * The board itself has various fields to represent the board state, as well
 * as a halfmove counter, fullmove counter, etc.
 *
 * Mementos are stored in FEN, and there are methods in this class to parse mementos into
 * board states.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Memento_pattern#Structure">Memento Pattern Structure</a>
 * @see <a href="https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">Forsyth-Edwards Notation (FEN)</a>
 */
public class Board {
    private final char[][] BOARD_STATE = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
    };

    private boolean isWhiteTurn = true;

    private long castles = 0xFL;
    private long enPassant = 0x0L;

    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;

    /**
     * Initializes board.
     */
    public Board() {

    }

    /**
     * Clears the board
     */
    public void clearBoard() {
        int size = BOARD_STATE.length * BOARD_STATE[0].length;

        for (int i = 0; i < 64; i++)
            BOARD_STATE[i / 8][i % 8] = ' ';
    }

    // region Memento Pattern
    public Memento createMemento() {
        return new Memento(this);
    }

    public void restore(Memento memento) {
        String state = memento.toString(), board = "", turn = "", cas = "", enp = "";

        // Repopulate board
        // This is done by clearing the board, then parsing
        // the FEN board
        board = state.substring(0, state.indexOf(' '));
        clearBoard();
        for (int i = 0; i < 8; i++) {
            String rank = board;

            if (i < 7)
                rank = board.substring(0, board.indexOf('/'));

            int blankFile = 0;
            for (int j = 0; j < 8; j++) {
                char tile = rank.charAt(0);

                if (Character.isDigit(tile) && blankFile == 0) {
                    blankFile = Integer.parseInt("" + tile);

                    if (rank.length() > 1)
                        rank = rank.substring(1);
                }

                if (blankFile > 0) {
                    BOARD_STATE[i][j] = ' ';
                    blankFile -= 1;
                } else {
                    BOARD_STATE[i][j] = tile;

                    if (rank.length() > 1)
                        rank = rank.substring(1);
                }
            }

            if (i < 7)
                board = board.substring(board.indexOf('/') + 1);
        }

        state = state.substring(state.indexOf(' ') + 1);

        // Check if it is white's turn
        turn = state.substring(0, state.indexOf(' '));
        isWhiteTurn = turn == "w";
        state = state.substring(state.indexOf(' ') + 1);

        // Get castling abilities
        cas = state.substring(0, state.indexOf(' '));
        castles = 0x0L;
        if (cas.contains("K"))
            castles += 0x8L;
        if (cas.contains("Q"))
            castles += 0x4L;
        if (cas.contains("k"))
            castles += 0x2L;
        if (cas.contains("q"))
            castles += 0x1L;
        state = state.substring(state.indexOf(' ') + 1);

        enp = state.substring(0, state.indexOf(' '));
        if ("-".equals(enp))
            enPassant = 0x0L;
        else
            enPassant = AlgebraicNotation.getTile(enp);
        state = state.substring(state.indexOf(' ') + 1);

        halfmoveClock = Integer.parseInt(state.substring(0, state.indexOf(' ')));
        state = state.substring(state.indexOf(' ') + 1);

        fullmoveNumber = Integer.parseInt(state);
    }

    /**
     * Memento class used to store history of board state.
     */
    public static class Memento {
        private String state;

        /**
         * Converts the specified board into FEN notation, for easier storage.
         * @param b the Board to store in the memento
         */
        public Memento(Board b) {
            String newMemento = "";
            // Start by storing the board itself
            for (int i = 0; i < b.BOARD_STATE.length; i++) {
                int blankSpace = 0;
                for (int j = 0; j < b.BOARD_STATE[i].length; j++) {
                    char curTile = b.BOARD_STATE[i][j];
                    if (curTile == ' ') {
                        blankSpace += 1;
                        if (j == b.BOARD_STATE[i].length - 1)
                            newMemento += "" + blankSpace;
                    } else {
                        if (blankSpace > 0) {
                            newMemento += "" + blankSpace;
                            blankSpace = 0;
                        }
                        newMemento += curTile;
                    }
                }
                if (i < b.BOARD_STATE.length - 1)
                    newMemento += "/";
            }

            newMemento += " ";

            // Get current turn
            newMemento += b.isWhiteTurn ? "w" : "b";

            newMemento += " ";

            // Get castles
            if (b.castles == 0L)
                newMemento = "-";
            else {
                String castlesBin = "000" + Long.toBinaryString(b.castles);
                castlesBin = castlesBin.substring(castlesBin.length() - 4);

                if (castlesBin.charAt(0) == '1')
                    newMemento += 'K';
                if (castlesBin.charAt(1) == '1')
                    newMemento += 'Q';
                if (castlesBin.charAt(2) == '1')
                    newMemento += 'k';
                if (castlesBin.charAt(3) == '1')
                    newMemento += 'q';
            }

            newMemento += " ";

            // Get en passant tile
            String bbEnPasant = "000000000000000000000000000000000000000000000000000000000000000" + Long.toBinaryString(b.enPassant);
            bbEnPasant = bbEnPasant.substring(bbEnPasant.length() - 64);
            if (!bbEnPasant.contains("1") || bbEnPasant.indexOf('1') != bbEnPasant.lastIndexOf('1'))
                newMemento += "-";
            else {
                int loc = bbEnPasant.indexOf('1');
                newMemento += AlgebraicNotation.getAN(loc / 8, loc % 8);
            }

            newMemento += " ";

            // Add halfmove clock and fullmove number
            newMemento += "" + b.halfmoveClock + " " + b.fullmoveNumber;

            state = newMemento;
        }

        public String toString() {
            return state;
        }
    }
    // endregion

    /**
     * Class that contains some string constants for various moves and states in algebraic notation
     *
     * @see <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic Notation (AN)</a>
     */
    public static final class AlgebraicNotation {
        public static final String CAPTURE = "x";
        public static final String PROMOTION = "=";
        public static final String CHECK = "+";
        public static final String CHECKMATE = "++";
        public static final String CASTLE_KINGSIDE = "0-0";
        public static final String CASTLE_QUEENSIDE = "0-0-0";

        /**
         * Gets the AN for a given tile
         * @param r the rank of the tile
         * @param f the file of the tile
         * @return string representation of the given rank and file
         */
        public static String getAN(int r, int f) {
            if (r > 7 || r < 0 ) {
                throw new IllegalArgumentException("Rank is outside range!");
            } else if (f > 7  || f < 0) {
                throw new IllegalArgumentException("File is outside range!");
            }

            return "" + (char)(r + 97) + (f + 1);
        }

        /**
         * Gets the location for a given tile
         * @param an the AN for the tile
         * @return long representing the bitboard of the tile location
         */
        public static long getTile(String an) {
            if (an.length() != 2 || Character.isDigit(an.charAt(0)) || !Character.isDigit(an.charAt(1)))
                throw new IllegalArgumentException("Invalid format!");

            int rank = Integer.parseInt("" + an.charAt(1)) - 1;
            int file = (int)an.charAt(0) - 96;

            return (long)Math.pow(2, rank * 8 + file - 1);
        }

        private AlgebraicNotation() {
            // Never be able to construct this class, this is just a utility class.
        }
    }
}
