package com.endoman123.board;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.endoman123.pieces.Piece;
import com.endoman123.util.AlgebraicNotation;
import com.endoman123.util.Assets;

/**
 * Object to represent one tile on a board.
 *
 * @author Jared Tulayan
 */
public class Cell {
    public final int FILE, RANK;
    private Piece piece;
    private final TextureRegion BG;

    /**
     * Creates a new cell with the specified file and rank.
     * @param f the file of the cell
     * @param r the rank of the cell
     */
    public Cell(int f, int r) {
        FILE = f;
        RANK = r;

        TextureAtlas t = Assets.MANAGER.get(Assets.GameObjects.BOARD_ATLAS);

        if ((FILE + RANK) % 2 == 0)
            BG = t.findRegion("black");
        else
            BG = t.findRegion("white");
    }

    /**
     * Copy constructor
     * @param c the cell to copy
     */
    public Cell(Cell c) throws CloneNotSupportedException {
        FILE = c.FILE;
        RANK = c.RANK;

        if (c.piece != null)
            piece = c.piece.cpy();
        else
            piece = null;

        BG = c.BG;
    }

    /**
     * Gets the piece on this cell
     * @return the piece, or null if empty
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets the piece on this cell
     * @param piece the piece to place on this cell, or null if removing the piece
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * Draws the cell. This method assumes that the batch has already been started.
     *
     * @param b       the {@code Batch} to use for drawing.
     * @param xOffset the x-offset of the board
     * @param yOffset the y-offset of the board
     * @param width   the width of the cell
     * @param height  the height of the cell
     */
    public void draw(Batch b, float xOffset, float yOffset, float width, float height) {
        float x = xOffset + width * FILE;
        float y = yOffset + height * RANK;

        b.draw(BG, x, y, width, height);
    }

    public String toString() {
        return AlgebraicNotation.notatePosition(FILE, RANK);
    }
}
