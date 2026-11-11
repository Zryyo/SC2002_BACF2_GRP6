# Stage 1 test notes

## Rules fixed for reproducibility
- **Food choice (deterministic):** customer number n orders menu item ((n - 1) mod 3), so customers 1 to 6 order
  Salad, Burger, Pasta, Salad, Burger, Pasta. The choice is shown when the order is placed.
- **Arrivals:** one Regular customer at the start of turns 1, 4, 7, 10, 13 and 16 (customer numbers 1 to 6).
- **Waiter and Chef:** the Waiter's chosen task takes effect first, then the Chef is asked, so the Chef can cook an
  order the Waiter took in the same turn.

## How to run
From the project folder:

    mkdir -p out
    javac -d out src/restaurant/*.java
    java -cp out restaurant.RestaurantRushApp                              # play by hand
    java -cp out restaurant.RestaurantRushApp < tests/win_input.txt        # replay the victory run
    java -cp out restaurant.RestaurantRushApp < tests/lose_input.txt       # replay the defeat run
    java -cp out restaurant.RestaurantRushApp < tests/brief_example_input.txt   # replay the worked example

Each input file holds one menu number per line. The matching `*_output.txt` is the expected full output, so
`java -cp out restaurant.RestaurantRushApp < tests/win_input.txt | diff - tests/win_output.txt` prints nothing when the game behaves the same.

## Run 1: Victory (tests/win_input.txt)
Strategy: seat each customer on arrival, take the order the next turn, have the Chef cook it at once, and serve as soon
as the dish is READY. When the Waiter must choose between serving and seating (turns 10 and 13), serve first so the
served customer stops losing satisfaction.

| Customer | Dish | Paid on turn | Satisfaction recorded |
|---|---|---|---|
| #1 | Salad $9.00 | 5 | 84 |
| #2 | Burger $15.00 | 8 | 84 |
| #3 | Pasta $18.00 | 12 | 76 |
| #4 | Salad $9.00 | 15 | 76 |
| #5 | Burger $15.00 | 18 | 76 |

Result: 5 paid, revenue $66.00, average satisfaction 79.2: **VICTORY**.
Customer #6 arrives on turn 16 and cannot finish paying within 18 turns, which the brief says is intended.

## Run 2: Defeat (tests/lose_input.txt)
Strategy: the manager chooses Wait for both staff on every turn. Customers #1 and #2 lose 8 per turn and leave at the
end of turns 13 and 16. Nobody pays.

Result: 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures: **DEFEAT**.

## Check against the brief's worked example, Section 3.3 (tests/brief_example_input.txt)
Only Customer #3 (Pasta) is given attention; #1 and #2 are ignored. #3 arrives on turn 7, so the brief's table is
shifted by 6 turns: seated on turn 7, order and first unit on turn 8, READY on turn 9, served on turn 10, READY_TO_PAY
on turn 11, paid $18.00 on turn 12 with satisfaction 76 recorded. Satisfaction at the end of each turn: 92, 84, 76, 76, 76.

## Other behaviour checked
- Letters and out-of-range numbers are rejected and asked again.
- Picking a task that is not possible (for example Serve with nothing READY) prints the reason and asks again; the task is not used up.
- If the input ends early, the game stops with a message instead of crashing.


# Stage 2 (tag stage-2) notes

## Rules fixed for reproducibility
- **Arrivals:** still six, on turns 1, 4, 7, 10, 13 and 16. Turn 4 is a VIP (customer #2), turn 10 is a Critic
  (customer #4), the others are Regular.
- **VIP:** loses 5 per waiting turn; bill is 90% of the price, rounded half up to a whole cent
  (Salad $8.10, Burger $13.50, Pasta $16.20). Chooses food by the Stage 1 rule, so customer #2 orders a Burger.
- **Critic:** loses 12 per waiting turn; pays full price; always orders the dish with the most preparation units
  (ties: higher price), which is Pasta.
- The three Stage 1 runs above replay exactly at tag `stage-1`. From Stage 2 on, customers #2 and #4 behave
  differently, so they no longer match.

## Run 3: Stage 2 victory with a VIP and a Critic (tests/stage2_win_input.txt)
Strategy: each turn the Waiter serves a READY dish if there is one, else takes a seated customer's order, else seats
the first customer in the queue at the first free table. The Chef always prepares the oldest order in the kitchen.

| Customer | Type | Dish | Paid on turn | Satisfaction recorded |
|---|---|---|---|---|
| #1 | Regular | Salad $9.00 | 5 | 84 |
| #2 | VIP | Burger $13.50 (10% off) | 8 | 90 |
| #3 | Regular | Pasta $18.00 | 12 | 76 |
| #4 | Critic | Pasta $18.00 | 16 | 52 |
| #5 | Regular | Burger $15.00 | 18 | 76 |

Result: 5 paid, revenue $73.50, average satisfaction 75.6: **VICTORY**. Customer #6 cannot finish paying in time.
The Critic loses 12 a turn and ends at 52, the VIP loses only 5 a turn and ends at 90.

## Run 4: Stage 2 defeat with a VIP and a Critic (tests/stage2_lose_input.txt)
Strategy: Wait for both staff on every turn. Customer #1 (Regular) leaves at the end of turn 13 and the Critic
(#4, arriving on turn 10) leaves at the end of turn 18, after 9 turns. The VIP (#2) loses only 5 a turn and is still
waiting with 25 at the end.

Result: 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures: **DEFEAT**.

## Stage 1 behaviour still works
With `VIP_ARRIVAL_TURNS` and `CRITIC_ARRIVAL_TURNS` set to empty, all six arrivals are Regular again and the three
Stage 1 runs replay byte for byte against their saved outputs.