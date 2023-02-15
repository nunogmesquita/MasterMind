package academy.mindswap.game.commands;

import academy.mindswap.game.Game;

public class QuitHandler implements CommandHandler {

    @Override
    public void execute(Game game, Game.PlayerConnectionHandler playerConnectionHandler) {
        game.removePlayer(playerConnectionHandler);
        game.broadcast(playerConnectionHandler.getName(), playerConnectionHandler.getName() + " has left.");
        playerConnectionHandler.close();
    }
}
