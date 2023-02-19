package academy.mindswap.game.commands;

public enum Command {
    COLORS("/colors", new ColorsHandler()),
    HELP("/help", new HelpHandler()),
    SHOW_CODE("/show", new ShowHandler()),
    START_NEW_GAME("/start", new StartNewGameHandler()),
    QUIT("/quit", new QuitHandler());

    private final String description;
    private final CommandHandler handler;

    Command(String description, CommandHandler handler) {
        this.description = description;
        this.handler = handler;
    }

    public static Command getCommandFromDescription(String description) {
        for (Command command : values()) {
            if (description.equals(command.description)) {
                return command;
            }
        }
        return null;
    }

    public CommandHandler getHandler() {
        return handler;
    }
}