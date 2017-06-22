package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;
import com.endoman123.util.Assets;

/**
 * @author Jared Tulayan
 */
public class King extends Piece {
    private boolean inCheck;
    private boolean hasMoved;

    public King(Team t) {
        super('K', t);
        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.PIECES_ATLAS);

        setSprite(a.createSprite(t.getPath() + "king"));
    }

    public void setCheck(boolean check) {
        inCheck = check;
    }

    /**
     * Toggle to set moved to true.
     */
    public void toggleMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isInCheck() {
        return inCheck;
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
            boolean opposing = c.getPiece() != null && c.getPiece().getTeam() != getTeam();
            boolean empty = c.getPiece() == null;

            if (opposing || empty)
                POSSIBLE_MOVES.add(c);
        }

        // Castling
        if (!hasMoved && !inCheck) {
            Cell castleLeft = board[rank][2];
            Cell castleRight = board[rank][6];
            Piece rookLeft = board[rank][0].getPiece();
            Piece rookRight = board[rank][7].getPiece();

            boolean leftSideEmpty = board[rank][1].getPiece() == null && castleLeft.getPiece() == null && board[0][3].getPiece() == null;
            boolean rightSideEmpty = board[rank][6].getPiece() == null && castleRight.getPiece() == null;
            boolean leftRookValid = rookLeft instanceof Rook && rookLeft.getTeam() == getTeam() && !((Rook)rookLeft).hasMoved();
            boolean rightRookValid = rookRight instanceof Rook && rookRight.getTeam() == getTeam() && !((Rook)rookRight).hasMoved();

            if (leftSideEmpty && leftRookValid)
                POSSIBLE_MOVES.add(castleLeft);
            if (rightSideEmpty && rightRookValid)
                POSSIBLE_MOVES.add(castleRight);
        }

        return POSSIBLE_MOVES;
    }
}
