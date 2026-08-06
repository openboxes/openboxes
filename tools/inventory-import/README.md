# CSV → OpenBoxes inventory import

Two small, **generic, source-agnostic** Groovy scripts that load quantity-on-hand from an
arbitrary inventory CSV into OpenBoxes, as a deliberate **two-step flow** so you can review
exactly what will be imported before it is:

1. **`CsvToInventoryImport.groovy` — transform.** Source CSV + a column-mapping config →
   batched OpenBoxes import files. Writes only; imports nothing.
2. **`ImportInventoryBatches.groovy` — import.** Uploads the reviewed `.csv` batches to
   OpenBoxes via its inventory import API.

Splitting the steps means the artifact you inspect (and can hand-edit) is exactly the artifact
that gets imported — nothing is re-derived behind your back.

There is **no source-specific logic in either script**. Everything about your source file —
which column is the product code, which is the quantity, how to filter rows — lives in an
external **JSON mapping config** you supply at runtime, keeping site-specific data (a
particular ERP export, a particular facility) out of the codebase and in a config the operator
owns.

Motivating use case: reconciling quantity-on-hand from an external system into OpenBoxes so
allocation failures (`IllegalArgumentException: Insufficient stock`) can be attributed to
genuine stock-outs rather than QoH drift between the two systems.

## The flow at a glance

```
 source.csv ──┬─► CsvToInventoryImport.groovy ──► out/inventory_batch_001.csv   (canonical / API)
 mapping.json ┘        (transform)               out/inventory_batch_001.xls   (manual UI import)
                                                 out/skipped-records.csv
                                                          │
                                    review / edit ────────┤
                                                          ▼
                                   ImportInventoryBatches.groovy ──► OpenBoxes
                                          (import, fail-fast)
```

## Requirements

- Java + Groovy 2.5 / 3.x (`groovy` on your `PATH`).
- **Transform** needs internet on first run — [Grape](https://groovy-lang.org/grape.html)
  downloads Apache POI (for the `.xls`). The script pins Maven Central via `@GrabResolver`, so
  it works regardless of your local Grape config. If you previously hit `unresolved dependency:
  org.apache.poi#poi ... not found` (older Groovy defaults to the shut-down JCenter), clear the
  cached failure and retry: `rm -rf ~/.groovy/grapes/org.apache.poi`. Behind a proxy, set
  `-Dhttps.proxyHost`/`-Dhttps.proxyPort`.
- **Import** has no third-party dependencies at all (pure JDK) — no POI, no groovy-json.

---

## Stage 1 — transform (`CsvToInventoryImport.groovy`)

```bash
# 1. See the source CSV's columns (so you know what to put in the config)
groovy CsvToInventoryImport.groovy --input data.csv --list-columns

# 2. Copy the example config and edit the source column names to match your CSV
cp mapping.example.json mapping.json && $EDITOR mapping.json

# 3. Dry run: parse, filter, batch, report counts (writes nothing)
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --dry-run

# 4. Generate the batch files (--clean clears any leftovers from a previous run)
groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --output-dir out --clean
```

> **Re-running:** the output dir is not cleared automatically — same-named files are overwritten,
> but a shorter run leaves stale higher-numbered `inventory_batch_*.csv` behind that stage 2 would
> still upload. Use `--clean` (or a fresh `--output-dir`) when regenerating. Without `--clean` the
> transform warns if it finds leftovers.

### What it produces

Into `--output-dir`:

| File | Purpose |
|------|---------|
| `inventory_batch_NNN.csv` | Canonical / API-ready. Reviewed and uploaded by stage 2, or POST manually. |
| `inventory_batch_NNN.xls` | For the manual **Record Inventory → Import Inventory** UI. |
| `skipped-records.csv` | Every source row that was dropped, with the reason and original data — nothing is silently discarded. |

Which files appear depends on `--format` (`xls`, `csv`, or `both` — the default).

### The mapping config

A small JSON file (see [`mapping.example.json`](mapping.example.json)) with three blocks, all
optional except the two required mapping fields:

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
    "format": ["xls", "csv"]
  }
}
```

- **`mapping`** maps OpenBoxes fields to your source column headers. Only `productCode` and
  `quantity` are required; leave the rest blank/absent and they're left empty. Header matching
  is case- and punctuation-insensitive.
- **`filter`** (optional) keeps only rows where a source column equals a value — handy when one
  export spans several warehouses but you're importing into one OpenBoxes facility.
- **`options`** set defaults for output; any equivalent command-line flag overrides them.

You can skip the config for quick runs and pass the mapping inline:

```bash
groovy CsvToInventoryImport.groovy --input data.csv \
       --mapping productCode=PartNo,quantity=QtyOnHand --filter Warehouse=MAIN
```

Inline `--mapping` / `--filter` also override individual entries from a `--config` file.

### Transform options

```
--input <file>            Source inventory CSV (required)
--config <mapping.json>   JSON column-mapping config (see mapping.example.json)
--list-columns            Print the CSV's headers, then exit
--dry-run                 Parse, filter, batch; report counts but write nothing
--mapping <k=v,...>       Inline mapping/override, e.g. productCode=PartNo,quantity=QtyOnHand
--filter <Column=Value>   Keep only source rows where Column == Value
--output-dir <dir>        Output directory (default: inventory-output)
--clean                   Remove old batch/skipped files first (default: warn on leftovers)
--format <list>           Output formats, comma-separated: xls (manual UI), csv (API). e.g.
                          xls,csv. Config accepts a JSON list ["xls","csv"]. "both"/omitted = all
