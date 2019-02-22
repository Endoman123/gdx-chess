package com.jtulayan.main;

import com.badlogic.gdx.ScreenAdapter;
import com.jtulayan.chess.ChessHandler;

/**
 * Screen that displays the main game.
 */
public class GameScreen extends ScreenAdapter {
    public GameScreen() {
        
    }

    @Override
    public void show() {
        ChessHandler.main(null);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}