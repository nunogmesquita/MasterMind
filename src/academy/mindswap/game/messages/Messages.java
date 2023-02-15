package academy.mindswap.game.messages;

public abstract class Messages {
    public static final String GAME_STARTED = "MASTERMIND";
    public static final String WELCOME = "Welcome to Mastermind %s!";
    public static final String NO_SUCH_COMMAND = "⚠️ Invalid command!";
    public static final String COMMANDS_LIST = """
            List of available commands:
            /list -> gets you the list of connected clients
            /shout <message> -> lets you shout a message to all connected clients
            /whisper <username> <message> -> lets you whisper a message to a single connected player
            /name <new name> -> lets you change your name
            /quit -> exits the game""";
    public static final String CLIENT_ERROR = "Something went wrong with this player's connection. Error: ";

    public static final String INVALID_TRY = "Please insert a valid 4 digits number.";
    public static final String INSERT_TRY = "Please insert a 4 digit code.";
}