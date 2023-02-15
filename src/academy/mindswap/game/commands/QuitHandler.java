package academy.mindswap.game.commands;

import academy.mindswap.game.Game;

public class QuitHandler implements CommandHandler {

    @Override
    public void execute(Game game, Game.ClientConnectionHandler clientConnectionHandler) {
        game.removeClient(clientConnectionHandler);
        game.broadcast(clientConnectionHandler.getName(), clientConnectionHandler.getName() + " has left.");
        clientConnectionHandler.close();
    }
}
