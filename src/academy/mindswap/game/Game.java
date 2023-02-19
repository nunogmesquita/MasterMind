package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class Game {

    Code code;
    Server.ConnectedPlayer player;

    List<String> playerGuess;

    int attempts;

    Board board;

    List<String> secretCode;

    List<String> turnResult;

    String attempt;

    private String gameMode;
    ArrayList<String> opCode;


//    HashMap<String,Integer> attemptMap;

    boolean rightGuess = false;

    public Game(Server.ConnectedPlayer connectedPlayer) {
        this.player = connectedPlayer;
        this.board = new Board();
        this.secretCode = Code.generateCode();
    }



    public void play() throws IOException {
        System.out.println(secretCode + " " + this.player.getName());
        while (!rightGuess) {
            try {
                player.send(Messages.INSERT_TRY);
                attempt = player.askForGuess();
                checkPlayerGuess();
                turnResult = Code.compareCodes(this.playerGuess,this.secretCode);
                sendBoard();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        player.send(Messages.RIGHT_GUESS.formatted(attempts));
        player.send(Messages.QUIT_OR_NEW_GAME);
    }

    private void checkPlayerGuess() {
        playerGuess = new ArrayList<>();
        this.attempts++;
        for (int i = 0; i < attempt.length(); i++) {
           playerGuess.add(String.valueOf(attempt.charAt(i)));
        }
    }

    public void sendBoard() {
            player.send(Messages.LEGEND) ;
        for (String s : board.updatedBoard(this.playerGuess,this.turnResult))
            player.send(s);
    }

    public List<String> getSecretCode() {
        return secretCode;
    }

    public int getAttempts() {
        return attempts;
    }


}
