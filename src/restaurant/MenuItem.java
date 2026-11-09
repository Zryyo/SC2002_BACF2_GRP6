package restaurant;

public enum MenuItem {
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

    public String getDisplayName() {
        return displayName;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }
}
