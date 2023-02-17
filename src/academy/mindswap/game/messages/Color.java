package academy.mindswap.game.messages;



public enum Color {
     White("\u001B[37m☗\u001B[0m","Correct Color Wrong Possition"),
     RED("\u001B[31m☗\u001B[0m","Correct Color and Position"),
     BLACK("\u001B[30m☗\u001B[0m","Wrong Color"),
     PURPLE("\u001B[35m⬤\u001B[0m","P"),
     YELLOW("\u001B[33m⬤\u001B[0m","Y"),
     GREEN("\u001B[32m⬤\u001B[0m","G"),
     BLUE("\u001B[34m⬤\u001B[0m","B"),
     ORANGE("\u001B[38;2;255;165;0m⬤\u001B[0m","O");

     public String c;
     public String letter;
     Color(String c,String letter) {
         this.c = c;
         this.letter = letter;
     }

     public String getC() {
          return c;
     }

     @Override
     public String toString() {
          return letter + " -->  ".concat(c);
     }
}
