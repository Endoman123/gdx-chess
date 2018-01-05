package com.jtulayan.chess;

/**
 * Class containing all move generators.
 *
 * Moves are notated by a list of origin squares to destination squares
 * as well as the piece on the destination square and special flags,
 * written as "f1r1f2r2dF."
 * Elements in the list are separated with /.
 *
 * Flags are 1 byte words that represent captures, promotions, and other
 * special identifiers.
 */
public class MoveGenerators {
    // Flags
    // If you know anything about bitwise operations,
    // this should make total sense.
    public static final int
        F_CAPTURE = 0x4,
        F_PROMO = 0x8,
        F_SPECIAL_0 = 0x1,
        F_SPECIAL_1 = 0x2;

    /**
     * Encodes the move given the properties of the move.
     * @param from      the index of the origin tile (0-63)
     * @param to        the index of the destination tile (0-63)
     * @param destPiece the piece on the destination tile
     * @param flags     special flags to specify the type of move
     * @return the encoded move in a string
     */
    public static String encodeMove(int from, int to, char destPiece, long flags) {
        String move = "";

        // Encode from-to section
        move += Board.AlgebraicNotation.getAN(from / 8, from % 8) + Board.AlgebraicNotation.getAN(to / 8, to % 8);

        // Encode destination piece and flags
        char d = destPiece == ' ' ? '-' : destPiece;
        move += "" + d + flags;

        return move;
    }

    public static String generateKingMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char king = b.BOARD_STATE[rank][file];

        return list;
    }
}
