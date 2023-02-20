package academy.mindswap.game.commands;


public enum Command {
    HELP("/HELP", new HelpHandler()),
    QUIT("/QUITt", new QuitHandler()),
    START("/START", new StartNewGameHandler()),
    SHOW("/SHOW", new ShowHandler());

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