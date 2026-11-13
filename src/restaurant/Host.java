package restaurant;

/**
 * Stage 3: the Host seats at most ONE queued customer per turn, in addition to the Waiter's task.
 * It uses the same seating rule as the Waiter (Restaurant.seatingProblem).
 */
public class Host extends Staff {

    @Override
    public String getRole() {
        return "Host";
    }

    public ActionResult seat(Restaurant r, Customer c, Table t) {
        if (hasActed()) {
            return ActionResult.fail("The Host has already seated someone this turn.");
        }
        String problem = r.seatingProblem(c, t);
        if (problem != null) {
            return ActionResult.fail(problem);
        }

        r.seatCustomer(c, t);
        markActed();
        return ActionResult.ok("Host seats Customer #" + c.getId() + " at Table " + t.getNumber() + ".");
    }
}
