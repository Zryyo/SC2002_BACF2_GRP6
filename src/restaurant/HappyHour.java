package restaurant;

/**
 * Stage 3 pricing policy. The manager may switch it on once per game, at the start of a turn.
 * It lasts that turn and the next. While active: orders taken get 20% off (after any customer discount),
 * and customers still waiting for a table or food recover 15 satisfaction at turn start (max 100).
 */
public class HappyHour {
    private boolean used;
    private int startTurn = -1;

    public boolean hasBeenUsed() {
        return used;
    }

    public ActionResult activate(int turn) {
        if (used) {
            return ActionResult.fail("Happy Hour has already been used this game.");
        }
        used = true;
        startTurn = turn;
        return ActionResult.ok("Happy Hour is ON for this turn and the next: 20% off orders taken, "
                + "and waiting customers recover " + GameConfig.HAPPY_HOUR_RECOVERY + " satisfaction at turn start.");
    }

    public boolean isActive(int turn) {
        return used && turn >= startTurn && turn < startTurn + GameConfig.HAPPY_HOUR_TURNS;
    }

    /** Turns left including the current one: 2 on the first turn, 1 on the second, 0 when off. */
    public int remainingTurns(int turn) {
        return isActive(turn) ? startTurn + GameConfig.HAPPY_HOUR_TURNS - turn : 0;
    }

    /** Takes 20% off a bill if Happy Hour is active this turn, rounded half up to a whole cent. */
    public int applyDiscount(int billCents, int turn) {
        if (!isActive(turn)) {
            return billCents;
        }
        return (billCents * (100 - GameConfig.HAPPY_HOUR_DISCOUNT_PERCENT) + 50) / 100;
    }

    /** Customers still waiting for a table or for food recover satisfaction. Customers who left are not affected. */
    public void recoverSatisfaction(Restaurant r) {
        for (Customer c : r.getCustomers()) {
            if (c.isAwaitingService()) {
                c.recoverSatisfaction(GameConfig.HAPPY_HOUR_RECOVERY);
            }
        }
    }
}
