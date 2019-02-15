/**
 * General interface for any object that can call out moves to make on the chessboard.
 */
public abstract class Player {
    public boolean isWhite = false;

    /**
     * Method to get the move from this player.
     * @return a move, notated as "<origin_space><dest_space>"
     */
    public abstract String createMove();

    public boolean getIsWhite() {
        return isWhite;
    }
}