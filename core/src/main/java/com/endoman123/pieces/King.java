package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class King extends Piece {
    private boolean isInCheck;

    public King(Team t) {
        super('K', t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        if (t.equals(Team.WHITE))
            setSprite(a.createSprite("king_white"));
        else
            setSprite(a.createSprite("king_black"));
    }

    public void setCheck(boolean check) {
        isInCheck = check;
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    @Override
    public Array<Cell> getMoves(Cell[][] board, int file, int rank) {
        int up = rank + 1, down = rank - 1, left = file - 1, right = file + 1;

        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        Array<Cell> possibleMoves = new Array<Cell>();

        boolean validUp = up < board.length;
        boolean validDown = down >= 0;
        boolean validLeft = left >= 0;
        boolean validRight = right < board[0].length;

        if (validUp)
            possibleMoves.add(board[up][file]);
        if (validDown)
            possibleMoves.add(board[down][file]);
        if (validLeft)
            possibleMoves.add(board[rank][left]);
        if (validRight)
            possibleMoves.add(board[rank][right]);
        if (validUp && validRight)
            possibleMoves.add(board[up][right]);
        if (validUp && validLeft)
            possibleMoves.add(board[up][left]);
        if (validDown && validRight)
            possibleMoves.add(board[down][right]);
        if (validDown && validLeft)
            possibleMoves.add(board[down][left]);

        for (Cell c : possibleMoves) {
            boolean opposing = c != null && c.getPiece() != null && c.getPiece().getTeam() != getTeam();
            boolean empty = c != null && c.getPiece() == null;

            if (opposing || empty)
                POSSIBLE_MOVES.add(c);
        }

        return POSSIBLE_MOVES;
    }
}
