package restaurant;

import java.util.List;

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
    public Dish chooseDish(List<Dish> menu) {
        Dish best = menu.get(0);
        for (Dish item : menu) {
            if (item.getPrepUnits() > best.getPrepUnits() // always orders the dish that takes the most preparation units
                    || (item.getPrepUnits() == best.getPrepUnits() && item.getPriceCents() > best.getPriceCents())) {
                best = item;
            } // in event of tie, dish selected is the one with higher price (Pasta on the Stage 2 menu; the Salad + Pasta combo, 3 units, on the Stage 3 menu).
        }
        return best;
    }
}
