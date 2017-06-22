package com.endoman123.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Application extends Game {
    private Viewport viewport;
    private SpriteBatch batch;

    @Override
    public void create() {
        viewport = new FitViewport(800, 800);
        batch = new SpriteBatch();

        setScreen(new LoadingScreen());
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (getScreen() != null)
            getScreen().render(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public Viewport getViewport() {
        return viewport;
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}