# CSV → OpenBoxes inventory import

A **generic, source-agnostic** Groovy script that transforms an arbitrary inventory CSV
(whatever columns your source system exports) into OpenBoxes **inventory import** files
(quantity-on-hand baseline), split into batches, with an option to upload each batch
directly to an OpenBoxes instance.

There is **no source-specific logic in the script**. Everything about your source file —
which column is the product code, which is the quantity, how to filter rows — lives in an
external **JSON mapping config** you supply at runtime. The script only knows about
OpenBoxes: its import columns, file formats, batching rules and quantity semantics. That
keeps site-specific data (a particular ERP export, a particular facility) out of the
codebase and in a config the operator owns.

Motivating use case: reconciling quantity-on-hand from an external system into OpenBoxes so
allocation failures (`IllegalArgumentException: Insufficient stock`) can be attributed to
genuine stock-outs rather than QoH drift between the two systems.

## What it produces

Into `--output-dir`:

| File | Purpose |
|------|---------|
| `inventory_batch_NNN.xls` | Matches the OpenBoxes inventory import template. Upload manually via **Record Inventory → Import Inventory**. |
| `inventory_batch_NNN.csv` | For the API path (`POST /api/facilities/{id}/inventories/import`, `text/csv`). Also what `--upload` sends. |
| `skipped-records.csv` | Every source row that was dropped, with the reason and the original data — nothing is silently discarded. |

Which files appear depends on `--format` (`xls`, `csv`, or `both` — the default).

## Requirements

