package academy.mindswap.game.commands;

import academy.mindswap.game.Server;
import academy.mindswap.game.messages.Messages;

public class ColorsHandler implements CommandHandler {

    @Override
    public void execute(Server server, Server.ConnectedPlayer connectedPlayer) {
        connectedPlayer.send(Messages.LEGEND);
    }
}