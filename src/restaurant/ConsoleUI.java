package restaurant;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;

/**
 * The command-line screen. It only displays the state and collects the manager's choices.
 * It contains no game rules.
 */
public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);

    public void showTurnStart(int turn, Restaurant r) {
        Ledger ledger = r.getLedger();
        List<Customer> queue = r.getWaitingQueue();

        System.out.println();
        System.out.println("==================== TURN " + turn + " of " + GameConfig.MAX_TURNS + " ====================");
        System.out.println("Revenue: " + formatMoney(ledger.getRevenueCents())
                + "    Avg satisfaction (paid customers): " + averageText(ledger));

        System.out.println("Waiting queue:" + (queue.isEmpty() ? " (empty)" : ""));
        for (Customer c : queue) {
            System.out.println("   " + describe(c));
        }

        System.out.println("Tables:");
        for (Table t : r.getTables()) {
            if (t.isFree()) {
                System.out.println("   Table " + t.getNumber() + ": free");
            } else {
                System.out.println("   Table " + t.getNumber() + ": " + describe(t.getOccupant()));
            }
        }

        int ready = 0;
        for (Order o : r.getUnservedOrders()) {
            if (o.isReady()) {
                ready++;
            }
        }
        System.out.println("Staff tasks available:");
        System.out.println("   Waiter (1 task): seat a customer [" + queue.size() + " queued, "
                + r.getFreeTables().size() + " free table(s)], take an order ["
                + r.getSeatedWithoutOrder().size() + " seated], serve [" + ready + " READY]");
        System.out.println("   Chef (1 unit):   prepare an order [" + r.getOrdersInPreparation().size() + " in the kitchen]");
        System.out.println("   Cashier:         automatic, one payment per turn");
    }

    public StaffAction chooseWaiterAction(List<StaffAction> options) {
        return choose("WAITER - choose ONE task:", options);
    }

    public StaffAction chooseChefAction(List<StaffAction> options) {
        return choose("CHEF - choose ONE task:", options);
    }

    public void showMessage(String msg) {
        System.out.println("  >> " + msg);
    }

    public void showEndOfTurn(int turn, Restaurant r) {
        Ledger ledger = r.getLedger();
        System.out.println("--- End of turn " + turn + " ---");
        System.out.println("Served: " + r.countServed()
                + " | Unhappy departures: " + r.countUnhappyDepartures()
                + " | Revenue: " + formatMoney(ledger.getRevenueCents())
                + " | Avg satisfaction (paid): " + averageText(ledger)
                + " | Turns used: " + turn + "/" + GameConfig.MAX_TURNS);
    }

    public void showResult(GameResult result, Restaurant r) {
        Ledger ledger = r.getLedger();
        System.out.println();
        System.out.println("==================== GAME OVER ====================");
        System.out.println("Paid customers:   " + ledger.getPaidCount()
                + "   (need at least " + GameConfig.TARGET_PAID + ")");
        System.out.println("Revenue:          " + formatMoney(ledger.getRevenueCents())
                + "   (need at least " + formatMoney(GameConfig.TARGET_REVENUE_CENTS) + ")");
        System.out.println("Avg satisfaction: " + averageText(ledger)
                + "   (need at least " + GameConfig.TARGET_AVG_SATISFACTION + ")");
        System.out.println("RESULT: " + result);
    }

    // ---------------- private helpers ----------------

    private StaffAction choose(String title, List<StaffAction> options) {
        System.out.println();
        System.out.println(title);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + options.get(i).getLabel());
        }
        int choice = readChoice(1, options.size());
        return options.get(choice - 1);
    }

    /** Keeps asking until the manager types a whole number in range. */
    private int readChoice(int min, int max) {
        while (true) {
            System.out.print("Choose " + min + "-" + max + ": ");
            System.out.flush();
            String line = scanner.nextLine().trim();   // throws NoSuchElementException if the input has ended
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  Invalid input. Please type a whole number from " + min + " to " + max + ".");
        }
    }

    // 900 -> "$9.00". Used to formatt for all money shown by the UI (the Cashier's payment message formats its own).
    private String formatMoney(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    private String averageText(Ledger ledger) {
        OptionalDouble avg = ledger.getAverageSatisfaction();
        return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
    }

    private String describe(Customer c) {
        String s = "#" + c.getId() + " " + c.getTypeName()
                + " | satisfaction " + c.getSatisfaction()
                + " | " + c.getStatus();
        Order o = c.getOrder();
        if (o != null) {
            s += " | order: " + o.getItem().getDisplayName() + " " + o.getStatus()
                    + " (" + formatMoney(o.getBillCents()) + ")";
            if (o.canPrepare()) {
                s += " " + o.getRemainingUnits() + " unit(s) left";
            }
        }
        return s;
    }
}
