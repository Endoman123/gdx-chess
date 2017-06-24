package com.endoman123.util;

import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
import com.endoman123.board.Cell;
import com.endoman123.pieces.King;
import com.endoman123.pieces.Piece;
import com.endoman123.pieces.Team;

/**
 * Class containing methods to filter movesets.
 *
 * @author Jared Tulayan
 */
public class MoveFilters {
    /**
     * Removes the move that captures the king
     * @param moves the set of moves that can be made
     * @return the filtered moveset
     */
    public static Array<Cell> filterKingCapture(Array<Cell> moves) {
        for (Cell c : moves) {
            Piece curPiece = c.getPiece();

            if (curPiece != null && curPiece instanceof King) {
                moves.removeValue(c, true);
                break;
            }
        }

        return moves;
    }

    /**
     * Fiters out moves that would leave the king in check.
     *
     * @param board the {@code Board} that these moves are being made on
     * @param moves list of moves that can be made
     * @param src   the beginning position of the
     * @return a filtered list of moves for the current board state
     */
    public static Array<Cell> filterCheck(Board board, Array<Cell> moves, Cell src, Team t) {
        int i = 0;
        while (i < moves.size) {
            Cell curMove = moves.get(i);

            if (isMoveCheck(board, src, curMove))
                moves.removeValue(curMove, true);
            else
                i++;
        }

        return moves;
    }

    /**
     * Filters illegal castles from the moveset
     * @param board the board to check castling on
     * @param moves the king's moveset
     * @param src   the king's starting position
     * @return the filtered moveset
     */
    public static Array<Cell> filterCastle(Board board, Array<Cell> moves, Cell src) {
        int i = 0;
        while (i < moves.size) {
            Cell cell = moves.get(i);
            int dist = cell.FILE - src.FILE;
            if (Math.abs(dist) != 2) {
                i++;
                continue;
            }

            int direction = (int) Math.signum(dist);

            if (!moves.contains(board.CELLS[src.RANK][cell.FILE + direction], true))
                moves.removeValue(cell, true);
            else
                i++;
        }

        return moves;
    }

    /**
     * Single move check using a brute-force method of copying the current board state and checking against all
     * enemy moves to see if the move being made will leave the king in check.
     *
     * @param board the board state to test on
     * @param src   the starting position of the piece
     * @param dst   the cell the piece is moving to
     * @return whether or not the current move is going to place its king in check,
     *         given the current board state
     */
    private static boolean isMoveCheck(Board board, Cell src, Cell dst) {
        Array<Cell> possibleMoves = new Array<Cell>();
        Cell[][] boardCopy;
        Team team = src.getPiece().getTeam();
        Cell srcCopy;
        Cell dstCopy;
        Cell kingCopy;

        // Clone board
        try {
            boardCopy = new Cell[board.NUM_RANKS][board.NUM_FILES];
            for (int i = 0; i < board.NUM_FILES * board.NUM_RANKS; i++) {
                int file = i % board.NUM_FILES;
                int rank = i / board.NUM_FILES;

                boardCopy[rank][file] = new Cell(board.CELLS[rank][file]);
            }

            srcCopy = boardCopy[src.RANK][src.FILE];
            dstCopy = boardCopy[dst.RANK][dst.FILE];

            if (src.getPiece() instanceof King) // If the king is actually who is moving
                kingCopy = dstCopy;
            else {
                Cell kingCell = board.getCellContaining(board.getKing(team));
                kingCopy = boardCopy[kingCell.RANK][kingCell.FILE];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Do pseudo-move
        Piece p1 = srcCopy.getPiece();

        srcCopy.setPiece(null);
        dstCopy.setPiece(p1);

        // Check if the move leaves the king in check
        for (int i = 0; i < board.NUM_FILES * board.NUM_RANKS; i++) {
            Cell c = boardCopy[i / board.NUM_FILES][i % board.NUM_FILES];

            possibleMoves.clear();
            if (c.getPiece() == null || c.getPiece().getTeam() == team)
                continue;

            possibleMoves.addAll(c.getPiece().getMoves(boardCopy, c.FILE, c.RANK));

            if (possibleMoves.contains(kingCopy, true))
                return true;
        }

        return false;
    }
}
