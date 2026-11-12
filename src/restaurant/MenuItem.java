package restaurant;

/** The three base dishes. Stage 3: implements Dish so combos and single items share one type. */
public enum MenuItem implements Dish {
    SALAD("Salad", 900, 1),
    BURGER("Burger", 1500, 1),
    PASTA("Pasta", 1800, 2);

    private final String displayName;
    private final int priceCents;
    private final int prepUnits;

    MenuItem(String displayName, int priceCents, int prepUnits) {
        this.displayName = displayName;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public int getPrepUnits() {
        return prepUnits;
    }
}
