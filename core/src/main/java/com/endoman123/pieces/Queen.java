package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class Queen extends Piece {
    public Queen(Team t) {
        super('Q', t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        if (t.equals(Team.WHITE))
            setSprite(a.createSprite("queen_white"));
        else
            setSprite(a.createSprite("queen_black"));
    }

    @Override
    public Array<Cell> getMoves(Cell[][] board, int file, int rank) {
        boolean endUp = false, endDown = false, endLeft = false, endRight = false, endUR = false, endUL = false, endDR = false, endDL = false;
        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        // Clever raycast of sorts I guess
        int i = 1;
        while (!endUp || !endDown || !endLeft || !endRight || !endUR || !endUL || !endDR || !endDL) {
            int up = rank + i;
            int down = rank - i;
            int left = file - i;
            int right = file + i;

            endUp = endUp || up >= board.length;
            endDown = endDown || down < 0;
            endLeft = endLeft || left < 0;
            endRight = endRight || right >= board.length;

            endUR = endUR || up >= board.length || right >= board[0].length;
            endUL = endUL || up >= board.length || left < 0;
            endDR = endDR || down < 0 || right == board[0].length;
            endDL = endDL || down < 0 || left < 0;

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

            if (!endUR) {
                Cell c = board[up][right];

                if (c.getPiece() != null) {
                    endUR = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endUL) {
                Cell c = board[up][left];

                if (c.getPiece() != null) {
                    endUL = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endDR) {
                Cell c = board[down][right];
                if (c.getPiece() != null) {
                    endDR = true;
                    if (c.getPiece().getTeam() != getTeam())
                        POSSIBLE_MOVES.add(c);
                } else
                    POSSIBLE_MOVES.add(c);
            }

            if (!endDL) {
                Cell c = board[down][left];

                if (c.getPiece() != null) {
                    endDL = true;
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
