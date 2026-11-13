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

# Stage 3 (tag stage-3) notes

## Rules fixed for reproducibility
- **Menu:** Salad $9.00 (1 unit), Burger $15.00 (1), Pasta $18.00 (2), Salad + Pasta combo $24.30 (3),
  Burger + Salad combo $21.60 (2). Combo price = 90% of the two parts, rounded half up to the nearest cent.
- **Food choice:** customer n orders dish ((n - 1) mod 5): Salad, Burger, Pasta, Salad + Pasta combo,
  Burger + Salad combo, Salad. The Critic (customer #4) picks the most preparation units: the Salad + Pasta combo.
- **Turn order:** Happy Hour prompt (until used), arrivals, turn screen, Host, Waiter, Chef, eating and payment,
  waiting and abandonment. The Host is skipped with a message when nobody can be seated.
- **Happy Hour:** lasts the turn it is switched on and the next. Orders taken then get 20% off after the VIP discount.
  Customers waiting for a table or food recover 15 at the start of each active turn (max 100).
- New input lines: one Happy Hour answer per turn until it is used (1 = no, 2 = yes), and one Host answer on turns
  where someone can be seated. So Stage 1 and 2 input files only replay at their own tags.

## Run 5: Stage 3 victory (tests/stage3_win_input.txt)
Strategy: Happy Hour on turn 4. The Host seats each arrival; the Waiter serves a READY dish if there is one, else takes
an order; the Chef prepares the oldest order.

| Customer | Type | Dish | Bill | Paid on turn | Satisfaction recorded |
|---|---|---|---|---|---|
| #1 | Regular | Salad | $9.00 | 4 | 92 |
| #2 | VIP | Burger | $10.80 (VIP 10% off, then Happy Hour 20% off) | 7 | 100 (lost 5 on turn 4, recovered 15 on turn 5, capped at 100) |
| #3 | Regular | Pasta | $18.00 | 11 | 84 |
| #4 | Critic | Salad + Pasta combo | $24.30 | 15 | 64 |
| #5 | Regular | Burger + Salad combo | $21.60 | 18 | 76 |

Result: 5 paid, revenue $83.70, average satisfaction 83.2: **VICTORY**. Customer #6 cannot finish paying in time.
Compared with Stage 2, the Host seating on arrival lets the Waiter take the order the same turn, so every customer
is served sooner: customer #3 records 84 (76 in Stage 2) and the Critic 64 (52 in Stage 2).

## Run 6: Stage 3 defeat (tests/stage3_lose_input.txt)
Strategy: Host, Waiter and Chef always wait; Happy Hour is switched on at turn 10.
On turns 10 and 11 every waiting customer recovers 15 (for example #1: 36 -> 43 -> 50). The Critic who arrives on
turn 10 drops to 88 and is capped back at 100, not 103. On turn 12 Happy Hour shows as used and recovery stops.

Result: 0 paid, revenue $0.00, average satisfaction N/A: **DEFEAT**. Happy Hour slows the losses but cannot win
the game if nobody is served.

## Other Stage 3 behaviour checked
- Bad input at the Happy Hour prompt (letters, 3) is rejected and asked again.
- Happy Hour is offered only until it is used; afterwards the prompt no longer appears.
- Orders taken before Happy Hour keep their price (customer #1's Salad stays $9.00 in Run 5), and orders taken after it
  ends pay full price (customer #3's Pasta, $18.00).
