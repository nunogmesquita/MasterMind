package academy.mindswap.game;

import java.util.*;

public class Code {

    static ArrayList<String> generateCode() {
        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> possibleChoices = new ArrayList<>(Arrays.asList("G","B","Y","O","P"));
        for (int i = 0; i < 4 ; i++) {
            code.add(possibleChoices.get(new Random().nextInt(5)));
        }
        return code;
    }

    static boolean rightGuess(Game game, List<String> playerGuess) {
        if (game.secretCode.equals(playerGuess)) {
            for (int i = 0; i <playerGuess.size() ; i++) {
                game.turnResult.set(i,"+");
            }
            game.rightGuess = true;
            return true;
        }
        return false;
    }

    static void compareCodes(Game game, List<String> playerGuess, List<String> secretCode) {
        if (!rightGuess(game, playerGuess)) {
            game.turnResult = new ArrayList<>();
            List<String> playerGuessCopy = new ArrayList<>(playerGuess);
            List<String> secretCodeCopy = new ArrayList<>(secretCode);
            for (int i = 0; i < playerGuess.size(); i++) {
                if (playerGuessCopy.get(i).equals(secretCodeCopy.get(i)) && playerGuessCopy.get(i) != null) {
                    game.turnResult.add("+");
                    playerGuessCopy.set(i, null);
                    secretCodeCopy.set(i, null);
                }
            }
             long num = playerGuessCopy.stream().filter(secretCodeCopy::contains).filter(Objects::nonNull).count();
             for (int i = 0; i < num; i++) {
                 game.turnResult.add("-");
             }
            while (game.turnResult.size() != playerGuess.size()) {
                game.turnResult.add(" ");
            }
            secretCodeCopy.clear();
            playerGuessCopy.clear();
        }
    }
}
