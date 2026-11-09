package restaurant;

public class TakeOrderAction implements StaffAction {
    private final Customer customer;   // null = nobody is waiting to order

    public TakeOrderAction(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String getLabel() {
        if (customer == null) {
            return "Take an order [not possible: nobody is seated and waiting to order]";
        }
        return "Take the order of Customer #" + customer.getId();
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return r.getWaiter().takeOrder(r, customer);
    }
}
