package restaurant;

/**
 * The Cashier works automatically: at most ONE payment per turn, oldest READY_TO_PAY customer first.
 * Anyone who became READY_TO_PAY this turn is charged next turn at the earliest.
 */
public class Cashier extends Staff {

    @Override
    public String getRole() {
        return "Cashier";
    }

    public ActionResult collectPayment(Restaurant r, int turn) {
        if (hasActed()) {
            return ActionResult.fail("The Cashier has already taken a payment this turn.");
        }

        Customer next = null;
        for (Customer c : r.getReadyToPayCustomers()) {
            if (c.canPay(turn) && (next == null || c.getReadyToPayTurn() < next.getReadyToPayTurn())) {
                next = c;
            }
        }
        if (next == null) {
            return ActionResult.fail("Nobody can be charged this turn.");
        }

        Order order = next.getOrder();
        order.markPaid();
        next.markPaid();
        next.getTable().release();
        r.getLedger().recordPayment(order.getBillCents(), next.getSatisfaction());
        markActed();
        return ActionResult.ok("Cashier collects payment from Customer #" + next.getId()
                + ". Table " + next.getTable().getNumber() + " is free.");
    }
}
