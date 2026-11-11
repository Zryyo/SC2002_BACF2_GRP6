package restaurant;

//Critic customer loses 12 satisfaction per waiting turn and pays full price.
public class CriticCustomer extends Customer {

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    protected int waitLossPerTurn() {
        return 12;
    }

    @Override
    public MenuItem chooseDish(MenuItem[] menu) {
        MenuItem best = menu[0];
        for (MenuItem item : menu) {
            if (item.getPrepUnits() > best.getPrepUnits() // always orders the dish that takes the most preparation units
                    || (item.getPrepUnits() == best.getPrepUnits() && item.getPriceCents() > best.getPriceCents())) {
                best = item;
            } // in event of tie, dish selected is the one with higher price (On the base menu that is Pasta).
        }
        return best;
    }
}
