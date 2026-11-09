package restaurant;

import java.util.Set;

/** The base-setting numbers from the brief, kept in one place. */
public final class GameConfig {
    private GameConfig() {}

    public static final int MAX_TURNS = 18;
    public static final int TABLE_COUNT = 2;
    public static final Set<Integer> ARRIVAL_TURNS = Set.of(1, 4, 7, 10, 13, 16);
    public static final int START_SATISFACTION = 100;

    // Victory targets: all three must be met after the last turn.
    public static final int TARGET_PAID = 4;
    public static final int TARGET_REVENUE_CENTS = 4500;
    public static final int TARGET_AVG_SATISFACTION = 60;
}
