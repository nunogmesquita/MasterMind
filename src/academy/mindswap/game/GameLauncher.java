package academy.mindswap.game;

import java.io.IOException;

public class GameLauncher {

    public static void main(String[] args) {
        Game game = new Game();

        try {
           game.start(8082);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
