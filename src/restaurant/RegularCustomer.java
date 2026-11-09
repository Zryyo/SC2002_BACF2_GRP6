package restaurant;

/** Regular customer: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitLossPerTurn() {
        return 8;
    }
}
