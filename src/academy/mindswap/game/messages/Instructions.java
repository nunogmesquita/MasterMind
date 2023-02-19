package academy.mindswap.game.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Instructions {

    public static String readInstruction() {
        try {
            File file = new File("resources/GameRules.txt");
            Scanner scanner = new Scanner(file);

            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine() + "\n");
            }
            scanner.close();
            return stringBuilder.toString().concat(Messages.RESULT_RULES + "\n" + Messages.LEGEND);
        } catch (FileNotFoundException e) {
            System.out.println(Messages.INVALID_FILE + e.getMessage());
        }
        return null;
    }
}