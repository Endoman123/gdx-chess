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
import com.badlogic.gdx.scenes.scene2d.Stage;
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
    private final Stage UI_CANVAS;

    private float x, y, width, height;
    private final float PIECE_SCALE = 0.5f;
    public final int NUM_FILES, NUM_RANKS;
    private final TextureRegion SELECT_TILE, MOVE_TILE, ATTACK_TILE, CHECK_TILE;

    private final Array<Cell> POSSIBLE_MOVES;
    private final Cell[][] BOARD;
    private Team teamA, teamB;
    private King kingA, kingB;
    private Cell selected, destination;

    private int curTurn = 1;
    private Team curTeam;
    private final StringBuilder LOG_BUILDER;

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
        BOARD = new Cell[NUM_RANKS][NUM_FILES];
        LOG_BUILDER = new StringBuilder();

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

        reset(Team.LIGHT_GRAY, Team.DARK_GRAY);

        UI_CANVAS = new Stage(APP.getViewport(), APP.getBatch());
    }

    /**
     * Method to initialize UI
     */
/*    private void initializeUI() {
        final Skin SKIN = ;
        final Table TABLE = new Table();
        final TextArea PANE = new TextArea("Test", new Skin());

        TABLE.setFillParent(true);
        TABLE.add().expand().fill().uniform();
        TABLE.add(PANE).expand().fill().pad(16).uniform();

        UI_CANVAS.addActor(TABLE);
        UI_CANVAS.setDebugAll(true);
    }*/

    /**
     * Updates the state of the game
     *
     * @param delta the time passed in between updates.
     */
    public void update(float delta) {
        // Update canvas
        UI_CANVAS.act(delta);

        // Do a move when the chance arises
        if (selected != null && destination != null) {
            Piece p1 = selected.getPiece();
            Piece p2 = destination.getPiece();

            clearHistory(curTeam);

            // Booleans for castling and "passing"
            boolean castle = p1 instanceof King && Math.abs(destination.FILE - selected.FILE) == 2,
                    enPassant = p1 instanceof Pawn && destination.FILE != selected.FILE && p2 == null;

            // Move piece
            if (castle) {
                int rank = selected.RANK;
                int rookDirection = (int)Math.signum(destination.FILE - selected.FILE);
                Piece rook;
                Cell rookTile;

                if (destination.FILE - selected.FILE == -2) // queenside castle
                    rookTile = BOARD[rank][0];
                else
                    rookTile = BOARD[rank][7];

                rook = rookTile.getPiece();
                rookTile.setPiece(null);
                BOARD[rank][destination.FILE - rookDirection].setPiece(rook);
                rook.toggleMoved();
                castle = true;
            } else if (enPassant) {
                int dir = ((Pawn)p1).DIRECTION;

                BOARD[destination.RANK - dir][destination.FILE].setPiece(null);
            }

            selected.setPiece(null);
            destination.setPiece(p1);
            p1.toggleMoved();
            p1.setLastMove(destination);

            // Flip turns
            if (curTeam == teamA)
                curTeam = teamB;
            else
                curTeam = teamA;

            // Check if cur king is in check
            King curKing = getKing(curTeam);
            updateCheck(curTeam);

            boolean checkmate = false;

            // Notate
            if (curTeam == teamB)
                LOG_BUILDER.append(curTurn).append(". ");

            if (castle) {
                if (destination.FILE - selected.FILE == -2) // Queenside
                    LOG_BUILDER.append(AlgebraicNotation.CASTLE_QUEENSIDE);
                else
                    LOG_BUILDER.append(AlgebraicNotation.CASTLE_KINGSIDE);
            } else {
                LOG_BUILDER.append(p1);
                if (p2 != null) {
                    if (p1 instanceof Pawn)
                        LOG_BUILDER.append(AlgebraicNotation.convertToBase26(selected.FILE + 1));

                    LOG_BUILDER.append(AlgebraicNotation.CAPTURE);
                }

                LOG_BUILDER.append(destination);

                if (enPassant)
                    LOG_BUILDER.append(AlgebraicNotation.EN_PASSANT);
            }

            if (curKing.isInCheck()) {
                checkmate = isCheckmate(curTeam);
                if (checkmate)
                    LOG_BUILDER.append(AlgebraicNotation.CHECKMATE);
                else
                    LOG_BUILDER.append(AlgebraicNotation.CHECK);
            }

            if (curTeam == teamA || checkmate) {
                System.out.println(LOG_BUILDER);
                if (checkmate) {
                    if (curTeam == teamA) // Team B won
                        System.out.println(AlgebraicNotation.WIN_B);
                    else // Team A won
                        System.out.println(AlgebraicNotation.WIN_A);
                }
                LOG_BUILDER.delete(0, LOG_BUILDER.length());
                curTurn++;
            } else
                LOG_BUILDER.append(" ");

            // Set up for next phase
            selected = null;
            destination = null;
            POSSIBLE_MOVES.clear();
        }
    }

    /**
     * Draws the board and everything on it, including highlights on tiles
     * @param b the {@code Batch} to use for drawing
     */
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
                boolean inCheck = containsPiece && cell.getPiece().getTeam() == curTeam && cell.getPiece() instanceof King && ((King) cell.getPiece()).isInCheck();

                cell.draw(b, x, y, tileWidth, tileHeight);

                if (cell == selected)
                    b.draw(SELECT_TILE, xPos, yPos, tileWidth, tileHeight);
                else if (POSSIBLE_MOVES.contains(cell, true)) {
                    if (cell.getPiece() != null)
                        b.draw(ATTACK_TILE, xPos, yPos, tileWidth, tileHeight);
                    else if (selected.getPiece() instanceof Pawn && selected.FILE != cell.FILE) { // Case specifically to make en passant same as an attack
                        int dir = ((Pawn)selected.getPiece()).DIRECTION;
                        Piece passant = BOARD[cell.RANK - dir][cell.FILE].getPiece();
                        if (passant instanceof Pawn && passant.getTeam() != curTeam)
                            b.draw(ATTACK_TILE, xPos, yPos, tileWidth, tileHeight);
                    } else
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
        UI_CANVAS.draw();
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

    /**
     * Gets the king for the specified team
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

    /**
     * Clears the last immediate move from all the pieces of the specified team
     *
     * @param t the team whose history should be cleared.
     */
    public void clearHistory(Team t) {
        for (Cell[] rank : BOARD) {
            for(Cell c : rank) {
                if (c.getPiece() != null && c.getPiece().getTeam() == t)
                    c.getPiece().setLastMove(null);
            }
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

    /**
     * Gets whether or not the specified is in checkmate.
     *
     * @param t the team to check checkmate for
     * @return if the current team playing is in checkmate
     */
    public boolean isCheckmate(Team t) {
        King curKing = getKing(t);
        Array<Cell> moveCache = new Array<Cell>();

        if (!curKing.isInCheck()) // If the king is not in check, it's hardly checkmate.
            return false;

        for (Cell[] rank : BOARD) {
            for (Cell cell : rank) {
                if (cell.getPiece() != null && cell.getPiece().getTeam() == curTeam) {
                    moveCache.clear();
                    moveCache.addAll(cell.getPiece().getMoves(BOARD, cell.FILE, cell.RANK));
                    filterMoves(moveCache, cell, t);

                    if (moveCache.size > 0)
                        return false;
                }
            }
        }

        return true;
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
            if (!c.getPiece().getTeam().equals(t) && c.getPiece() instanceof King)
                remove = true;

            // Illegal moves should (obviously) not be made
            if (checkIllegalMove(c, m, curTeam))
                remove = true;

            // Illegal castling needs to be filtered
            boolean isCastle = c.getPiece() instanceof King && m.RANK == c.RANK && Math.abs(m.FILE - c.FILE) == 2;
            if (isCastle && checkIllegalCastle(c, m)) {
                System.out.println(m + " is an illegal castle!");
                remove = true;
            }

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
    public boolean checkIllegalMove(Cell src, Cell dst, Team t) {
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

            if (src.getPiece() instanceof King) // If the king is actually who is moving
                kingCopy = dstCopy;
            else
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

            if (possibleMoves.contains(kingCopy, true))
                return true;
        }

        return false;
    }

    /**
     * Checks if the current move is an illegal castle.
     *
     * @param src the {@code Cell} that the king is standing on
     * @param dst the {@code Cell} that the king is castling to
     * @return if the cell in between the King and the castle destination is a legal move on its own.
     */
    public boolean checkIllegalCastle(Cell src, Cell dst) {
        int direction = (int)Math.signum(dst.FILE - src.FILE);
        Team team = src.getPiece().getTeam();

        return checkIllegalMove(src, BOARD[src.RANK][src.FILE + direction], team);
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
        for (Cell[] arr : BOARD)
            for (Cell c : arr)
                c.setPiece(null);

        // Initialize pieces
        for (int i = 0; i < NUM_FILES; i++) {
            BOARD[1][i].setPiece(new Pawn(teamA, 1));
            BOARD[6][i].setPiece(new Pawn(teamB, -1));

            switch(i) {
                case 0:
                case 7:
                    BOARD[0][i].setPiece(new Rook(teamA));
                    BOARD[7][i].setPiece(new Rook(teamB));
                    break;
                case 1:
                case 6:
                    BOARD[0][i].setPiece(new Knight(teamA));
                    BOARD[7][i].setPiece(new Knight(teamB));
                    break;
                case 2:
                case 5:
                    BOARD[0][i].setPiece(new Bishop(teamA));
                    BOARD[7][i].setPiece(new Bishop(teamB));
                    break;
                case 3:
                    BOARD[0][i].setPiece(new Queen(teamA));
                    BOARD[7][i].setPiece(new Queen(teamB));
                    break;
                case 4:
                    BOARD[0][i].setPiece(kingA);
                    BOARD[7][i].setPiece(kingB);
                    break;
            }
        }

        POSSIBLE_MOVES.clear();
        selected = null;
        destination = null;

        curTeam = teamA;
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
