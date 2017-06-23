package com.endoman123.main;

import com.badlogic.gdx.ScreenAdapter;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Jared Tulayan
 */
public class NetworkClientTest extends ScreenAdapter {
    private final Client CLIENT;
    public NetworkClientTest() {
        CLIENT = new Client();
        CLIENT.start();

        try {
            CLIENT.connect(5000, InetAddress.getLocalHost(), 25565);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CLIENT.sendTCP("Test");
    }
}
