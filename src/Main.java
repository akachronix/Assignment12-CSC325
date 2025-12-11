import game.Game;

// Entry point
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Supermarket Simulator...");
        new Game(args); // pass CLI args through to Game
    }
}