--batch-size <n>          Rows per batch; products never split (default: 100; 0 = one file)
--max <n>                 Cap the number of import rows for a quick test run (0 = no cap)
--include-zero            Keep rows with quantity 0 (default: skip)
--default-bin <name>      Force a bin location for every row (default: blank)
--bin-blank-values <a,b>  Source values that truly mean "no bin" -> blank (e.g. NONE)
--comment <text>          Comment for rows without their own comment
```

---

## Stage 2 — import (`ImportInventoryBatches.groovy`)

Review the generated `.csv` batches, then upload them:

```bash
# Review what would be uploaded, in order, with row counts (no upload)
groovy ImportInventoryBatches.groovy --input-dir out --dry-run

# Upload every inventory_batch_*.csv in ./out
groovy ImportInventoryBatches.groovy --input-dir out \
       --url https://your-openboxes-host/openboxes --facility-id <locationId> \
       --username <user> --password <pass>
```

It posts each batch to `POST {url}/api/facilities/{facility-id}/inventories/import`
(`Content-Type: text/csv`). Authentication is session-cookie based (OpenBoxes has no API-key
mechanism) — provide **one** of:

- `--session-cookie "JSESSIONID=..."` — copy it from a logged-in browser session (DevTools →
  Application → Cookies). Simplest and most reliable.
- `--username <u> --password <p>` — the script form-logs-in at `{url}/auth/handleLogin`.

`--facility-id` is the OpenBoxes **location id** of the depot whose inventory you're setting.

**Fail-fast.** If a batch is rejected — most commonly a **product code that does not exist** —
the import endpoint errors and rolls that batch back. By default the run **aborts** so you
notice, rather than plowing on. Batches that already succeeded stay applied. `--continue-on-error`
pushes the rest anyway. Because each batch is small and atomic, a good habit is a small first
run against a local instance (e.g. `--batch-size 20` when transforming) before the full load.

### Import options

```
--input-dir <dir>         Directory of inventory_batch_*.csv files (sorted by name)
--input-files <a,b,...>   Alternative: explicit comma-separated list of CSV files
--url <baseUrl>           e.g. https://your-openboxes-host/openboxes
--facility-id <id>        OpenBoxes location id to import into
--session-cookie <c>      "JSESSIONID=..." from a logged-in browser
--username <u> / --password <p>   Alternative form-login
--dry-run                 List batches (and row counts) that would be uploaded; no upload
--continue-on-error       Keep uploading after a rejected batch (default: abort on first)
```

---

## How OpenBoxes interprets the quantity (important)

- **Quantity is absolute, not a delta.** OpenBoxes reads the current QoH from the database and
  creates an adjustment for the difference (`target − current`). The transform always puts the
  mapped quantity in the **Physical QOH** column and leaves **OB QOH** blank.
- **Zero and negative quantities are skipped by default.** Zeros add no stock and create
  needless transaction churn; use `--include-zero` (or `"includeZero": true`) to keep zeros.
  Negatives are always skipped — OpenBoxes rejects them.
- **The product must already exist** in OpenBoxes (matched by `productCode`). Rows for unknown
  products are rejected **at import time** (stage 2), which trips the fail-fast abort.
- **Bin / lot are left blank by default.** A bin or lot that doesn't already exist in the target
  depot causes OpenBoxes to reject that row. Only map them (or use `--default-bin`) when you know
  they exist. If you map a bin column, every value in it must be a real bin location in the
  depot — including any staging/virtual bins your source uses (map those through as-is; they
  must exist in OpenBoxes). Use **`binBlankValues`** (config) / `--bin-blank-values` *only* for
  values that genuinely mean "no bin" (e.g. a `NONE` sentinel), to turn them into a blank bin.

### ⚠️ Batching and the per-product zero-out (XLS / UI path)

When you import through the **UI (`.xls`)**, OpenBoxes reconciles per product: for every product
in the file, any **other** bin/lot of that product **not** in the same file is set to zero. To
stay safe, the transform **never splits one product's rows across two batches**. If you hand-edit
the batches, preserve that rule.

The **API path (`.csv`, stage 2)** is gentler: it only imports rows that **increase** stock and
never zeroes anything, which is why it's the recommended option. Each imported file is one atomic
baseline + adjustment transaction, so batching also avoids one giant inventory transaction.

## Notes / limitations

- Quantities with thousands separators (`1,250`), decimals (rounded to nearest integer), and
  accounting-style negatives (`(5)`) are handled. Non-numeric quantities are skipped and reported.
- `productName` is informational; OpenBoxes matches on `productCode`. A mismatch only produces a
  Levenshtein-distance warning during import.
- Large source files are fine — the transform batches into small files. Note the legacy `.xls`
  format caps at 65,535 rows per sheet; keep `--batch-size` well under that (the default 100 is
  far below it), or use `--format csv` for the API path.
