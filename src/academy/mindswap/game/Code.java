package academy.mindswap.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Code {

    static ArrayList<Integer> generateCode() {
        ArrayList<Integer> code = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int digit = new Random().nextInt(5);
            code.add(digit);
        }
        return code;
    }

    static boolean rightGuess(Game game, List<Integer> playerGuess) {
        if (game.secretCode.equals(playerGuess)) {
            game.rightGuess = true;
            return true;
        }
        return false;
    }

    static void compareCodes(Game game, List<Integer> playerGuess, List<Integer> secretCode) {
        while (!rightGuess(game, playerGuess)) {
            game.turnResult = new ArrayList<>();
            List<Integer> playerGuessCopy = new ArrayList<>(playerGuess);
            List<Integer> secretCodeCopy = new ArrayList<>(secretCode);
            for (int i = 0; i < playerGuess.size(); i++) {
                if (playerGuessCopy.get(i).equals(secretCodeCopy.get(i)) && playerGuessCopy.get(i) != null) {
                    game.turnResult.add("+");
                    playerGuessCopy.set(i, null);
                    secretCodeCopy.set(i, null);
                }
            }

            for (int i = 0; i < playerGuess.size(); i++) {
                if (secretCodeCopy.contains(playerGuessCopy.get(i)) && playerGuessCopy.get(i) != null) {
                    game.turnResult.add("-");
                    playerGuessCopy.set(i, null);
                    secretCodeCopy.set(i, null);
                }
            }
            while (game.turnResult.size() != playerGuess.size()) {
                game.turnResult.add(" ");
            }
            playerGuessCopy.clear();
            secretCodeCopy.clear();
        }
    }
}
