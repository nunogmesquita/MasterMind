package academy.mindswap.game;

import academy.mindswap.game.commands.Command;
import academy.mindswap.game.messages.Messages;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Game {
    private ServerSocket serverSocket;
    private ExecutorService service;
    int numOfPlayers;
    private final List<PlayerConnectionHandler> playersList;
    List<Integer> secretCode;
    boolean win = false;
    private final HashMap<String, Socket> userSocketMap;
    List<String> turnResult;


    public Game(int numOfPlayers) {
        playersList = new CopyOnWriteArrayList<>();
        userSocketMap = new HashMap<>();
//        userBoardMap = new HashMap<>();
        this.numOfPlayers = numOfPlayers;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        service = Executors.newFixedThreadPool(numOfPlayers);
        System.out.printf(Messages.GAME_STARTED);

        while (true) {
            acceptConnection();
        }
    }

    public void acceptConnection() throws IOException {
        Socket playerSocket = serverSocket.accept();
        PlayerConnectionHandler playerConnectionHandler =
                new PlayerConnectionHandler(playerSocket);
        service.submit(playerConnectionHandler);
    }


    public void broadcast(String name, String message) {
        playersList.stream()
                .filter(handler -> !handler.getName().equals(name))
                .forEach(handler -> handler.send(name + ": " + message));
    }

    public void removePlayer(PlayerConnectionHandler playerConnectionHandler) {
        playersList.remove(playerConnectionHandler);
    }

    public void broadcastBoard(PlayerConnectionHandler player) {
        updateBoard(player);
        System.out.println(player.board.size());
        for (int i = 0; i < player.board.size(); i++) {
            System.out.println(i);
            player.send(player.board.get(i));
        }
    }
    public void updateBoard(PlayerConnectionHandler player) {
        String newTry = "_______________________ \n" +
                "|  " +  player.playerGuess.get(0).toString() +
                "  |  " + player.playerGuess.get(1).toString() +
                "  |  " + player.playerGuess.get(2).toString() +
                "  |  " + player.playerGuess.get(3).toString() + "  |  " + " [==] "
                + turnResult.get(0) +
                turnResult.get(1) + " " +
                " " + turnResult.get(2) +
                turnResult.get(3) + "\n_______________________    ____";
        System.out.println(newTry);
        player.board.add(newTry);
    }

    private void generateCode() {
        Random random = new Random();
        secretCode = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(5);
            secretCode.add(digit);
        }
    }

    private void compareCodes(List<Integer> playerGuess, List<Integer> secretCode) {
        turnResult = new ArrayList<>();
        List<Integer> playerGuessCopy = new ArrayList<>(playerGuess);
        List<Integer> secretCodeCopy = new ArrayList<>(secretCode);
        for (int i = 0; i < playerGuess.size(); i++) {
            if (playerGuessCopy.get(i).equals(secretCodeCopy.get(i)) && playerGuessCopy.get(i) != null ) {
                turnResult.add("+");
                playerGuessCopy.set(i,null);
                secretCodeCopy.set(i,null);
            }
        }

        for (int i = 0; i < playerGuess.size(); i++) {
            if (secretCodeCopy.contains(playerGuessCopy.get(i)) && playerGuessCopy.get(i) != null) {
                turnResult.add("-");
                playerGuessCopy.set(i,null);
                secretCodeCopy.set(i,null);
            }
        }
        playerGuessCopy.clear();
        secretCodeCopy.clear();
    }

    private void checkWinner(List<Integer> playerGuess) {
        if (secretCode.equals(playerGuess)) {
            win = true;
        }
    }

    public class PlayerConnectionHandler implements Runnable {

        private String name;
        private final Socket playerSocket;
        private final BufferedWriter out;
        private String message;
        List<Integer> playerGuess;
        private int turns;
        private ArrayList<String> board;

        public PlayerConnectionHandler(Socket playerSocket) throws IOException {
            this.playerSocket = playerSocket;
            this.out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
            this.board = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                addPlayer(this);
                generateCode();
                    while (!win) {
                    send(Messages.INSERT_TRY);
                    System.out.println(secretCode);
                    communicate();
                    checkWinner(playerGuess);
                    compareCodes(playerGuess, secretCode);
                    send(turnResult.toString());
                    broadcastBoard(this);
                    }
                removePlayer(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void addPlayer(PlayerConnectionHandler playerConnectionHandler) throws IOException {
            playersList.add(playerConnectionHandler);
            send("Please insert your username!");
            BufferedReader inputName = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            this.name = inputName.readLine();
            playerConnectionHandler.send(Messages.WELCOME.formatted(playerConnectionHandler.getName()));
            playerConnectionHandler.send(readInstruction());
            userSocketMap.put(playerConnectionHandler.getName(),playerConnectionHandler.playerSocket);


        }

        private String readInstruction() {
            try {
                File file = new File("resources/GameRules.txt");
                Scanner scanner = new Scanner(file);

                StringBuilder stringBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine());
                    stringBuilder.append("\n");
                }
                scanner.close();
                return stringBuilder.toString();
            } catch (FileNotFoundException e) {
                System.out.println("File not found " + e.getMessage());
            }
            return null;
        }

        private void communicate() throws IOException {
            try {
                Scanner in = new Scanner(playerSocket.getInputStream());
                message = in.nextLine();
                if (isCommand(message)) {
                    dealWithCommand(message);
                }
                validatePlay();
                checkPlayerGuess();
            } catch (IOException e) {
                System.err.println(Messages.PLAYER_ERROR + e.getMessage());
            }
        }

        private void checkPlayerGuess() {
            playerGuess = new ArrayList<>(message.length());
            for (int i = 0; i < message.length(); i++) {
                playerGuess.add(parseInt(String.valueOf(message.charAt(i))));
            }
        }

        private void validatePlay() throws IOException {
            String regex = "^\\d{4}$";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(message);
            if (!matcher.find()) {
                send(Messages.INVALID_TRY);
                communicate();
            }
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
            command.getHandler().execute(Game.this, this);
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