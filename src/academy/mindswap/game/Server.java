package academy.mindswap.game;

import academy.mindswap.game.commands.Command;
import academy.mindswap.game.messages.Instructions;
import academy.mindswap.game.messages.Messages;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private ServerSocket serverSocket;

    private ExecutorService service;

    int numOfPlayers;

    protected final List<ConnectedPlayer> playersList;

    public Server(int numOfPlayers) {
        playersList = new CopyOnWriteArrayList<>();
        this.numOfPlayers = numOfPlayers;
    }

    public void start(int port) throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        service = Executors.newFixedThreadPool(numOfPlayers);
        System.out.printf(Messages.GAME_STARTED);
        acceptConnection();
    }

    public void acceptConnection() throws IOException {
        Socket playerSocket = serverSocket.accept();
        ConnectedPlayer connectedPlayer = new ConnectedPlayer(playerSocket);
        addPlayer(connectedPlayer);
        service.submit(connectedPlayer);
    }

    private void addPlayer(ConnectedPlayer connectedPlayer) throws IOException {
        playersList.add(connectedPlayer);
        connectedPlayer.send("Please insert your username:");
        connectedPlayer.name = new BufferedReader(new InputStreamReader(connectedPlayer.playerSocket.getInputStream())).readLine();
        connectedPlayer.send(Messages.WELCOME.formatted(connectedPlayer.getName()));
    }

    public void removePlayer(ConnectedPlayer connectedPlayer) {
        playersList.remove(connectedPlayer);
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
            this.game = new Game(this);
        }

        @Override
        public void run() {
            try {
                send(Instructions.readInstruction());
                game.play();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String askForGuess() throws IOException {
            send(Messages.INSERT_TRY);
            try {
                Scanner in = new Scanner(playerSocket.getInputStream());
                message = in.nextLine();
                if (isCommand(message)) {
                    dealWithCommand(message);
                }
            } catch (IOException e) {
                System.err.println(Messages.PLAYER_ERROR + e.getMessage());
            }
            return message;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }
    }
}