package restaurant;

/**
 * The Waiter does ONE task per turn: seat a customer, take an order, or serve a READY order.
 * Each method checks the rules first. A failed check returns the reason and the task is not used up.
 */
public class Waiter extends Staff {

    @Override
    public String getRole() {
        return "Waiter";
    }

    public ActionResult seat(Restaurant r, Customer c, Table t) {
        if (hasActed()) {
            return ActionResult.fail("The Waiter has already done a task this turn.");
        }
        String problem = r.seatingProblem(c, t);   // Stage 3: same rule as the Host
        if (problem != null) {
            return ActionResult.fail(problem);
        }

        r.seatCustomer(c, t);
        markActed();
        return ActionResult.ok("Waiter seats Customer #" + c.getId() + " at Table " + t.getNumber() + ".");
    }

    public ActionResult takeOrder(Restaurant r, Customer c, int turn) {
        if (hasActed()) {
            return ActionResult.fail("The Waiter has already done a task this turn.");
        }
        if (c == null) {
            return ActionResult.fail("No seated customer is waiting to order.");
        }
        if (!c.canOrder()) {
            return ActionResult.fail("Customer #" + c.getId() + " is not waiting to order.");
        }

        Dish item = c.chooseDish(r.getMenu());             // the customer chooses, never the manager
        int bill = c.billFor(item);                        // customer's own discount first (VIP)...
        HappyHour happyHour = r.getHappyHour();
        bill = happyHour.applyDiscount(bill, turn);        // ...then Happy Hour. The bill is locked now.
        c.placeOrder(new Order(c, item, bill));
        markActed();
        String note = happyHour.isActive(turn) ? " (Happy Hour price)" : "";
        return ActionResult.ok("Customer #" + c.getId() + " orders " + item.getDisplayName()
                + " for " + String.format("$%d.%02d", bill / 100, bill % 100) + note + ".");
    }

    public ActionResult serve(Order o, int turn) {
        if (hasActed()) {
            return ActionResult.fail("The Waiter has already done a task this turn.");
        }
        if (o == null) {
            return ActionResult.fail("No dish is READY to serve.");
        }
        if (!o.isReady()) {
            return ActionResult.fail("That order is not READY yet (status: " + o.getStatus() + ").");
        }

        o.markServed();
        o.getCustomer().markServed(turn);
        markActed();
        return ActionResult.ok("Waiter serves " + o.getItem().getDisplayName()
                + " to Customer #" + o.getCustomer().getId() + ".");
    }
}
