package restaurant;

public class ServeOrderAction implements StaffAction {
    private final Order order;   // null = no dish is READY

    public ServeOrderAction(Order order) {
        this.order = order;
    }

    @Override
    public String getLabel() {
        if (order == null) {
            return "Serve a dish [not possible: no dish is READY]";
        }
        return "Serve " + order.getItem().getDisplayName() + " to Customer #" + order.getCustomer().getId();
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return r.getWaiter().serve(order, turn);
    }
}
