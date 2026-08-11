# Scenario 03 — Product Catalog Management

## Story

Your organization has started distributing a new item — an electric syringe pump — and it doesn't exist in OpenBoxes yet. Nobody can order it, receive it, or count it until it's in the product catalog. You've been asked to create the product record correctly the first time, because every purchase order, shipment, and inventory transaction downstream depends on it.

## Learning objectives

- Understand what a product record is and why the product code matters.
- Create a new product with the key fields (code, name, category, unit of measure).
- Add a translated synonym for a product name.
- Understand formularies/catalogs, product families, and tags as optional grouping tools.

## Roles / permissions

Admin, Product Manager. Only users with these permissions can create or edit products — everyone else can search and view them.

## Prerequisites

- Scenario 01 completed.
- Access to **Configuration > Products** (or the **Products** module, depending on your menu configuration).

## Walkthrough

1. Go to the **Products** module and search first — by OB Code (the 4–5 digit auto-generated identifier), product name, vendor name, or manufacturer name — to confirm the syringe pump truly doesn't already exist under a different name or spelling. This single habit prevents most duplicate-product problems.
2. Select **Create Product** (or **Add Product**).
3. Fill in the core fields:
   - **Name** — the name your organization will use, e.g. "Syringe Pump, Electric."
   - **Category** — where it sits in your product hierarchy (used for reporting and ABC analysis later).
   - **Unit of Measure (UoM)** — how it is counted (each, box, case).
   - Optional identifiers: manufacturer name/code, vendor code, UNSPSC.
4. Save. Note the system-generated **product code** — this short alphanumeric code (e.g. `TS123`) is now the product's identifier everywhere in the system: purchase orders, shipments, inventory, reports.
5. Add a **synonym**: if a colleague in another locale would search for this product under a different name (for example, the Spanish name "Bomba de jeringa, eléctrica"), add the English synonym "Syringe Pump, Electric" so both spellings resolve to the same product record.
6. (Optional, if used at your organization) Add the product to a **formulary/catalog** so it's grouped with other products used at a specific facility or service line, and/or apply a **tag** for cross-cutting grouping (e.g. "cold chain").
7. Confirm the finished record by searching for it again using the OB Code, then by the synonym you just added.

## Checkpoints

- [ ] The new product exists and has a system-generated product code.
- [ ] The product can be found by searching its name, its code, and its synonym.
- [ ] You can state, without looking it up, what a product's UoM controls (how quantities of that item are counted/ordered).

## Common mistakes

- Creating a duplicate product because a search wasn't tried first — this fragments inventory history across two "different" items that are really the same thing.
- Confusing **Manufacturer name** (what the manufacturer calls the product) with **Manufacturer code** (the manufacturer's SKU) — these are two different fields.
- Skipping category assignment — this quietly breaks downstream reports (like ABC analysis) that depend on it.

## Discussion questions

- Why does OpenBoxes restrict product creation to Admin/Product Manager roles instead of letting any user add products on the fly?
- When would you use a product family versus a tag versus a formulary to group the same set of products?

## Further reading

- https://help.openboxes.com/article/65-intro-to-products
- https://help.openboxes.com/article/396-translate-product-names
- https://help.openboxes.com/article/374-product-tag
- https://help.openboxes.com/article/388-product-family-create-and-add
