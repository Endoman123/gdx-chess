package com.endoman123.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.endoman123.main.Application;
import com.endoman123.pieces.*;
import com.endoman123.util.Assets;
import com.endoman123.util.MoveFilters;

/**
 * Container of {@link Cell}s with methods to update the state of the board.
 *
 * @author Jared Tulayan
 */
public class Board {
    private final Application APP;

    private float x, y, width, height;
    private final float PIECE_SCALE = 0.5f;
    public final int NUM_FILES, NUM_RANKS;
    private final TextureRegion SELECT_TILE, MOVE_TILE, ATTACK_TILE, CHECK_TILE;

    private final Array<Cell> POSSIBLE_MOVES;
    public final Cell[][] CELLS;
    private Team teamA, teamB;
    private King kingA, kingB;

    /**
     * Constructs a new {@link Board} at the specified location with the specified size and number of rows and columns
     * @param x the x-coordinate for the bottom-left corner
     * @param y the y-coordinate for the bottom-left corner
     * @param w the width of the board
     * @param h the height of the board
     * @param f the number of files (columns)
     * @param r the number of ranks (rows)
     */
    public Board(float x, float y, float w, float h, int f, int r) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        NUM_FILES = f;
        NUM_RANKS = r;

        POSSIBLE_MOVES = new Array<Cell>(Cell.class);
        CELLS = new Cell[NUM_RANKS][NUM_FILES];

        // Initialize board
        int size = NUM_FILES * NUM_RANKS;
        for (int i = 0; i < size; i++) {
            int rank = i / NUM_FILES;
            int file = i % NUM_FILES;

            CELLS[rank][file] = new Cell(file, rank);
        }

