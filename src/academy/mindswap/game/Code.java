package academy.mindswap.game;

import academy.mindswap.game.messages.Messages;

import java.util.*;

public class Code {

    static ArrayList<String> generateCode() {
        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> possibleChoices = new ArrayList<>(Arrays.asList("G", "B", "Y", "O", "P"));
        for (int i = 0; i < 4; i++) {
            code.add(possibleChoices.get(new Random().nextInt(5)));
        }
        return code;
    }

    static List<String> compareCodes(List<String> playerGuess, List<String> secretCode) {
        List<String> compareResults = new ArrayList<>();
        List<String> playerGuessCopy = new ArrayList<>(playerGuess);
        List<String> secretCodeCopy = new ArrayList<>(secretCode);
        for (int i = 0; i < playerGuess.size(); i++) {
            if (playerGuessCopy.get(i).equals(secretCodeCopy.get(i)) && playerGuessCopy.get(i) != null) {
                compareResults.add("+");
                playerGuessCopy.set(i, null);
                secretCodeCopy.set(i, null);
            }
        }

        for (int i = 0; i < playerGuessCopy.stream()
                .filter(secretCodeCopy::contains)
                .filter(Objects::nonNull).count(); i++) {
            compareResults.add("-");
        }
        while (compareResults.size() != playerGuess.size()) {
            compareResults.add(" ");
        }
        playerGuessCopy.clear();
        secretCodeCopy.clear();
        return compareResults;
    }

    public static void showCode(Server.ConnectedPlayer player) {
        player.send(Messages.SHOW_CODE.formatted(player.game.secretCode));
    }
}