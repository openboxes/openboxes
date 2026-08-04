# Parts Master → Inventory (OBLS-903)

A standalone Groovy script that transforms an Excede **Parts Master** CSV export into
OpenBoxes **inventory import** files (quantity-on-hand baseline), split into batches,
with an option to upload each batch directly to an OpenBoxes instance.

Built for UAT reconciliation: load real quantity-on-hand from Excede into OpenBoxes so
allocation failures (`IllegalArgumentException: Insufficient stock`) can be attributed to
genuine stock-outs rather than QoH drift between the two systems.

## What it produces

From one Parts Master CSV it writes, into `--output-dir`:

| File | Purpose |
|------|---------|
| `inventory_batch_NNN.xls` | Matches the OpenBoxes inventory import template. Upload manually via **Record Inventory → Import Inventory**. |
| `inventory_batch_NNN.csv` | For the API path (`POST /api/facilities/{id}/inventories/import`, `text/csv`). Also what `--upload` sends. |
| `skipped-records.csv` | Every source row that was dropped, with the reason and the original data — nothing is silently discarded. |

Which files appear depends on `--format` (`xls`, `csv`, or `both` — the default).

## Requirements

- Java + Groovy 2.5 / 3.x (`groovy` on your `PATH`).
- Internet access on first run — [Grape](https://groovy-lang.org/grape.html) downloads Apache POI.

## Quick start

```bash
# 1. Inspect the CSV and see how columns are guessed (writes nothing)
groovy PartsMasterToInventory.groovy --input partsmaster.csv --list-columns

# 2. Dry run: parse, filter, batch, report counts (writes nothing)
groovy PartsMasterToInventory.groovy --input partsmaster.csv --dry-run

# 3. Generate batched .xls + .csv files
groovy PartsMasterToInventory.groovy --input partsmaster.csv --output-dir out --batch-size 100

# 4. Generate and upload directly via the API (only raises stock, never zeroes)
groovy PartsMasterToInventory.groovy --input partsmaster.csv --format csv --upload \
       --url https://vvg.openboxes.com/openboxes \
       --facility-id <locationId> \
       --session-cookie "JSESSIONID=..."
```

## Column mapping

The script maps Parts Master columns onto the OpenBoxes inventory columns. It first
**auto-detects** by header name (case-insensitive, punctuation-insensitive), then applies
any explicit overrides. Only **`productCode`** and **`quantity`** are required; everything
else is optional and left blank when absent.

| OB field | Meaning | Auto-detected from headers like |
|----------|---------|--------------------------------|
| `productCode` | OpenBoxes product code (must already exist) | PartNo, Part Number, SKU, Item, Product Code |
| `quantity`    | **Quantity on hand** (absolute target) | QtyOnHand, Quantity On Hand, QOH, On Hand, Qty |
| `productName` | Informational only | Description, Product Name, Name |
| `binLocation` | Bin (must already exist in the depot) | Bin, Bin Location, Shelf |
| `lotNumber`   | Lot / batch | Lot, Lot Number, Batch |
| `comments`    | Per-row comment | Comment, Note, Remark |
| `sourceFacility` | Warehouse column used **only** for `--source-facility` filtering | Warehouse, Facility, Site, Branch |

Run `--list-columns` to see exactly what was guessed. Override anything with `--mapping`:

```bash
--mapping productCode=PartNo,quantity=QtyOnHand,binLocation=BinLoc
```

If a required column can't be detected, the script stops and prints the available columns.

## How OpenBoxes interprets the quantity (important)

- **Quantity is absolute, not a delta.** OpenBoxes reads the current QoH from the database
  and creates an adjustment for the difference (`target − current`). The script always puts
  the Parts Master quantity in the **Physical QOH** column and leaves **OB QOH** blank.
- **Zero and negative quantities are skipped by default.** Excede rows with QoH ≤ 0 don't
  add stock, and importing a huge number of them creates needless transaction churn. Use
  `--include-zero` to keep zero rows (negatives are always skipped — OpenBoxes rejects them).
- **The product must already exist** in OpenBoxes (matched by `productCode`). Rows for
  unknown products will be rejected **at import time** by OpenBoxes, not by this script.
- **Bin / lot are left blank by default.** A bin or lot that doesn't already exist in the
  target depot causes OpenBoxes to reject that row. Only map them (or use `--default-bin`)
  when you know they exist.

### ⚠️ Batching and the per-product zero-out (XLS / UI path)

When you import a file through the **UI (`.xls`)**, OpenBoxes reconciles per product: for
every product in the file, any **other** bin/lot of that product that is **not** in the same
file is set to zero. To stay safe, the script **never splits one product's rows across two
batches**. If you hand-edit the batches, preserve that rule.

The **API path (`.csv` / `--upload`)** is gentler: it only imports rows that **increase**
stock and never zeroes anything, which is why it's the recommended option for UAT.

Each imported file becomes its own baseline + adjustment transaction pair, so batching also
avoids one giant inventory transaction.

## Uploading

The API path posts each CSV batch to:

```
POST {url}/api/facilities/{facility-id}/inventories/import
Content-Type: text/csv
```

Authentication is session-cookie based (OpenBoxes has no API-key mechanism). Provide **one** of:

- `--session-cookie "JSESSIONID=..."` — copy it from a logged-in browser session (DevTools →
  Application → Cookies). Simplest and most reliable.
- `--username <u> --password <p>` — the script form-logs-in at `{url}/auth/handleLogin` and
  reuses the returned session.

`--facility-id` is the OpenBoxes **location id** to import into (the depot whose inventory
you're setting).

## All options

```
--input <file>            Parts Master CSV (required)
--list-columns            Print CSV headers + guessed mapping, then exit
--dry-run                 Parse, filter, batch; report counts but write nothing

--mapping <k=v,...>       Override column mapping, e.g. productCode=PartNo,quantity=QtyOnHand
--source-facility-col <h> Header of the warehouse/facility column
--source-facility <val>   Keep only source rows whose facility column == val

--output-dir <dir>        Output directory (default: inventory-output)
--format <xls|csv|both>   xls = manual UI import, csv = API import (default: both)
--batch-size <n>          Rows per batch; products never split (default: 100; 0 = one file)
--include-zero            Keep rows with quantity <= 0 (default: skip)
--default-bin <name>      Force a bin location for every row (default: blank)
--comment <text>          Comment for rows without their own comment

--upload                  Upload each batch (CSV/API path)
--url <baseUrl>           e.g. https://vvg.openboxes.com/openboxes
--facility-id <id>        OpenBoxes location id to import into
--session-cookie <c>      "JSESSIONID=..." from a logged-in browser
--username <u> / --password <p>   Alternative form-login
```

## Notes / limitations

- Quantities with thousands separators (`1,250`), decimals (rounded to nearest integer), and
  accounting-style negatives (`(5)`) are handled. Non-numeric quantities are skipped and
  reported.
- `productName` is informational; OpenBoxes matches on `productCode`. A mismatch only produces
  a Levenshtein-distance warning during import.
- Scoped for UAT reconciliation, not a production-recurring interface (see OBLS-903).
