# Scenario 04 — User & Role Management

## Story

Two new hires start next week: one will work the receiving dock, the other will approve purchase orders. As the administrator, you need to create their accounts, give them access to the right location(s), and assign roles that match what they're actually allowed to do — no more, no less.

## Learning objectives

- Create a new user account and change the default password.
- Understand OpenBoxes's role model: general/hierarchical roles vs. functional/operational roles.
- Assign a role to a user at a specific location (location roles).
- Deactivate a user instead of deleting them when someone leaves.

## Roles / permissions

Admin (or Superuser). User administration screens are hidden from every other role.

## Prerequisites

- Scenario 01 completed.
- You are logged in as a user with Admin/Superuser access.

## Background: the role model

OpenBoxes roles come in a few families (defined in `RoleType`):

- **General/hierarchical roles** — Superuser, Admin, Manager, Assistant, Browser — each broader than the last, used for overall system access level.
- **Warehouse operational roles** — Order Clerk, Picker, Packer, Receiver, Shipment Clerk, Stocker, Worker, Employee — map directly to physical warehouse jobs.
- **Purchasing roles** — Buyer, Purchase Approver.
- **Approval roles** — Requisition Approver.
- **Specialist roles** — Product Manager, Financial User, Invoice User, Pharmacist, Pharmacy Tech.
- **Party/organization roles** — Supplier, Manufacturer, Carrier, Donor, etc. (used for non-user parties, not staff accounts).

Roles are assigned **per location** — a user can be a Picker at Warehouse A and have no access at all to Warehouse B.

## Walkthrough

1. Go to **Configuration > Users** (or the **Users** module) and select **Create User**.
2. Fill in the new user's name, username, and contact details, and set a temporary password.
3. Save the user, then open their profile and go to **Location Roles** (or **Create Location Roles**).
4. Assign the receiving-dock hire the **Receiver** role (and **Stocker** if they'll also putaway) at your warehouse location.
5. Assign the second hire the **Purchase Approver** role (and **Order Clerk** if they'll also create orders, not just approve them) at the same or relevant location.
6. Save, then log in as (or ask each new user to log in as) themselves and confirm: they can only see the menus their role grants, and only for the location(s) you assigned.
7. Practice the reverse: when someone leaves the organization, use **Toggle Activation** to deactivate their account rather than deleting it — this preserves the audit trail of everything they did while active.
8. (If available in your instance) Practice **Change Password** to reset a user's forgotten password, and **Impersonate** (Admin-only, use sparingly) to see the system exactly as a specific user would, which is invaluable for diagnosing "I can't see X" support requests.

## Checkpoints

- [ ] Two new user accounts exist, each with a role appropriate to their job.
- [ ] Logging in as each new user shows only the menus/actions their role should allow.
- [ ] You can explain the difference between deactivating and deleting a user, and why OpenBoxes encourages the former.

## Common mistakes

- Assigning **Manager** or **Admin** by default "to be safe" instead of the specific operational role actually needed — this violates least-privilege and makes it harder to reason about who can do what.
- Forgetting that roles are per-location — assigning a role at the wrong location leaves the user with no visible access and looks like a bug.
- Deleting a departed employee's account instead of deactivating it, which can break historical reporting that references them.

## Discussion questions

- Why might a warehouse want separate Picker, Packer, and Shipment Clerk roles instead of one combined "Warehouse Worker" role?
- What's the risk of one person holding both Buyer and Purchase Approver at the same location?

## Further reading

- https://help.openboxes.com/category/24-users-and-people
- https://help.openboxes.com/article/31-configure-parties
