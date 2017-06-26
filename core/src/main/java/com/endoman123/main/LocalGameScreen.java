package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
import com.endoman123.board.Cell;
import com.endoman123.pieces.King;
import com.endoman123.pieces.Pawn;
import com.endoman123.pieces.Piece;
import com.endoman123.pieces.Team;
import com.endoman123.util.AlgebraicNotation;
import com.endoman123.util.MoveFilters;

/** First screen of the application. Displayed after the application is created. */
public class LocalGameScreen extends ScreenAdapter {
    private final Application APP;
    private final Board BOARD;

    private Team curTeam;
    private final Team TEAM_A, TEAM_B;
    private int curTurn = 1;

    private final Array<Cell> POSSIBLE_MOVES;
    private Cell source, destination;

    public LocalGameScreen(Team a, Team b) {
        APP = (Application) Gdx.app.getApplicationListener();
        float endSize = APP.getViewport().getWorldHeight() - 64;

        BOARD = new Board(32, 32, endSize, endSize);

        TEAM_A = a;
        TEAM_B = b;

        BOARD.reset(TEAM_A, TEAM_B);
        curTeam = TEAM_A;

        source = null;
        destination = null;

        POSSIBLE_MOVES = new Array<Cell>();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 mouseSelected = APP.getViewport().unproject(new Vector2(screenX, screenY));
                float tileWidth = BOARD.getTileWidth();
                float tileHeight = BOARD.getTileHeight();
                mouseSelected.sub(BOARD.getX(), BOARD.getY()).set(MathUtils.floor(mouseSelected.x / tileWidth), MathUtils.floor(mouseSelected.y / tileHeight));

                boolean validX = mouseSelected.x >= 0 && mouseSelected.x < BOARD.NUM_FILES;
                boolean validY = mouseSelected.y >= 0 && mouseSelected.y < BOARD.NUM_RANKS;

                if (validX && validY) {
                    Cell cell = BOARD.CELLS[(int)mouseSelected.y][(int)mouseSelected.x];

                    if (source != null && POSSIBLE_MOVES.contains(cell, true)) {
                        destination = cell;
                    } else if (cell == source || cell.getPiece() == null) {
                        source = null;
                        POSSIBLE_MOVES.clear();
                    } else if (cell.getPiece() != null && cell.getPiece().getTeam() == curTeam) {
                        source = cell;
                        POSSIBLE_MOVES.clear();
                        POSSIBLE_MOVES.addAll(source.getPiece().getMoves(BOARD.CELLS, source.FILE, source.RANK));
                        MoveFilters.filterKingCapture(POSSIBLE_MOVES);
                        MoveFilters.filterCheck(BOARD, POSSIBLE_MOVES, source);

                        if (source.getPiece() instanceof King)
                            MoveFilters.filterCastle(BOARD, POSSIBLE_MOVES, BOARD.getCellContaining(BOARD.getKing(curTeam)));
                    }
                } else {
                    source = null;
                    POSSIBLE_MOVES.clear();
                }

                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        if (source != null && destination != null) {
            if (source.getPiece() == null)
                throw new IllegalStateException("The source move is null!");

            King curKing = BOARD.getKing(curTeam), otherKing;
            Piece p1 = source.getPiece();
            Piece p2 = destination.getPiece();
            Cell src = null, dst = null;

            // Make cell copies for notating
            try {
                src = new Cell(source);
                dst = new Cell(destination);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            // Castle/En Passant check
            boolean castle = p1 instanceof King && Math.abs(destination.FILE - source.FILE) == 2,
                    enPassant = p1 instanceof Pawn && destination.FILE != source.FILE && p2 == null;

            // Clear the move history of all the pieces of the current team
            BOARD.clearMoveHistory(curTeam);

            // Do move
            BOARD.performMove(source, destination);

            // Flip the turns
            flipTurns();

            // Check if cur king is in check
            otherKing = BOARD.getKing(curTeam);
            BOARD.updateKing(curTeam);

            // Notate
            if (curTeam == TEAM_B)
                System.out.print(curTurn + ". ");

            System.out.print(AlgebraicNotation.notateMove(src, dst, otherKing) + " ");

            if (curTeam == TEAM_A) {
                System.out.println();
                curTurn++;
            }

            if (otherKing.isInCheck() && !otherKing.canMove()) { // Checkmate
                System.out.println(otherKing.getTeam());
                if (curTeam == TEAM_A) // Team B won
                    System.out.println(AlgebraicNotation.Constants.WIN_B);
                else // Team A won
                    System.out.println("\n" + AlgebraicNotation.Constants.WIN_A);

            }

            source = null;
            destination = null;
            POSSIBLE_MOVES.clear();
        }

        BOARD.drawBoard(APP.getBatch());
        BOARD.drawHighlights(APP.getBatch(), POSSIBLE_MOVES, source);
        BOARD.drawPieces(APP.getBatch());
    }

    private void flipTurns() {
        if (curTeam == TEAM_A)
            curTeam = TEAM_B;
        else
            curTeam = TEAM_A;
    }
}