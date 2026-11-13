package restaurant;

/**
 * Stage 3: a combo made of two existing dishes.
 * Preparation units = the sum of both parts. Price = 90% of the combined price, rounded to the nearest cent.
 * Example: Salad + Pasta = ($9 + $18) x 0.9 = $24.30, 3 units.
 */
public class ComboMeal implements Dish {
    private final Dish first;
    private final Dish second;

    public ComboMeal(Dish first, Dish second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String getDisplayName() {
        return first.getDisplayName() + " + " + second.getDisplayName() + " combo";
    }

    @Override
    public int getPriceCents() {
        int combined = first.getPriceCents() + second.getPriceCents();
        return (combined * (100 - GameConfig.COMBO_DISCOUNT_PERCENT) + 50) / 100;   // round half up
    }

    @Override
    public int getPrepUnits() {
        return first.getPrepUnits() + second.getPrepUnits();
    }
}
