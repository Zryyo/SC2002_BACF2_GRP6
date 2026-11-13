package restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

/**
 * Runs the game: 18 turns, each following the six steps in the brief.
 * It joins the screen, the staff and the restaurant together but keeps the rules in the domain classes.
 */
public class Game {
    private final Restaurant restaurant;
    private final ConsoleUI ui;
    private int nextCustomerId = 1;

    public Game(ConsoleUI ui) {
        this.ui = ui;
        this.restaurant = new Restaurant();
    }

    /** Always plays all 18 turns (no early ending), then returns the result. */
    public GameResult run() {
        for (int turn = 1; turn <= GameConfig.MAX_TURNS; turn++) {
            runTurn(turn);
        }
        GameResult result = evaluateResult();
        ui.showResult(result, restaurant);
        return result;
    }

    private void runTurn(int turn) {
        restaurant.startTurn();

        stepHappyHour(turn);                 // Stage 3: switched on at the start of a turn, before arrivals
        stepArrivals(turn);                  // 1
        ui.showTurnStart(turn, restaurant);
        stepHost(turn);                      // Stage 3: the Host seats before the Waiter acts...
        stepWaiter(turn);                    // 2 and 3: ...so the Waiter can take that customer's order. Waiter first...
        stepChef(turn);                      //          ...then the Chef (and may cook what the Waiter just took)
        stepEatingAndPayment(turn);          // 4
        stepWaitingAndAbandonment();         // 5
        ui.showEndOfTurn(turn, restaurant);
    }

    // Stage 3: offer Happy Hour until it has been used; while active, waiting customers recover satisfaction.
    private void stepHappyHour(int turn) {
        HappyHour happyHour = restaurant.getHappyHour();
        if (!happyHour.hasBeenUsed() && ui.askActivateHappyHour(turn)) {
            ui.showMessage(happyHour.activate(turn).getMessage());
        }
        if (happyHour.isActive(turn)) {
            happyHour.recoverSatisfaction(restaurant);
        }
    }

    // Step 1
    private void stepArrivals(int turn) {
        if (GameConfig.ARRIVAL_TURNS.contains(turn)) {
            Customer c = createArrival(turn);
            restaurant.addArrival(c);
            ui.showMessage("Customer #" + c.getId() + " (" + c.getTypeName()
                    + ") arrives and joins the queue with satisfaction " + c.getSatisfaction() + ".");
        }
    }

    //Modifications to enable create
    private Customer createArrival(int turn) {
        if (GameConfig.VIP_ARRIVAL_TURNS.contains(turn)) {
            return new VIPCustomer(nextCustomerId++);
        }
        if (GameConfig.CRITIC_ARRIVAL_TURNS.contains(turn)) {
            return new CriticCustomer(nextCustomerId++);
        }
        return new RegularCustomer(nextCustomerId++);
    }

    // Stage 3 (Host): seats at most one queued customer, in addition to the Waiter.
    // If nobody can be seated there is nothing to choose, so the Host is skipped.
    private void stepHost(int turn) {
        List<StaffAction> options = hostOptions();
        if (options.size() == 1) {
            ui.showMessage("Host: nobody can be seated this turn.");
            return;
        }
        runChoice(ui::chooseHostAction, options, turn);
    }

    // Steps 2 and 3 (Waiter). A failed choice explains why and asks again; the task is not used up.
    private void stepWaiter(int turn) {
        runChoice(ui::chooseWaiterAction, waiterOptions(), turn);
    }

    // Steps 2 and 3 (Chef). The options are built now, so an order the Waiter just took is included.
    private void stepChef(int turn) {
        runChoice(ui::chooseChefAction, chefOptions(), turn);
    }

    /** Asks until a choice succeeds. A failed choice explains why and does not use up the staff task. */
    private void runChoice(Function<List<StaffAction>, StaffAction> ask, List<StaffAction> options, int turn) {
        while (true) {
            StaffAction chosen = ask.apply(options);
            ActionResult result = chosen.execute(restaurant, turn);
            ui.showMessage(result.getMessage());
            if (result.isSuccess()) {
                return;
            }
        }
    }

    // Step 4
    private void stepEatingAndPayment(int turn) {
        for (Customer c : restaurant.advanceEating(turn)) {
            ui.showMessage("Customer #" + c.getId() + " has finished eating and is ready to pay.");
        }
        ActionResult payment = restaurant.getCashier().collectPayment(restaurant, turn);
        if (payment.isSuccess()) {
            ui.showMessage(payment.getMessage());
        }
    }

    // Step 5
    private void stepWaitingAndAbandonment() {
        for (Customer c : restaurant.applyEndOfTurnWaiting()) {
            ui.showMessage("Customer #" + c.getId() + " ran out of patience and LEFT without paying.");
        }
    }

    /** Stage 3: every seating the Host could do, plus Wait. */
    private List<StaffAction> hostOptions() {
        List<StaffAction> options = new ArrayList<>();
        for (Customer c : restaurant.getWaitingQueue()) {
            for (Table t : restaurant.getFreeTables()) {
                options.add(new HostSeatAction(c, t));
            }
        }
        options.add(new WaitAction("Host"));
        return options;
    }

    /** Every Waiter task the manager could pick. Tasks that cannot be done are listed with the reason. */
    private List<StaffAction> waiterOptions() {
        List<StaffAction> options = new ArrayList<>();

        List<Customer> queue = restaurant.getWaitingQueue();
        List<Table> freeTables = restaurant.getFreeTables();
        if (queue.isEmpty() || freeTables.isEmpty()) {
            options.add(new SeatCustomerAction(null, null));
        } else {
            for (Customer c : queue) {
                for (Table t : freeTables) {
                    options.add(new SeatCustomerAction(c, t));
                }
            }
        }

        List<Customer> seated = restaurant.getSeatedWithoutOrder();
        if (seated.isEmpty()) {
            options.add(new TakeOrderAction(null));
        } else {
            for (Customer c : seated) {
                options.add(new TakeOrderAction(c));
            }
        }

        boolean anyReady = false;
        for (Order o : restaurant.getUnservedOrders()) {
            if (o.isReady()) {
                options.add(new ServeOrderAction(o));
                anyReady = true;
            }
        }
        if (!anyReady) {
            options.add(new ServeOrderAction(null));
        }

        options.add(new WaitAction("Waiter"));
        return options;
    }

    private List<StaffAction> chefOptions() {
        List<StaffAction> options = new ArrayList<>();
        List<Order> inKitchen = restaurant.getOrdersInPreparation();
        if (inKitchen.isEmpty()) {
            options.add(new PrepareOrderAction(null));
        } else {
            for (Order o : inKitchen) {
                options.add(new PrepareOrderAction(o));
            }
        }
        options.add(new WaitAction("Chef"));
        return options;
    }

    // Step 6
    private GameResult evaluateResult() {
        Ledger ledger = restaurant.getLedger();
        OptionalDouble average = ledger.getAverageSatisfaction();

        boolean enoughPaid = ledger.getPaidCount() >= GameConfig.TARGET_PAID;
        boolean enoughRevenue = ledger.getRevenueCents() >= GameConfig.TARGET_REVENUE_CENTS;
        boolean happyEnough = average.isPresent() && average.getAsDouble() >= GameConfig.TARGET_AVG_SATISFACTION;

        return (enoughPaid && enoughRevenue && happyEnough) ? GameResult.VICTORY : GameResult.DEFEAT;
    }
}
