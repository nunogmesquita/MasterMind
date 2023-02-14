package academy.mindswap.game.commands;

import academy.mindswap.game.Game;

public class ShoutHandler implements CommandHandler {
    @Override
    public void execute(Game game, Game.ClientConnectionHandler clientConnectionHandler) {
        String message = clientConnectionHandler.getMessage();
        String messageToSend = message.substring(6);
        game.broadcast(clientConnectionHandler.getName(), messageToSend.toUpperCase());
    }
}
