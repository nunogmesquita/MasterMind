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
        transformGuessResult(playerGuess,turnResult);
        updateBoard();
        return this.board;
    }

    public void updateBoard() {
        String newTry =  Items.WB.c +Items.HTBAR.c.repeat(48)+ Items.R.c + "\n" +
                Items.WB.c + Items.VBAr.c + guessColor.get(0) + Items.WB.c+
                Items.VBAr.c + guessColor.get(1) +Items.WB.c+
                Items.VBAr.c + guessColor.get(2) +Items.WB.c+
                Items.VBAr.c + guessColor.get(3) + Items.WB.c +Items.VBAr.c+Items.WB.c + " [==] "+
                Items.WB.c + resultColor.get(0) + Items.WB.c+
                resultColor.get(1) + Items.WB.c+
                resultColor.get(2) + Items.WB.c+
                resultColor.get(3) + Items.R.c+  "\n" + Items.WB.c
                + Items.HBBAR.c.repeat(48)+ Items.R.c;
                board.add(newTry);
    }

    public void transformGuessResult(List<String> playerGuess, List<String> turnResult) {
        guessColor = new ArrayList<>();
        resultColor = new ArrayList<>();
        for (String letter : playerGuess) {
             switch (letter) {
                case"G" -> guessColor.add(Items.GREEN.c);
                case"B" -> guessColor.add(Items.BLUE.c);
                case"Y" -> guessColor.add(Items.YELLOW.c);
                case"O" -> guessColor.add(Items.ORANGE.c);
                case"P" -> guessColor.add(Items.PURPLE.c);
            }
        }
        for (String symb : turnResult) {
            if(symb.equals("+")) resultColor.add(Items.RED.c);
            if(symb.equals("-")) resultColor.add(Items.BLACK.c);
            if(symb.equals(" ")) resultColor.add(Items.WHITE.c);}
        }
    }
