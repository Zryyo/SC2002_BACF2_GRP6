package restaurant;

/**
 * Anything a customer can order: a single MenuItem or a ComboMeal.
 * Stage 3: MenuItem is an enum, so a combo cannot be one of its constants. This common type lets
 * Order, Customer and the kitchen treat items and combos the same way.
 */
public interface Dish {
    String getDisplayName();

    int getPriceCents();

    int getPrepUnits();
}
