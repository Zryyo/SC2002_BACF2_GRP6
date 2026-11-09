package restaurant;

/**
 * Outcome of a staff action. A failed result carries the reason shown to the manager
 * and does NOT use up the staff member's task for the turn.
 */
public class ActionResult {
    private final boolean success;
    private final String message;

    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResult ok(String msg) {
        return new ActionResult(true, msg);
    }

    public static ActionResult fail(String reason) {
        return new ActionResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
