package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
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
    public Array<Cell> getMoves(Board board, int file, int rank) {
        int dir;
        boolean hasMoved;

        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        if (getTeam().equals(Team.WHITE)) {
            dir = 1; // Going forward
            hasMoved = rank == 1;
        } else {
            dir = -1; // Going backward
            hasMoved = rank == 6;
        }

        Cell
            fwd1 = board.getCellAt(file, rank + dir),
            fwd2 = board.getCellAt(file, rank + 2 * dir),
            attackL = board.getCellAt(file - 1, rank + dir),
            attackR = board.getCellAt(file + 1, rank + dir);

        if (fwd1.getPiece() == null) {
            POSSIBLE_MOVES.add(fwd1);
            if (hasMoved && fwd2.getPiece() == null)
                POSSIBLE_MOVES.add(fwd2);
        }
        if (attackL != null && attackL.getPiece() != null && !attackL.getPiece().getTeam().equals(getTeam()))
            POSSIBLE_MOVES.add(attackL);
        if (attackR != null && attackR.getPiece() != null && !attackR.getPiece().getTeam().equals(getTeam()))
            POSSIBLE_MOVES.add(attackR);

        return POSSIBLE_MOVES;
    }
}
