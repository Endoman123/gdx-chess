package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
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
    public Array<Cell> getMoves(Board board, int file, int rank) {
        boolean endUp = false, endDown = false, endLeft = false, endRight = false, endUR = false, endUL = false, endDR = false, endDL = false;
        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        // Clever raycast of sorts I guess
        int i = 1;
        while (!endUp || !endDown || !endLeft || !endRight || !endUR || !endUL || !endDR || !endDL) {
            if (!endUp) {
                int up = rank + i;

                if (up == board.NUM_RANKS)
                    endUp = true;
                else {
                    Cell c = board.getCellAt(file, up);

                    if (c.getPiece() != null) {
                        endUp = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endRight) {
                int right = file + i;

                if (right == board.NUM_FILES)
                    endRight = true;
                else {
                    Cell c = board.getCellAt(right, rank);

                    if (c.getPiece() != null) {
                        endRight = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endDown) {
                int down = rank - i;

                if (down < 0)
                    endDown = true;
                else {
                    Cell c = board.getCellAt(file, down);

                    if (c.getPiece() != null) {
                        endDown = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endLeft) {
                int left = file - i;

                if (left < 0)
                    endLeft = true;
                else {
                    Cell c = board.getCellAt(left, rank);

                    if (c.getPiece() != null) {
                        endLeft = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endUR) {
                int up = rank + i;
                int right = file + i;

                if (up == board.NUM_RANKS || right == board.NUM_FILES)
                    endUR = true;
                else {
                    Cell c = board.getCellAt(right, up);

                    if (c.getPiece() != null) {
                        endUR = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endUL) {
                int up = rank + i;
                int left = file - i;

                if (up == board.NUM_RANKS || left < 0)
                    endUL = true;
                else {
                    Cell c = board.getCellAt(left, up);

                    if (c.getPiece() != null) {
                        endUL = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endDR) {
                int down = rank - i;
                int right = file + i;

                if (down < 0 || right == board.NUM_FILES)
                    endDR = true;
                else {
                    Cell c = board.getCellAt(right, down);
                    if (c.getPiece() != null) {
                        endDR = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            if (!endDL) {
                int down = rank - i;
                int left = file - i;

                if (down < 0 || left < 0)
                    endDL = true;
                else {
                    Cell c = board.getCellAt(left, down);

                    if (c.getPiece() != null) {
                        endUL = true;
                        if (c.getPiece().getTeam() != getTeam())
                            POSSIBLE_MOVES.add(c);
                    } else
                        POSSIBLE_MOVES.add(c);
                }
            }

            i++;
        }

        return POSSIBLE_MOVES;
    }
}
