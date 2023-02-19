package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class Game {

    Server.ConnectedPlayer player;

    int maxAttempts = 10;

    int attempts;

    Board board;

    List<String> playerGuess;

    List<String> secretCode;

    List<String> turnResult;

    String attempt;

    boolean rightGuess = false;

    public Game(Server.ConnectedPlayer connectedPlayer) {
        this.player = connectedPlayer;
        this.board = new Board();
        this.secretCode = Code.generateCode();
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

    void checkPlayerGuess() {
        playerGuess = new ArrayList<>();
        this.attempts++;
        for (int i = 0; i < attempt.length(); i++) {
            playerGuess.add(String.valueOf(attempt.charAt(i)));
        }
    }

    public void sendBoard() {
        player.send(Messages.LEGEND);
        for (String s : board.updatedBoard(this.playerGuess, this.turnResult))
            player.send(s);
    }

    public List<String> getSecretCode() {
        return secretCode;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = new ArrayList<>();
        for (int i = 0; i < this.secretCode.size(); i++) {
            this.secretCode.add(String.valueOf(secretCode.charAt(i)));
        }
    }
}