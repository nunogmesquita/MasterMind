package academy.mindswap.game;

import java.io.IOException;

public class ServerLauncher {

    public static void main(String[] args) {
        Server server = new Server(2);

        try {
            server.start(8082);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}