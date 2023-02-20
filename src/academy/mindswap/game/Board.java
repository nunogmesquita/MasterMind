package academy.mindswap.game;

import academy.mindswap.game.messages.Items;

import java.util.ArrayList;
import java.util.List;

public class Board {

    List<String> board;

    ArrayList<String> guessColor;

    ArrayList<String> resultColor;

    public Board() {
        this.board = new ArrayList<>();
    }

    public List<String> updatedBoard(List<String> playerGuess, List<String> turnResult) {
        transformGuessResult(playerGuess, turnResult);
        updateBoard();
        return this.board;
    }

    public void updateBoard() {
        String newTry = Items.WB.color + Items.HTBAR.color.repeat(48) + Items.R.color + "\n" +
                Items.WB.color + Items.VBAr.color + guessColor.get(0) + Items.WB.color +
                Items.VBAr.color + guessColor.get(1) + Items.WB.color +
                Items.VBAr.color + guessColor.get(2) + Items.WB.color +
                Items.VBAr.color + guessColor.get(3) + Items.WB.color + Items.VBAr.color + Items.WB.color + " [==] " +
                Items.WB.color + resultColor.get(0) + Items.WB.color +
                resultColor.get(1) + Items.WB.color +
                resultColor.get(2) + Items.WB.color +
                resultColor.get(3) + Items.R.color + "\n" + Items.WB.color
                + Items.HBBAR.color.repeat(48) + Items.R.color;
        board.add(newTry);
    }

    public void transformGuessResult(List<String> playerGuess, List<String> turnResult) {
        guessColor = new ArrayList<>();
        resultColor = new ArrayList<>();
        for (String letter : playerGuess) {
            switch (letter) {
                case "G" -> guessColor.add(Items.GREEN.color);
                case "B" -> guessColor.add(Items.BLUE.color);
                case "Y" -> guessColor.add(Items.YELLOW.color);
                case "O" -> guessColor.add(Items.ORANGE.color);
                case "P" -> guessColor.add(Items.PURPLE.color);
            }
        }
        for (String symbol : turnResult) {
            if (symbol.equals("+")) resultColor.add(Items.RED.color);
            if (symbol.equals("-")) resultColor.add(Items.BLACK.color);
            if (symbol.equals(" ")) resultColor.add(Items.WHITE.color);
        }
    }
}
