package com.jtulayan.chess;

/**
 * Class that contains some string constants for various moves and states in algebraic notation.
 * This class will not create move notations themselves, just the means to do so.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic Notation (AN)</a>
 */
public final class AlgebraicNotation {
    public static final String
            CAPTURE = "x",
            PROMOTION = "=",
            EN_PASSANST = "e.p.",
            CHECK = "+",
            CHECKMATE = "++",
            CASTLE_KINGSIDE = "0-0",
            CASTLE_QUEENSIDE = "0-0-0";

    /**
     * Gets the AN for a given tile
     * @param r the rank of the tile
     * @param f the file of the tile
     * @return string representation of the given rank and file
     */
    public static String getAN(int r, int f) {
        if (r > 7 || r < 0 ) {
            throw new IllegalArgumentException("Rank is outside range! Was " + r);
        } else if (f > 7  || f < 0) {
            throw new IllegalArgumentException("File is outside range! Was " + f);
        }

        return "" + (char)(f + 97) + (8 - r);
    }

    /**
     * Gets the location for a given tile as the tile's index
     * @param an the AN for the tile
     * @return int representing the index of the tile location
     */
    public static int getTileIndex(String an) {
        if (an.length() != 2 || Character.isDigit(an.charAt(0)) || !Character.isDigit(an.charAt(1)))
            throw new IllegalArgumentException("Invalid format!");

        int rank = Integer.parseInt("" + an.charAt(1)) - 1;
        int file = (int)an.charAt(0) - 96;

        return rank * 8 + file;
    }

    /**
     * Gets the location for a given tile as a bitboard
     * @param an the AN for the tile
     * @return long representing the bitboard of the tile location
     */
    public static long getTileBitboard(String an) {
        return (long)Math.pow(2, getTileIndex(an));
    }

    private AlgebraicNotation() {
        // Never be able to construct this class, this is just a utility class.
    }
}
