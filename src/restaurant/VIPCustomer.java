package restaurant;

// VIP customer: loses 5 satisfaction per waiting turn and gets 10% off the bill.
public class VIPCustomer extends Customer {

    public VIPCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitLossPerTurn() {
        return 5;
    }

    // 90% of the price, rounded half up to a whole cent.
    @Override
    public int billFor(MenuItem item) {
        return (item.getPriceCents() * 90 + 50) / 100;
    }
}