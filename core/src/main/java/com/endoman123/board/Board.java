package com.endoman123.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.endoman123.main.Application;
import com.endoman123.pieces.*;
import com.endoman123.util.AlgebraicNotation;
import com.endoman123.util.Assets;

/**
 * Container of {@link Cell}s with methods to update the state of the board.
 *
 * @author Jared Tulayan
 */
public class Board extends InputAdapter{
    private final Application APP;
    private final TextureRegion SELECT_TILE, MOVE_TILE, ATTACK_TILE, CHECK_TILE;
    private final Array<Cell> CELLS, POSSIBLE_MOVES;
    private Cell selected, destination;
    private float x, y, width, height;
    private final float PIECE_SCALE = 0.15f;
    public final int NUM_FILES, NUM_RANKS;
    private final King WHITE_KING, BLACK_KING;
    private int curTurn = 1;
    private Team curTeam;

    public Board(float x, float y, float w, float h, int f, int r) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        NUM_FILES = f;
        NUM_RANKS = r;

        CELLS = new Array<Cell>(Cell.class);
        POSSIBLE_MOVES = new Array<Cell>(Cell.class);

        // Initialize board
        int size = NUM_FILES * NUM_RANKS;
        for (int i = 0; i < size; i++) {
            int rank = i / NUM_FILES;
            int file = i % NUM_FILES;

            CELLS.add(new Cell(file, rank));
        }

        APP = (Application) Gdx.app.getApplicationListener();

        TextureAtlas a = Assets.MANAGER.get(Assets.GameObjects.BOARD_ATLAS);

        SELECT_TILE = a.findRegion("selected");
        MOVE_TILE = a.findRegion("move");
        ATTACK_TILE = a.findRegion("attack");
        CHECK_TILE = a.findRegion("check");

        WHITE_KING = new King(Team.WHITE);
        BLACK_KING = new King(Team.BLACK);

