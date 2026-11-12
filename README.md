# SC2002 Restaurant Rush

A turn-based restaurant management game in Java with a command-line interface.
You play the manager. Staff time is limited, so you decide which customers and orders come first.

- **Repository:** https://github.com/Zryyo/SC2002_BACF2_GRP6
- **Stages (git tags):** `stage-1`, `stage-2`, `stage-3`

## Build and run

Requires a JDK (Java 17 or newer). From the project folder:

```
mkdir -p out
javac -d out src/restaurant/*.java
java -cp out restaurant.RestaurantRushApp
```

Type the number of your choice at each prompt. Press Ctrl+C to quit.

To replay the reproducible test runs (see [Testing](#testing)):

```
java -cp out restaurant.RestaurantRushApp < tests/win_input.txt
java -cp out restaurant.RestaurantRushApp < tests/lose_input.txt
java -cp out restaurant.RestaurantRushApp < tests/brief_example_input.txt
java -cp out restaurant.RestaurantRushApp < tests/stage2_win_input.txt
java -cp out restaurant.RestaurantRushApp < tests/stage2_lose_input.txt
java -cp out restaurant.RestaurantRushApp < tests/stage3_win_input.txt
java -cp out restaurant.RestaurantRushApp < tests/stage3_lose_input.txt
```

Each stage's runs replay exactly at that stage's tag (`git checkout stage-1`, `stage-2` or `stage-3`).

## Game setup

Your game must always offer this base setting, so that a TA can reproduce your runs. Other difficulty settings are optional.

| **Setting** | **Required base value** |
| --- | --- |
| **Turns** | 18 turns maximum |
| **Tables** | 2 |
| **Staff** | Waiter × 1 (one task per turn)<br>Chef × 1 (one preparation unit per turn)<br>Cashier × 1 (one payment per turn) |
| **Arrivals** | One customer at the start of turns 1, 4, 7, 10, 13 and 16 (six in total) |
| **Objective** | At the end of turn 18: **at least 4 paid customers** AND revenue **at least $45** AND average satisfaction of paid customers **at least 60** |

| **Menu item** | **Price** | **Preparation units** |
| --- | --- | --- |
| **Salad** | $9.00 | 1 |
| **Burger** | $15.00 | 1 |
| **Pasta** | $18.00 | 2 |

## How a turn works

### Start-of-turn display

At the start of every turn, show: the turn number, revenue, average satisfaction of paid customers, the waiting queue, occupied tables, each customer’s satisfaction, order status, and the staff tasks available.

### Turn steps

Each turn then runs these six steps in order.

| **Step** | **What happens** |
| --- | --- |
| **1  Arrivals** | The scheduled customer joins the waiting queue with satisfaction 100. |
| **2  Manager choices** | **Waiter:** choose at most one task, or Wait: seat one queued customer at a free table, take one seated customer’s order, or serve one READY order. **Chef:** choose at most one order to advance by one preparation unit, or Wait. The Chef may work on an order the Waiter took this turn. |
| **3  Kitchen and serving** | The Waiter acts before the Chef. When an order’s last preparation unit is done, it becomes READY. A dish can be served only if it was already READY when the Waiter acts, so a dish finished this turn is served next turn at the earliest. Serving sets it to SERVED. |
| **4  Eating and payment** | A customer served on an earlier turn finishes one full turn of eating and becomes READY_TO_PAY. The Cashier automatically handles at most one READY_TO_PAY customer per turn, oldest first. A customer who became READY_TO_PAY this turn pays next turn at the earliest. Payment adds the bill to revenue, marks the customer and order PAID, and frees the table. |
| **5  Waiting and abandonment** | At turn end, every waiting or seated customer who has not been served loses satisfaction (Regular: 8 points, minimum 0). A customer who reaches 0 leaves without paying: cancel any unfinished order and free the table. Customers who are served, eating or ready to pay lose nothing. |
| **6  Result** | The game always runs all 18 turns; there is no early ending. After turn 18: **Victory** if all three targets in the objective above are met, otherwise **Defeat**. |

## Design decisions

- **Customers choose their own food.** Deterministic rule: customer number *n* orders menu item *(n - 1) mod 3*, so customers 1 to 6 order Salad, Burger, Pasta, Salad, Burger, Pasta. The rule lives in `Customer.chooseDish`, and the choice is shown when the order is placed. In Stage 2 the Critic overrides this rule (see below), so customer 4 orders Pasta instead of Salad.
- **Money is stored as integer cents** and shown with two decimals: by `ConsoleUI`, and by the Cashier's payment message.
- **Rules stay out of the UI.** `ConsoleUI` only shows the state and collects the manager's choice. Rules live in the domain classes (`Customer`, `Order`, `Waiter`, `Chef`, `Cashier`, `Restaurant`) and in `Game`.
- **Manager choices are objects.** Each possible task is a `StaffAction` (`SeatCustomerAction`, `TakeOrderAction`, `ServeOrderAction`, `PrepareOrderAction`, `WaitAction`). `Game` builds the list of options, `ConsoleUI` shows their labels, and the chosen action runs itself. A new kind of task is a new class implementing `StaffAction`; `Game` only has to add it to its list of options.
- **Waiter, then Chef.** The Waiter's valid action takes effect immediately, then the Chef's options are built. This lets the Chef work on an order the Waiter took in the same turn.
- **Impossible tasks stay in the menu with the reason.** Choosing one explains why it failed and asks again, and the staff task is not used up.
- **Behaviour that differs by customer type sits in overridable methods** (`waitLossPerTurn`, `billFor`, `chooseDish`), so `Game` never checks what type a customer is.
- **Customer types (Stage 2).** Regular loses 8 per waiting turn and pays full price. VIP loses 5 and pays 90% of the price, rounded half up to a whole cent. Critic loses 12, pays full price, and always orders the dish with the most preparation units (ties: higher price), which is Pasta on the base menu.
- **Who arrives (Stage 2).** Still six arrivals on turns 1, 4, 7, 10, 13 and 16. Turn 4 is a VIP (customer 2), turn 10 is a Critic (customer 4), the rest are Regular. `Game.createArrival` picks the type by turn number and is the only place that names the customer subclasses.
- **Host (Stage 3).** Acts before the Waiter each turn and seats at most one queued customer, so the Waiter can take that customer's order in the same turn. The Waiter can still seat customers too. If nobody can be seated, the Host is skipped with a message instead of showing an empty menu. Both use one seating rule, `Restaurant.seatingProblem`, so their checks cannot drift apart.
- **ComboMeal (Stage 3).** Two combos join the menu: Salad + Pasta ($24.30, 3 units) and Burger + Salad ($21.60, 2 units). Price is 90% of the two parts, rounded half up to the nearest cent; units are the sum. `MenuItem` (an enum) and `ComboMeal` both implement the new `Dish` interface, so orders, the kitchen and customers treat them alike.
- **Combo choice rule (Stage 3).** The food rule is unchanged: customer *n* orders dish *(n - 1) mod menu size*, and the menu now has 5 dishes. So customers 1 to 6 order Salad, Burger, Pasta, Salad + Pasta combo, Burger + Salad combo, Salad. The Critic still picks the dish with the most preparation units, which is now the Salad + Pasta combo.
- **Happy Hour (Stage 3).** Asked at the start of every turn until it is used, before arrivals. It lasts that turn and the next; the turn screen shows how many turns are left. Orders taken while it is active get 20% off, after the VIP discount (VIP Burger: $15 × 0.9 × 0.8 = $10.80), and the price is locked when the order is taken. At the start of each active turn, customers still waiting for a table or food recover 15 satisfaction (max 100). Customers who have left are not affected.
- **Single source folder (`src/restaurant`) with git tags.** Each stage is a tagged commit on `main`, not a copy of the code.

## Project structure (Stage 3)

| File | Responsibility |
| --- | --- |
| `RestaurantRushApp` | Entry point: creates the screen and the game and starts it |
| `Game` | Runs the 18 turns and the six steps, builds the staff options, decides Victory or Defeat |
| `ConsoleUI` | Command-line screen: displays state, reads the manager's choice, formats money |
| `GameConfig`, `GameResult` | Base-setting numbers (including which arrivals are VIP or Critic) and the Victory or Defeat result |
| `Restaurant` | Game state: tables, waiting queue, customers, staff, ledger |
| `Customer`, `RegularCustomer` | Customer behaviour and lifecycle (`Customer.Status` is a nested enum) |
| `VIPCustomer`, `CriticCustomer` | Stage 2 customer types: override the waiting loss, the bill (VIP) and the dish choice (Critic) |
| `Order` | One order: preparation progress, locked bill, legal status changes (`Order.Status` is a nested enum) |
| `Dish` | Stage 3: anything that can be ordered (name, price in cents, preparation units) |
| `MenuItem` | Salad, Burger and Pasta with price in cents and preparation units; implements `Dish` |
| `ComboMeal` | Stage 3: two dishes sold together at 90% of their combined price |
| `HappyHour` | Stage 3: once-per-game, two-turn discount and satisfaction recovery |
| `Table` | A table and its occupant |
| `Ledger` | Revenue and the satisfaction recorded at payment |
| `Staff`, `Waiter`, `Chef`, `Cashier`, `Host` | Staff roles; each enforces its own one-task-per-turn rule (`Host` is Stage 3) |
| `StaffAction`, `SeatCustomerAction`, `TakeOrderAction`, `ServeOrderAction`, `PrepareOrderAction`, `WaitAction`, `HostSeatAction` | The manager's choices as objects (`HostSeatAction` is Stage 3) |
| `ActionResult` | Success, or the reason for failure, returned by every staff action |

## Testing

Reproducible runs are in `tests/` (details in `tests/TEST_NOTES.md`). Each input file holds one menu number per line, and the matching output file is the expected full output.

| Run | Input file | Result |
| --- | --- | --- |
| Stage 3 victory: Host seats every arrival, Happy Hour on turn 4 | `tests/stage3_win_input.txt` | 5 paid, $83.70, average satisfaction 83.2. Shows the VIP Burger at $10.80 and the Salad + Pasta combo at $24.30 |
| Stage 3 defeat: staff always wait, Happy Hour on turn 10 | `tests/stage3_lose_input.txt` | 0 paid, $0.00, average satisfaction N/A. Shows waiting customers recovering 15 on turns 10 and 11 (Critic capped at 100) |
| Stage 2 victory, with a VIP and a Critic | `tests/stage2_win_input.txt` | 5 paid, $73.50, average satisfaction 75.6 |
| Stage 2 defeat, with a VIP and a Critic | `tests/stage2_lose_input.txt` | 0 paid, $0.00, average satisfaction N/A, 2 unhappy departures (customer 1 and the Critic) |
| Victory | `tests/win_input.txt` | 5 paid, $66.00, average satisfaction 79.2 |
| Defeat | `tests/lose_input.txt` | 0 paid, $0.00, average satisfaction N/A, 2 unhappy departures |
| Worked example from the brief (Section 3.3) | `tests/brief_example_input.txt` | Customer 3 (Pasta) is seated on turn 7, paid $18.00 on turn 12 with satisfaction 76 recorded, which is the brief's example shifted by 6 turns |

The three Stage 1 runs replay exactly at tag `stage-1`, and the two Stage 2 runs at tag `stage-2`. Later stages add prompts (Happy Hour, Host) and menu items, so older input files no longer line up; replay them at their own tag.

To check a run still behaves the same, nothing should print:

```
java -cp out restaurant.RestaurantRushApp < tests/win_input.txt | diff - tests/win_output.txt
```

## Stage comparison

### Stage 1 (tag `stage-1`)

Basic restaurant operation: Regular customers, menu, Waiter, Chef, Cashier, Table, Order, waiting queue, the full turn sequence, victory and defeat.

### Stage 1 to Stage 2 (VIP and Critic customers)

- **Compare:** https://github.com/Zryyo/SC2002_BACF2_GRP6/compare/stage-1...stage-2
- **New files:** `VIPCustomer.java`, `CriticCustomer.java`; `tests/stage2_win_input.txt` and `tests/stage2_lose_input.txt` with their expected outputs.
- **Changed or deleted files and why:**
  - `Game.java`: new private `createArrival(turn)`, called by `stepArrivals` instead of always creating a `RegularCustomer`. The brief requires at least one VIP and one Critic among the six arrivals, and this is the one place that decides which type arrives.
  - `GameConfig.java`: new `VIP_ARRIVAL_TURNS` = {4} and `CRITIC_ARRIVAL_TURNS` = {10}, so the arrival setup stays in one place.
  - `README.md` and `tests/TEST_NOTES.md`: Stage 2 rules and test runs.
  - No files deleted.
- **Unchanged and why:** `Customer` and `RegularCustomer` already had overridable `waitLossPerTurn`, `billFor` and `chooseDish`, so the new types only override them. No other class changed, since every customer type follows the same order and service flow. With the VIP and Critic turns emptied, the three Stage 1 runs replay byte for byte.

### Stage 2 to Stage 3 (Host, ComboMeal, Happy Hour)

- **Compare:** https://github.com/Zryyo/SC2002_BACF2_GRP6/compare/stage-2...stage-3
- **New files:**
  - `Dish.java`: common type for anything that can be ordered. Needed because `MenuItem` is an enum, so a combo cannot be one of its constants.
  - `ComboMeal.java`: two dishes; units are the sum, price is 90% of the combined price.
  - `HappyHour.java`: the pricing policy (activation once per game, 2 turns, 20% off, +15 recovery, turns left for the display).
  - `Host.java` (extends `Staff`) and `HostSeatAction.java` (implements `StaffAction`): the new role and the manager's Host choice.
  - `tests/stage3_win_input.txt`, `tests/stage3_lose_input.txt` and their expected outputs.
- **Changed files and why:**
  - `MenuItem.java`: now `implements Dish`. Nothing else changes.
  - `Customer.java`: `chooseDish` takes a `List<Dish>` and returns a `Dish`; `billFor` takes a `Dish`; new `recoverSatisfaction(points)` capped at 100, for Happy Hour.
  - `VIPCustomer.java`, `CriticCustomer.java`: follow the new `Customer` signatures. Behaviour is the same.
  - `Order.java`: the item is a `Dish`, so an order can be for a combo.
  - `Restaurant.java`: owns the `Host`, the `HappyHour` and the menu (`List<Dish>`: 3 items + 2 combos); `startTurn` resets the Host; new `seatingProblem(c, t)` so Waiter and Host share one seating rule.
  - `Waiter.java`: `seat` uses `Restaurant.seatingProblem`; `takeOrder` takes the turn, chooses from the restaurant's menu, and applies Happy Hour after the customer's own discount. The price is locked when the order is taken.
  - `TakeOrderAction.java`: passes the turn to `takeOrder`.
  - `Game.java`: new `stepHappyHour` (start of turn, before arrivals) and `stepHost` (before the Waiter), plus `hostOptions()`. The repeated "ask until the choice succeeds" loop is now one `runChoice` method used by Host, Waiter and Chef.
  - `ConsoleUI.java`: new `askActivateHappyHour` and `chooseHostAction`; the turn screen shows Happy Hour status with turns left, and the Host's task.
  - `GameConfig.java`: `MAX_SATISFACTION`, `COMBO_DISCOUNT_PERCENT`, `HAPPY_HOUR_TURNS`, `HAPPY_HOUR_DISCOUNT_PERCENT`, `HAPPY_HOUR_RECOVERY`, so the numbers stay in one place.
  - `README.md`, `tests/TEST_NOTES.md`: Stage 3 rules and runs.
  - No files deleted.
- **Unchanged and why:** `Staff`, `Chef`, `Cashier`, `Ledger`, `Table`, `StaffAction`, the other actions, `ActionResult`, `GameResult` and `RestaurantRushApp`. The Chef already works in preparation units, so a 3-unit combo needs no kitchen change, and the Cashier charges the bill locked in the `Order`, so discounted bills need no payment change. The `StaffAction` interface let the Host's choice be added as a new class.
- **What still works:** the stage-2 rules are unchanged: VIP still loses 5 and gets 10% off, the Critic still loses 12, waiting loss, eating, payment and the result check are the same. If Happy Hour is never switched on and the Host always waits, the game plays by the Stage 2 rules, with the bigger menu.

## Video timestamps

| # | Segment | Timestamp | Presenter |
| --- | --- | --- | --- |
| 1 | The game launching from the submitted project | _TODO_ | _TODO_ |
| 2 | Meaningful manager choices, including a decision where staff are constrained | _TODO_ | _TODO_ |
| 3 | A dish going READY → SERVED → payment | _TODO_ | _TODO_ |
| 4 | A VIP or Critic behaviour | _TODO_ | _TODO_ |
| 5 | A Stage 3 feature | _TODO_ | _TODO_ |
| 6 | One victory and one defeat | _TODO_ | _TODO_ |
| 7 | The stage tag comparisons and the diagrams | _TODO_ | _TODO_ |
