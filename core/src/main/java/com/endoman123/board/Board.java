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
    private final Array<Cell> POSSIBLE_MOVES;
    private final Cell[][] BOARD;
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

        POSSIBLE_MOVES = new Array<Cell>(Cell.class);
        BOARD = new Cell[NUM_RANKS][NUM_FILES];

        // Initialize board
        int size = NUM_FILES * NUM_RANKS;
        for (int i = 0; i < size; i++) {
            int rank = i / NUM_FILES;
            int file = i % NUM_FILES;

            BOARD[rank][file] = new Cell(file, rank);
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

        return BOARD[r][f];
    }

    /**
     * Gets the cell containing the specified piece
     * @param p the piece to search for in all the cells
     * @return the cell containing p, or null if none of them have it.
     */
    public Cell getCellContaining(Piece p) {
        int size = NUM_FILES * NUM_RANKS;
        for (int i = 0; i < size; i++) {
            int rank = i / NUM_FILES;
            int file = i % NUM_FILES;
            Cell c = BOARD[rank][file];

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

        for (int i = 0; i < NUM_FILES * NUM_RANKS; i++) {
            Cell c = BOARD[i / NUM_FILES][i % NUM_FILES];

            possibleMoves.clear();
            if (c.getPiece() == null || c.getPiece().getTeam() == t)
                continue;

            possibleMoves.addAll(c.getPiece().getMoves(BOARD, c.FILE, c.RANK));

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
        for (int rank = 0; rank < NUM_RANKS; rank++) {
            for (int file = 0; file < NUM_FILES; file++) {
                Cell cell = BOARD[rank][file];
                float xPos = x + tileWidth * file;
                float yPos = y + tileHeight * rank;
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
        for (Cell[] arr : BOARD)
            for (Cell c : arr)
                c.setPiece(null);

        // Initialize pieces
        for (int i = 0; i < NUM_FILES; i++) {
            BOARD[1][i].setPiece(new Pawn(Team.WHITE));
            BOARD[6][i].setPiece(new Pawn(Team.BLACK));

            switch(i) {
                case 0:
                case 7:
                    BOARD[0][i].setPiece(new Rook(Team.WHITE));
                    BOARD[7][i].setPiece(new Rook(Team.BLACK));
                    break;
                case 1:
                case 6:
                    BOARD[0][i].setPiece(new Knight(Team.WHITE));
                    BOARD[7][i].setPiece(new Knight(Team.BLACK));
                    break;
                case 2:
                case 5:
                    BOARD[0][i].setPiece(new Bishop(Team.WHITE));
                    BOARD[7][i].setPiece(new Bishop(Team.BLACK));
                    break;
                case 3:
                    BOARD[0][i].setPiece(new Queen(Team.WHITE));
                    BOARD[7][i].setPiece(new Queen(Team.BLACK));
                    break;
                case 4:
                    BOARD[0][i].setPiece(WHITE_KING);
                    BOARD[7][i].setPiece(BLACK_KING);
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
     * @param c             the piece's starting cell
     * @param t             the current team playing
     * @return the filtered list
     */
    public Array<Cell> filterMoves(Array<Cell> possibleMoves, Cell c, Team t) {
        int i = 0;
        while (i < possibleMoves.size) {
            boolean remove = false;
            Cell m = possibleMoves.get(i);
            // Should not be allowed to take opposing king
            if (c.getPiece() != null && !c.getPiece().getTeam().equals(t) && c.getPiece() instanceof King)
                remove = true;

            // Illegal moves should (obviously) not be made
            if (isIllegal(c, m, curTeam))
                remove = true;

            if (remove)
                possibleMoves.removeValue(m, true);
            else
                i++;
        }

        return possibleMoves;
    }

    /**
     * Brute forces a move check by playing the move out and checking if it is a legal move to make
     * i.e.: it does not put or leave the king in check
     *
     * @param src the starting cell
     * @param dst the ending cell
     * @return whether or not the move to be made is legal
     */
    public boolean isIllegal(Cell src, Cell dst, Team t) {
        Array<Cell> possibleMoves = new Array<Cell>();
        Cell[][] boardCopy = new Cell[NUM_RANKS][NUM_FILES];
        Cell kingCell = getCellContaining(getKing(t));
        Cell srcCopy;
        Cell dstCopy;
        Cell kingCopy;

        // Clone board
        try {
            for (int i = 0; i < NUM_FILES * NUM_RANKS; i++) {
                int file = i % NUM_FILES;
                int rank = i / NUM_FILES;

                boardCopy[rank][file] = new Cell(BOARD[rank][file]);
            }

            srcCopy = boardCopy[src.RANK][src.FILE];
            dstCopy = boardCopy[dst.RANK][dst.FILE];
            kingCopy = boardCopy[kingCell.RANK][kingCell.FILE];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Do pseudo-move
        Piece p1 = srcCopy.getPiece();

        srcCopy.setPiece(null);
        dstCopy.setPiece(p1);

        // Check if the move leaves the king in check
        for (int i = 0; i < NUM_FILES * NUM_RANKS; i++) {
            Cell c = boardCopy[i / NUM_FILES][i % NUM_FILES];

            possibleMoves.clear();
            if (c.getPiece() == null || c.getPiece().getTeam() == t)
                continue;

            possibleMoves.addAll(c.getPiece().getMoves(boardCopy, c.FILE, c.RANK));

            if (possibleMoves.contains(kingCopy, true)) {
                System.out.println(dst + " leaves king in check!");
                return true;
            }
        }

        return false;
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
            Cell cell = BOARD[(int)mouseSelected.y][(int)mouseSelected.x];

            if (selected != null && POSSIBLE_MOVES.contains(cell, true)) {
                destination = cell;
            } else if (cell == selected || cell.getPiece() == null) {
                selected = null;
                POSSIBLE_MOVES.clear();
            } else if (cell.getPiece() != null && cell.getPiece().getTeam() == curTeam) {
                selected = cell;
                POSSIBLE_MOVES.clear();
                POSSIBLE_MOVES.addAll(selected.getPiece().getMoves(BOARD, selected.FILE, selected.RANK));
                filterMoves(POSSIBLE_MOVES, selected, curTeam);
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
