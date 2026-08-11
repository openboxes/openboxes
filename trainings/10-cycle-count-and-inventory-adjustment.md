# Scenario 10 — Cycle Counts & Inventory Adjustments

## Story

Something feels off: the system says you have 40 boxes of gauze in Bin A3, but nobody can remember the last time anyone counted it. Rather than waiting for a full annual physical inventory, you'll do a targeted **cycle count** of that bin and correct the record if reality doesn't match.

## Learning objectives

- Perform a cycle count on a specific bin/product.
- Understand quantity on hand (QoH) vs. quantity available.
- Create an inventory adjustment with a documented reason, and know when an adjustment is (and isn't) the right tool.

## Roles / permissions

Stocker, Manager. Larger adjustments may require Manager sign-off depending on your organization's internal controls.

## Prerequisites

- Scenario 02 completed (bins exist). Scenario 06 completed (there is stock at a specific bin to count).

## Walkthrough

1. Go to **Inventory > Cycle Count** and select the bin (or product) you want to count — in this case, Bin A3.
2. OpenBoxes shows the **system quantity** currently recorded for that bin/product/lot combination.
3. Physically count the actual stock in that bin.
4. Enter the **counted quantity**. If it matches the system quantity, record the count as confirmed and move on — this is itself valuable, since a "no discrepancy found" count still proves the record is trustworthy as of today.
5. If it doesn't match, OpenBoxes will prompt you toward an **inventory adjustment** to reconcile the difference. Enter a **reason code** (e.g. "Cycle count discrepancy," "Damaged," "Expired") — never leave an adjustment unexplained.
6. Save the adjustment. Confirm the product's quantity on hand at that bin now reflects the physical count.
7. Check the product's **electronic stock card** (transaction history) and find the adjustment you just made — this is the same history a colleague would check six months from now to understand why the numbers changed.

## Key concepts to reinforce

- **Quantity on Hand (QoH)** — the total recorded quantity at a location, including anything on hold, allocated to a requisition, or otherwise committed.
- **Quantity available** — QoH *minus* anything on hold or already committed/picked. This is the number that actually matters when deciding whether you can fulfill a new request — QoH alone can be misleading if a lot of stock is tied up.

## Checkpoints

- [ ] A cycle count was performed against a real bin/product and recorded, whether or not it found a discrepancy.
- [ ] If a discrepancy was found, an inventory adjustment exists with a specific reason code — not a generic placeholder.
- [ ] You can state, without checking, the difference between quantity on hand and quantity available, and give an example of when they'd differ.

## Common mistakes

- Using an inventory adjustment as a shortcut instead of investigating why a count is off (e.g. stock actually sitting in the wrong bin, uncorrected, rather than truly missing) — adjustments should reconcile *known* reality, not paper over an unresolved mystery.
- Adjusting quantities without a reason code, or with a vague one like "count," which is useless for spotting patterns later (e.g. repeated shrinkage in one bin pointing to a real problem).
- Confusing QoH with quantity available when deciding whether stock can be picked for an outbound request.

## Discussion questions

- If the same bin comes up short in three consecutive monthly cycle counts, what should happen beyond just adjusting the number each time?
- Why might a warehouse prefer frequent small cycle counts over one big annual physical inventory?

## Further reading

- https://help.openboxes.com/article/73-outbound-shipment-page-by-page-edit
- https://help.openboxes.com/article/39-electronic-stock-card
