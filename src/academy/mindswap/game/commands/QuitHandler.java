package academy.mindswap.game.commands;

import academy.mindswap.game.Server;

public class QuitHandler implements CommandHandler {

    @Override
    public void execute(Server server, Server.ConnectedPlayer connectedPlayer) {
        server.removePlayer(connectedPlayer);
        connectedPlayer.close();
    }
}
