# OpenBoxes New User Training

This directory contains a self-contained training program for new OpenBoxes users. It is built around realistic, role-based scenarios that mirror the actual screens and workflows in the application, cross-referenced with the official [OpenBoxes Help Center](https://help.openboxes.com) and the [User Guide](../docs/user-guide/index.md).

## Who this is for

Anyone being onboarded to use OpenBoxes day-to-day: warehouse staff (pickers, packers, receivers, stockers), order clerks and buyers, approvers and managers, and system administrators. It assumes no prior OpenBoxes experience, but does assume the trainee has basic computer/browser literacy.

## How to use this program

1. Start with **[00-trainer-guide.md](00-trainer-guide.md)** if you are facilitating training — it covers environment setup, role mapping, and how to run each session.
2. New users should read **[01-login-navigation-dashboard.md](01-login-navigation-dashboard.md)** first regardless of their eventual role.
3. Pick the scenarios that match the trainee's job function using the role map below, working through them in order — later scenarios assume data created in earlier ones.
4. Finish with **[12-capstone-end-to-end-supply-chain-day.md](12-capstone-end-to-end-supply-chain-day.md)**, a combined exercise that plays out a full supply chain cycle across multiple trainees/roles in one sitting.

Each scenario file follows the same format: **Story → Objectives → Roles/Permissions → Prerequisites → Walkthrough → Checkpoints → Common Mistakes → Discussion Questions → Further Reading**.

## Scenario map

| # | Scenario | Primary roles |
|---|----------|----------------|
| 01 | [Login, Navigation & Dashboard](01-login-navigation-dashboard.md) | Everyone |
| 02 | [Locations, Zones & Bins](02-locations-and-bin-setup.md) | Admin, Manager |
| 03 | [Product Catalog Management](03-product-catalog-management.md) | Admin, Product Manager |
| 04 | [User & Role Management](04-user-and-role-management.md) | Admin |
| 05 | [Purchase Order → Receiving](05-purchase-order-to-receiving.md) | Buyer, Purchase Approver, Order Clerk |
| 06 | [Putaway After Receiving](06-putaway-after-receiving.md) | Receiver, Stocker |
| 07 | [Internal Stock Request: Pick, Pack, Ship](07-internal-stock-request-pick-pack-ship.md) | Order Clerk, Picker, Packer, Shipment Clerk, Requisition Approver |
| 08 | [Partial Receiving at Destination](08-partial-receiving-at-destination.md) | Receiver |
| 09 | [Stocklists & Replenishment](09-stocklist-and-replenishment.md) | Manager, Order Clerk |
| 10 | [Cycle Counts & Inventory Adjustments](10-cycle-count-and-inventory-adjustment.md) | Stocker, Manager |
| 11 | [Hold Bins & Quality Holds](11-hold-bins-and-quality-hold.md) | Manager, Receiver |
| 12 | [Capstone: End-to-End Supply Chain Day](12-capstone-end-to-end-supply-chain-day.md) | All roles (group exercise) |

## Reference material

- Glossary of terms: [`docs/user-guide/glossary.md`](../docs/user-guide/glossary.md)
- Onboarding checklist: [`docs/user-guide/onboarding/index.md`](../docs/user-guide/onboarding/index.md)
- Full self-service knowledge base: https://help.openboxes.com
- Demo login (non-production/demo instances only): `admin` / `password`

Every scenario links to the specific help.openboxes.com article(s) it is based on — use those as the "read more" material if a trainee wants more depth than the scenario provides.
