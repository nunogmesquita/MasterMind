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
    private final HashMap<PlayerConnectionHandler, ArrayList<String>> userBoardMap;
    List<String> turnResult;


    public Game(int numOfPlayers) {
        playersList = new CopyOnWriteArrayList<>();
        userSocketMap = new HashMap<>();
        userBoardMap = new HashMap<>();
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

    public void broadcastBoard(PlayerConnectionHandler playerConnectionHandler) {
        updateBoard(playerConnectionHandler);
        ArrayList<String> boardToSend = null;
        for (PlayerConnectionHandler newPlayer : userBoardMap.keySet()) {
            if (newPlayer.getName().equals(playerConnectionHandler.getName())) {
                boardToSend = userBoardMap.get(playerConnectionHandler.getName());
            }
        }
        if (boardToSend != null) {
            for (int i = 0; i < boardToSend.size(); i++) {
                playerConnectionHandler.send(boardToSend.get(i));
            }
        }
    }

    public void updateBoard(PlayerConnectionHandler player) {
        String newTry = (" _______________________ \n" + "|  " + player.playerGuess.get(0) + "  |  " + player.playerGuess.get(0) + "  |  " + player.playerGuess.get(0) + "  |  " + player.playerGuess.get(0) + "  |  " + " [==] "
                + turnResult.get(0) + turnResult.get(1) + turnResult.get(2) + turnResult.get(3) + "\n_______________________    ____");
        ArrayList<String> boardNewCopy;
        for (PlayerConnectionHandler playerConnectionHandler : userBoardMap.keySet()) {
            if (player.getName().equals(playerConnectionHandler.getName())) {
                boardNewCopy = userBoardMap.get(playerConnectionHandler.getName());
                boardNewCopy.add(newTry);
                userBoardMap.put(playerConnectionHandler, boardNewCopy);
            }
        }
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
        for (int i = 0; i < playerGuess.size(); i++) {
            if (playerGuess.get(i).equals(secretCode.get(i))) {
                turnResult.add("+");
                playerGuess.remove(i);
                secretCode.remove(i);
            }
        }
        for (int i = 0; i < playerGuess.size(); i++) {
            if (secretCode.contains(playerGuess.get(i))) {
                turnResult.add("-");
                playerGuess.remove(i);
                secretCode.remove(i);
            }
        }
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

        public PlayerConnectionHandler(Socket playerSocket) throws IOException {
            this.playerSocket = playerSocket;
            this.out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        }

        @Override
        public void run() {
            try {
                addPlayer(this);
                send(Messages.INSERT_TRY);
                generateCode();
                while (!win) {
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
            userSocketMap.put(playerConnectionHandler.getName(), this.playerSocket);
            send("Please insert your username!");
            BufferedReader inputName = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            this.name = inputName.readLine();
            playerConnectionHandler.send(Messages.WELCOME.formatted(playerConnectionHandler.getName()));
            playerConnectionHandler.send(waitForNewPlayer());
            playerConnectionHandler.send(readInstruction());
        }

        private String readInstruction() {
            try {
                File file = new File("/Users/nunogilmesquita/Documents/mindswap_2023/MasterMind/src/academy/mindswap/game/commands/GameRules.txt");
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
        private String waitForNewPlayer() {

            while(playersList.size() < numOfPlayers){
                System.out.println("Waiting for " + (numOfPlayers - playersList.size()) + " more player(s) to join...");
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "All players have joined. Starting the game...";
        }

        private void communicate() throws IOException {
            try {
                Scanner in = new Scanner(playerSocket.getInputStream());
                message = in.nextLine();
                if (isCommand(message)) {
                    dealWithCommand(message);
                }
                validatePlay();
                playerGuess();
            } catch (IOException e) {
                System.err.println(Messages.PLAYER_ERROR + e.getMessage());
            }
        }

        private void playerGuess() {
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