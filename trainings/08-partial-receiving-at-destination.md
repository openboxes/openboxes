# Scenario 08 — Partial Receiving at Destination

## Story

The IV fluids shipped in Scenario 07 have arrived at the District Clinic — but the box was crushed in transit and two units are visibly damaged. As the Receiver at the clinic, you need to record exactly what actually arrived in usable condition, not just rubber-stamp the shipped quantity.

## Learning objectives

- Distinguish "Shipped" vs. "Partially Received" vs. "Received" status.
- Receive against a shipment with a quantity mismatch and document why.
- Understand how to complete receiving later for whatever wasn't received yet.

## Roles / permissions

Receiver.

## Prerequisites

- Partial receiving enabled for the destination location (an admin/config setting — confirm with your administrator before this session if it's not already on).
- Scenario 07 completed, or any shipment in **Shipped** status exists for your location.

## Walkthrough

1. Go to **Inbound > List Inbound Movements** at the destination location and find the shipment — it should show status **Shipped** (or **Partially Received** if someone already started this process).
2. Click the shipment identifier to open it, then select **Receive**.
3. For each line item, enter the **actual quantity received in usable condition**. For the damaged IV fluids, enter the reduced quantity, not the originally shipped quantity.
4. Add a **comment** on the discrepant line explaining what happened (e.g. "2 units damaged in transit, box crushed, discarded on arrival").
5. Update recipient information, bin location, lot number, or expiry date on this screen if needed and if your location tracks these.
6. On the review page, check the **Cancel Remaining** checkbox *only* if you are certain the missing quantity will never arrive (e.g. confirmed lost). Leave it unchecked if you expect the rest to show up later — this is the default and is what keeps the shipment open as **Partially Received** rather than closing it out incorrectly.
7. Select **Receive Shipment** to confirm. The status becomes **Partially Received** (since not everything shipped was received) or **Receiving**, depending on your version.
8. When (in a real scenario, days or weeks later) the remainder shows up — or if you're simulating it in the same session — repeat steps 1–7 against the same shipment. Once every unit shipped has been accounted for (received or explicitly cancelled), the status becomes fully **Received**.

## Checkpoints

- [ ] The shipment shows **Partially Received**, not **Received**, after step 7 (because you deliberately under-received to simulate damage).
- [ ] The damaged-quantity line has an explanatory comment.
- [ ] You can explain what the **Cancel Remaining** checkbox does and why you left it unchecked in this exercise.
- [ ] You've either completed the second receiving pass to close the shipment, or can explain exactly what a colleague would need to do to close it later.

## Common mistakes

- Checking **Cancel Remaining** reflexively "to clean things up," which permanently writes off quantity that might still be legitimately in transit.
- Receiving the full shipped quantity despite visible damage, because it's faster than documenting the discrepancy — this corrupts your on-hand inventory and hides a supplier/carrier quality issue that should be tracked.
- Not realizing partial receiving must be enabled for the location first, and getting confused when the expected workflow doesn't appear.

## Discussion questions

- Who else in the organization (buyer, supplier relationship manager, finance) might care about the "2 units damaged in transit" comment, and how would they find it later?
- How would this scenario differ if the damaged units were still physically present but you wanted to quarantine rather than discard them? (Preview of Scenario 11 — Hold Bins.)

## Further reading

- https://help.openboxes.com/article/300-partial-receiving
