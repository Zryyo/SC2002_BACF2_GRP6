package restaurant;

public class SeatCustomerAction implements StaffAction {
    private final Customer customer;   // null = nobody can be seated right now
    private final Table table;

    public SeatCustomerAction(Customer customer, Table table) {
        this.customer = customer;
        this.table = table;
    }

    @Override
    public String getLabel() {
        if (customer == null || table == null) {
            return "Seat a customer [not possible: nobody is waiting, or no table is free]";
        }
        return "Seat Customer #" + customer.getId() + " at Table " + table.getNumber();
    }

    @Override
    public ActionResult execute(Restaurant r, int turn) {
        return r.getWaiter().seat(r, customer, table);
    }
}
