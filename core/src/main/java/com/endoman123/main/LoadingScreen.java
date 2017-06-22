package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.endoman123.util.Assets;

/**
 * Screen made specifically just for loading resources.
 * Can be implemented to show some async loading thing.
 *
 * @author Jared Tulayan
 */
public class LoadingScreen extends ScreenAdapter {
    public void show() {
        Assets.MANAGER.load(Assets.GameObjects.BOARD_ATLAS);
        Assets.MANAGER.load(Assets.GameObjects.PIECES_ATLAS);
    }

    @Override
    public void render(float delta) {
        if (Assets.MANAGER.update()) {
            Application app = (Application) Gdx.app.getApplicationListener();
            app.setScreen(new GameScreen());
            this.dispose();
        }
    }
}