        reset();
    }

    public Cell getCellAt(int f, int r) {
        if (f >= NUM_FILES || r >= NUM_RANKS || f < 0 || r < 0) // If out of range
            return null;

        int index = r * NUM_FILES + f;

        return CELLS.get(index);
    }

    /**
     * Gets the cell containing the specified piece
     * @param p the piece to search for in all the cells
     * @return the cell containing p, or null if none of them have it.
     */
    public Cell getCellContaining(Piece p) {
        for (Cell c : CELLS) {
            if (c.getPiece() == p)
                return c;
        }
        return null;
    }

    public void update(float delta) {
        // Do a move when the chance arises
        if (selected != null && destination != null) {
            StringBuilder notation = new StringBuilder();
            Piece p1 = selected.getPiece();
            Piece p2 = destination.getPiece();

            // Move piece
            selected.setPiece(null);
            destination.setPiece(p1);

            // Flip turns
            if (curTeam == Team.WHITE)
                curTeam = Team.BLACK;
            else
                curTeam = Team.WHITE;

            // Check if cur king is in check
            King curKing;
            updateCheck(curTeam);

            curKing = getKing(curTeam);

            // Notate
            notation.append(curTurn);
            notation.append(". ");
            notation.append(p1);

            if (p2 == null) {
                notation.append(selected);
                notation.append(AlgebraicNotation.MOVE);
                notation.append(p1);
            } else {
                if (p1.toString().isEmpty())
                    notation.append(AlgebraicNotation.convertToBase26(selected.FILE + 1));
                notation.append(AlgebraicNotation.CAPTURE);
            }
            notation.append(destination);
            if (curKing.getCheck())
                notation.append(AlgebraicNotation.CHECK);

            System.out.println(notation);

            // Set up for next phase
            curTurn++;
            selected = null;
            destination = null;
            POSSIBLE_MOVES.clear();
        }
    }

    /**
     * Checks if the specified team's king is in check, then updates the king if it is.
     *
     * @param t the team the king belongs to
     */
    public void updateCheck(Team t) {
        King curKing = getKing(t);
        Cell curCell = getCellContaining(curKing);
        Array<Cell> possibleMoves = new Array<Cell>();
        curKing.setCheck(false);

        for (Cell c : CELLS) {
            possibleMoves.clear();
            if (c.getPiece() == null || c.getPiece().getTeam() == t)
                continue;

            possibleMoves.addAll(c.getPiece().getMoves(this, c.FILE, c.RANK));

            if (possibleMoves.contains(curCell, true)) {
                curKing.setCheck(true);
            }
        }
    }

    public void draw(Batch b) {
        Camera camera = APP.getViewport().getCamera();
        float tileWidth = width / NUM_FILES;
        float tileHeight = height / NUM_RANKS;

        camera.update();
        b.setProjectionMatrix(camera.combined);
        b.begin();
        for (int i = 0; i < CELLS.size; i++) {
            Cell cell = CELLS.get(i);
            float xPos = x + tileWidth * (i % NUM_FILES);
            float yPos = y + tileHeight * (i / NUM_FILES);
            boolean containsPiece = cell.getPiece() != null;
            boolean inCheck = containsPiece && cell.getPiece().getTeam() == curTeam && cell.getPiece() instanceof King && ((King) cell.getPiece()).getCheck();

            cell.draw(b, x, y, tileWidth, tileHeight);

            if (cell == selected)
                b.draw(SELECT_TILE, xPos, yPos, tileWidth, tileHeight);
            else if (POSSIBLE_MOVES.contains(cell, true)) {
                if (cell.getPiece() != null)
                    b.draw(ATTACK_TILE, xPos, yPos, tileWidth, tileHeight);
                else
                    b.draw(MOVE_TILE, xPos, yPos, tileWidth, tileHeight);
            } else if (inCheck)
                b.draw(CHECK_TILE, xPos, yPos, tileWidth, tileHeight);

            if (cell.getPiece() != null) {
                Sprite s = cell.getPiece().getSprite();
                float halfWidth = tileWidth / 2f;
                float halfHeight = tileHeight / 2f;

                s.setScale(PIECE_SCALE);
                s.setPosition(xPos + halfWidth - s.getOriginX(), yPos + halfHeight - s.getOriginY());
                s.draw(b);
            }
        }
        b.end();
    }

    public King getKing(Team t) {
        if (t == Team.WHITE)
            return WHITE_KING;
        else
            return BLACK_KING;
    }

    public void reset() {
        // Clear the table
        for (Cell c : CELLS)
            c.setPiece(null);

        // Initialize pieces
        for (int i = 0; i < NUM_FILES; i++) {
            CELLS.get(8 + i).setPiece(new Pawn(Team.WHITE));
            CELLS.get(48 + i).setPiece(new Pawn(Team.BLACK));

            switch(i) {
                case 0:
                case 7:
                    CELLS.get(i).setPiece(new Rook(Team.WHITE));
                    CELLS.get(56 + i).setPiece(new Rook(Team.BLACK));
                    break;
                case 1:
                case 6:
                    CELLS.get(i).setPiece(new Knight(Team.WHITE));
                    CELLS.get(56 + i).setPiece(new Knight(Team.BLACK));
                    break;
                case 2:
                case 5:
                    CELLS.get(i).setPiece(new Bishop(Team.WHITE));
                    CELLS.get(56 + i).setPiece(new Bishop(Team.BLACK));
                    break;
                case 3:
                    CELLS.get(i).setPiece(new Queen(Team.WHITE));
                    CELLS.get(56 + i).setPiece(new Queen(Team.BLACK));
                    break;
                case 4:
                    CELLS.get(i).setPiece(WHITE_KING);
                    CELLS.get(56 + i).setPiece(BLACK_KING);
                    break;
            }
        }

        POSSIBLE_MOVES.clear();
        selected = null;
        destination = null;

        curTeam = Team.WHITE;
    }

    /**
     * Filters the current moveset for unplayable moves due to the current state of the game.
     * @param possibleMoves the list of cells containing the current moveset
     * @param t             the current team playing
     * @return the filtered list
     */
    public Array<Cell> filterMoves(Array<Cell> possibleMoves, Team t) {
        for (Cell c : possibleMoves) {
            // Should not be allowed to take opposing king
            if (c.getPiece() != null && !c.getPiece().getTeam().equals(t) && c.getPiece() instanceof King)
                possibleMoves.removeValue(c, true);
        }

        return possibleMoves;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 mouseSelected = APP.getViewport().unproject(new Vector2(screenX, screenY));
        float tileWidth = width / NUM_FILES;
        float tileHeight = height / NUM_RANKS;
        mouseSelected.sub(x, y).set(MathUtils.floor(mouseSelected.x / tileWidth), MathUtils.floor(mouseSelected.y / tileHeight));

        boolean validX = mouseSelected.x >= 0 && mouseSelected.x < NUM_FILES;
        boolean validY = mouseSelected.y >= 0 && mouseSelected.y < NUM_RANKS;

        if (validX && validY) {
            Cell cell = getCellAt((int)mouseSelected.x, (int)mouseSelected.y);

            if (selected != null && POSSIBLE_MOVES.contains(cell, true)) {
                destination = cell;
            } else if (cell == selected || cell.getPiece() == null) {
                selected = null;
                POSSIBLE_MOVES.clear();
            } else if (cell.getPiece() != null && cell.getPiece().getTeam().equals(curTeam)) {
                selected = cell;
                POSSIBLE_MOVES.clear();
                POSSIBLE_MOVES.addAll(selected.getPiece().getMoves(this, selected.FILE, selected.RANK));
                filterMoves(POSSIBLE_MOVES, curTeam);
            }
        } else {
            selected = null;
            POSSIBLE_MOVES.clear();
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return super.mouseMoved(screenX, screenY);
    }
}
