package restaurant;

/** The Chef advances ONE order by one preparation unit per turn. */
public class Chef extends Staff {

    @Override
    public String getRole() {
        return "Chef";
    }

    public ActionResult prepare(Order o) {
        if (hasActed()) {
            return ActionResult.fail("The Chef has already worked this turn.");
        }
        if (o == null) {
            return ActionResult.fail("There is no order in the kitchen to prepare.");
        }
        if (!o.canPrepare()) {
            return ActionResult.fail("That order cannot be prepared (status: " + o.getStatus() + ").");
        }

        boolean nowReady = o.advancePrep();
        markActed();
        String dish = o.getItem().getDisplayName();
        int id = o.getCustomer().getId();
        if (nowReady) {
            return ActionResult.ok("Chef finishes " + dish + " for Customer #" + id
                    + ": READY (it can be served next turn at the earliest).");
        }
        return ActionResult.ok("Chef prepares " + dish + " for Customer #" + id
                + " (" + o.getRemainingUnits() + " unit(s) left).");
    }
}
