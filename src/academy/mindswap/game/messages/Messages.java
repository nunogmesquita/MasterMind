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
    public static final String PLAYER_QUIT = " has quitted the game. Do you wish to continue?";
    public static final String CONNECTION_CLOSED = "GAME OVER";
    public static final String GAME_MODE = " \n" + "\033[1m  What kind of game do you prefer ? \033[0m\n" + " \033[1m ⒈ ⇨ SOLO  \033[0m \n" + " \033[1m ⒉ ⇨ 2 PLAYERS  \033[0m";
    public static final String OPPONENT_CODE = "Please insert a code for your Opponent!";
    public static final String LEGEND = Items.PURPLE.descr + Items.PURPLE.c + Items.ORANGE.descr + Items.ORANGE.c +"\n"
            + Items.BLUE.descr + Items.BLUE.c + Items.GREEN.descr + Items.GREEN.c +"\n" + Items.YELLOW.descr + Items.YELLOW.c;

    public static final String RESULT_RULES = Items.RED.c + "  Corresponds a correct number in the correct position.\n" +
                                             Items.BLACK.c +  "  Corresponds a correct number, but in the wrong position.\n";
}