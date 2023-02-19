package academy.mindswap.game.messages;

public abstract class Messages {
    public static final String GAME_STARTED = "NEW GAME: MASTERMIND\n";
    public static final String WELCOME = "Welcome to Mastermind %s!";
    public static final String NO_SUCH_COMMAND = "⚠️ Invalid command!";
    public static final String COMMANDS_LIST = """
            List of available commands:
            /list -> gets you the list of connected players
            /quit -> exits the server
            /start -> starts a new game
            /show -> shows the secret code and exits the server""";

    public static final String PLAYER_ERROR = "Something went wrong with this player's connection. Error: ";
    public static final String SERVER_ERROR = "Something went wrong with the server. Connection closing...";
    public static final String ASK_NAME = "Please insert your username:";
    public static final String INVALID_TRY = "Please insert a valid 4 letter code.";
    public static final String INSERT_TRY = "Please insert a 4 letter code.";
    public static final String WAITING_ALL_PLAYERS = "Waiting for %s player(s) to begin the game";
    public static final String RIGHT_GUESS = "CONGRATULATIONS! You have found the secret code in %s attempts!";
    public static final String PLAYER_QUIT = " has quitted the game. Do you wish to continue?";
    public static final String CONNECTION_CLOSED = "GAME OVER";
    public static final String INVALID_FILE = "File not found ";
    public static final String INVALID_NAME = "we already have a player with that name, please choose a new one!";
    public static final String SHOW_CODE = "Here is your secret code %s";
    public static final String QUIT_OR_NEW_GAME = "Type /quit for exit or /start for a new game.";
    public static final String INVALID_FIRST_NAME = "The name must have al least one character.";
}