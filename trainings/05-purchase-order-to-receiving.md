# Scenario 05 — Purchase Order → Receiving

## Story

The Central Warehouse is running low on exam gloves. As the Buyer, you need to order more from your regular supplier, get the order approved, and — once the truck actually arrives — get it checked in against what was ordered.

## Learning objectives

- Create a purchase order (PO) against a supplier location.
- Understand the approval step and why it exists.
- Receive an inbound shipment against a PO and reconcile ordered vs. received quantities.
- Recognize the shipment status lifecycle (Shipped → Receiving/Partially Received → Received).

## Roles / permissions

Buyer (create), Purchase Approver (approve), Receiver (receive). In a small team one person may hold all three roles; in a larger org they are deliberately separated as an internal control.

## Prerequisites

- Scenario 03 completed (the product being ordered must exist in the catalog).
- A Supplier location exists (Scenario 02 covers creating locations generally).

## Walkthrough

### Part A — Create and approve the order

1. Go to **Inbound > Purchase Orders** (or **Procurement**) and select **Create Purchase Order**.
2. Choose the **supplier** location, the **destination** (your warehouse), and add the requested product(s), quantities, and expected unit price.
3. Save the order as a draft, then submit it for approval.
4. Switch to (or hand off to) the **Purchase Approver**, who reviews the line items and either approves or sends it back with comments. Only once approved should the order be placed with the supplier.
5. Mark the order as **placed** — this is the point of no return where the supplier is expected to start fulfilling it.

### Part B — Receive the shipment when it arrives

6. When the supplier ships, a corresponding record appears under **Inbound > List Inbound Movements**, with status **Shipped**.
7. Click the shipment identifier to open its detail page, then select **Receive**.
8. For each line item, enter the **actual quantity received** — this is what determines whether the shipment ends up marked **Received** (everything matched) or **Partially Received** (something didn't).
9. If a quantity differs from what was shipped, add a **comment** explaining the discrepancy (e.g. "1 case damaged in transit, discarded by receiving") — this comment becomes part of the permanent record.
10. Update lot numbers, expiry dates, and bin locations if your instance tracks them and you're ready to also do putaway now (see Scenario 06 for putaway as its own step).
11. Review the summary page, then confirm with **Receive Shipment**.

## Checkpoints

- [ ] A purchase order exists, was approved, and shows as placed.
- [ ] An inbound shipment/movement exists against that order and was received.
- [ ] You can state the shipment's current status (Received vs. Partially Received) and why.
- [ ] Any quantity discrepancy has an explanatory comment attached.

## Common mistakes

- Receiving without checking the packing slip/physical count first — the system will faithfully record whatever number you type, right or wrong.
- Leaving a discrepancy unexplained — six months later, nobody (including you) will remember why 2 units went missing.
- Skipping the approval step by having the Buyer also "approve" their own order out of habit — this defeats the purpose of having two roles.

## Discussion questions

- Why does OpenBoxes separate "placed" from "approved" instead of treating a submitted PO as automatically active?
- If a shipment is only partially received today, what happens the next time more of it arrives? (Preview of Scenario 08.)

## Further reading

- https://help.openboxes.com/article/300-partial-receiving
- https://help.openboxes.com/category/16-purchasing