        APP = (Application) Gdx.app.getApplicationListener();

        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.BOARD_ATLAS);

        SELECT_TILE = a.findRegion("selected");
        MOVE_TILE = a.findRegion("move");
        ATTACK_TILE = a.findRegion("attack");
        CHECK_TILE = a.findRegion("check");
    }

    /**
     * Performs the specified move by moving the piece in the source cell to the destination cell.
     *
     * @param src the beginning position
     * @param dst the destination
     */
    public void performMove(Cell src, Cell dst) {
        Piece p1 = src.getPiece();
        Piece p2 = dst.getPiece();

        // Castle/En Passant check
        boolean castle = p1 instanceof King && Math.abs(dst.FILE - src.FILE) == 2,
                enPassant = p1 instanceof Pawn && dst.FILE != src.FILE && p2 == null;

        // Move piece
        if (castle) {
            int rank = src.RANK;
            int direction = dst.FILE - src.FILE;
            Piece rook;
            Cell rookTile;

            if (direction == -2) // queenside castle
                rookTile = CELLS[rank][0];
            else
                rookTile = CELLS[rank][7];

            rook = rookTile.getPiece();
            rookTile.setPiece(null);
            CELLS[rank][dst.FILE - (int)Math.signum(direction)].setPiece(rook);
            rook.toggleMoved();
        } else if (enPassant) {
            int dir = ((Pawn)p1).DIRECTION;

            CELLS[dst.RANK - dir][dst.FILE].setPiece(null);
        }

        src.setPiece(null);
        dst.setPiece(p1);
        p1.toggleMoved();
        p1.setLastMove(dst);
    }

    /**
     * Draws the board cells
     *
     * @param b the {@code Batch} to use for drawing
     */
    public void drawBoard(Batch b) {
        Camera camera = APP.getViewport().getCamera();
        float tileWidth = width / NUM_FILES;
        float tileHeight = height / NUM_RANKS;

        camera.update();
        b.setProjectionMatrix(camera.combined);
        b.begin();
        for (int rank = 0; rank < NUM_RANKS; rank++) {
            for (int file = 0; file < NUM_FILES; file++) {
                Cell cell = CELLS[rank][file];

                cell.draw(b, x, y, tileWidth, tileHeight);
            }
        }
        b.end();
    }

    /**
     * Draws the highlights on the board, given the possible moves that can be made
     * @param b        the batch to use for moving
     * @param moves    the possible moves list
     * @param selected the selected tile
     */
    public void drawHighlights(Batch b, Array<Cell> moves, Cell selected) {
        Camera camera = APP.getViewport().getCamera();

        camera.update();
        b.setProjectionMatrix(camera.combined);
        b.begin();
        for (Cell[] rank : CELLS) {
            for (Cell cell : rank) {
                float xPos = x + getTileWidth() * cell.FILE;
                float yPos = y + getTileHeight() * cell.RANK;
                boolean containsPiece = cell.getPiece() != null;
                boolean checkCell = cell.getPiece() instanceof King && ( (King)cell.getPiece()).isInCheck();

                if (cell == selected)
                    b.draw(SELECT_TILE, xPos, yPos, getTileWidth(), getTileHeight());
                else if (moves.contains(cell, true)) {
                    boolean passantAttack = selected.getPiece() instanceof Pawn && selected.FILE != cell.FILE;
                    if (cell.getPiece() != null || passantAttack)
                        b.draw(ATTACK_TILE, xPos, yPos, getTileWidth(), getTileHeight());
                    else
                        b.draw(MOVE_TILE, xPos, yPos, getTileWidth(), getTileHeight());
                } else if (checkCell)
                    b.draw(CHECK_TILE, xPos, yPos, getTileWidth(), getTileHeight());
            }
        }
        b.end();
    }

    public void drawPieces(Batch b) {
        Camera camera = APP.getViewport().getCamera();
        float tileWidth = width / NUM_FILES;
        float tileHeight = height / NUM_RANKS;

        camera.update();
        b.setProjectionMatrix(camera.combined);
        b.begin();
        for (int rank = 0; rank < NUM_RANKS; rank++) {
            for (int file = 0; file < NUM_FILES; file++) {
                Cell cell = CELLS[rank][file];
                float xPos = x + getTileWidth() * file;
                float yPos = y + getTileHeight() * rank;

                if (cell.getPiece() != null) {
                    Sprite s = cell.getPiece().getSprite();
                    float halfWidth = tileWidth / 2f;
                    float halfHeight = tileHeight / 2f;

                    s.setScale(PIECE_SCALE);
                    s.setPosition(xPos + halfWidth - s.getOriginX(), yPos + halfHeight - s.getOriginY());
                    s.draw(b);
                }
            }
        }
        b.end();
    }

    /**
     * Clears the last immediate move from all the pieces of the specified team
     *
     * @param t the team whose history should be cleared.
     */
    public void clearMoveHistory(Team t) {
        for (Cell[] rank : CELLS) {
            for(Cell c : rank) {
                if (c.getPiece() != null && c.getPiece().getTeam() == t)
                    c.getPiece().setLastMove(null);
            }
        }
    }

    /**
     * Updates the specified team's king's status (checked, can move, etc.)
     *
     * @param team the {@code team} whose king's states to update
     */
    public void updateKing(Team team) {
        Cell kingCell = getCellContaining(getKing(team));
        King king = (King)kingCell.getPiece();
        Array<Cell> cellCache = new Array<Cell>();

        king.setCheck(false);
        king.setCanMove(false);

        for (Cell[] rank : CELLS) {
            for (Cell cell : rank) {
                if (cell.getPiece() != null && cell.getPiece().getTeam() != team) {
                    cellCache.clear();

                    cellCache.addAll(cell.getPiece().getMoves(CELLS, cell.FILE, cell.RANK));

                    if (cellCache.contains(kingCell, true))
                        king.setCheck(true);
                }

                if (!king.canMove() && cell.getPiece() != null && cell.getPiece().getTeam() == team) {
                    cellCache.clear();
                    cellCache.addAll(cell.getPiece().getMoves(CELLS, cell.FILE, cell.RANK));
                    MoveFilters.filterCheck(this, cellCache, kingCell);

                    if (cellCache.size > 0) {
                        System.out.println("" + cell.getPiece() + cell);
                        king.setCanMove(true);
                    }
                }
            }
        }
    }

    /**
     * Resets the game
     */
    public void reset(Team a, Team b) {
        if (a == b)
            throw new IllegalArgumentException("a cannot equal b");

        teamA = a;
        teamB = b;

        kingA = new King(teamA);
        kingB = new King(teamB);

        // Clear the table
        for (Cell[] arr : CELLS)
            for (Cell c : arr)
                c.setPiece(null);

        // Initialize pieces
        for (int i = 0; i < NUM_FILES; i++) {
            CELLS[1][i].setPiece(new Pawn(teamA, 1));
            CELLS[6][i].setPiece(new Pawn(teamB, -1));

            switch(i) {
                case 0:
                case 7:
                    CELLS[0][i].setPiece(new Rook(teamA));
                    CELLS[7][i].setPiece(new Rook(teamB));
                    break;
                case 1:
                case 6:
                    CELLS[0][i].setPiece(new Knight(teamA));
                    CELLS[7][i].setPiece(new Knight(teamB));
                    break;
                case 2:
                case 5:
                    CELLS[0][i].setPiece(new Bishop(teamA));
                    CELLS[7][i].setPiece(new Bishop(teamB));
                    break;
                case 3:
                    CELLS[0][i].setPiece(new Queen(teamA));
                    CELLS[7][i].setPiece(new Queen(teamB));
                    break;
                case 4:
                    CELLS[0][i].setPiece(kingA);
                    CELLS[7][i].setPiece(kingB);
                    break;
            }
        }
    }

    // region Getters and Setters
    /**
     * Gets the width of a single cell in the board.
     * @return {@code width / NUM_FILES}
     */
    public float getTileWidth() {
        return width / NUM_FILES;
    }

    /**
     * Gets the height of a single cell in the board.
     * @return {@code height / NUM_RANKS}
     */
    public float getTileHeight() {
        return width / NUM_FILES;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Gets the cell containing the specified piece
     *
     * @param p the piece to search for in all the cells
     * @return the cell containing p, or null if none of them have it.
     */
    public Cell getCellContaining(Piece p) {
        int size = NUM_FILES * NUM_RANKS;
        for (int i = 0; i < size; i++) {
            int rank = i / NUM_FILES;
            int file = i % NUM_FILES;
            Cell c = CELLS[rank][file];

            if (c.getPiece() == p)
                return c;
        }

        return null;
    }

    /**
     * Gets the king for the specified team
     *
     * @param t the team to get the king for
     * @return the white king or black king, depending on the team
     */
    public King getKing(Team t) {
        if (t != teamA && t != teamB)
            throw new IllegalArgumentException("t must be a playing team color");

        if (t == teamA)
            return kingA;
        else
            return kingB;
    }

    // endregion
}
