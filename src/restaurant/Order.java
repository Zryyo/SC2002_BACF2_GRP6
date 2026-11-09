package restaurant;

/** One customer's order. It owns its own preparation progress and refuses illegal status changes. */
public class Order {

    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }

    private final Customer customer;
    private final MenuItem item;
    private final int billCents;      // locked when the order is taken
    private int unitsDone;
    private Status status = Status.PREPARING;

    public Order(Customer customer, MenuItem item, int billCents) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
    }

    /** True while the Chef can still work on this order. */
    public boolean canPrepare() {
        return status == Status.PREPARING;
    }

    /** Does one preparation unit. Returns true if that was the last unit (the order is now READY). */
    public boolean advancePrep() {
        if (!canPrepare()) {
            throw new IllegalStateException("Order cannot be prepared in status " + status);
        }
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = Status.READY;
            return true;
        }
        return false;
    }

    public boolean isReady() {
        return status == Status.READY;
    }

    public void markServed() {
        if (status != Status.READY) {
            throw new IllegalStateException("Only a READY order can be served.");
        }
        status = Status.SERVED;
    }

    public void markPaid() {
        if (status != Status.SERVED) {
            throw new IllegalStateException("Only a SERVED order can be paid.");
        }
        status = Status.PAID;
    }

    public void cancel() {
        status = Status.CANCELLED;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public Status getStatus() {
        return status;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getRemainingUnits() {
        return item.getPrepUnits() - unitsDone;
    }
}
