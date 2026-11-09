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
        if (c == null || t == null) {
            return ActionResult.fail("Nobody can be seated right now: no customer is waiting, or no table is free.");
        }
        if (!c.canBeSeated()) {
            return ActionResult.fail("Customer #" + c.getId() + " is not waiting for a table.");
        }
        if (!t.isFree()) {
            return ActionResult.fail("Table " + t.getNumber() + " is already occupied.");
        }

        r.seatCustomer(c, t);
        markActed();
        return ActionResult.ok("Waiter seats Customer #" + c.getId() + " at Table " + t.getNumber() + ".");
    }

    public ActionResult takeOrder(Restaurant r, Customer c) {
        if (hasActed()) {
            return ActionResult.fail("The Waiter has already done a task this turn.");
        }
        if (c == null) {
            return ActionResult.fail("No seated customer is waiting to order.");
        }
        if (!c.canOrder()) {
            return ActionResult.fail("Customer #" + c.getId() + " is not waiting to order.");
        }

        MenuItem item = c.chooseDish(MenuItem.values());   // the customer chooses, never the manager
        int bill = c.billFor(item);                        // locked now
        c.placeOrder(new Order(c, item, bill));
        markActed();
        return ActionResult.ok("Customer #" + c.getId() + " orders " + item.getDisplayName() + ".");
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
