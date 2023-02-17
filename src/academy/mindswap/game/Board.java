package academy.mindswap.game;

import academy.mindswap.game.messages.Color;

import java.util.ArrayList;
import java.util.List;

public class Board {
    List<String> board;

   private static final String  WB ="\u001B[47m)";
    private static final String R = "\u001B[0m";
    ArrayList<Color> guessColor;
    ArrayList<Color> resultColor;

    public Board() {
        this.board = new ArrayList<>();
    }

    public void printBoard(Game game) {
        tansformGuessResult(game);
        updateBoard();
        for (Color color: Color.values()) {
                game.player.send(color.toString());
        }
        for (String s : board) game.player.send(s);
    }

    public void updateBoard() {
        String newTry = WB +"_______________________"+ R + "\n" +
                  WB +"|  " + guessColor.get(0) +
                "  |  " + guessColor.get(1) +
                "  |  " + guessColor.get(2) +
                "  |  " + guessColor.get(3) + "  |  " + " [==] "
         + resultColor.get(0) +
                resultColor.get(1) +
                resultColor.get(2) +
                resultColor.get(3) + R +  "\n" + WB + "_______________________     ____" + R;
        board.add(newTry);
    }

    public void tansformGuessResult (Game game) {
        guessColor = new ArrayList<>();
        resultColor = new ArrayList<>();
        for (String letter : game.playerGuess) {
             switch (letter) {
                case"G" -> guessColor.add(Color.GREEN);
                case"B" -> guessColor.add(Color.BLUE);
                case"Y" -> guessColor.add(Color.YELLOW);
                case"O" -> guessColor.add(Color.ORANGE);
                case"P" -> guessColor.add(Color.PURPLE);
            }
        }
        for (String symb : game.turnResult) {
            if(symb.equals("+")) resultColor.add(Color.RED);
            if(symb.equals("-")) resultColor.add(Color.BLACK);
            resultColor.add(Color.White);}
        }
    }
