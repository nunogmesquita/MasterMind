package academy.mindswap.game;

import academy.mindswap.game.commands.Command;
import academy.mindswap.game.messages.Color;
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

    private ExecutorService service;
    private List <ConnectedPlayer> p2pList;
    int numOfPlayers;
    public HashMap<Integer,ArrayList<String>> playerCodes;
    protected final List<ConnectedPlayer> playersList;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        playersList = new CopyOnWriteArrayList<>();
        playerCodes = new HashMap<>();
        p2pList = new CopyOnWriteArrayList<>();
    }

    public void start(int numOfPlayers) throws IOException, InterruptedException {
        this.numOfPlayers = numOfPlayers;
        service = Executors.newFixedThreadPool(numOfPlayers);
        System.out.printf(Messages.GAME_STARTED);
        while (playersList.size() < numOfPlayers) {
            acceptConnection();
        }
    }

    public void acceptConnection() throws IOException {
        Socket playerSocket = serverSocket.accept(); // blocking method
        ConnectedPlayer connectedPlayer = new ConnectedPlayer(playerSocket);
        service.submit(connectedPlayer);
    }

    private synchronized void addPlayer(ConnectedPlayer connectedPlayer) throws IOException, InterruptedException {
        verifyPlayerName(connectedPlayer, playersList);
        welcomePlayer(connectedPlayer);
        playersList.add(connectedPlayer);
        if (playersList.size() < numOfPlayers) {
            connectedPlayer.send(Messages.WAITING_ALL_PLAYERS.formatted(numOfPlayers - playersList.size()));
            this.wait();
        } else {
            this.notifyAll();
        }
    }



    public synchronized Game getGame(ConnectedPlayer player) throws InterruptedException {
        if(p2pList.size() == 2) {
            ArrayList<String> code;
            if (playersList.get(0).getName().equalsIgnoreCase(player.getName())) {
                code = playerCodes.get(2);
            } else {
                code = playerCodes.get(1);
            }
            return new Game(player, code);
        } else {
            return new Game(player);
        }
    }

    private void verifyPlayerName(ConnectedPlayer connectedPlayer, List<ConnectedPlayer> playersList) throws IOException, InterruptedException {
        connectedPlayer.send(Messages.ASK_NAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connectedPlayer.playerSocket.getInputStream()));
        String playerName = reader.readLine();
        validateName(connectedPlayer, playerName);
        if (true) {
            if (playersList.stream().
                    anyMatch(player -> player.getName().
                            equals(playerName))) {
                connectedPlayer.send(Messages.INVALID_NAME);
                verifyPlayerName(connectedPlayer, playersList);
            } else {
                connectedPlayer.name = playerName;
            }
        }
    }

    private boolean validateName(ConnectedPlayer connectedPlayer, String name) throws IOException, InterruptedException {
        String regex = "^\\S+$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            connectedPlayer.send(Messages.INVALID_FIRST_NAME);
            verifyPlayerName(connectedPlayer,playersList);
            return false;
        }
        return true;
    }

    private void welcomePlayer(ConnectedPlayer connectedPlayer) {
      // connectedPlayer.name = new BufferedReader(new InputStreamReader(connectedPlayer.playerSocket.getInputStream())).readLine();
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

    public class ConnectedPlayer implements Runnable {

        private String name;

        private final Socket playerSocket;

        private final BufferedWriter out;

        private String message;

        Game game;
        private String gameMode;

        public ConnectedPlayer(Socket playerSocket) throws IOException {
            this.playerSocket = playerSocket;
            this.out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
            game = new Game(this);
        }

        @Override
        public void run() {
            try {
                addPlayer(this);
                send(Instructions.readInstruction());
                send(Messages.RESULT_RULES  +  Messages.LEGEND);
                askGameMode();
                game = getGame(this);
                game.play();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
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

        public void startNewGame(){
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

        private void askGameMode() throws IOException, InterruptedException {
            this.send(Messages.GAME_MODE);
            gameMode = new BufferedReader(new InputStreamReader(this.playerSocket.getInputStream())).readLine();
            if(gameMode.equals("2")) {
                p2pList.add(this);
                if(p2pList.size() == 2) {
                    this.notifyAll();
                    this.askForOpponentCode();
                } else {
                    this.send(Messages.WAITING_ALL_PLAYERS);
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

        public String getGameMode() {
            return gameMode;
        }
    }
}