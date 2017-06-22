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
public class Board extends InputAdapter {
    private final Application APP;
    private final TextureRegion SELECT_TILE, MOVE_TILE, ATTACK_TILE;
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

    public void update(float delta) {
        // Do a move when the chance arises
        if (selected != null && destination != null) {
            Piece p1 = selected.getPiece();
            Piece p2 = destination.getPiece();

            // Move piece
            selected.setPiece(null);
            destination.setPiece(p1);

            // Notate
            System.out.print(curTurn + ". " + p1);

            if (p2 == null)
                System.out.print(selected + AlgebraicNotation.MOVE + p1);
            else {
                if (p1.toString().isEmpty())
                    System.out.print(AlgebraicNotation.convertToBase26(selected.FILE + 1));
                System.out.print(AlgebraicNotation.CAPTURE);
            }

            System.out.println(destination);

            // Flip turns
            if (curTeam == Team.WHITE)
                curTeam = Team.BLACK;
            else
                curTeam = Team.WHITE;

            // Set up for next phase
            curTurn++;
            selected = null;
            destination = null;
            POSSIBLE_MOVES.clear();
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

            cell.draw(b, x, y, tileWidth, tileHeight);

            if (cell == selected)
                b.draw(SELECT_TILE, xPos, yPos, tileWidth, tileHeight);
            else if (POSSIBLE_MOVES.contains(cell, true)) {
                if (cell.getPiece() != null)
                    b.draw(ATTACK_TILE, xPos, yPos, tileWidth, tileHeight);
                else
                    b.draw(MOVE_TILE, xPos, yPos, tileWidth, tileHeight);
            }

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
            if (c.getPiece() == null)
                continue;

            if (!c.getPiece().getTeam().equals(t)) {

            }
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
