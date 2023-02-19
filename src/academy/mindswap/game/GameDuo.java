package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.io.IOException;
import java.util.ArrayList;

public class GameDuo extends Game {

    public GameDuo(Server.ConnectedPlayer connectedPlayer) {
        super(connectedPlayer);
    }

    public void play() throws IOException {
        System.out.println(secretCode + " " + this.player.getName());
        while (!rightGuess) {
            player.send(Messages.INSERT_TRY);
            attempt = player.askForGuess();
            checkPlayerGuess();
            turnResult = Code.compareCodes(this.playerGuess, this.secretCode);
            sendBoard();
            if (playerGuess.equals(secretCode)) {
                rightGuess = true;
            }
            if (attempts == maxAttempts) {
                player.send(Messages.OUT_OF_TRIES);
                break;
            }
        }
        if (rightGuess) {
            player.send(Messages.RIGHT_GUESS.formatted(attempts));
        }
    }
}