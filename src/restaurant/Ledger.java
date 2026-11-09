package restaurant;

import java.util.OptionalDouble;

/** Keeps the money and the satisfaction scores recorded at payment time. */
public class Ledger {
    private int revenueCents;
    private int paidCount;
    private int paidSatisfactionTotal;

    public void recordPayment(int billCents, int satisfaction) {
        revenueCents += billCents;
        paidCount++;
        paidSatisfactionTotal += satisfaction;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    /** Empty until the first customer has paid (the screen then shows N/A). */
    public OptionalDouble getAverageSatisfaction() {
        if (paidCount == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) paidSatisfactionTotal / paidCount);
    }
}
