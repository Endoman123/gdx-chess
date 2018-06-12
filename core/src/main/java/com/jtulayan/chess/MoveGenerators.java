package com.jtulayan.chess;

import java.util.ArrayList;

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
        F_QUIET = 0x0,
        F_PROMO = 0x8,
        F_CAPTURE = 0x4,
        F_SPECIAL_1 = 0x2,
        F_SPECIAL_0 = 0x1;

    /**
     * Encodes the move given the properties of the move.
     * @param from      the index of the origin tile (0-63)
     * @param to        the index of the destination tile (0-63)
     * @param destPiece the piece on the destination tile
     * @param flags     special flags to specify the type of move
     * @return the encoded move in a string
     */
    public static String encodeMove(int from, int to, char destPiece, int flags) {
        String move = "";

        // Encode from-to section
        move += AlgebraicNotation.getAN(from / 8, from % 8) + AlgebraicNotation.getAN(to / 8, to % 8);

        // Encode destination piece and flags
        char d = destPiece == ' ' ? '-' : destPiece;
        move += "" + d + String.format("%02d", flags);

        return move;
    }

    public static String listKingMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char king = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(king);

        for (int i = -4; i <= 4; i++) {
            int cur = from + i;
            int flags = F_QUIET;
            char curPiece;
            // Skip numbers that are itself and out of index
            if (cur == from || cur < 0 || cur > 63)
                continue;

            curPiece = b.getPiece(cur / 8, cur % 8);

            // Case-checking to determine if the piece
            // on the tile is on the same team as the king.
            if (' ' != curPiece && Character.isUpperCase(curPiece) == isWhite)
                continue;

            // If it's a capture
            if (' ' != curPiece)
                flags |= F_CAPTURE;

            list += encodeMove(from, cur, ' ', flags) + "/";
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 2);

        return list;
    }

    public static String listQueenMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char queen = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(queen);

        // Loop to cover every direction.
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Ignore (0, 0).
                if (i == 0 && j == 0)
                    continue;

                int counter = 1;
                boolean blocked = false;
                while (!blocked) {
                    int curRank = rank + i * counter;
                    int curFile = file + j * counter;
                    int cur = curRank * 8 + curFile;
                    int flags = F_QUIET;

                    if (curRank > 7 || curRank < 0 || curFile > 7 || curFile < 0)
                        break;

                    char curPiece = b.getPiece(curRank, curFile);

                    blocked = ' ' != curPiece;

                    if (blocked) {
                        if (Character.isUpperCase(curPiece) != isWhite)
                            flags |= F_CAPTURE;
                        else
                            break;
                    }

                    list += encodeMove(from, cur, ' ', flags) + "/";

                    counter++;
                }
            }
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    public static String listKnightMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char knight = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(knight);

        for (int r = -2; r <= 2; r++) {
            int curRank = rank + r;

            // If rank is directly on 0
            // or is out of range
            if (r == 0 || curRank < 0 || curRank > 7)
                continue;

            for (int f = -2; f <= 2; f++) {
                int curFile = file + f;

                // If file is on 0,
                // displacement is directly diagonal,
                // or file is out of range
                if (f == 0 || Math.abs(r) == Math.abs(f) || curFile < 0 || curFile > 7)
                    continue;

                int flags = F_QUIET;
                char piece = b.getPiece(rank + r, file + f);

                // Color check
                if (' ' != piece) {
                    if (Character.isUpperCase(piece) != isWhite)
                        flags |= F_CAPTURE;
                    else
                        continue;
                }

                list += encodeMove(from, curRank * 8 + curFile, piece, flags) + "/";
            }
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    public static String listBishopMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char bishop = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(bishop);

        // Loop to cover every direction.
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int counter = 1;
                boolean blocked = false;
                while (!blocked) {
                    int curRank = rank + i * counter;
                    int curFile = file + j * counter;
                    int cur = curRank * 8 + curFile;
                    int flags = F_QUIET;

                    if (curRank > 7 || curRank < 0 || curFile > 7 || curFile < 0)
                        break;

                    char curPiece = b.getPiece(curRank, curFile);

                    blocked = ' ' != curPiece;

                    if (blocked) {
                        if (Character.isUpperCase(curPiece) == isWhite)
                            break;
                        else
                            flags = F_CAPTURE;
                    }

                    list += encodeMove(from, cur, ' ', flags) + "/";

                    counter++;
                }
            }
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    public static String listRookMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char rook = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(rook);

        // Loop to cover every direction.
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (Math.abs(i) == Math.abs(j))
                    continue;

                int counter = 1;
                boolean blocked = false;
                while (!blocked) {
                    int curRank = rank + i * counter;
                    int curFile = file + j * counter;
                    int cur = curRank * 8 + curFile;
                    int flags = F_QUIET;

                    if (curRank > 7 || curRank < 0 || curFile > 7 || curFile < 0)
                        break;

                    char curPiece = b.getPiece(curRank, curFile);

                    blocked = ' ' != curPiece;

                    if (blocked) {
                        if (Character.isUpperCase(curPiece) == isWhite)
                            break;
                        else
                            flags = F_CAPTURE;
                    }

                    list += encodeMove(from, cur, ' ', flags) + "/";

                    counter++;
                }
            }
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    public static String listPawnMoves(Board b, int from) {
        String list = "";

        // Gotta get all the info from the board
        int rank = from / 8;
        int file = from % 8;
        char pawn = b.getPiece(rank, file);
        boolean isWhite = Character.isUpperCase(pawn);

        // Color-specific stuff
        int dir = 0;
        int promoRank = 0;
        boolean starting = false;

        // Decide on color-specific items now, makes this process much easier.
        if (isWhite) {
            dir = -1; // Go up
            promoRank = 0;
            starting = rank == 6; // On white's starting rank
        } else {
            dir = 1; // Go down
            promoRank = 7;
            starting = rank == 1;
        }

        // If the space in front of it is empty, it is pseudo-legal.
        if (b.getPiece(rank + dir, file) == ' ') {
            list += encodeMove(from, from + 8 * dir, ' ', F_QUIET) + "/";
            if (starting && b.getPiece(rank + dir, file) == ' ')
                list += encodeMove(from, from + 16 * dir, ' ', F_SPECIAL_0) + "/";
        }

        // Check if possible captures can be made
        int attackRank = rank + dir;
        for (int f = -1; f <= 1; f += 2) {
            int curFile = file + f;
            if (curFile > 0 && curFile < 7) {
                char curPiece = b.getPiece(rank + dir, file + f);
                int flags = F_QUIET;

                if (' ' != curPiece && Character.isUpperCase(curPiece) != isWhite)
                    flags = F_CAPTURE;
                else if (' ' == curPiece && attackRank * 8 + curFile == b.getEnPassant()) {
                    curPiece = isWhite ? 'p' : 'P';
                    flags = F_CAPTURE | F_SPECIAL_0;
                }

                if ((flags & F_CAPTURE) == F_CAPTURE)
                    list += encodeMove(from, from + 8 * dir + f, curPiece, flags) + "/";
            }
        }

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    /**
     * Filters king captures from the list
     * 
     * @param ml movelist to filter
     */
    public String filterKingCaptures(String ml) {
        ArrayList<String> moveList = new ArrayList<>();
        
        // Parse each move and check if it captures the opponent's king.
        for (int i = 0; i < moveList.size(); i++) {
            String move = moveList.get(i);
            char capPiece = move.toLowerCase().charAt(4);
            int flags = Integer.parseInt(move.substring(5)); 
            
            if (capPiece == 'k') {
                
            }
        }
        
        return moveList;
    }
}