- Java + Groovy 2.5 / 3.x (`groovy` on your `PATH`).
- Internet access on first run — [Grape](https://groovy-lang.org/grape.html) downloads Apache POI.
  The script pins Maven Central via `@GrabResolver`, so it works regardless of your local Grape
  config. If you previously hit `unresolved dependency: org.apache.poi#poi ... not found` (older
  Groovy defaults to the shut-down JCenter), that cached failure can linger — clear it and retry:
  `rm -rf ~/.groovy/grapes/org.apache.poi` (add `groovy -Dgroovy.grape.report.downloads=true ...`
  to see download progress). Behind a proxy, set `-Dhttp.proxyHost`/`-Dhttps.proxyHost`.

## Quick start

```bash
# 1. See the source CSV's columns (so you know what to put in the config)
groovy CsvToInventoryImport.groovy --input data.csv --list-columns

# 2. Copy the example config and edit the source column names to match your CSV
cp mapping.example.json mapping.json && $EDITOR mapping.json

# 3. Dry run: parse, filter, batch, report counts (writes nothing)
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --dry-run

# 4. Generate batched .xls + .csv files
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --output-dir out

# 5. Generate and upload via the API (only raises stock, never zeroes)
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --format csv \
       --upload --url https://your-openboxes-host/openboxes \
       --facility-id <locationId> --session-cookie "JSESSIONID=..."
```

## The mapping config

A small JSON file (see [`mapping.example.json`](mapping.example.json)) with three blocks,
all optional except the two required mapping fields:

```json
{
  "mapping": {
    "productCode": "Part Number",      // required
    "quantity":    "Quantity On Hand", // required (quantity on hand)
    "productName": "Description",       // optional (informational)
    "binLocation": "",                 // optional
    "lotNumber":   "",                 // optional
    "comments":    ""                  // optional
  },
  "filter": {                          // optional row filter
    "column": "Warehouse",
    "equals": "MAIN"
  },
  "options": {                         // optional; CLI flags override these
    "includeZero": false,
    "defaultBin": "",
    "comment": "Inventory reconciliation import",
    "batchSize": 100,
    "format": "both"
  }
}
```

- **`mapping`** maps OpenBoxes fields to your source column headers. Only `productCode` and
  `quantity` are required; leave the rest blank/absent and they're left empty. Header
  matching is case- and punctuation-insensitive.
- **`filter`** (optional) keeps only rows where a source column equals a value — handy when
  one export spans several warehouses but you're importing into one OpenBoxes facility.
- **`options`** set defaults for output; any equivalent command-line flag overrides them.

You can skip the config entirely for quick runs and pass the mapping inline:

```bash
groovy CsvToInventoryImport.groovy --input data.csv \
       --mapping productCode=PartNo,quantity=QtyOnHand --filter Warehouse=MAIN
```

Inline `--mapping` / `--filter` also override individual entries from a `--config` file.

## How OpenBoxes interprets the quantity (important)

- **Quantity is absolute, not a delta.** OpenBoxes reads the current QoH from the database
  and creates an adjustment for the difference (`target − current`). The script always puts
  the mapped quantity in the **Physical QOH** column and leaves **OB QOH** blank.
- **Zero and negative quantities are skipped by default.** Zeros add no stock and create
  needless transaction churn; use `--include-zero` (or `"includeZero": true`) to keep zeros.
  Negatives are always skipped — OpenBoxes rejects them.
- **The product must already exist** in OpenBoxes (matched by `productCode`). Rows for
  unknown products are rejected **at import time** by OpenBoxes, not by this script.
- **Bin / lot are left blank by default.** A bin or lot that doesn't already exist in the
  target depot causes OpenBoxes to reject that row. Only map them (or use `--default-bin`)
  when you know they exist.

### ⚠️ Batching and the per-product zero-out (XLS / UI path)

When you import through the **UI (`.xls`)**, OpenBoxes reconciles per product: for every
product in the file, any **other** bin/lot of that product **not** in the same file is set
to zero. To stay safe, the script **never splits one product's rows across two batches**. If
you hand-edit the batches, preserve that rule.

The **API path (`.csv` / `--upload`)** is gentler: it only imports rows that **increase**
stock and never zeroes anything, which is why it's the recommended option.

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
- `--username <u> --password <p>` — the script form-logs-in at `{url}/auth/handleLogin`.

`--facility-id` is the OpenBoxes **location id** of the depot whose inventory you're setting.

**Fail-fast on rejected batches.** If a batch is rejected — most commonly because a row
references a **product code that does not exist** in OpenBoxes — the import endpoint errors
and rolls that batch back. By default the script then **aborts** so you notice, rather than
plowing through the remaining batches. Batches that already succeeded stay applied. Pass
`--continue-on-error` to push the rest anyway. Because each batch is small and atomic, a good
habit is a small first run (e.g. `--batch-size 20` against one facility) before the full load.

### Testing against a local instance

Point `--url` at your local OpenBoxes and use the seeded credentials (or a browser cookie):

```bash
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json \
       --format csv --batch-size 20 \
       --upload --url http://localhost:8080/openboxes \
       --facility-id <localLocationId> --username admin --password password
```

Do a `--dry-run` first to sanity-check counts, then a single small batch, and confirm the
quantities land where you expect (Record Inventory → transactions) before running the full file.

## All options

```
--input <file>            Source inventory CSV (required)
--config <mapping.json>   JSON column-mapping config (see mapping.example.json)
--list-columns            Print the CSV's headers, then exit
--dry-run                 Parse, filter, batch; report counts but write nothing

--mapping <k=v,...>       Inline mapping/override, e.g. productCode=PartNo,quantity=QtyOnHand
--filter <Column=Value>   Keep only source rows where Column == Value

--output-dir <dir>        Output directory (default: inventory-output)
--format <xls|csv|both>   xls = manual UI import, csv = API import (default: both)
--batch-size <n>          Rows per batch; products never split (default: 100; 0 = one file)
--include-zero            Keep rows with quantity 0 (default: skip)
--default-bin <name>      Force a bin location for every row (default: blank)
--comment <text>          Comment for rows without their own comment

--upload                  Upload each batch (CSV/API path)
--url <baseUrl>           e.g. https://your-openboxes-host/openboxes
--facility-id <id>        OpenBoxes location id to import into
--session-cookie <c>      "JSESSIONID=..." from a logged-in browser
--username <u> / --password <p>   Alternative form-login
--continue-on-error       Keep uploading after a rejected batch (default: abort on first)
```

## Notes / limitations

- Quantities with thousands separators (`1,250`), decimals (rounded to nearest integer), and
  accounting-style negatives (`(5)`) are handled. Non-numeric quantities are skipped and
  reported.
- `productName` is informational; OpenBoxes matches on `productCode`. A mismatch only
  produces a Levenshtein-distance warning during import.
- Large source files are fine — the tool batches into small files, so no single output file
  is large. Note the legacy `.xls` format caps at 65,535 rows per sheet; keep `--batch-size`
  well under that (the default 100 is far below it), or use `--format csv` for the API path.
