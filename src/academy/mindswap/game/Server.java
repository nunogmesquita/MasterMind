package academy.mindswap.game;

import academy.mindswap.game.commands.Command;
import academy.mindswap.game.messages.Instructions;
import academy.mindswap.game.messages.Messages;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private final ServerSocket serverSocket;
    private ExecutorService serviceSolo;
    private ExecutorService serviceDuo;

    int numOfPlayers;

    protected final List<ConnectedPlayer> playersList;
    private boolean duoGame;
    private ConnectedPlayer playerWithLeastTurns;
    private ConnectedPlayer playerWithMostTurns;
    private ConnectedPlayer playerWithSameTurns;


    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        playersList = new CopyOnWriteArrayList<>();

    }

    public void start() throws IOException, InterruptedException {
        serviceSolo = Executors.newCachedThreadPool();

        System.out.printf(Messages.GAME_STARTED);

        acceptConnection();
    }


    public void sendCodes(ConnectedPlayer player) throws IOException {
        if(player.equals(playersList.get(1))) {
            playersList.get(1).send(Messages.OPPONENT_CODE);
            String stringCode = player.askForGuess();
            playersList.get(0).gameDuo.setSecretCode(stringCode);
        } else {
            playersList.get(0).send(Messages.OPPONENT_CODE);
            String stringCode1 = player.askForGuess();
            playersList.get(1).gameDuo.setSecretCode(stringCode1);
        }
    }
    public void acceptConnection() throws IOException, InterruptedException {
        Socket playerSocket = serverSocket.accept(); // blocking method
        ConnectedPlayer connectedPlayer = new ConnectedPlayer(playerSocket, duoGame);
        addPlayer(connectedPlayer);
        gameMode(connectedPlayer);

    }

    private void gameMode(ConnectedPlayer player) throws IOException, InterruptedException {
        player.send(Messages.GAME_MODE);
        String gameMode = new BufferedReader(new InputStreamReader(player.getPlayerSocket().getInputStream())).readLine();
        switch (gameMode) {
            case "1":
                serviceSolo.submit(player);
                break;
            case "2":
                playersList.add(player);
                duoGame = true;
                startDuoGame(player);
                break;
            default:
                gameMode(player);
        }
    }


