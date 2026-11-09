package restaurant;

import java.util.NoSuchElementException;

/** Entry point: builds the screen and the game, then starts it. */
public class RestaurantRushApp {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        Game game = new Game(ui);
        try {
            game.run();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ended before the game finished. Game stopped.");
        }
    }
}
