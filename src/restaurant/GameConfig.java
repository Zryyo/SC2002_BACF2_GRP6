package restaurant;

import java.util.Set;

// The base-setting numbers from the brief
public final class GameConfig {
    private GameConfig() {}

    public static final int MAX_TURNS = 18;
    public static final int TABLE_COUNT = 2;
    public static final Set<Integer> ARRIVAL_TURNS = Set.of(1, 4, 7, 10, 13, 16);

    //VIP arrives at turn 4 and Critic arrives at turn 10 for reproducibility
    public static final Set<Integer> VIP_ARRIVAL_TURNS = Set.of(4);
    public static final Set<Integer> CRITIC_ARRIVAL_TURNS = Set.of(10);
    public static final int START_SATISFACTION = 100;
    public static final int MAX_SATISFACTION = 100;

    // Stage 3: ComboMeal and Happy Hour numbers.
    public static final int COMBO_DISCOUNT_PERCENT = 10;
    public static final int HAPPY_HOUR_TURNS = 2;               // the turn it is switched on, and the next
    public static final int HAPPY_HOUR_DISCOUNT_PERCENT = 20;
    public static final int HAPPY_HOUR_RECOVERY = 15;

    // Victory targets: all three must be met after the last turn.
    public static final int TARGET_PAID = 4;
    public static final int TARGET_REVENUE_CENTS = 4500;
    public static final int TARGET_AVG_SATISFACTION = 60;
}
