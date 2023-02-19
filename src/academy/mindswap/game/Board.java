package academy.mindswap.game;

import academy.mindswap.game.messages.Items;
import academy.mindswap.game.messages.Messages;

import java.util.ArrayList;
import java.util.List;


public class Board {
    List<String> board;
    ArrayList<String> guessColor;
    ArrayList<String> resultColor;

    public Board() {
        this.board = new ArrayList<>();
    }

    public void printBoard(Game game) {
        transformGuessResult(game);
        updateBoard();

        game.player.send(Messages.LEGEND) ;
        for (String s : board) game.player.send(s);
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

    public void transformGuessResult(Game game) {
        guessColor = new ArrayList<>();
        resultColor = new ArrayList<>();
        for (String letter : game.playerGuess) {
             switch (letter) {
                case"G" -> guessColor.add(Items.GREEN.c);
                case"B" -> guessColor.add(Items.BLUE.c);
                case"Y" -> guessColor.add(Items.YELLOW.c);
                case"O" -> guessColor.add(Items.ORANGE.c);
                case"P" -> guessColor.add(Items.PURPLE.c);
            }
        }
        for (String symb : game.turnResult) {
            if(symb.equals("+")) resultColor.add(Items.RED.c);
            if(symb.equals("-")) resultColor.add(Items.BLACK.c);
            if(symb.equals(" ")) resultColor.add(Items.WHITE.c);}
        }
    }
