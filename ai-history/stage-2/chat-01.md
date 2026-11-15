# Chat History: Restaurant Rush Stage 2

---

## User

I am currently working on a java project and this is a brief description of what I am working on:
Restaurant Rush is a turn-based restaurant management game played in the command line. The player is the restaurant manager. Over 18 turns they direct a small staff to seat customers, take orders, cook and serve dishes, and collect payment, while keeping customers happy enough not to walk out.
The manager never cooks or chooses food. Each turn they decide what the Waiter and the Chef should do, and the game applies the restaurant's rules to show the result. Stage 1 is the complete base game. It has two tables, one Waiter, one Chef and one Cashier, and six Regular customers who arrive on turns 1, 4, 7, 10, 13 and 16 (fixed for reproducible runs, fixed seed randomness can be implemented for actual gameplays). The menu has three dishes: Salad ($9.00, 1 preparation unit), Burger ($15.00, 1 unit) and Pasta ($18.00, 2 units). The player wins if, after turn 18, at least 4 customers have paid, revenue is at least $45.00, and the average satisfaction of paying customers is at least 60.

Key features of this game include:

1. Full turn sequence so every turn runs six steps in a fixed order (arrivals, manager choices, kitchen and serving, eating and payment, waiting and abandonment, and the result). The game always runs all 18 turns, then declares Victory or Defeat.
2. The manager decides staff actions so each turn, the Waiter can do one task (from seat a queued customer at a free table, take a seated customer's order, serve a READY dish, or wait). The Chef can advance one order by one preparation unit, or wait. The Cashier charges at most one customer per turn automatically, oldest first.
3. To simulate an actual restaurant, the Waiter acts before the Chef, so a dish finished this turn can only be served next turn. A served customer spends one full turn eating before they can pay, and pays the following turn at the earliest.
4. Customers have satisfaction meters and abandonment ability. Customers arrive with satisfaction 100. Each turn they wait unserved, whether queued, seated or waiting for food, they lose 8 points. A customer who reaches 0 leaves without paying, their order is cancelled and their table is freed. Customers who have been served lose nothing.
5. Customers choose their own food (for reproducibility purposes, customer n orders menu item (n − 1) mod 3). The choice is shown when the order is placed.
6. The start of each turn shows:
   1. the turn number, revenue and average satisfaction (N/A until someone pays)
   2. the waiting queue and the tables
   3. each customer's satisfaction and order status
   4. the staff tasks available

7. The end of each turn shows the served count, unhappy departures, revenue, average satisfaction and turns used.
8. Tasks that can't be done are still listed, with reason. Choosing one, or typing a letter or an out-of-range number, explains what went wrong and asks again without using up the staff member's task.
9. All amounts are stored as whole cents and displayed as dollars with two decimals, so there are no rounding errors.
10. The project includes saved input and output files for a victory run, a defeat run for fixed event game run (can be substituted for a randomizer with fixed seed but for consistency purposes fixed events will be used).

I am currently working on stage 2 which will build on the above information

---

## Claude

Thanks for the Stage 1 summary. You said you're working on Stage 2 and that it builds on this, but your message ends before saying what Stage 2 actually adds. What would you like it to include?

If you're still deciding, here are directions that fit the design you've described, so you can pick or adapt:

- **More staff and tables:** a second Waiter or Chef, more tables, and a rule for which staff member handles which task. This tests how well your turn sequence scales.
- **Customer types:** for example a Impatient customer who loses satisfaction faster, a VIP who pays more but leaves sooner, and a Regular as in Stage 1. This is a natural fit for an abstract `Customer` class or interface with subclasses overriding the decay rate.
- **Expanded menu:** more dishes with different prep units and prices, possibly with ingredient stock that can run out.
- **Randomized events with a seed:** you mentioned a fixed-seed randomizer, so Stage 2 could introduce it properly while keeping the fixed-event mode for the saved victory and defeat runs.
- **Upgrades or scoring:** spend revenue on faster cooking or extra tables, or add tips tied to satisfaction.
- **Architecture work:** interfaces for staff actions, a command pattern for tasks, and tests for the turn engine.

To help effectively, tell me:

1. What Stage 2 is supposed to add, whether from your assignment spec or your own plan
2. Whether you want design guidance, code, a review of existing code, or help with test and run files
3. Any constraints, such as required OOP concepts, a fixed set of classes, or "no new libraries"

If you paste your current class structure or the key classes (`Game`, `Customer`, `Staff`, and so on), I can suggest how to extend them with minimal changes.

---

## User

Stage 2 adds the following features:
Add VIPCustomer and CriticCustomer alongside regular customers.
Customer Satisfaction Loss per waiting turn: 8 for Regular, 5 for VIP, 12 for critic
Bill: Regular Full price, VIP 10% off, Critic  Full price
Special behaviour: A distinct order preference or reaction to service that you define and document
Requirements:

* A reproducible base-setting run must include at least one VIP and one Critic. You may replace two of the regular arrivals but there must still be six arrivals.
* All customer types follow the same order and service flow.
* Different behaviour must be dispatched through the customer objects, not through a chain of type checks in the turn controller.
* Stage 2 is independently playable and uses the same win and lose criteria

I will implement the modifications by adding these new classes: VIPCustomer, CriticCustomer and changed these classes Game (new createArrival), GameConfig (new arrival-turn sets).
The base game has been tested and tried so assume it works.
Let me know if there are any logical flaws in my pseudo code before I implement the changes which I will send in the next prompt

---

## Claude

Your message describes the plan, but the pseudo code itself isn't attached yet. Send it and I'll go through it line by line. In the meantime, these are the spots in this spec where logic usually breaks, so you can check them first.

**1. Arrival configuration (`GameConfig`)**
- The VIP, Critic and Regular turn sets must be disjoint, and together they must cover exactly {1, 4, 7, 10, 13, 16}. A turn in two sets means a double arrival, and a turn in none means a missing one.
- The spec says to replace two regulars, so you need exactly one VIP turn and one Critic turn, or more if you want more. Make sure the sets total six.
- Customer numbering (the "n" in `(n − 1) mod 3`) should come from arrival order, not from type. Otherwise the food-choice sequence shifts when you change which turns are VIP or Critic.

**2. Polymorphism (the main grading risk)**
- `Game.createArrival` is where a type decision is allowed, since it's a factory. The turn controller must not contain `instanceof`, `getType()` comparisons or switch statements on type. Put these on `Customer` as overridable methods:
  - `waitLoss()`: 8, 5 or 12
  - `billFor(priceCents)`: full price, 90%, full price
  - hooks such as `onSeated()`, `onServed()` and `onPaid()`, which are no-ops by default
- Don't make the base class store the loss rate as a field set by a type string. That is a hidden type check.

**3. Money**
- VIP 10% off in whole cents is `price * 90 / 100` in integer math. Every menu price (900, 1500, 1800) divides cleanly, so there are no rounding issues. Still, state the rounding rule in your docs in case a price changes.
- Revenue must be the sum of what was billed. A common bug is adding the menu price instead of the discounted bill.

**4. Satisfaction**
- Clamp at 0 and keep the abandonment check as `<= 0`. With loss 12, a Critic hits 0 after 9 waiting turns (100 → 4 → clamp), and a VIP lasts 20.
- Served customers still lose nothing, whatever their type.
- If your special behaviour changes satisfaction (for example a Critic penalty for slow service), decide whether the value used in the average is the one at payment time, after the reaction. Be consistent between the display and the win check.

**5. Special behaviour**
- It must not change the order and service flow (seat, order, cook, serve, eat for one turn, pay). It can only change preferences or reactions, such as the dish ordered, satisfaction bonuses or penalties, or the reaction to waiting.
- Hooks should act on the customer's own state, or return a value to the controller. Avoid having them reach into global game state.
- Document the behaviour in the README and the output. The spec says to define and document it.

**6. Win and lose feasibility**
- VIP discounts and fast-decaying Critics lower revenue and average satisfaction. Check by hand that a victory run still reaches at least $45.00, 4 payers and an average of 60 or more, and that your defeat run still loses for a clear reason.
- Re-record the saved victory and defeat input/output files, since the arrival turns changed.

When you send the pseudo code, I'll check it against these points, plus turn-order edge cases such as a Critic abandoning on the same turn they could be seated.

---

## User

Firstly for choosing who arrives
stepArrivals(turn):
    if turn is in ARRIVAL_TURNS {1, 4, 7, 10, 13, 16}:
        customer ← createArrival(turn)
        add customer to the back of the waiting queue (satisfaction 100)
        show "Customer #id (type) arrives"

createArrival(turn):
    if turn is in VIP_ARRIVAL_TURNS {4}:        return new VIPCustomer(next id)
    else if turn is in CRITIC_ARRIVAL_TURNS {10}: return new CriticCustomer(next id)
    else:                                       return new RegularCustomer(next id)
For VIPCustomer:
waitLossPerTurn():
    return 5

billFor(dish):
    return (dish price in cents × 90 + 50) ÷ 100     // 10% off, rounded half up, whole cents
if customer is a critic:
waitLossPerTurn():
    return 12

chooseDish(menu):
    best ← first dish on the menu
    for each dish on the menu:
        if dish has more preparation units than best,
           or the same units and a higher price:
            best ← dish
    return best                                       // Pasta on the base menu
and these are the logic changes to the game sequence:
end of every turn (Customer.applyWaitingLoss):
    if customer is waiting, seated or ordered but not yet served:
        satisfaction ← max(0, satisfaction − waitLossPerTurn())   // 8, 5 or 12 depending on the object
        if satisfaction = 0: customer leaves

Waiter takes an order (Waiter.takeOrder):
    dish ← customer.chooseDish(menu)      // Critic's own rule, or the Stage 1 rule
    bill ← customer.billFor(dish)         // VIP discount, or full price
    create Order(customer, dish, bill)    // the bill is locked here

---

## Claude

Your pseudo code is mostly sound: the factory, the polymorphic loss and billing, and locking the bill at order time all match the spec. I found one gap against the spec and a few places where it could break when implemented.

## Issues to fix

**1. VIP has no special behaviour.**
The spec lists loss rate, bill and special behaviour as separate columns. Your VIP only has a loss rate (5) and a discount (the bill column), so it reads as missing the third. The Critic's "always orders the most demanding dish" covers it for the Critic. Give VIP something small that doesn't change the flow, for example:
- A dish preference through `chooseDish`, such as always ordering the Burger.
- A reaction hook, such as a satisfaction bonus when served within N turns of arriving.

Either way, document it.

**2. The "(type)" in the arrival message.**
`show "Customer #id (type) arrives"` is where a type check tends to sneak into the controller. Add a `typeLabel()` method to `Customer` that returns "Regular", "VIP" or "Critic", and have the controller print whatever it returns.

**3. Remove the hard-coded 8 from Stage 1.**
`applyWaitingLoss` must call `waitLossPerTurn()` instead of a constant. Check that `RegularCustomer` (or the base class) returns 8 and that no old constant like `WAIT_LOSS = 8` is still used anywhere. Likewise, move the Stage 1 rule `(id − 1) mod 3` into the base `Customer.chooseDish(menu)`. If the Waiter still computes the dish itself, the Critic override never takes effect.

**4. The Cashier must charge `order.bill`.**
Locking the bill at order time only works if payment adds `order.bill` to revenue. If the Cashier recomputes from `dish.price`, the VIP discount silently disappears. Check this explicitly.

**5. Validate the arrival sets in `GameConfig`.**
Your sets are fine (VIP {4}, Critic {10}, so Regular is {1, 7, 13, 16}). Add a startup assertion that the three sets are disjoint and union to `ARRIVAL_TURNS`, so a later edit can't produce a double or missing arrival.

## Checked and fine

- **Rounding:** `(price × 90 + 50) ÷ 100` is correct half-up integer math. The menu gives 810, 1350 and 1620 cents, all exact. My earlier suggestion of `price × 90 / 100` is equivalent here, so yours is the safer choice if prices change.
- **Critic's `chooseDish`:** the loop returns Pasta (2 units) on the base menu. It compares the first dish against itself, which is harmless.
- **Loss clamp:** `max(0, …)` with `= 0` means leaving is fine. A Critic who is never served leaves after 9 losses (100 → 4 → 0).
- **Locking the bill:** abandonment cancels the order, so the bill is never collected, which is correct.

## Feasibility check with your arrivals

Dishes by arrival id, with the Critic as #4:

| # | Type | Turn | Dish | Bill |
|---|------|------|------|------|
| 1 | Regular | 1 | Salad | $9.00 |
| 2 | VIP | 4 | Burger | $13.50 |
| 3 | Regular | 7 | Pasta | $18.00 |
| 4 | Critic | 10 | Pasta | $18.00 |
| 5 | Regular | 13 | Burger | $15.00 |
| 6 | Regular | 16 | Salad | $9.00 |

If all six pay, revenue is $82.50, so the $45.00 and 4-customer thresholds are comfortable. The tighter risk is the Chef's workload. Customers #3 and #4 both order 2-unit Pasta within 3 turns of each other, and the Critic loses 12 per turn. With optimal play, the Critic is served on turn 13 at satisfaction 64, which is above the 60 threshold only by a small margin. Check your average-satisfaction calculation on the victory run.

Remember to re-record the saved victory and defeat runs, because customer #4's dish changed from Salad to Pasta and every later output shifts with it.

When you send the code next, I'll look especially at `Customer`'s method signatures, the Cashier's payment path and where the controller prints types.

---

## User

Thanks for helping me point these out, I have updated the changes and checked that the game works, can you help me formatt the new features into a markdown formatt for me to add it into my README.md

---

## Claude

*[Attached file: `stage2-readme-section.md`, a Stage 2 README section covering the customer types table, arrival schedule, design notes and a run section.]*

I've written the Stage 2 section as `stage2-readme-section.md`, ready to paste into your README. It covers the customer types table, the arrival schedule, the design notes, and a run section.

Two parts are placeholders for you to fill in:
- **VIP special behaviour:** I don't know what you chose, so that cell says `[Describe your VIP behaviour here]`.
- **Running Stage 2:** add your run command and the names of the saved victory and defeat files.

Check the arrival table against your actual output too. I listed the VIP's dish as Burger, which follows the base `(n − 1) mod 3` rule. If your VIP behaviour changes the dish, update that row and the bill.

---

## User

Generate a chat history in a md file for this chat (prompts and responses)

---

## Claude
I've saved the whole conversation as `chat-history.md`, with each of your prompts and my replies in order under "User" and "Claude" headings.

For the README-formatting turn, the file only has a short note naming `stage2-readme-section.md` as the attachment, plus my reply text. The section itself is in that separate file.
