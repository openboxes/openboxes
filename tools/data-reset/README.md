# data-reset

Scripts for wiping transactional / work-in-progress data from an OpenBoxes
database so a UAT, demo, or sandbox environment can be returned to a known
state without a full DB restore.

**Inventory (on-hand stock, transactions, product_availability) is preserved
by the WIP scripts. It is *cleared* by the full transactional reset.**
All scripts are irreversible — back up first.

## Scripts

| Script | Purpose | Related ticket |
| --- | --- | --- |
| `reset-inbound-wip.groovy` | Clear inbound WIP at one facility: POs, inbound stock movements, incoming shipments, open putaway tasks, and their children. | [OBLS-875](https://openboxes.atlassian.net/browse/OBLS-875) |
| `reset-outbound-wip.groovy` | Clear outbound WIP at one facility: outbound stock movements, outgoing shipments, outbound returns/transfers, picklists, and their children. | [OBLS-878](https://openboxes.atlassian.net/browse/OBLS-878) |
| `reset-transactional-data.groovy` | Full transactional reset (Groovy console flavor): every order, shipment, receipt, requisition, picklist, invoice, cycle count, and inventory transaction. Master / reference / configuration data preserved. | [OBLS-762](https://openboxes.atlassian.net/browse/OBLS-762) |
| `reset-transactional-data.sql` | Same as above, as a raw SQL script for MySQL clients (mysql CLI, Workbench, DBeaver). | [OBLS-762](https://openboxes.atlassian.net/browse/OBLS-762) |
| `verify-wip-columns.sql` | Pre-run check: asserts every (table, column) touched by the WIP reset scripts exists in the target DB. Run before the WIP scripts. | — |
| `verify-reset-tables.sql` | Pre-run check: asserts every table touched by the full transactional reset exists in the target DB. Run before the full reset. | — |

## Safety model

All reset scripts are **no-ops unless you explicitly opt in**:

- Groovy scripts: `DRY_RUN = true` by default; prints per-table counts that
  *would* be deleted. Flip to `false` to actually run.
- `reset-transactional-data.sql`: aborts unless `@CONFIRM_RESET = 'YES'` is
  set (uncomment the line in the SAFETY GUARD block, or pass it via
  `--init-command`).

Each script runs inside a single DB transaction with `FOREIGN_KEY_CHECKS = 0`
so cross-reference join rows can be cleaned in any order.

## Usage

### WIP reset (per facility)

```
# 1. Back up the DB, or work against a restored copy.
# 2. Verify schema:
mysql <db> < verify-wip-columns.sql        # every row must show status = 'ok'

# 3. Open the OpenBoxes admin console (Configuration > Console),
#    paste reset-inbound-wip.groovy (or reset-outbound-wip.groovy):
#      FACILITY = "Boston Warehouse"       // location id or exact name
#      DRY_RUN  = true                     // review the counts printed
#
# 4. Flip DRY_RUN = false, re-run to perform the deletion.
# 5. Refresh the dashboard: inbound/outbound tiles should reflect cleared state.
```

### Full transactional reset

Groovy flavor (admin console):

```
# 1. Back up the DB. This is irreversible.
# 2. Verify schema:
mysql <db> < verify-reset-tables.sql
# 3. Paste reset-transactional-data.groovy into the console,
#    flip CONFIRM_RESET = "YES", run.
# 4. Restart the app so no stale entities are cached.
```

SQL flavor (any MySQL client):

```
mysql <db> < verify-reset-tables.sql
mysql --init-command="SET @CONFIRM_RESET='YES'" <db> < reset-transactional-data.sql
# restart the app
```

## Testing locally

See `../data-tools/` for helper scripts that fetch a production dump, restore
it into a local MariaDB, snapshot a baseline, and repoint the running app —
everything you need to iterate on these reset scripts safely against a real
dataset.
