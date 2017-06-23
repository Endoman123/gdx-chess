package com.endoman123.util;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Wrapper class for assets manager.
 *
 * @author Jared Tulayan
 */
public class Assets {
    public static final AssetManager MANAGER;

    static {
        MANAGER = new AssetManager();
    }

    /**
     * Class containing {@link AssetDescriptor}s for game object textures
     */
    public static class GameObjects {
        public static AssetDescriptor<TextureAtlas> PIECES_ATLAS = new AssetDescriptor<TextureAtlas>("pieces/pieces.atlas", TextureAtlas.class);
        public static AssetDescriptor<TextureAtlas> BOARD_ATLAS = new AssetDescriptor<TextureAtlas>("board/board.pack", TextureAtlas.class);
    }

    /**
     * Class containing {@link AssetDescriptor}s for ui skins
     */
    public static class UI {
        public static AssetDescriptor<Skin> SKIN = new AssetDescriptor<Skin>("ui/uiskin.json", Skin.class);
    }
}
