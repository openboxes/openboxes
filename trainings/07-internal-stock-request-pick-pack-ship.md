# Scenario 07 — Internal Stock Request: Pick, Pack, Ship

## Story

The District Clinic has run low on IV fluids and needs a resupply from the Central Warehouse. This is not a purchase from a supplier — it's an internal transfer between two locations you already manage. You'll play through the whole chain: the clinic's request, the warehouse's approval, picking the actual stock, packing it, and shipping it out.

## Learning objectives

- Create an outbound stock movement (internal transfer) between two locations you manage.
- Understand "committed" inventory and why picked stock is no longer available for other transactions.
- Use AutoPick, manually edit a pick, and generate a picklist.
- Pack and send the shipment, and understand what happens to its status next.

## Roles / permissions

Order Clerk / Requestor (creates the request), Requisition Approver (approves), Picker (picks), Packer (packs), Shipment Clerk (ships).

## Prerequisites

- Scenario 01 completed. Two locations exist (source warehouse and destination clinic/facility). Products with inventory on hand exist at the source (Scenarios 03, 05, 06).

## Walkthrough

### Part A — Request and approve

1. Go to **Outbound > Create Stock Movement** (or, if your instance uses the classic flow, **Requisitions > Create**).
2. Set the **origin** (Central Warehouse) and **destination** (District Clinic), and add the requested products and quantities.
3. Submit the request. A **Requisition Approver** reviews it — they may approve as requested, adjust quantities (e.g. if requested quantity exceeds what should reasonably be sent), or choose a substitute product if the exact one requested is out of stock.

### Part B — Pick

4. Once approved, open the movement's **Pick** page. OpenBoxes auto-generates picks using a "first expiring lot" strategy (ties broken by picking from the bin with the smaller quantity first) — this is deliberate, so older stock moves before newer stock.
5. Review the AutoPick selection. If you need to override it — say, a specific bin is hard to reach right now — click **Edit** on that line, choose a different lot/bin in the modal, and save.
6. If you change your mind, use **Undo** to revert a line back to its AutoPick selection, or **Refresh AutoPick** to recompute picks against current availability.
7. Generate and print the **picklist** (use **Sort by Bin** so warehouse staff walk an efficient route instead of jumping around by item code).
8. Physically pick the stock. Note that **the moment items are picked, they become "committed"** — they're subtracted from what's available for any other outbound movement, even though they're still physically in the warehouse until shipped.
9. Click **Next** to advance toward packing/sending.

### Part C — Pack and send

10. On the pack page, confirm the physically packed contents match the pick (box/pallet counts, etc.).
11. Generate shipment documents (packing list, etc.) as needed.
12. Select **Send Shipment**. The movement's status changes to reflect it is now in transit to the destination, and it will appear on the destination's **Inbound** list for receiving (Scenario 08).

## Checkpoints

- [ ] A stock movement exists with the correct origin/destination and was approved before picking began.
- [ ] You can explain what "committed inventory" means and when it first applies (at picking, not at shipping).
- [ ] A picklist was generated and at least one line item's pick was manually edited and then undone, so you've seen both paths.
- [ ] The shipment was sent and is now visible from the destination side as an inbound movement.

## Common mistakes

- Letting picked-but-unshipped movements sit indefinitely — since picked stock is committed, a forgotten movement quietly locks up inventory that other outbound requests can't use. Review and delete/cancel stale movements regularly.
- Overriding AutoPick's lot selection without a good reason, which undermines FEFO (first-expired-first-out) stock rotation and increases the risk of expired product accumulating in the back of a bin.
- Sending a shipment before actually verifying the physical pack matches the picklist.

## Discussion questions

- Why does committing inventory at pick time (rather than at ship time) matter for a warehouse running multiple outbound movements in parallel?
- What's the operational risk of a Picker also being the Requisition Approver for the same request?

## Further reading

- https://help.openboxes.com/article/74-outbound-shipment-page-by-page-pick
- https://help.openboxes.com/article/73-outbound-shipment-page-by-page-edit
- ../docs/api-guide/outbound/stockMovement.md
