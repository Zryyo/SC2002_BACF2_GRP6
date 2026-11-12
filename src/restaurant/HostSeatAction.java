package restaurant;

/** Stage 3: the manager's Host choice, in the same style as SeatCustomerAction. */
public class HostSeatAction implements StaffAction {
    private final Customer customer;
    private final Table table;

    public HostSeatAction(Customer customer, Table table) {
        this.customer = customer;
        this.table = table;
    }

    @Override
    public String getLabel() {
        return "Host seats Customer #" + customer.getId() + " at Table " + table.getNumber();
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return r.getHost().seat(r, customer, table);
    }
}
