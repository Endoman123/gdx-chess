package com.endoman123.pieces;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Cell;

/**
 * Object to represent a board piece.
 *
 * @author Jared Tulayan
 */
public abstract class Piece implements Cloneable {
    private Team team; // Team is based on index, might change to enum
    private Sprite sprite;
    private final Character ID;
    protected final Array<Cell> POSSIBLE_MOVES;

    public Piece(Character id, Team t) {
        team = t;
        ID = id;

        POSSIBLE_MOVES = new Array<Cell>();
    }

    public Team getTeam() {
        return team;
    }

    public Sprite getSprite() {
        return sprite;
    }

    /**
     * Sets the sprite for this object and re-centers it.
     *
     * @param s the sprite to change the current one to
     */
    public void setSprite(Sprite s) {
        sprite = s;
        sprite.setOriginCenter();
    }

    /**
     * Updates the list of moves that this piece can make.
     *
     * @param board the list of cells that make up the board
     * @param file  the piece's current file
     * @param rank  the piece's current rank
     * @return the list of possible moves
     */
    public abstract Array<Cell> getMoves(Cell[][] board, int file, int rank);

    public String toString() {
        return ID.toString().trim();
    }

    public Piece cpy() throws CloneNotSupportedException {
        return (Piece) clone();
    }
}
