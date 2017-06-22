package com.endoman123.util;

/**
 * Class to make algebraic notations (AN) for moves, positions, etc.
 * Also contains constants for certain notations such as captures, checks, and checkmates.
 *
 * @author Jared Tulayan
 */
public class AlgebraicNotation {
    public static final String
        CAPTURE = "x",
        CHECK = "+",
        CHECKMATE = "#",
        WIN_A = "1-0",
        WIN_B = "0-1",
        DRAW = "\u00BD-\u00BD";

    /**
     * Notates the specified location in AN.
     * @param file the file (column) of the location
     * @param rank the rank (row) of the location
     * @return the location {@code (file + 1, rank + 1)} in AN
     */
    public static String notatePosition(int file, int rank) {
        return convertToBase26(file + 1) + (rank + 1);
    }

    /**
     * Converts the specified number to a string of letters representing the number in base 26.
     * @param i the number in base 10
     * @return the number in base 26, digits represented with letters a-z
     */
    public static String convertToBase26(int i) {
        StringBuilder str = new StringBuilder();

        while (i > 0) {
            i--;
            int remain = i % 26;
            str.insert(0, (char)('a' + remain));

            i = (i - remain) / 26;
        }

        return str.toString();
    }
}
