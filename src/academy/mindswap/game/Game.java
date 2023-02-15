package academy.mindswap.game;
import academy.mindswap.game.commands.Command;
import academy.mindswap.game.messages.Messages;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Game {


    private ServerSocket serverSocket;
    private ExecutorService service;
    private final List<ClientConnectionHandler> playersList;
    private BufferedWriter outputName;


    public Game() {
        playersList = new CopyOnWriteArrayList<>();
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        service = Executors.newFixedThreadPool(2);
        System.out.printf(Messages.SERVER_STARTED, port);

        while (true) {
            acceptConnection();
        }
    }

    public void acceptConnection() throws IOException {
        Socket clientSocket = serverSocket.accept();
        outputName = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        outputName.write("Please insert your name!");
        outputName.newLine();
        outputName.flush();
        BufferedReader inputName = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String username = inputName.readLine();
        ClientConnectionHandler clientConnectionHandler =
                new ClientConnectionHandler(clientSocket,username);
        service.submit(clientConnectionHandler);
    }

    private void addClient(ClientConnectionHandler clientConnectionHandler) {

        playersList.add(clientConnectionHandler);
        clientConnectionHandler.send(Messages.WELCOME.formatted(clientConnectionHandler.getName()));
       // clientConnectionHandler.send(Messages.COMMANDS_LIST);
       // broadcast(clientConnectionHandler.getName(), Messages.CLIENT_ENTERED_CHAT);
    }

    public void broadcast(String name, String message) {
        playersList.stream()
                .filter(handler -> !handler.getName().equals(name))
                .forEach(handler -> handler.send(name + ": " + message));
    }


  //  public String listClients() {
  //      StringBuffer buffer = new StringBuffer();
  //      playersList.forEach(client -> buffer.append(client.getName()).append("\n"));
  //      return buffer.toString();
  //  }

    public void removeClient(ClientConnectionHandler clientConnectionHandler) {
        playersList.remove(clientConnectionHandler);

    }

    public Optional<ClientConnectionHandler> getClientByName(String name) {
        return playersList.stream()
                .filter(clientConnectionHandler -> clientConnectionHandler.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    private List<Integer> generateCode() {
        Random random = new Random();
        List<Integer> secretCode = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(5);
            secretCode.add(digit);
        }
        return secretCode;
    }

    private boolean compareLists(List<Integer> list1, List<Integer> list2) {
        if (list1.size() != list2.size()) {
            System.out.println("We are playing with a four digit code");
            return false;// trocar para validação de tudo
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    private int [] compareCodes (List<Integer> code1,List<Integer>code2){
        int numCorrectDigits = 0;
        int numCorrectPositions; //equals com streams e fazer primeiro

        for (int i = 0; i < code1.size(); i++) {
            int digit = code1.get(i);
            if(code2.contains(digit)){
                numCorrectDigits ++;
            }

        }int [] result ={numCorrectDigits,numCorrectPositions};
        return result;
    }


    public class ClientConnectionHandler implements Runnable {

        private String name;
        private final Socket clientSocket;
        private final BufferedWriter out;
        private String message;

        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.clientSocket = clientSocket;
            this.name = name;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        @Override
        public void run() {
            addClient(this);
            try {
                Scanner in = new Scanner(clientSocket.getInputStream());
                while (in.hasNext()) {
                    message = in.nextLine();
                    if (isCommand(message)) {
                        dealWithCommand(message);
                        continue;
                    }
                    if (message.equals("")) {
                        continue;
                    }

                    broadcast(name, message);
                }
            } catch (IOException e) {
                System.err.println(Messages.CLIENT_ERROR + e.getMessage());
            } finally {
                removeClient(this);
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

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }
    }
}
