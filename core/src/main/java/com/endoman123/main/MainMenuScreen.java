package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.endoman123.pieces.Team;
import com.endoman123.util.Assets;

/**
 * {@link Screen} that contains an UI for choosing local or network play and options for each
 *
 * @author Jared Tulayan
 */
public class MainMenuScreen implements Screen {
    private final Application APP;
    private final Stage CANVAS;

    public MainMenuScreen() {
        APP = (Application) Gdx.app.getApplicationListener();
        CANVAS = new Stage(APP.getViewport(), APP.getBatch());
        generateUI();
    }

    private void generateUI() {
        final Screen ME = this;
        final Skin SKIN = Assets.MANAGER.get(Assets.UI.SKIN);
        final Table TABLE = new Table();

        final TextField TXT_IP = new TextField("0.0.0.0", SKIN);
        final TextField TXT_PORT = new TextField("12345", SKIN);
        final TextButton BTN_LOCAL_PLAY = new TextButton("Local", SKIN);
        final TextButton BTN_HOST = new TextButton("Host", SKIN);
        final TextButton BTN_CONNECT = new TextButton("Connect", SKIN);

        TABLE.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (BTN_LOCAL_PLAY.isPressed()) {
                    APP.setScreen(new LocalGameScreen(Team.LIGHT_GRAY, Team.RED));
                    ME.dispose();
                }

                if (BTN_HOST.isPressed()) {
                    APP.setScreen(new NetworkGameScreen(Team.LIGHT_GRAY, Integer.parseInt(TXT_PORT.getText())));
                    ME.dispose();
                }

                if (BTN_CONNECT.isPressed()) {
                    APP.setScreen(new NetworkGameScreen(Team.RED, TXT_IP.getText(), Integer.parseInt(TXT_PORT.getText())));
                    ME.dispose();
                }
            }
        });

        TABLE.center().setFillParent(true);
        TABLE.setSkin(SKIN);
        TABLE.add(TXT_IP).pad(5).colspan(2).fill();
        TABLE.add(TXT_PORT).pad(5).colspan(1).fill().row();
        TABLE.add(BTN_LOCAL_PLAY).pad(5).uniform().fill();
        TABLE.add(BTN_HOST).pad(5).uniform().fill();
        TABLE.add(BTN_CONNECT).pad(5).uniform().fill();

        CANVAS.addActor(TABLE);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(CANVAS);
    }

    @Override
    public void render(float delta) {
        CANVAS.act(delta);
        CANVAS.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

    }
}
