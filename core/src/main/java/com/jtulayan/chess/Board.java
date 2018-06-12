package com.jtulayan.chess;

/**
 * Code representation of the game board.
 * In the context of the Memento Pattern, this is the Originator.
 *
 * The board itself has various fields to represent the board state, as well
 * as a halfmove counter, fullmove counter, etc.
 *
 * Mementos are stored in FEN, and there are methods in this class to parse mementos into
 * board states.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Memento_pattern#Structure">Memento Pattern Structure</a>
 * @see <a href="https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">Forsyth-Edwards Notation (FEN)</a>
 */
public class Board {
    private final char[][] BOARD_STATE = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
    };

    private int posKingWhite = 4;
    private int posKingBlack = 60;

    private boolean isWhiteTurn = true;

    private long castles = 0xFL;
    private int enPassant = -1;

    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;

    /**
     * Initializes board.
     */
    public Board() {

    }

    /**
     * Performs the specified move.
     * This assumes the given move is valid to begin with,
     * i.e.: it is a legal move made
     * by the side whose turn it currently is
     *
     * @param move the move to make
     * @return AN of move
     */
    public String makeMove(String move) {
        // Info
        String an = "";
        boolean irreversible = false;

        String fromAN = move.substring(0, 2);
        String toAN = move.substring(2, 4);
        int from = AlgebraicNotation.getTileIndex(fromAN);
        int to = AlgebraicNotation.getTileIndex(toAN);
        int fromRank = from / 8;
        int fromFile = from % 8;
        int toRank = to / 8;
        int toFile = to % 8;

        char destPiece = move.charAt(4);

        // Parse flags
        String flags = "" + "000" + Integer.toBinaryString(Integer.parseInt(move.substring(5)));
        flags = flags.substring(flags.length() - 4);
        boolean isPromo = '1' == flags.charAt(0);
        boolean isCapture = '1' == flags.charAt(1);
        boolean isSpecial1 = '1' == flags.charAt(2);
        boolean isSpecial0 = '1' == flags.charAt(3);

        char piece = BOARD_STATE[fromRank][fromFile];
        char pieceAN = Character.toUpperCase(piece);

        // Perform move
        BOARD_STATE[toRank][toFile] = piece;
        BOARD_STATE[fromRank][fromFile] = ' ';

        // Per-piece special things
        enPassant = -1;
        switch (pieceAN) {
            case 'K':
                break;
            case 'Q':
                break;
            case 'N':
                break;
            case 'B':
                break;
            case 'R':
                break;
            case 'P':
                irreversible = true;

                if (isPromo) {
                    // Promotion logic
                } else if (isSpecial0) {
                    int dir = (int)Math.signum(toRank - fromRank);

                    if (isCapture) // En passant case
                        BOARD_STATE[toRank - dir][toFile] = ' ';
                    else // Double pawn push case
                        enPassant = 8 * toRank - 8 * dir + fromFile;
                }

                break;
        }

        irreversible |= isCapture;

        // Modify halfmove clock
        if (!irreversible)
            halfmoveClock++;
        else
            halfmoveClock = 0;

        // Modify fullmove counter
        if (!isWhiteTurn)
            fullmoveNumber++;

        // Turn over
        isWhiteTurn = !isWhiteTurn;

        // Notate the move
        if (!isPromo && !isCapture && isSpecial1) { // It's some type of castle
            if (isSpecial0)
                an = AlgebraicNotation.CASTLE_QUEENSIDE;
            else
                an = AlgebraicNotation.CASTLE_KINGSIDE;
        } else if (pieceAN != 'P') {
            an += pieceAN;
            if (isCapture)
                an += AlgebraicNotation.CAPTURE;
            an += toAN;
        } else if (pieceAN == 'P') {
            if (isCapture) {
                an += fromAN.charAt(0) + AlgebraicNotation.CAPTURE + toAN;
                if (isSpecial0) // En passant
                    an += AlgebraicNotation.EN_PASSANST;
            } else {
                an += toAN;
                if (isPromo) {
                    an += AlgebraicNotation.PROMOTION;
                    if (isSpecial1 && isSpecial0)
                        an += 'Q';
                    else if (!isSpecial1 && isSpecial0)
                        an += 'B';
                    else if (isSpecial1 && !isSpecial0)
                        an += 'R';
                    else
                        an += 'N';
                }
            }
        }

        return an;
    }

    /**
     * Undoes the specified move
     * @param move the move to unmake
     */
    public void unmakeMove(String move) {

    }

    /**
     * Notates the given move in AN.
     * @param move the move to notate
     */
    public void notateMove(String move) {

    }

    /**
     * Gets the board piece at the specific location.
     * @param rank the rank of the piece
     * @param file the file of the piece
     * @return the piece located at (file, rank)
     */
    public char getPiece(int rank, int file) {
        if (rank > 7 || rank < 0 )
            throw new IllegalArgumentException("Rank is outside range! Was " + rank);
        else if (file > 7  || file < 0)
            throw new IllegalArgumentException("File is outside range! Was " + file);


        return BOARD_STATE[rank][file];
    }

    /**
     * Gets the en passant tile.
     * @return the index of the en passant tile
     */
    public int getEnPassant() {
        return enPassant;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    /**
     * Clears the board
     */
    public void clearBoard() {
        int size = BOARD_STATE.length * BOARD_STATE[0].length;

        for (int i = 0; i < 64; i++)
            BOARD_STATE[i / 8][i % 8] = ' ';
    }

    /**
     * Lists all possible moves that can be made for a given side.
     *
     * @param isWhite if the team to get for is the white team
     * @return string of all moves.
     *
     * @see MoveGenerators
     */
    public String listPossibleMoves(boolean isWhite) {
        String list = "";
        for (int i = 0; i < 64; i++) {
            String entry = "";
            char curPiece = BOARD_STATE[i / 8][i % 8];
            if (Character.isUpperCase(curPiece) != isWhite)
                continue;

            switch (Character.toUpperCase(curPiece)) {
                case 'K':
                    entry = MoveGenerators.listKingMoves(this, i);
                    break;
                case 'Q':
                    entry = MoveGenerators.listQueenMoves(this, i);
                    break;
                case 'P':
                    entry = MoveGenerators.listPawnMoves(this, i);
                    break;
                case 'N':
                    entry = MoveGenerators.listKnightMoves(this, i);
                    break;
                case 'B':
                    entry = MoveGenerators.listBishopMoves(this, i);
                    break;
                case 'R':
                    entry = MoveGenerators.listRookMoves(this, i);
                    break;
                default:
                    break;
            }

            if (entry.length() > 0)
                list += entry + "/";
        }

        // Filter king captures

        // Return everything but the redundant "/" at the end.
        if (list.length() > 0)
            list = list.substring(0, list.length() - 1);

        return list;
    }

    public String filterKingCaptures(Board b, String ml) {
        String moveList = "";
        Memento memento = null;
        
        // Parse each move and check if it captures the opponent's king.
        int i = 0;
        boolean finished = false;
        while (!finished) {
            memento = b.createMemento();
        }
        
        return moveList;
    }

    public String toString() {
        String board = "";

        // Start with the board itself
        for (int r = 0; r < 8; r++) {
            board += "|";

            for (int f = 0; f < 8; f++) {
                char tile = BOARD_STATE[r][f];

                if (tile == ' ')
                    tile = '-';

                board += tile;

                if (f < 7)
                    board += " ";
            }

            board += "|";

            if (r < 7)
                board += "\n";
        }

        return board;
    }

    // region Memento Pattern
    public Memento createMemento() {
        return new Memento(this);
    }

    public void restore(Memento memento) {
        String state = memento.toString(), board = "", turn = "", cas = "", enp = "";

        // Repopulate board
        // This is done by clearing the board, then parsing
        // the FEN board
        board = state.substring(0, state.indexOf(' '));
        clearBoard();
        for (int i = 0; i < 8; i++) {
            String rank = board;

            if (i < 7)
                rank = board.substring(0, board.indexOf('/'));

            int blankFile = 0;
            for (int j = 0; j < 8; j++) {
                char tile = rank.charAt(0);

                if (Character.isDigit(tile) && blankFile == 0) {
                    blankFile = Integer.parseInt("" + tile);

                    if (rank.length() > 1)
                        rank = rank.substring(1);
                }

                if (blankFile > 0) {
                    BOARD_STATE[i][j] = ' ';
                    blankFile -= 1;
                } else {
                    BOARD_STATE[i][j] = tile;

                    if (rank.length() > 1)
                        rank = rank.substring(1);
                }
            }

            if (i < 7)
                board = board.substring(board.indexOf('/') + 1);
        }

        state = state.substring(state.indexOf(' ') + 1);

        // Check if it is white's turn
        turn = state.substring(0, state.indexOf(' '));
        isWhiteTurn = turn == "w";
        state = state.substring(state.indexOf(' ') + 1);

        // Get castling abilities
        cas = state.substring(0, state.indexOf(' '));
        castles = 0x0L;
        if (cas.contains("K"))
            castles += 0x8L;
        if (cas.contains("Q"))
            castles += 0x4L;
        if (cas.contains("k"))
            castles += 0x2L;
        if (cas.contains("q"))
            castles += 0x1L;
        state = state.substring(state.indexOf(' ') + 1);

        enp = state.substring(0, state.indexOf(' '));
        if ("-".equals(enp))
            enPassant = 0;
        else
            enPassant = AlgebraicNotation.getTileIndex(enp);

        state = state.substring(state.indexOf(' ') + 1);

        halfmoveClock = Integer.parseInt(state.substring(0, state.indexOf(' ')));
        state = state.substring(state.indexOf(' ') + 1);

        fullmoveNumber = Integer.parseInt(state);
    }

    /**
     * Memento class used to store history of board state.
     */
    public static class Memento {
        private String state;

        /**
         * Converts the specified board into FEN notation, for easier storage.
         * @param b the Board to store in the memento
         */
        public Memento(Board b) {
            String newMemento = "";
            // Start by storing the board itself
            for (int i = 0; i < b.BOARD_STATE.length; i++) {
                int blankSpace = 0;
                for (int j = 0; j < b.BOARD_STATE[i].length; j++) {
                    char curTile = b.BOARD_STATE[i][j];
                    if (curTile == ' ') {
                        blankSpace += 1;
                        if (j == b.BOARD_STATE[i].length - 1)
                            newMemento += "" + blankSpace;
                    } else {
                        if (blankSpace > 0) {
                            newMemento += "" + blankSpace;
                            blankSpace = 0;
                        }
                        newMemento += curTile;
                    }
                }
                if (i < b.BOARD_STATE.length - 1)
                    newMemento += "/";
            }

            newMemento += " ";

            // Get current turn
            newMemento += b.isWhiteTurn ? "w" : "b";

            newMemento += " ";

            // Get castles
            if (b.castles == 0L)
                newMemento = "-";
            else {
                String castlesBin = "000" + Long.toBinaryString(b.castles);
                castlesBin = castlesBin.substring(castlesBin.length() - 4);

                if (castlesBin.charAt(0) == '1')
                    newMemento += 'K';
                if (castlesBin.charAt(1) == '1')
                    newMemento += 'Q';
                if (castlesBin.charAt(2) == '1')
                    newMemento += 'k';
                if (castlesBin.charAt(3) == '1')
                    newMemento += 'q';
            }

            newMemento += " ";

            // Get en passant tile
            if (b.enPassant == -1)
                newMemento += "-";
            else
                newMemento += AlgebraicNotation.getAN(b.enPassant / 8, b.enPassant % 8);

            newMemento += " ";

            // Add halfmove clock and fullmove number
            newMemento += "" + b.halfmoveClock + " " + b.fullmoveNumber;

            state = newMemento;
        }

        public String toString() {
            return state;
        }
    }
    // endregion
}
