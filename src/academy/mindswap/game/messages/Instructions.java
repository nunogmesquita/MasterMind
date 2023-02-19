package academy.mindswap.game.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Instructions {

    public static String readInstruction() {

        try {

//            for (Items color: Items.values()) {
//                System.out.println(color.toString());
//
//            }
            File file = new File("resources/GameRules.txt");
            Scanner scanner = new Scanner(file);

            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
                stringBuilder.append("\n");
            }
            scanner.close();
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            System.out.println(Messages.INVALID_FILE + e.getMessage());
        }
        return null;
    }

}