package academy.mindswap.game.commands;

import academy.mindswap.game.Game;

public class ListHandler implements CommandHandler {

    @Override
    public void execute(Game game, Game.ClientConnectionHandler clientConnectionHandler) {
       clientConnectionHandler.send(game.listClients());
    }
}
