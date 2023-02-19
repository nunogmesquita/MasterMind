package academy.mindswap.game.commands;

import academy.mindswap.game.Server;

public class StartNewGameHandler implements CommandHandler {
    @Override
    public void execute(Server server, Server.ConnectedPlayer connectedPlayer) {
            connectedPlayer.startNewGame();
    }
}