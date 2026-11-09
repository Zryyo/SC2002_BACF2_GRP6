package restaurant;

/**
 * One thing the manager can ask a staff member to do this turn.
 * The Game builds a list of these, the ConsoleUI shows the labels, and the chosen one runs itself.
 * A new kind of task (Stage 3) is a new class implementing this interface.
 */
public interface StaffAction {
    String getLabel();

    ActionResult execute(Restaurant r, int turn);
}
