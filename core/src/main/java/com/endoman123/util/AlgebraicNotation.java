package com.endoman123.util;

import com.endoman123.board.Cell;
import com.endoman123.pieces.King;
import com.endoman123.pieces.Pawn;
import com.endoman123.pieces.Piece;

/**
 * Class to make algebraic notations (AN) for moves, positions, etc.
 * Also contains constants for certain notations such as captures, checks, and checkmates.
 *
 * @author Jared Tulayan
 */
public class AlgebraicNotation {
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

    public static String notateMove(Cell src, Cell dst, King king) {
        StringBuilder notation = new StringBuilder();
        Piece p1 = src.getPiece();

        boolean capture = dst.getPiece() != null;
        boolean enPassant = p1 instanceof Pawn && dst.FILE != src.FILE && dst.getPiece() == null;
        boolean castle_kingside = p1 instanceof King && dst.FILE - src.FILE == 2;
        boolean castle_queenside = p1 instanceof King && dst.FILE - src.FILE == -2;

        if (castle_kingside)
            notation.append(Constants.CASTLE_KINGSIDE);
        else if (castle_queenside)
            notation.append(Constants.CASTLE_QUEENSIDE);
        else {
            notation.append(p1);

            if (capture || enPassant) {
                if (p1 instanceof Pawn)
                    notation.append(AlgebraicNotation.convertToBase26(src.FILE + 1));
                notation.append(Constants.CAPTURE);
            }

            notation.append(dst);

            if (enPassant)
                notation.append(Constants.EN_PASSANT);

            if (king.isInCheck()) {
                if (king.canMove())
                    notation.append(Constants.CHECK);
                else
                    notation.append(Constants.CHECKMATE);
            }
        }

        return notation.toString();
    }

    /**
     * Constant string notations used in AN
     */
    public static class Constants {
        public static final String
                CAPTURE = "x",
                CHECK = "+",
                CHECKMATE = "#",
                EN_PASSANT = "e.p.",
                CASTLE_KINGSIDE = "0-0",
                CASTLE_QUEENSIDE = "0-0-0",
                WIN_A = "1-0",
                WIN_B = "0-1",
                DRAW = "\u00BD-\u00BD";
    }
}