//if(p2pList.size() == 2)
//
//    {
//        ArrayList<String> code;
//        if (playersList.get(0).getName().equalsIgnoreCase(player.getName())) {
//            code = playerCodes.get(2);
//        } else {
//            code = playerCodes.get(1);
//        }

    public synchronized void startDuoGame(ConnectedPlayer connectedplayer) throws InterruptedException, IOException {
        if (playersList.size() % 2 == 0) {
            serviceDuo = Executors.newFixedThreadPool(2);
            playersList.notifyAll();
            serviceDuo.submit(connectedplayer);

        } else {
            connectedplayer.send(Messages.WAITING_ALL_PLAYERS);
            connectedplayer.wait();
            serviceDuo.submit(connectedplayer);
        }
    }


    private synchronized void addPlayer(ConnectedPlayer connectedPlayer) throws IOException, InterruptedException {
        verifyPlayerName(connectedPlayer);
        welcomePlayer(connectedPlayer);
    }

    private void verifyPlayerName(ConnectedPlayer connectedPlayer) throws IOException, InterruptedException {
        connectedPlayer.send(Messages.ASK_NAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connectedPlayer.playerSocket.getInputStream()));
        String playerName = reader.readLine();
        validateName(connectedPlayer, playerName);
        if (playersList.stream().
                anyMatch(player -> player.getName().
                        equals(playerName))) {
            connectedPlayer.send(Messages.INVALID_NAME);
            verifyPlayerName(connectedPlayer);
        } else {
            connectedPlayer.name = playerName;
        }
    }

    private void validateName(ConnectedPlayer connectedPlayer, String name) throws IOException, InterruptedException {
        String regex = "^\\S+$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            connectedPlayer.send(Messages.INVALID_FIRST_NAME);
            verifyPlayerName(connectedPlayer);
        }
    }

    private void welcomePlayer(ConnectedPlayer connectedPlayer) {
        connectedPlayer.send(Messages.WELCOME.formatted(connectedPlayer.getName()));
    }

    public void removePlayer(ConnectedPlayer connectedPlayer) {
        playersList.remove(connectedPlayer);
    }

    private void broadcast(String name, String message) {
        playersList.stream()
                .filter(player -> !player.getName().equals(name))
                .forEach(player -> player.send(name.concat(message)));
    }

    private synchronized void checkWinnerAttempts(Game game, ConnectedPlayer connectedPlayer) throws InterruptedException {

        if (playerWithMostTurns == null || connectedPlayer.game.getAttempts() > playerWithMostTurns.game.getAttempts()) {
            playerWithMostTurns = connectedPlayer;
        }

        if (playerWithLeastTurns == null || connectedPlayer.game.getAttempts() < playerWithLeastTurns.game.getAttempts()) {
            playerWithLeastTurns = connectedPlayer;
        }
        if (playerWithSameTurns == null || connectedPlayer.game.getAttempts() == playerWithSameTurns.game.getAttempts()) {
            connectedPlayer.send(playerWithSameTurns.getName().concat("It's a tie"));
        }

        connectedPlayer.send(playerWithLeastTurns.getName().concat(" Win with least attempts!"));
    }

    public class ConnectedPlayer implements Runnable {

        private String name;

        private final Socket playerSocket;

        private final BufferedWriter out;

        private String message;
        private boolean duoGame;
        Game game;
        GameDuo gameDuo;

        public ConnectedPlayer(Socket playerSocket,boolean duoGame) throws IOException {
            this.playerSocket = playerSocket;
            this.out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
            this.duoGame = duoGame;
        }

        @Override
        public void run() {
            try {
                send(Instructions.readInstruction());
                startGame();
//                sendCodes(this);
                gameDuo.play();
                game.play();
                checkWinnerAttempts(this.game, this);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void startGame() {
            if(this.duoGame == true) {
                gameDuo = new GameDuo(this);
            } else {
                game = new Game(this);
            }
        }

        String askForGuess() throws IOException {
            while (!playerSocket.isClosed()) {
                try {
                    Scanner in = new Scanner(playerSocket.getInputStream());
                    message = in.nextLine();
                    if (isCommand(message)) {
                        dealWithCommand(message);
                        break;
                    }
                } catch (IOException e) {
                    System.err.println(Messages.PLAYER_ERROR + e.getMessage());
                }
                if (!validInput()) {
                    askForGuess();
                }
                return message;
            }
            return null;
        }

        public void showCode(ConnectedPlayer connectedPlayer) {
            connectedPlayer.send(Messages.SHOW_CODE.formatted(game.getSecretCode()));
        }

        public void startNewGame() {
            try {
                game.play();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean validInput() {
            String regex = "^[OYBPG]{4}$";
            message = message.toUpperCase();
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(message);
            if (!matcher.find()) {
                send(Messages.INVALID_TRY);
                return false;
            }
            return true;
        }

        private boolean isCommand(String message) {
            return message.startsWith("/");
        }

        private void dealWithCommand(String message) throws IOException {
            String description = message.split(" ")[0];
            Command command = Command.getCommandFromDescription(description);
            if (command == null) {
                out.write(Messages.NO_SUCH_COMMAND);
                out.newLine();
                out.flush();
                return;
            }
            command.getHandler().execute(Server.this, this);
        }


        public void send(String message) {
            try {
                out.write(message);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                removePlayer(this);
                e.printStackTrace();
            }
        }


        public void close() {
            try {
                playerSocket.close();
                broadcast(this.getName(), Messages.PLAYER_QUIT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }



        public Socket getPlayerSocket() {
            return playerSocket;
        }
    }
}