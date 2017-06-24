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
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

/** First screen of the application. Displayed after the application is created. */
public class NetworkGameScreen extends ScreenAdapter {
    private final Application APP;
    private Board curBoard;

    private final Kryo KRYO;
    private Connection connection;
    private boolean hasPlayer = false;
    private boolean gameStart = false;

    private final Team MY_TEAM;
    private Team otherTeam;
    private int curTurn = 1;
    private boolean myTurn = false;

    private final Array<Cell> POSSIBLE_MOVES;
    private Cell source, destination;

    /**
     * Server constructor
     * @param team team A
     * @param port port to bind to
     */
    public NetworkGameScreen(Team team, int port) {
        // Init stuff
        APP = (Application) Gdx.app.getApplicationListener();
        POSSIBLE_MOVES = new Array<Cell>();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (!myTurn)
                    return false;

                Vector2 mouseSelected = APP.getViewport().unproject(new Vector2(screenX, screenY));
                float tileWidth = curBoard.getTileWidth();
                float tileHeight = curBoard.getTileHeight();
                mouseSelected.sub(curBoard.getX(), curBoard.getY()).set(MathUtils.floor(mouseSelected.x / tileWidth), MathUtils.floor(mouseSelected.y / tileHeight));

                boolean validX = mouseSelected.x >= 0 && mouseSelected.x < curBoard.NUM_FILES;
                boolean validY = mouseSelected.y >= 0 && mouseSelected.y < curBoard.NUM_RANKS;

                if (validX && validY) {
                    Cell cell = curBoard.CELLS[(int)mouseSelected.y][(int)mouseSelected.x];

                    if (source != null && POSSIBLE_MOVES.contains(cell, true)) {
                        destination = cell;
                    } else if (cell == source || cell.getPiece() == null) {
                        source = null;
                        POSSIBLE_MOVES.clear();
                    } else if (cell.getPiece() != null && cell.getPiece().getTeam() == MY_TEAM) {
                        source = cell;
                        POSSIBLE_MOVES.clear();
                        POSSIBLE_MOVES.addAll(source.getPiece().getMoves(curBoard.CELLS, source.FILE, source.RANK));
                        MoveFilters.filterKingCapture(POSSIBLE_MOVES);
                        MoveFilters.filterCheck(curBoard, POSSIBLE_MOVES, source, MY_TEAM);

                        if (source.getPiece() instanceof King)
                            MoveFilters.filterCastle(curBoard, POSSIBLE_MOVES, curBoard.getCellContaining(curBoard.getKing(MY_TEAM)));
                    }
                } else {
                    source = null;
                    POSSIBLE_MOVES.clear();
                }

                return true;
            }
        });

        // Set team
        MY_TEAM = team;

        // Init curBoard
        float halfWidth = APP.getViewport().getWorldWidth() / 2f;
        curBoard = new Board(32, 32, halfWidth - 64, halfWidth - 64, 8, 8);

        Server server = new Server();
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
                        curBoard.reset(MY_TEAM, otherTeam);
                        connection.sendTCP(MY_TEAM);
                        gameStart = true;
                        myTurn = true;
                    }

                    if (object instanceof Board)
                        curBoard = (Board)object;

                }
            }
        });
        server.start();
        KRYO = server.getKryo();
        initKryo();

        try {
            server.bind(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        source = null;
        destination = null;
    }

    /**
     * Clien constructor
     * @param team team B
     * @param host server host
     * @param port server port
     */
    public NetworkGameScreen(Team team, String host, int port) {
        // Init stuff
        APP = (Application) Gdx.app.getApplicationListener();
        POSSIBLE_MOVES = new Array<Cell>();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 mouseSelected = APP.getViewport().unproject(new Vector2(screenX, screenY));
                float tileWidth = curBoard.getTileWidth();
                float tileHeight = curBoard.getTileHeight();
                mouseSelected.sub(curBoard.getX(), curBoard.getY()).set(MathUtils.floor(mouseSelected.x / tileWidth), MathUtils.floor(mouseSelected.y / tileHeight));

                boolean validX = mouseSelected.x >= 0 && mouseSelected.x < curBoard.NUM_FILES;
                boolean validY = mouseSelected.y >= 0 && mouseSelected.y < curBoard.NUM_RANKS;

                if (validX && validY) {
                    Cell cell = curBoard.CELLS[(int)mouseSelected.y][(int)mouseSelected.x];

                    if (source != null && POSSIBLE_MOVES.contains(cell, true)) {
                        destination = cell;
                    } else if (cell == source || cell.getPiece() == null) {
                        source = null;
                        POSSIBLE_MOVES.clear();
                    } else if (cell.getPiece() != null && cell.getPiece().getTeam() == MY_TEAM) {
                        source = cell;
                        POSSIBLE_MOVES.clear();
                        POSSIBLE_MOVES.addAll(source.getPiece().getMoves(curBoard.CELLS, source.FILE, source.RANK));
                        MoveFilters.filterKingCapture(POSSIBLE_MOVES);
                        MoveFilters.filterCheck(curBoard, POSSIBLE_MOVES, source, MY_TEAM);

                        if (source.getPiece() instanceof King)
                            MoveFilters.filterCastle(curBoard, POSSIBLE_MOVES, curBoard.getCellContaining(curBoard.getKing(MY_TEAM)));
                    }
                } else {
                    source = null;
                    POSSIBLE_MOVES.clear();
                }

                return true;
            }
        });

        // Set team
        MY_TEAM = team;

        // Init curBoard
        float halfWidth = APP.getViewport().getWorldWidth() / 2f;
        curBoard = new Board(32, 32, halfWidth - 64, halfWidth - 64, 8, 8);

        Client client = new Client();
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Team) {
                    otherTeam = (Team) object;
                    curBoard.reset(otherTeam, MY_TEAM);
                    gameStart = true;
                    myTurn = false;
                }

                if (object instanceof Board) {
                    curBoard = (Board) object;
                    myTurn = true;
                }
            }
        });
        client.start();
        KRYO = client.getKryo();
        initKryo();

        try {
            client.connect(5000, host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.sendTCP(MY_TEAM);

        source = null;
        destination = null;
    }

    private void initKryo() {
        KRYO.setRegistrationRequired(false);
        KRYO.register(Board.class);
        KRYO.register(Team.class);
        KRYO.register(Cell.class);
        KRYO.register(Piece.class);
    }

    @Override
    public void render(float delta) {
        if (source != null && destination != null) {
            if (source.getPiece() == null)
                throw new IllegalStateException("The source move is null!");

            King curKing = curBoard.getKing(MY_TEAM), otherKing = curBoard.getKing(otherTeam);
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
            curBoard.clearMoveHistory(MY_TEAM);

            // Do move
            curBoard.performMove(source, destination);

            // Check if cur king is in check
            curBoard.updateKing(otherKing);

            // Notate
            System.out.print(curTurn + ". ");
            System.out.print(AlgebraicNotation.notateMove(src, dst, otherKing) + " ");

            if (otherKing.isInCheck() && !otherKing.canMove()) // Checkmate
                System.out.println("\n" + AlgebraicNotation.Constants.WIN_A);

            source = null;
            destination = null;
            POSSIBLE_MOVES.clear();
            curTurn++;
            connection.sendTCP(curBoard);
            myTurn = false;
        }

        curBoard.drawBoard(APP.getBatch());
        if (gameStart) {
            curBoard.drawHighlights(APP.getBatch(), POSSIBLE_MOVES, source, MY_TEAM);
            curBoard.drawPieces(APP.getBatch());
        }
    }
}