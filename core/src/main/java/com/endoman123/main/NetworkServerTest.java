package com.endoman123.main;

import com.badlogic.gdx.ScreenAdapter;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

/**
 * @author Jared Tulayan
 */
public class NetworkServerTest extends ScreenAdapter {
    private final Server SERVER;
    public NetworkServerTest() {
        SERVER = new Server();
        SERVER.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof String) {
                    String str = (String) object;
                    System.out.println(str);
                }
            }
        });
        SERVER.start();
        try {
            SERVER.bind(25565);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
