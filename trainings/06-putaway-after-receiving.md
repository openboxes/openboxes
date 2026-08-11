# Scenario 06 — Putaway After Receiving

## Story

The exam gloves from Scenario 05 have been received at the dock, but they're still sitting on a pallet by the door. Nobody can find them or pick them for an outbound order until they're actually shelved and OpenBoxes knows where. As a Stocker, your job is to move them to their bin and record that in the system.

## Learning objectives

- Understand putaway as the step that connects "received" inventory to a specific bin location.
- Generate and use a putaway list/document.
- Confirm putaway and see the resulting quantity-on-hand reflected at the bin level.

## Roles / permissions

Receiver, Stocker.

## Prerequisites

- Scenario 02 completed (bin locations exist to put stock into).
- Scenario 05 completed (there is received inventory waiting for putaway).

## Walkthrough

1. Go to the **Putaway** module (often reached directly from the just-completed receipt, or via **Inventory > Putaway**).
2. Locate the receipt you just processed in Scenario 05. OpenBoxes lists the items received but not yet assigned to a bin.
3. For each line item, assign a destination **bin location** — pick one appropriate to the product (e.g. the cold room zone/bin for cold-chain items, general shelving for everything else).
4. Generate/print the putaway list (PDF) if your team uses paper lists on the floor, and physically move the stock to the assigned bins.
5. Confirm putaway in the system once the physical move is done — this is what actually updates quantity-on-hand *at that bin location*, not just at the location level.
6. Verify: go to **Inventory > Browse** (or the electronic stock card for the product) and confirm the received quantity now shows against the correct bin.

## Checkpoints

- [ ] Every line item from the Scenario 05 receipt has been assigned a bin.
- [ ] The product's stock card/inventory browse view shows the quantity at the specific bin, not just "somewhere in the warehouse."
- [ ] You can explain the difference between "received" and "put away" — a shipment being Received does not by itself mean staff can find the stock on a shelf.

## Common mistakes

- Treating receiving and putaway as the same step and skipping putaway entirely — this leaves inventory technically "in the system" but practically unfindable, which is often worse than not having it in the system at all.
- Putting cold-chain or hazardous items into a general bin because the correct zone wasn't set up ahead of time (this is why Scenario 02 comes before this one).
- Forgetting to confirm putaway after the physical move, leaving the system out of sync with the floor.

## Discussion questions

- What problems would a warehouse have if it received stock in the system correctly but regularly skipped or delayed putaway?
- How would you handle putaway for a product that needs to be split across two different bins because one bin doesn't have enough room?

## Further reading

- https://help.openboxes.com/article/289-managing-bin-and-zone-locations
- ../docs/api-guide/inbound/putaway.md
