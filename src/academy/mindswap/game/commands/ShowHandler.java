package academy.mindswap.game.commands;

import academy.mindswap.game.Code;
import academy.mindswap.game.Server;
import academy.mindswap.game.messages.Messages;

public class ShowHandler implements CommandHandler {

    @Override
    public void execute(Server server, Server.ConnectedPlayer connectedPlayer) {
        Code.showCode(connectedPlayer);
        connectedPlayer.send(Messages.CONNECTION_CLOSED);
        connectedPlayer.close();
    }
}