package academy.mindswap.game;

import java.util.ArrayList;
import java.util.List;

public class Board {

    List<String> board;

    public Board() {
        this.board = new ArrayList<>();
    }

    public void printBoard(Game game) {
        updateBoard(game);
        for (String s : board) game.player.send(s);
    }

    public void updateBoard(Game game) {
        String newTry = "_______________________ \n" +
                "|  " + game.playerGuess.get(0).toString() +
                "  |  " + game.playerGuess.get(1).toString() +
                "  |  " + game.playerGuess.get(2).toString() +
                "  |  " + game.playerGuess.get(3).toString() + "  |  " + " [==] "
                + game.turnResult.get(0) +
                game.turnResult.get(1) +
                game.turnResult.get(2) +
                game.turnResult.get(3) + "\n_______________________     ____";
        board.add(newTry);
    }
}