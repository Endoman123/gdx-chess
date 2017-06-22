package com.endoman123.pieces;

/**
 * Enumerator of all the team colors in the game.
 *
 * @author Jared Tulayan
 */
public enum Team {
    WHITE {
        @Override
        public int getDirection() {
            return 1;
        }
    },
    BLACK {
        @Override
        public int getDirection() {
            return -1;
        }
    };

    public abstract int getDirection();
}
