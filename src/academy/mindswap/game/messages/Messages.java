package academy.mindswap.game.messages;

public abstract class Messages {
    public static final String GAME_STARTED = "NEW GAME: MASTERMIND\n";
    public static final String WELCOME = "Welcome to Mastermind %s!";
    public static final String NO_SUCH_COMMAND = "⚠️ Invalid command!";
    public static final String COMMANDS_LIST = """
            List of available commands:
            /list -> gets you the list of connected clients
            /quit -> exits the server""";
    public static final String PLAYER_ERROR = "Something went wrong with this player's connection. Error: ";
    public static final String INVALID_TRY = "Please insert a valid 4 digits number.";
    public static final String INSERT_TRY = "Please insert a 4 digit code.";
    public static final String WAITING_ALL_PLAYERS = "Waiting for %s player(s) to begin the game";
    public static final String RIGHT_GUESS = "CONGRATULATIONS! You have found the secret code in %s attempts!";
}