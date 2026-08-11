# Scenario 09 — Stocklists & Replenishment

## Story

Every month, the District Clinic sends the Central Warehouse the same rough list of consumables — gloves, gauze, IV fluids. Instead of building that request from scratch every time, you'll set up a **stocklist** so the clinic (or the warehouse, depending on the model) can generate the request automatically based on a plan you define once.

## Learning objectives

- Create a stocklist with header and line-item detail.
- Understand the difference between PUSH and PULL replenishment.
- Publish a stocklist so it becomes usable for generating real requests.

## Roles / permissions

Manager, Order Clerk.

## Prerequisites

- Scenario 03 completed (products exist). Origin and destination locations exist.

## Walkthrough

1. Go to **Stock Lists** and select **Create Stock List**.
2. Fill in the header:
   - **Name** — descriptive, e.g. "Peds ward monthly consumables."
   - **Origin** — the sending depot (Central Warehouse).
   - **Destination** — the receiving location (District Clinic).
   - **Managed by** — the user responsible for this list.
   - **Replenishment period** — days between replenishments (15 = biweekly, 30 = monthly).
   - **Replenishment type** — **PUSH** or **PULL** (see below).
   - **Sort by** — date, category, or sort index, for how items display.
   - **Description** — optional notes.
3. Save the header, then select **Add** to add line items: enter a product code, confirm the match, set its **Unit of Measure**, and — for PUSH lists only — its **Max quantity**.
4. Repeat for every product the destination regularly needs, then **Save**.
5. Select **Publish** to activate the stocklist. An unpublished stocklist is a draft only — it won't generate real requests.
6. Generate a request from the stocklist (or wait for the next replenishment cycle, depending on your workflow) and confirm it produces a stock movement pre-filled with the expected items — compare this to building the same request manually in Scenario 07 to feel the time savings.

## PUSH vs. PULL — make sure trainees can explain both

- **PUSH**: you (the supplying side) decide how much to send, based on your own demand predictions. You manually set a **max quantity** per item; the list won't exceed that at any single replenishment.
- **PULL**: the system decides how much to send by looking at the **quantity on hand (QoH)** at the destination and its recent demand — it autofills the resupply quantity needed to bring the destination back up to its target. This needs roughly 2–3 months of consumption history to work well; a brand-new destination doesn't have that yet, so PUSH is usually the right starting choice until enough history accumulates.

## Checkpoints

- [ ] A stocklist exists with a defined origin, destination, and replenishment period, and has been published.
- [ ] At least one product line was added with an appropriate UoM (and Max quantity if PUSH).
- [ ] You can correctly explain, in your own words, when you'd choose PUSH over PULL for a new destination.

## Common mistakes

- Choosing PULL for a brand-new facility with no consumption history yet, which produces meaningless auto-calculated quantities.
- Forgetting to **Publish** after building the list, then wondering why nothing happens on the replenishment cycle.
- Setting a PUSH max quantity far above what the destination can actually store or use before the next cycle, leading to overstock and possible expiry.

## Discussion questions

- If a facility's demand pattern is highly seasonal (e.g. malaria supplies spiking in rainy season), how well would PULL replenishment handle that on its own?
- Who should own updating a stocklist's line items over time as a facility's needs change?

## Further reading

- https://help.openboxes.com/article/54-create-a-stocklist
- https://help.openboxes.com/article/362-intro-to-stock-list
- https://help.openboxes.com/article/52-intro-to-stocklists
