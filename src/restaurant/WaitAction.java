package restaurant;

/** Choosing to do nothing this turn. Always allowed. */
public class WaitAction implements StaffAction {
    private final String role;

    public WaitAction(String role) {
        this.role = role;
    }

    @Override
    public String getLabel() {
        return "Wait (the " + role + " does nothing this turn)";
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return ActionResult.ok("The " + role + " waits.");
    }
}
