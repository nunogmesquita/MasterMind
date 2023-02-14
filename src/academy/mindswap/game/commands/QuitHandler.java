package academy.mindswap.game.commands;

import academy.mindswap.game.Game;
import academy.mindswap.game.messages.Messages;

public class QuitHandler implements CommandHandler {

    @Override
    public void execute(Game game, Game.ClientConnectionHandler clientConnectionHandler) {
        game.removeClient(clientConnectionHandler);
        game.broadcast(clientConnectionHandler.getName(), clientConnectionHandler.getName() + Messages.CLIENT_DISCONNECTED);
        clientConnectionHandler.close();
    }
}
