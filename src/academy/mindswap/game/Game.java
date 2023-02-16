package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Game {

    Server.ConnectedPlayer player;

    List<Integer> playerGuess;

    int attempts;

    Board board;

    List<Integer> secretCode;

    List<String> turnResult;

    String attempt;

    boolean rightGuess = false;

    public Game(Server.ConnectedPlayer connectedPlayer) {
        this.player = connectedPlayer;
        this.board = new Board();
        this.secretCode = Code.generateCode();
        System.out.println(secretCode);
    }

    public void play() throws IOException {
        while (!rightGuess) {
            try {
                attempt = player.askForGuess();
                validatePlay();
                checkPlayerGuess();
                Code.compareCodes(this, playerGuess, secretCode);
                board.printBoard(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        player.send("Congrats");
    }

    private void validatePlay() throws IOException {
        String regex = "^\\d{4}$";
        final Matcher matcher = Pattern.compile(regex).matcher(attempt);
        if (!matcher.find()) {
            player.send(Messages.INVALID_TRY);
            play();
        }
        this.attempts++;
    }

    private void checkPlayerGuess() {
        playerGuess = new ArrayList<>();
        for (int i = 0; i < attempt.length(); i++) {
            playerGuess.add(parseInt(String.valueOf(attempt.charAt(i))));
        }
    }
}