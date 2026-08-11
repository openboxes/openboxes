# Scenario 02 — Locations, Zones & Bins

## Story

Your organization is opening a new storage room for cold-chain items inside the Central Warehouse. Before anyone can put stock away, someone with admin access needs to model that physical space in OpenBoxes: the warehouse itself, the new zone inside it, and the individual bins/shelves within that zone.

## Learning objectives

- Understand the storage hierarchy: **Depot (Location) → Zone Location → Bin Location Type → Bin Location**.
- Create a new zone and bin locations, individually and via bulk import.
- Understand location types and "supported activities" (what a location is allowed to do — receive, ship, manage inventory).

## Roles / permissions

Admin, Manager. Configuration menus are only visible to roles with configuration access.

## Prerequisites

- An existing depot/warehouse location to add zones/bins to (created previously, or use a seeded demo location).
- Scenario 01 completed.

## Walkthrough

1. Go to **Configuration > Locations** and open the depot you're adding storage to (e.g. "Central Warehouse").
2. Confirm the location's **type and supported activities** are correct for how it's used — e.g. a warehouse that both receives and manages inventory needs those activities enabled; a pure pass-through "ward" or "dispensary" location only receives stock and never actively manages it.
3. Open the **Zone Locations** tab and add a new zone (e.g. "Cold Room 1"). A zone is just a named area/room within the depot that will contain multiple bins.
4. Open the **Bin Locations** tab. Add a bin individually: choose a bin location type, give it a name (e.g. "CR1-Shelf-A1"), and assign it to the "Cold Room 1" zone.
5. For a larger batch, use **Import Bin Locations**: download the Excel (.XLT) template, fill in the "Bin" and "Zone" columns for every shelf/pallet position you need, and upload it. This is the realistic approach for standing up a whole new room at once instead of adding shelves one at a time.
6. Edit an existing bin: rename it, reassign it to a different zone, or mark it inactive. Try deleting a bin that has never been used (should succeed) versus one you know has inventory history (OpenBoxes will block the delete and require deactivation instead) — this is a deliberate safety feature, not a bug.

## Checkpoints

- [ ] A new zone exists under the target depot.
- [ ] At least one bin location exists inside that zone, created individually.
- [ ] You have downloaded and understood the bin-location import template, even if you don't run a real bulk import during training.
- [ ] You can explain why OpenBoxes refuses to delete a bin that's been used in a transaction.

## Common mistakes

- Creating bins without assigning them to a zone — they'll exist but won't be organized the way staff expect on the picklist/putaway screens.
- Forgetting to enable the right "supported activity" on a location, which then makes it invisible in workflows that expect that activity (e.g. a location that can't receive won't show up as a destination on an inbound shipment).
- Trying to delete instead of deactivate a bin/zone that has transaction history.

## Discussion questions

- Why might a warehouse want a separate zone for cold-chain items instead of just labeling individual bins "cold"?
- What's the risk of importing a bin-location spreadsheet with a typo in the "Zone" column?

## Further reading

- https://help.openboxes.com/article/289-managing-bin-and-zone-locations
- https://help.openboxes.com/article/33-location-type-and-supported-activities
- https://help.openboxes.com/category/23-locations
