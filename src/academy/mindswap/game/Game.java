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
    private BufferedWriter outputName;
    private final List<ClientConnectionHandler> playersList;
    List<Integer> secretCode;
    boolean win = false;

    public Game(int numOfPlayers) {
        playersList = new CopyOnWriteArrayList<>();
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
        Socket clientSocket = serverSocket.accept();
        ClientConnectionHandler clientConnectionHandler =
                new ClientConnectionHandler(clientSocket);
        service.submit(clientConnectionHandler);
    }


    public void broadcast(String name, String message) {
        playersList.stream()
                .filter(handler -> !handler.getName().equals(name))
                .forEach(handler -> handler.send(name + ": " + message));
    }

    public void removeClient(ClientConnectionHandler clientConnectionHandler) {
        playersList.remove(clientConnectionHandler);
    }

    private void generateCode() {
        Random random = new Random();
        secretCode = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(5);
            secretCode.add(digit);
        }
    }

    private List<String> compareCodes(List<Integer> playerGuess, List<Integer> secretCode) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < playerGuess.size(); i++) {
            if (playerGuess.get(i).equals(secretCode.get(i))) {
                result.add("+");
                playerGuess.remove(i);
                secretCode.remove(i);
            }
        }
        for (int i = 0; i < playerGuess.size(); i++) {
            if (secretCode.contains(playerGuess.get(i))) {
                result.add("-");
                playerGuess.remove(i);
                secretCode.remove(i);
            }
        }

        return result;
    }

    private void checkWinner(List<Integer> playerGuess) {
        if (secretCode.equals(playerGuess)) {
            win = true;
        }
    }

    public class ClientConnectionHandler implements Runnable {

        private String name;
        private final Socket clientSocket;
        private final BufferedWriter out;
        private String message;
        List<Integer> playerGuess;

        public ClientConnectionHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        @Override
        public void run() {
            try {
                addClient(this);
                send(Messages.INSERT_TRY);
                generateCode();
                while (!win) {
                    System.out.println(secretCode);
                    communicate();
                    checkWinner(playerGuess);
                    send(compareCodes(playerGuess, secretCode).toString());
                }
                removeClient(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void addClient(ClientConnectionHandler clientConnectionHandler) throws IOException {
            playersList.add(clientConnectionHandler);
            outputName = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            outputName.write("Please insert your username!");
            outputName.newLine();
            outputName.flush();
            BufferedReader inputName = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.name = inputName.readLine();
            clientConnectionHandler.send(Messages.WELCOME.formatted(clientConnectionHandler.getName()));
            clientConnectionHandler.send(readInstruction());
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

        private void communicate() throws IOException {
            try {
                Scanner in = new Scanner(clientSocket.getInputStream());
                message = in.nextLine();
                if (isCommand(message)) {
                    dealWithCommand(message);
                }
                validatePlay();
                playerGuess();
            } catch (IOException e) {
                System.err.println(Messages.CLIENT_ERROR + e.getMessage());
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
                removeClient(this);
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

    }
}