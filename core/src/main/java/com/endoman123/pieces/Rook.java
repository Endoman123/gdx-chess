package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class Rook extends Piece {
    public Rook(Team t) {
        super('R', t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        setSprite(a.createSprite(t.getPath() + "rook"));
    }

    @Override
    public Array<Cell> getMoves(Cell[][] board, int file, int rank) {
        boolean endUp = false, endDown = false, endLeft = false, endRight = false;
        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        // Clever raycast of sorts I guess
        int i = 1;
        while (!endUp || !endDown || !endLeft || !endRight) {
            int up = rank + i;
            int down = rank - i;
            int left = file - i;
            int right = file + i;

            endUp = endUp || up >= board.length;
            endDown = endDown || down < 0;
            endLeft = endLeft || left < 0;
            endRight = endRight || right >= board.length;

            if (!endUp) {
                Cell c = board[up][file];

                if (c.getPiece() != null) {
                    endUp = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endRight) {
                Cell c = board[rank][right];

                if (c.getPiece() != null) {
                    endRight = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endDown) {
                Cell c = board[down][file];

                if (c.getPiece() != null) {
                    endDown = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endLeft) {
                Cell c = board[rank][left];

                if (c.getPiece() != null) {
                    endLeft = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            i++;
        }

        return POSSIBLE_MOVES;
    }
}
