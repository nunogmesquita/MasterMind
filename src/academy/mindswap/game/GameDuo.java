package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class GameDuo  {

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

    public GameDuo(Server.ConnectedPlayer connectedPlayer, ArrayList<String> code) {
        this.player = connectedPlayer;
        this.board = new Board();
        this.secretCode = code;
    }


    public void play() throws IOException {
        System.out.println(secretCode + " " + this.player.getName());
        while (!rightGuess()) {
            try {
                player.send(Messages.INSERT_TRY);
                attempt = player.askForGuess();
                checkPlayerGuess();
                turnResult = Code.compareCodes(playerGuess, secretCode);
                board.printBoard(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        player.send(Messages.RIGHT_GUESS.formatted(attempts));
        player.send(Messages.QUIT_OR_NEW_GAME);
    }

    private boolean rightGuess() {
        if(secretCode.equals(playerGuess)){
            return true;
        }
        return false;
    }

    private void checkPlayerGuess() {
        playerGuess = new ArrayList<>();
        this.attempts++;
        for (int i = 0; i < attempt.length(); i++) {
            playerGuess.add(String.valueOf(attempt.charAt(i)));
        }
    }

    public List<String> getSecretCode() {
        return secretCode;
    }

    public int getAttempts() {
        return attempts;
    }

    private void askGameMode(Server.ConnectedPlayer player ) throws IOException, InterruptedException {
        player.send(Messages.GAME_MODE);
        gameMode = new BufferedReader(new InputStreamReader(player.getPlayerSocket().getInputStream())).readLine();
        if(gameMode.equals("2")) {
            code.p2pList.add(this);
            if(p2pList.size() == 2) {
                this.notifyAll();
                this.askForOpponentCode();
            } else {
                player.send(Messages.WAITING_ALL_PLAYERS);
                this.wait();
            }} // aplicar aqui um wait para que p2pList tenha 2 de size()
    }

    public void askForOpponentCode() throws IOException {
        this.send(Messages.OPPONENT_CODE);
        String stringCode = askForGuess();
        ArrayList<String> opCode = new ArrayList<>();
        for (int i = 0; i < stringCode.length(); i++) {
            opCode.add(String.valueOf(stringCode.charAt(i)));
        }
        if (playerCodes.size() != 1) {
            playerCodes.put(1, opCode);
        } else {
            playerCodes.put(2, opCode);
        }
    }
}
