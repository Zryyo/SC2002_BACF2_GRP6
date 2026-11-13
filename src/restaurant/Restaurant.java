package restaurant;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Holds the game state: tables, waiting queue, customers, staff and the ledger.
 * Orders are reached through their customers, so there is no separate list to keep in step.
 */
public class Restaurant {
    private final List<Table> tables = new ArrayList<>();
    private final Deque<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Host host = new Host();                 // Stage 3
    private final Ledger ledger = new Ledger();
    private final HappyHour happyHour = new HappyHour();  // Stage 3
    private final List<Dish> menu = List.of(              // Stage 3: 3 items + 2 combos
            MenuItem.SALAD, MenuItem.BURGER, MenuItem.PASTA,
            new ComboMeal(MenuItem.SALAD, MenuItem.PASTA),
            new ComboMeal(MenuItem.BURGER, MenuItem.SALAD));

    public Restaurant() {
        for (int i = 1; i <= GameConfig.TABLE_COUNT; i++) {
            tables.add(new Table(i));
        }
    }

    // ---------- changes ----------

    public void startTurn() {
        waiter.resetForNewTurn();
        chef.resetForNewTurn();
        cashier.resetForNewTurn();
        host.resetForNewTurn();
    }

    public void addArrival(Customer c) {
        waitingQueue.addLast(c);
        customers.add(c);
    }

    /**
     * Stage 3: the one seating rule shared by the Waiter and the Host.
     * Returns why this seating is not allowed, or null if it is.
     */
    public String seatingProblem(Customer c, Table t) {
        if (c == null || t == null) {
            return "Nobody can be seated right now: no customer is waiting, or no table is free.";
        }
        if (!c.canBeSeated()) {
            return "Customer #" + c.getId() + " is not waiting for a table.";
        }
        if (!t.isFree()) {
            return "Table " + t.getNumber() + " is already occupied.";
        }
        return null;
    }

    public void seatCustomer(Customer c, Table t) {
        t.assign(c);
        c.seatAt(t);
        waitingQueue.remove(c);
    }

    /** Step 4: customers served on an earlier turn become READY_TO_PAY. Returns who just did. */
    public List<Customer> advanceEating(int turn) {
        List<Customer> nowReady = new ArrayList<>();
        for (Customer c : customers) {
            if (c.finishEatingIfDue(turn)) {
                nowReady.add(c);
            }
        }
        return nowReady;
    }

    /** Step 5: unserved customers lose satisfaction; anyone reaching 0 leaves. Returns who left. */
    public List<Customer> applyEndOfTurnWaiting() {
        List<Customer> departed = new ArrayList<>();
        for (Customer c : customers) {
            if (c.applyWaitingLoss()) {
                waitingQueue.remove(c);
                if (c.getOrder() != null) {
                    c.getOrder().cancel();
                }
                if (c.getTable() != null) {
                    c.getTable().release();
                }
                departed.add(c);
            }
        }
        return departed;
    }

    // ---------- questions ----------

    public List<Customer> getWaitingQueue() {
        return new ArrayList<>(waitingQueue);
    }

    public List<Table> getFreeTables() {
        List<Table> free = new ArrayList<>();
        for (Table t : tables) {
            if (t.isFree()) {
                free.add(t);
            }
        }
        return free;
    }

    public List<Customer> getSeatedWithoutOrder() {
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers) {
            if (c.canOrder()) {
                result.add(c);
            }
        }
        return result;
    }

    /** Orders taken but not yet served: still being prepared, or READY. */
    public List<Order> getUnservedOrders() {
        List<Order> result = new ArrayList<>();
        for (Customer c : customers) {
            Order o = c.getOrder();
            if (o != null && (o.getStatus() == Order.Status.PREPARING || o.getStatus() == Order.Status.READY)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> getOrdersInPreparation() {
        List<Order> result = new ArrayList<>();
        for (Order o : getUnservedOrders()) {
            if (o.canPrepare()) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Customer> getReadyToPayCustomers() {
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getStatus() == Customer.Status.READY_TO_PAY) {
                result.add(c);
            }
        }
        return result;
    }

    public int countServed() {
        int n = 0;
        for (Customer c : customers) {
            if (c.hasBeenServed()) {
                n++;
            }
        }
        return n;
    }

    public int countUnhappyDepartures() {
        int n = 0;
        for (Customer c : customers) {
            if (c.getStatus() == Customer.Status.LEFT) {
                n++;
            }
        }
        return n;
    }

    public List<Table> getTables()       { return tables; }
    public List<Customer> getCustomers() { return customers; }
    public Waiter getWaiter()            { return waiter; }
    public Chef getChef()                { return chef; }
    public Cashier getCashier()          { return cashier; }
    public Ledger getLedger()            { return ledger; }
    public Host getHost()                { return host; }
    public HappyHour getHappyHour()      { return happyHour; }
    public List<Dish> getMenu()          { return menu; }
}
