package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.endoman123.board.Board;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final Application APP;
    private final Board BOARD;
    public GameScreen() {
        APP = (Application) Gdx.app.getApplicationListener();
        BOARD = new Board(100, 100, 500, 500, 8, 8);

        Gdx.input.setInputProcessor(BOARD);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        BOARD.update(delta);
        BOARD.draw(APP.getBatch());
    }

    @Override
    public void resize(int width, int height) {
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