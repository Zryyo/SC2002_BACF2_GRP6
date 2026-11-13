package restaurant;

import java.util.List;

/**
 * Base class for all customers. Behaviour that differs by customer type (waiting loss, bill,
 * food choice) sits in methods that subclasses can override, so the game never needs to check
 * what type a customer is.
 */
public abstract class Customer {

    public enum Status { WAITING, SEATED, ORDERED, SERVED, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private int satisfaction = GameConfig.START_SATISFACTION;
    private Status status = Status.WAITING;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

    protected Customer(int id) {
        this.id = id;
    }

    /** Points lost at the end of every turn in which this customer is still waiting for service. */
    protected abstract int waitLossPerTurn();

    /**
     * Deterministic food choice (documented rule): customer number n orders menu item
     * ((n - 1) mod number of items). Stage 3 menu has 5 dishes (3 items + 2 combos), so customers 1 to 6 order
     * Salad, Burger, Pasta, Salad + Pasta combo, Burger + Salad combo, Salad.
     */
    public Dish chooseDish(List<Dish> menu) {
        return menu.get((id - 1) % menu.size());
    }

    /** What this customer pays for the dish, in cents, before any Happy Hour discount. */
    public int billFor(Dish item) {
        return item.getPriceCents();
    }

    /** End-of-turn loss for an unserved customer. Returns true if the customer has now left. */
    public boolean applyWaitingLoss() {
        if (!isAwaitingService()) {
            return false;
        }
        satisfaction = Math.max(0, satisfaction - waitLossPerTurn());
        if (satisfaction == 0) {
            leave();
            return true;
        }
        return false;
    }

    /** Waiting, seated or ordered but not yet served: these customers lose satisfaction. */
    public boolean isAwaitingService() {
        return status == Status.WAITING || status == Status.SEATED || status == Status.ORDERED;
    }

    /** Stage 3 (Happy Hour): satisfaction goes back up, capped at 100. */
    public void recoverSatisfaction(int points) {
        satisfaction = Math.min(GameConfig.MAX_SATISFACTION, satisfaction + points);
    }

    public boolean canBeSeated() {
        return status == Status.WAITING;
    }

    public boolean canOrder() {
        return status == Status.SEATED;
    }

    public void seatAt(Table t) {
        table = t;
        status = Status.SEATED;
    }

    public void placeOrder(Order o) {
        order = o;
        status = Status.ORDERED;
    }

    public void markServed(int turn) {
        servedTurn = turn;
        status = Status.SERVED;
    }

    /**
     * A customer served on an EARLIER turn has finished eating: becomes READY_TO_PAY.
     * Returns true if that just happened.
     */
    public boolean finishEatingIfDue(int turn) {
        if (status == Status.SERVED && servedTurn < turn) {
            status = Status.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    /** The Cashier cannot charge a customer who became READY_TO_PAY this same turn. */
    public boolean canPay(int turn) {
        return status == Status.READY_TO_PAY && readyToPayTurn < turn;
    }

    public void markPaid() {
        status = Status.PAID;
    }

    public void leave() {
        status = Status.LEFT;
    }

    public int getId()                 { return id; }
    public int getSatisfaction()       { return satisfaction; }
    public Status getStatus()          { return status; }
    public Table getTable()            { return table; }
    public Order getOrder()            { return order; }
    public int getReadyToPayTurn()     { return readyToPayTurn; }
    public boolean hasBeenServed()     { return servedTurn >= 0; }

    /** "Regular" for RegularCustomer, and so on. */
    public String getTypeName() {
        return getClass().getSimpleName().replace("Customer", "");
    }
}
