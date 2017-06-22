package com.endoman123.pieces;

/**
 * Enumerator of all the team colors in the game.
 *
 * @author Jared Tulayan
 */
public enum Team {
    RED {
        @Override
        public String getPath() {
            return "red/";
        }
    },
    ORANGE {
        public String getPath() {
            return "orange/";
        }
    },
    YELLOW {
        @Override
        public String getPath() {
            return "yellow/";
        }
    },
    GREEN {
        public String getPath() {
            return "green/";
        }
    },
    CYAN {
        public String getPath() {
            return "cyan/";
        }
    },
    BLUE {
        public String getPath() {
            return "blue/";
        }
    },
    PURPLE {
        public String getPath() {
            return "purple/";
        }
    },
    PINK {
        public String getPath() {
            return "pink/";
        }
    },
    DARK_GRAY {
        public String getPath() {
            return "dark_gray/";
        }
    },
    LIGHT_GRAY{
        public String getPath() {
            return "light_gray/";
        }
    },
    WHITE {
        @Override
        public String getPath() {
            return "white/";
        }
    },
    BLACK {
        public String getPath() {
            return "black/";
        }
    };


    public abstract String getPath();
}
