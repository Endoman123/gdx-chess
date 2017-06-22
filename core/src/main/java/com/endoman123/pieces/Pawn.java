package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class Pawn extends Piece {
    public Pawn(Team t) {
        super(Character.MIN_VALUE, t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        if (t.equals(Team.WHITE))
            setSprite(a.createSprite("pawn_white"));
        else
            setSprite(a.createSprite("pawn_black"));
    }

    @Override
    public Array<Cell> getMoves(Cell[][] board, int file, int rank) {
        int dir = getTeam().getDirection(), fwd = rank + dir, fwd2 = rank + 2 * dir, left = file - 1, right = file + 1;
        boolean hasMoved;

        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        if (getTeam() == Team.WHITE)
            hasMoved = rank != 1;
        else
            hasMoved = rank != 6;

        if (fwd < board.length) {
            if (board[fwd][file].getPiece() == null) {
                POSSIBLE_MOVES.add(board[fwd][file]);
                if (board[fwd2][file].getPiece() == null && !hasMoved)
                    POSSIBLE_MOVES.add(board[fwd2][file]);
            }
        }

        if (left >= 0) {
            Cell attack = board[fwd][left];

            if (attack.getPiece() != null && attack.getPiece().getTeam() != getTeam())
            POSSIBLE_MOVES.add(attack);
        }

        if (right < board[0].length) {
            Cell attack = board[fwd][right];

            if (attack.getPiece() != null && attack.getPiece().getTeam() != getTeam())
                POSSIBLE_MOVES.add(attack);
        }

        return POSSIBLE_MOVES;
    }
}
