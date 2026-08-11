# Scenario 11 — Hold Bins & Quality Holds

## Story

Your supplier just emailed: a specific lot number of IV fluids is under investigation and may be recalled. You have some of that lot in your warehouse right now, sitting in a normal picking bin. You need to physically and systemically isolate it *today*, before it accidentally gets picked for tomorrow's outbound shipment.

## Learning objectives

- Understand what a hold bin is and why it's different from a normal bin.
- Set up a hold bin location type and a hold bin (admin task, one-time setup).
- Move affected inventory into a hold bin and confirm it can no longer be picked.
- Release a hold once the issue is resolved.

## Roles / permissions

Manager, Receiver, Admin (for the one-time location-type setup).

## Prerequisites

- Scenario 02 completed (you understand zones/bins). A location and some inventory in a normal bin exist.

## Walkthrough

### Part A — One-time setup (Admin)

1. Go to **Configuration > Location Types** and select **Add Location Type**.
2. Set the **Location Type Code** to `BIN_LOCATION`, name it descriptively (e.g. "Depot Hold Bin"), and select **Hold Stock** as its supported activity. Save.
3. Go to **Configuration > Locations**, open your warehouse, select **Edit Location**, then the **Bin Location** tab, and **Add Bin Location**. Choose the hold-bin location type you just created, name it (e.g. "HOLD-01"), and save.

### Part B — Using the hold bin (this is the part every warehouse user should know how to do)

4. Locate the affected lot in **Inventory > Browse** — filter by the specific lot number from the supplier's notice.
5. Move that inventory into the hold bin, either through an inventory transfer/adjustment to the hold bin location, or through your organization's documented recall procedure.
6. Confirm the effect: try to pick that lot for a test outbound movement (Scenario 07 mechanics) and verify it does **not** appear as available — inventory placed in hold bins is not available to be picked for shipments.
7. Document why the hold was placed (supplier notice, date, lot number, investigating authority if relevant) somewhere durable — a comment on the transaction, and/or your organization's quality log.
8. When the supplier confirms the lot is cleared (or, alternately, confirms a real recall and the stock must be destroyed/returned), reverse the process: move the stock back to a normal bin (cleared) or process it as a loss/return (confirmed recall) — either way, document the resolution the same way you documented the hold.

## Checkpoints

- [ ] A hold bin exists at your warehouse.
- [ ] The specific affected lot has been physically and systemically moved into it.
- [ ] You verified — don't just assume — that the held lot cannot be picked for a shipment.
- [ ] The reason for the hold, and its eventual resolution, are both documented.

## Common mistakes

- Only handling the hold "in the system" without physically moving the stock, or vice versa — the two must match, or someone on the floor could still physically grab and ship held product.
- Forgetting to document *why* stock was held, which becomes a serious problem if a real recall is later confirmed and you need to trace exactly what happened to that lot.
- Never revisiting a hold — stock quietly sits in HOLD-01 for a year because nobody closed the loop with the supplier.

## Discussion questions

- What's the difference in urgency and process between a hold bin (temporary, may be released) and destroying/returning confirmed recalled stock?
- How would you find every other location in your network that might also have received the same affected lot?

## Further reading

- https://help.openboxes.com/article/67-creating-hold-bins
