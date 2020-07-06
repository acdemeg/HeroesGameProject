package com.neolab.heroesGame;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SetInitialStateTest {

    private static final String IP = "127.0.0.1";//"localhost";
    private static final int PORT = Server.PORT;

    @Test
    public void startServerAndClientsTest() throws InterruptedException {
        Thread server = new Thread(() -> {
            final Server server1 = new Server();
            try {
                server1.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.start();

        Thread client = new Thread(() -> {
            final Client client1 = new Client(IP, PORT);
            client1.startClient();
            try {
                client1.send("stop server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        client.start();

        server.join();
    }
}