package com.endoman123.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.endoman123.pieces.MovePacket;
import com.endoman123.pieces.Team;
import com.endoman123.util.Assets;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

/**
 * {@link Screen} that contains an UI for choosing local or network play and options for each
 *
 * @author Jared Tulayan
 */
public class MainMenuScreen extends ScreenAdapter {
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

        final TextField TXT_IP = new TextField("127.0.0.1", SKIN);
        final TextField TXT_PORT = new TextField("25565", SKIN);
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
                    try {
                        Server server = new Server();
                        Kryo kryo = server.getKryo();
                        int port = Integer.parseInt(TXT_PORT.getText());

                        registerKryo(kryo);
                        server.start();
                        server.bind(port);

                        APP.setScreen(new NetworkGameScreen(Team.LIGHT_GRAY, server));
                        ME.dispose();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                if (BTN_CONNECT.isPressed()) {
                    try {
                        Client client = new Client();
                        Kryo kryo = client.getKryo();
                        String ip = TXT_IP.getText();
                        int port = Integer.parseInt(TXT_PORT.getText());

                        registerKryo(kryo);
                        client.start();
                        client.connect(5000, ip, port);

                        APP.setScreen(new NetworkGameScreen(Team.RED, client));
                        ME.dispose();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });

        TABLE.center().pad(300).setFillParent(true);
        TABLE.setSkin(SKIN);
        TABLE.add(TXT_IP).pad(5).expand().fill().colspan(2).fill();
        TABLE.add(TXT_PORT).pad(5).expand().fill().colspan(1).fill().row();
        TABLE.add(BTN_LOCAL_PLAY).expand().fill().pad(5).uniform().fill();
        TABLE.add(BTN_HOST).pad(5).expand().fill().uniform().fill();
        TABLE.add(BTN_CONNECT).pad(5).expand().fill().uniform().fill();

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

    private void registerKryo(Kryo kryo) {
        kryo.register(Team.class);
        kryo.register(MovePacket.class);
    }
}
