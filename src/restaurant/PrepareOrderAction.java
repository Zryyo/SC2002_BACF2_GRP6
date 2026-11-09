package restaurant;

public class PrepareOrderAction implements StaffAction {
    private final Order order;   // null = nothing in the kitchen

    public PrepareOrderAction(Order order) {
        this.order = order;
    }

    @Override
    public String getLabel() {
        if (order == null) {
            return "Prepare an order [not possible: nothing is waiting in the kitchen]";
        }
        return "Prepare " + order.getItem().getDisplayName() + " for Customer #" + order.getCustomer().getId()
                + " (" + order.getRemainingUnits() + " unit(s) left)";
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return r.getChef().prepare(order);
    }
}
