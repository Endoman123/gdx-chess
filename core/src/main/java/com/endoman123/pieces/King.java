package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
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

    public boolean getCheck() {
        return isInCheck;
    }

    @Override
    public Array<Cell> getMoves(Board board, int file, int rank) {
        // You should always clear the moves list
        POSSIBLE_MOVES.clear();

        Cell[] area = new Cell[] {
                board.getCellAt(file + 1, rank),
                board.getCellAt(file - 1, rank),
                board.getCellAt(file, rank + 1),
                board.getCellAt(file, rank - 1),
                board.getCellAt(file + 1, rank + 1),
                board.getCellAt(file + 1, rank - 1),
                board.getCellAt(file - 1, rank + 1),
                board.getCellAt(file - 1, rank - 1)
        };

        for (int i = 0; i < area.length; i++) {
            Cell cur = area[i];

            boolean opposing = cur != null && cur.getPiece() != null && cur.getPiece().getTeam() != getTeam();
            boolean empty = cur != null && cur.getPiece() == null;

            if (opposing || empty)
                POSSIBLE_MOVES.add(cur);
        }

        return POSSIBLE_MOVES;
    }
}
