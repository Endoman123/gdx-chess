package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.endoman123.board.Board;
import com.endoman123.board.Cell;
import com.endoman123.pieces.*;
import com.endoman123.util.AlgebraicNotation;
import com.endoman123.util.MoveFilters;
import com.esotericsoftware.kryonet.*;

/** First screen of the application. Displayed after the application is created. */
public class NetworkGameScreen extends ScreenAdapter {
    private final Application APP;
    private final Board BOARD;

    private Connection connection;
    private EndPoint endPoint;
    private boolean hasPlayer = false, gameStart = false, isServer = false;

    private final Team MY_TEAM;
    private Team otherTeam;
    private int curTurn = 1;
    private boolean myTurn = false;

    private final Array<Cell> POSSIBLE_MOVES;
    private Cell source, destination;

    private NetworkGameScreen(Team team) {
        APP = (Application) Gdx.app.getApplicationListener();
        POSSIBLE_MOVES = new Array<Cell>();
        Gdx.input.setInputProcessor(new BoardInputAdapter());

        // Set team
        MY_TEAM = team;

        // Init board
        float halfWidth = APP.getViewport().getWorldWidth() / 2f;
        BOARD = new Board(32, 32, halfWidth - 64, halfWidth - 64, 8, 8);

        source = null;
        destination = null;
    }

    /**
     * Server constructor
     * @param team team A
     * @param server server
     */
    public NetworkGameScreen(Team team, Server server) {
        this(team);

        endPoint = server;
        isServer = true;

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                if (!hasPlayer) {
                    NetworkGameScreen.this.connection = connection;
                    hasPlayer = true;
                } else {
                    connection.close();
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (connection == NetworkGameScreen.this.connection) {
                    if (object instanceof Team) {
                        otherTeam = (Team) object;
                        BOARD.reset(MY_TEAM, otherTeam);
                        connection.sendTCP(MY_TEAM);
                        gameStart = true;
                        myTurn = true;
                    }

                    if (object instanceof MovePacket) {
                        MovePacket packet = (MovePacket) object;
                        Cell pSrc = BOARD.CELLS[packet.fromRank][packet.fromFile];
                        Cell pDst = BOARD.CELLS[packet.toRank][packet.toFile];
                        Cell src, dst;
                        King king = BOARD.getKing(MY_TEAM);

                        // Clone cells for notating
                        try {
                            src = new Cell(pSrc);
                            dst = new Cell(pDst);
                        } catch (CloneNotSupportedException e) {
                            src = null;
                            dst = null;

                            e.printStackTrace();
                        }

                        move(pSrc, pDst);
                        BOARD.updateKing(MY_TEAM);

                        System.out.println(AlgebraicNotation.notateMove(src, dst, king));

                        if (king.isInCheck() && !king.canMove()) // Checkmate
                            System.out.println("\n" + AlgebraicNotation.Constants.WIN_B);
                        else
                            myTurn = true;
                    }

                }
            }
        });
    }

    /**
     * Client constructor
     * @param team team B
     * @param client client
     */
    public NetworkGameScreen(Team team, Client client) {
        this(team);

        endPoint = client;
        isServer = false;
        connection = client;

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Team) {
                    otherTeam = (Team) object;
                    BOARD.reset(otherTeam, MY_TEAM);
                    gameStart = true;
                    myTurn = false;
                }

                if (object instanceof MovePacket) {
                    MovePacket packet = (MovePacket) object;
                    Cell pSrc = BOARD.CELLS[packet.fromRank][packet.fromFile];
                    Cell pDst = BOARD.CELLS[packet.toRank][packet.toFile];
                    Cell src, dst;
                    King king = BOARD.getKing(MY_TEAM);

                    // Clone cells for notating
                    try {
                        src = new Cell(pSrc);
                        dst = new Cell(pDst);
                    } catch (CloneNotSupportedException e) {
                        src = null;
                        dst = null;

                        e.printStackTrace();
                    }

                    move(pSrc, pDst);
                    BOARD.updateKing(MY_TEAM);

                    System.out.print(curTurn + ". ");
                    System.out.print(AlgebraicNotation.notateMove(src, dst, king) + " ");

                    if (king.isInCheck() && !king.canMove()) // Other team checkmate
                        System.out.println("\n" + AlgebraicNotation.Constants.WIN_A);
                    else
                        myTurn = true;
                }
            }
        });
        client.sendTCP(MY_TEAM);
    }

    @Override
    public void render(float delta) {
        if (source != null && destination != null) {
            King otherKing = BOARD.getKing(otherTeam);
            Cell src, dst;

            // Clone cells for notation
            try {
                src = new Cell(source);
                dst = new Cell(destination);
            } catch (CloneNotSupportedException e) {
                src = null;
                dst = null;

                e.printStackTrace();
            }

            // Send info to other server
            connection.sendTCP(new MovePacket(source, destination));

            // Do move
            move(source, destination);

            // Check if otherKing is in check
            BOARD.updateKing(otherTeam);

            // Notate
            if (isServer) // Server side is always first
                System.out.print(curTurn + ". ");

            // Notate pt 2
            System.out.print(AlgebraicNotation.notateMove(src, dst, otherKing));

            if (isServer)
                System.out.print(" ");
            else
                System.out.println();

            if (otherKing.isInCheck() && !otherKing.canMove()) { // Checkmate
                if (isServer) // A wins
                    System.out.println("\n" + AlgebraicNotation.Constants.WIN_A);
                else // B wins
                    System.out.println("\n" + AlgebraicNotation.Constants.WIN_B);
            }

            source = null;
            destination = null;
            POSSIBLE_MOVES.clear();
            curTurn++;
            myTurn = false;
        }

        BOARD.drawBoard(APP.getBatch());
        if (gameStart) {
            BOARD.drawHighlights(APP.getBatch(), POSSIBLE_MOVES, source);
            BOARD.drawPieces(APP.getBatch());
        }
    }

    private void move(Cell source, Cell destination) {
        Piece p1 = source.getPiece();
        Piece p2 = destination.getPiece();

        // We can probably get the team based on the source cell.
        Team curTeam = p1.getTeam();

        // Castle/En Passant check
        boolean castle = p1 instanceof King && Math.abs(destination.FILE - source.FILE) == 2,
                enPassant = p1 instanceof Pawn && destination.FILE != source.FILE && p2 == null;

        // Clear the move history of all the pieces of the current team
        BOARD.clearMoveHistory(curTeam);

        // Do move
        BOARD.performMove(source, destination);
    }

    /**
     * {@link InputAdapter} made to process input to select tiles on the board
     */
    private final class BoardInputAdapter extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (!myTurn)
                return false;

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
                } else if (cell.getPiece() != null && cell.getPiece().getTeam() == MY_TEAM) {
                    source = cell;
                    POSSIBLE_MOVES.clear();
                    POSSIBLE_MOVES.addAll(source.getPiece().getMoves(BOARD.CELLS, source.FILE, source.RANK));
                    MoveFilters.filterKingCapture(POSSIBLE_MOVES);
                    MoveFilters.filterCheck(BOARD, POSSIBLE_MOVES, source);

                    if (source.getPiece() instanceof King)
                        MoveFilters.filterCastle(BOARD, POSSIBLE_MOVES, BOARD.getCellContaining(BOARD.getKing(MY_TEAM)));
                }
            } else {
                source = null;
                POSSIBLE_MOVES.clear();
            }

            return true;
        }
    }

    @Override
    public void hide() {
        endPoint.stop();
        endPoint.close();
    }
}