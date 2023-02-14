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
    private final List<ClientConnectionHandler> clients;


    public Game() {
        clients = new CopyOnWriteArrayList<>();
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        service = Executors.newFixedThreadPool(2);
        int numberOfConnections = 0;
        System.out.printf(Messages.SERVER_STARTED, port);

        while (true) {
            acceptConnection(numberOfConnections);
            ++numberOfConnections;
        }
    }

    public void acceptConnection(int numberOfConnections) throws IOException {
        Socket clientSocket = serverSocket.accept();
        ClientConnectionHandler clientConnectionHandler =
                new ClientConnectionHandler(clientSocket,
                        Messages.DEFAULT_NAME + numberOfConnections);
        service.submit(clientConnectionHandler);
    }

    private void addClient(ClientConnectionHandler clientConnectionHandler) {

        clients.add(clientConnectionHandler);
        clientConnectionHandler.send(Messages.WELCOME.formatted(clientConnectionHandler.getName()));
        clientConnectionHandler.send(Messages.COMMANDS_LIST);
        broadcast(clientConnectionHandler.getName(), Messages.CLIENT_ENTERED_CHAT);
    }

    public void broadcast(String name, String message) {
        clients.stream()
                .filter(handler -> !handler.getName().equals(name))
                .forEach(handler -> handler.send(name + ": " + message));
    }


    public String listClients() {
        StringBuffer buffer = new StringBuffer();
        clients.forEach(client -> buffer.append(client.getName()).append("\n"));
        return buffer.toString();
    }

    public void removeClient(ClientConnectionHandler clientConnectionHandler) {
        clients.remove(clientConnectionHandler);

    }

    public Optional<ClientConnectionHandler> getClientByName(String name) {
        return clients.stream()
                .filter(clientConnectionHandler -> clientConnectionHandler.getName().equalsIgnoreCase(name))
                .findFirst();
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
