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

    private ExecutorService service;

    int numOfPlayers;

    protected final List<ConnectedPlayer> playersList;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        playersList = new CopyOnWriteArrayList<>();
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
        playersList.add(connectedPlayer);
        connectedPlayer.send("Please insert your username:");
        welcomePlayer(connectedPlayer);
        if (playersList.size() < numOfPlayers) {
            connectedPlayer.send(Messages.WAITING_ALL_PLAYERS.formatted(numOfPlayers - playersList.size()));
            this.wait();
        } else this.notifyAll();
    }

    private void welcomePlayer(ConnectedPlayer connectedPlayer) throws IOException {
        connectedPlayer.name = new BufferedReader(new InputStreamReader(connectedPlayer.playerSocket.getInputStream())).readLine();
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

        private boolean validInput() {
            String regex = "^[OYBPG]{4}$";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(message.toUpperCase());
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
    }
}