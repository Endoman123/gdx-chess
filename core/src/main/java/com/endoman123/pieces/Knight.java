package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class Knight extends Piece {
    public Knight(Team t) {
        super('N', t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        if (t.equals(Team.WHITE))
            setSprite(a.createSprite("knight_white"));
        else
            setSprite(a.createSprite("knight_black"));
    }

    @Override
    public Array<Cell> getMoves(Cell[][] board, int file, int rank) {
        int up = rank + 1, down = rank - 1, left = file - 1, right = file + 1;
        Array<Cell> possibleMoves = new Array<Cell>();
        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        boolean validUp = up < board.length;
        boolean validUp2 = up + 1 < board.length;
        boolean validDown = down >= 0;
        boolean validDown2 = down - 1 > 0;
        boolean validLeft = left >= 0;
        boolean validLeft2 = left - 1 > 0;
        boolean validRight = right < board[0].length;
        boolean validRight2 = right + 1 < board[0].length;

        if (validUp2) {
            if (validRight)
                possibleMoves.add(board[up + 1][right]);
            if (validLeft)
                possibleMoves.add(board[up + 1][left]);
        }

        if (validDown2) {
            if (validRight)
                possibleMoves.add(board[down - 1][right]);
            if (validLeft)
                possibleMoves.add(board[down - 1][left]);
        }

        if (validLeft2) {
            if (validUp)
                possibleMoves.add(board[up][left - 1]);
            if (validDown)
                possibleMoves.add(board[down][left - 1]);
        }

        if (validRight2) {
            if (validUp)
                possibleMoves.add(board[up][right + 1]);
            if (validDown)
                possibleMoves.add(board[down][right + 1]);
        }

        for (Cell c : possibleMoves) {
            boolean opposing = c != null && c.getPiece() != null && c.getPiece().getTeam() != getTeam();
            boolean empty = c != null && c.getPiece() == null;

            if (opposing || empty)
                POSSIBLE_MOVES.add(c);
        }

        return POSSIBLE_MOVES;
    }
}
