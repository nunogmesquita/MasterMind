package academy.mindswap.game.commands;

import academy.mindswap.game.Game;
import academy.mindswap.game.messages.Messages;

public class HelpHandler implements CommandHandler {

    @Override
    public void execute(Game game, Game.PlayerConnectionHandler playerConnectionHandler) {
        playerConnectionHandler.send(Messages.COMMANDS_LIST);
    }
}