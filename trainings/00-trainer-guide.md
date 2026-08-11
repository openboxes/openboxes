# Trainer Guide

Use this guide to plan and facilitate the scenarios in this directory. It is written for whoever is running the training session (an implementation lead, super-user, or team manager), not for the trainee.

## 1. Environment setup (do this before any session)

- Use a **non-production, demo, or staging instance** of OpenBoxes for training. Never train on live production data — trainees will create, edit, and delete real records.
- Default demo credentials are `admin / password` (per [`docs/user-guide/onboarding`](../docs/user-guide/onboarding/index.md)). Change this password immediately if the instance is reachable from outside your training room.
- Before the first session, seed the instance with:
  - At least two **locations** of type Warehouse/Depot (e.g. "Central Warehouse" and "District Clinic"), plus one **Supplier** location.
  - A handful of **products** across at least two categories, including one product with lot/expiry tracking enabled (needed for Scenarios 05, 06, 08, 10, 11).
  - One **role per training track** already created (see role map below) so trainees can be assigned realistic permissions rather than all training as Superuser.
- Create one OpenBoxes user account per trainee ahead of time (Scenario 04 covers how, in case you want the *admin* trainees to do this themselves as their first exercise instead).

## 2. Role map — which RoleType to assign each trainee

OpenBoxes roles are defined in `RoleType` (`src/main/groovy/org/pih/warehouse/core/RoleType.groovy`) and assigned per-location via *Location Roles*. Map trainees to roles based on their real job:

| Job function | OpenBoxes role(s) | Scenarios to run |
|---|---|---|
| Warehouse floor staff (picking) | Picker | 07 |
| Warehouse floor staff (packing/shipping) | Packer, Shipment Clerk | 07 |
| Receiving dock staff | Receiver, Stocker | 05, 06, 08, 11 |
| Purchasing/procurement | Buyer, Purchase Approver | 05, 09 |
| Facility staff requesting stock | Order Clerk, Requestor | 07, 09 |
| Approvers/supervisors | Requisition Approver, Manager | 07, 09, 10 |
| Catalog/master data owner | Product Manager | 03 |
| System administrator | Admin, Superuser | 02, 03, 04 |
| Read-only stakeholder (e.g. donor, auditor) | Browser | 01 only |

A trainee may hold more than one role if that mirrors their real responsibilities (common in small warehouses).

## 3. Session format

Each scenario is designed to run in **20–45 minutes**, including discussion. Suggested flow per scenario:

1. **Set the scene (2 min)** — read the Story aloud.
2. **Live demo (5–10 min)** — you perform the Walkthrough once on screen.
3. **Guided practice (10–20 min)** — trainees repeat the Walkthrough on their own login.
4. **Checkpoints (5 min)** — confirm each trainee actually hit the listed checkpoints (don't just ask "did that work?" — have them show you the resulting screen, e.g. the shipment status badge or the updated quantity on hand).
5. **Discussion (5 min)** — work through the Discussion Questions as a group; these surface edge cases the Walkthrough doesn't cover.

## 4. Suggested cohort schedules

- **New warehouse hire (1 day):** 01 → 05 → 06 → 07 → 08 → 12 (capstone as a wrap-up with the whole cohort).
- **New admin/implementer (half day):** 01 → 02 → 03 → 04.
- **New procurement/planning hire (half day):** 01 → 05 → 09 → 10.
- **Whole-team kickoff (multi-day rollout):** run 01 with everyone together, then split into role tracks for 02–11 in parallel across different trainers/rooms, and reconverge for 12.

## 5. Assessing readiness

A trainee is ready to work unsupervised in a given area when they can, **without prompting**:

- Locate the correct menu/module for the task without you pointing at the screen.
- Explain in their own words what status a document should be in before and after their action (e.g. "a shipment must be Shipped or Partially Received before I can receive against it").
- Identify at least one thing that would go wrong if they skipped a step (this is what the Common Mistakes sections train for).
- Know where to look up help themselves (the in-app help links or https://help.openboxes.com) rather than needing you.

## 6. Facilitator notes on data hygiene

Training inevitably creates junk data. Before returning a shared demo instance to "clean" state:

- Cancel/void any Purchase Orders, Shipments, or Requisitions created during training rather than leaving them in an ambiguous in-progress state for the next cohort.
- Deactivate (don't delete) any bin/zone locations or users created for the session — OpenBoxes prevents deleting anything already referenced in a transaction, so deactivation is the standard cleanup path.
- If cycle counts or inventory adjustments were practiced (Scenario 10), reconcile or reset quantities so the next cohort starts from known values.
