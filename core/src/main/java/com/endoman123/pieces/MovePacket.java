package com.endoman123.pieces;

import com.endoman123.board.Cell;
import com.endoman123.util.AlgebraicNotation;

/**
 * Object to use to send moves over the network
 *
 * @author Jared Tulayan
 */
public class MovePacket {
    public int fromFile, toFile, fromRank, toRank;

    public MovePacket() {
        fromFile = 0;
        toFile = 0;
        fromRank = 0;
        toRank = 0;
    }

    public MovePacket(Cell from, Cell to) {
        fromFile = from.FILE;
        fromRank = from.RANK;
        toFile = to.FILE;
        toRank = to.RANK;
    }

    public String toString() {
        return AlgebraicNotation.notatePosition(toFile, toRank);
    }
}
