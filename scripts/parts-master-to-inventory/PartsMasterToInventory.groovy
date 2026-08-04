#!/usr/bin/env groovy
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

/*
 * PartsMasterToInventory.groovy  (OBLS-903)
 * ------------------------------------------------------------------------------
 * Transforms an Excede "Parts Master" CSV export into OpenBoxes inventory import
 * files (quantity-on-hand baseline), split into batches, and optionally uploads
 * each batch to an OpenBoxes instance.
 *
 * Two output formats, both derived from the same canonical rows:
 *
 *   xls  - Matches the OpenBoxes inventory import template
 *          (grails-app/conf/templates/inventory.xls). Upload manually via
 *          "Record Inventory > Import Inventory". Columns are read BY POSITION
 *          (A..H) by InventoryExcelImporter, so column order matters, not the
 *          header text. The Parts Master quantity goes in column G (Physical QOH).
 *
 *   csv  - For the API path: POST {baseUrl}/api/facilities/{facilityId}/inventories/import
 *          with Content-Type: text/csv. Header cells are camel-cased by the server
 *          (CSVUtils.toCamelCase), so "Product Code" -> productCode, "Quantity" ->
 *          quantity. The API path only RAISES stock (never zeroes) and is the
 *          safest option for UAT reconciliation.
 *
 * Why "Physical QOH" (not "OB QOH"): OpenBoxes treats the imported quantity as the
 * ABSOLUTE target on-hand. It reads the current QoH from the database and creates
 * an adjustment for the difference (target - current). So we always populate the
 * quantity as the Parts Master value and leave "OB QOH" blank.
 *
 * SAFETY NOTE (batching): for every product present in an import file, OpenBoxes
 * zeroes out any OTHER bin/lot of that product that is NOT in the same file. This
 * tool therefore never splits a single product's rows across two batches. Keep this
 * in mind if you edit the batches by hand.
 *
 * Requirements: Groovy 2.5+ / 3.x with internet access on first run (Grape pulls
 * Apache POI). Everything else uses the JDK.
 *
 * Quick start:
 *   groovy PartsMasterToInventory.groovy --input partsmaster.csv --list-columns
 *   groovy PartsMasterToInventory.groovy --input partsmaster.csv --dry-run
 *   groovy PartsMasterToInventory.groovy --input partsmaster.csv --output-dir out
 *   groovy PartsMasterToInventory.groovy --input partsmaster.csv --format csv \
 *          --upload --url https://vvg.openboxes.com/openboxes \
 *          --facility-id <locationId> --session-cookie "JSESSIONID=..."
 *
 * See README.md for the full column-mapping guide.
 */

@Grab(group = 'org.apache.poi', module = 'poi', version = '5.2.5')
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

// ---------------------------------------------------------------------------
// Canonical OpenBoxes inventory columns (order matters for the XLS template).
// ---------------------------------------------------------------------------
// Each entry: [canonical field, XLS header, API CSV header]
// The XLS is read by column position, the API CSV by camel-cased header name.
final List<List<String>> OB_COLUMNS = [
        ['productCode', 'Product code', 'Product Code'],
        ['productName', 'Product name', 'Product'],
        ['lotNumber', 'Lot number', 'Lot Number'],
        ['expirationDate', 'Expiration date', 'Expiration Date'],
        ['binLocation', 'Bin location', 'Bin Location'],
        ['obQoh', 'OB QOH', 'Quantity On Hand'],   // always left blank; OB computes current
        ['quantity', 'Physical QOH', 'Quantity'],   // <-- the Parts Master QoH (absolute target)
        ['comments', 'Comment', 'Comments'],
]

// ---------------------------------------------------------------------------
// Default auto-detection aliases (normalized: lowercased, non-alphanumerics
// stripped). First matching source header wins for each canonical field.
// Override any of these at runtime with --mapping field=Header,...
// ---------------------------------------------------------------------------
final Map<String, List<String>> DEFAULT_ALIASES = [
        productCode : ['partnumber', 'partno', 'partid', 'part', 'sku', 'itemnumber',
                       'itemno', 'itemid', 'item', 'productcode', 'product', 'stocknumber',
                       'stockcode', 'materialnumber', 'matnr'],
        productName : ['description', 'desc', 'partdescription', 'itemdescription',
                       'productname', 'productdescription', 'name'],
        quantity    : ['qtyonhand', 'quantityonhand', 'onhandqty', 'onhandquantity',
                       'qoh', 'onhand', 'stockonhand', 'soh', 'availablequantity',
                       'availableqty', 'qtyavailable', 'qty', 'quantity'],
        binLocation : ['binlocation', 'binloc', 'bin', 'shelf', 'shelflocation', 'slot'],
        lotNumber   : ['lotnumber', 'lot', 'batchnumber', 'batch', 'lotno'],
        comments    : ['comment', 'comments', 'note', 'notes', 'remark', 'remarks'],
        // Not an OB column - used only to filter source rows by warehouse/facility:
        sourceFacility: ['warehouse', 'warehousecode', 'whse', 'facility', 'facilitycode',
                         'site', 'branch', 'store', 'plant', 'depot', 'loc', 'location'],
]

// ===========================================================================
// Arg parsing
// ===========================================================================
Map<String, String> opts = [:]
Set<String> flags = [] as Set
final Set<String> FLAG_NAMES = ['include-zero', 'dry-run', 'upload', 'list-columns', 'help'] as Set

for (int i = 0; i < args.length; i++) {
    String a = args[i]
    if (!a.startsWith('--')) {
        die("Unexpected argument: '${a}'. Use --help for usage.")
    }
    String name = a.substring(2)
    if (name in FLAG_NAMES) {
        flags << name
    } else {
        if (i + 1 >= args.length) {
            die("Option --${name} requires a value.")
        }
        opts[name] = args[++i]
    }
}

if ('help' in flags || !opts && !flags) {
    printUsage()
    return
}

String inputPath = opts['input']
if (!inputPath) {
    die("--input <partsMaster.csv> is required. Use --help for usage.")
}
File inputFile = new File(inputPath)
if (!inputFile.exists()) {
    die("Input file not found: ${inputFile.absolutePath}")
}

String outputDirPath = opts['output-dir'] ?: 'inventory-output'
String format = (opts['format'] ?: 'both').toLowerCase()
if (!(format in ['xls', 'csv', 'both'])) {
    die("--format must be one of: xls, csv, both (got '${format}')")
}
int batchSize = (opts['batch-size'] ?: '100') as int
boolean includeZero = 'include-zero' in flags
boolean dryRun = 'dry-run' in flags
boolean listColumns = 'list-columns' in flags
boolean upload = 'upload' in flags
String defaultBin = opts['default-bin']
String sourceFacilityFilter = opts['source-facility']
String comment = opts['comment'] ?: "Parts Master import ${new Date().format('yyyy-MM-dd')}"

// Validate upload arguments up front so we fail before doing any work.
if (upload) {
    if (!opts['url'] || !opts['facility-id']) {
        die("--upload requires --url <baseUrl> and --facility-id <locationId>.")
    }
    if (!opts['session-cookie'] && !(opts['username'] && opts['password'])) {
        die("--upload needs auth: pass --session-cookie \"JSESSIONID=...\" " +
                "or --username and --password.")
    }
}

// ===========================================================================
// Read the Parts Master CSV
// ===========================================================================
List<List<String>> rawRows = parseCsv(inputFile.getText('UTF-8'))
if (rawRows.size() < 2) {
    die("CSV appears to have no data rows: ${inputFile.absolutePath}")
}
List<String> headers = rawRows[0]
List<List<String>> dataRows = rawRows[1..-1]

// ---------------------------------------------------------------------------
// Build the mapping: canonical field -> source header. Auto-detect, then apply
// explicit --mapping overrides (field=Header,field=Header,...).
// ---------------------------------------------------------------------------
Map<String, Integer> headerIndex = [:]
headers.eachWithIndex { String h, int idx -> headerIndex[normalize(h)] = idx }

Map<String, String> mapping = [:]           // canonical field -> actual source header
DEFAULT_ALIASES.each { String field, List<String> aliases ->
    for (String alias : aliases) {
        if (headerIndex.containsKey(alias)) {
            mapping[field] = headers[headerIndex[alias]]
            break
        }
    }
}

// Apply explicit overrides
if (opts['mapping']) {
    opts['mapping'].split(',').each { String pair ->
        List<String> kv = pair.split('=', 2) as List
        if (kv.size() != 2) {
            die("Bad --mapping entry '${pair}'. Expected field=Header.")
        }
        String field = kv[0].trim()
        String header = kv[1].trim()
        if (!headerIndex.containsKey(normalize(header))) {
            die("--mapping references column '${header}' which is not in the CSV.\n" +
                    "Available columns: ${headers.join(', ')}")
        }
        mapping[field] = header
    }
}
if (opts['source-facility-col']) {
    String header = opts['source-facility-col']
    if (!headerIndex.containsKey(normalize(header))) {
        die("--source-facility-col '${header}' is not a CSV column. Columns: ${headers.join(', ')}")
    }
    mapping['sourceFacility'] = header
}

// ---------------------------------------------------------------------------
// --list-columns: show detected headers + guessed mapping and exit.
// ---------------------------------------------------------------------------
if (listColumns) {
    println "CSV columns detected in ${inputFile.name}:"
    headers.eachWithIndex { String h, int idx -> println "  [${idx}] ${h}" }
    println ""
    println "Guessed column mapping (override with --mapping field=Header,...):"
    ['productCode', 'productName', 'quantity', 'binLocation', 'lotNumber', 'comments', 'sourceFacility'].each { String f ->
        println "  ${f.padRight(16)} <- ${mapping[f] ?: '(not found)'}"
    }
    println ""
    println "Required: productCode, quantity. Everything else is optional."
    return
}

// Validate required mappings
List<String> missing = ['productCode', 'quantity'].findAll { !mapping[it] }
if (missing) {
    die("Could not auto-detect required column(s): ${missing.join(', ')}.\n" +
            "CSV columns are: ${headers.join(', ')}\n" +
            "Re-run with --list-columns to inspect, then set them explicitly, e.g.:\n" +
            "  --mapping productCode=PartNo,quantity=QtyOnHand")
}

println "Using column mapping:"
['productCode', 'productName', 'quantity', 'binLocation', 'lotNumber', 'comments', 'sourceFacility'].each { String f ->
    if (mapping[f]) println "  ${f.padRight(16)} <- ${mapping[f]}"
}
println ""

// ===========================================================================
// Transform rows -> canonical inventory rows, filtering + collecting skips.
// ===========================================================================
Integer idxProductCode = headerIndex[normalize(mapping['productCode'])]
Integer idxQuantity = headerIndex[normalize(mapping['quantity'])]
Integer idxProductName = mapping['productName'] ? headerIndex[normalize(mapping['productName'])] : null
Integer idxBin = mapping['binLocation'] ? headerIndex[normalize(mapping['binLocation'])] : null
Integer idxLot = mapping['lotNumber'] ? headerIndex[normalize(mapping['lotNumber'])] : null
Integer idxComment = mapping['comments'] ? headerIndex[normalize(mapping['comments'])] : null
Integer idxSourceFacility = mapping['sourceFacility'] ? headerIndex[normalize(mapping['sourceFacility'])] : null

List<Map> canonicalRows = []
List<Map> skipped = []       // [rowNumber, reason, raw]

dataRows.eachWithIndex { List<String> row, int i ->
    int rowNumber = i + 2   // 1-based, +1 for header
    def cell = { Integer idx -> idx != null && idx < row.size() ? (row[idx] ?: '').trim() : '' }

    // Filter by source facility/warehouse if requested
    if (sourceFacilityFilter != null) {
        if (idxSourceFacility == null) {
            die("--source-facility given but no warehouse/facility column detected. " +
                    "Use --source-facility-col <Header> to point at it.")
        }
        String facVal = cell(idxSourceFacility)
        if (!facVal.equalsIgnoreCase(sourceFacilityFilter)) {
            return   // silently skip other facilities (not an error)
        }
    }

    String productCode = cell(idxProductCode)
    if (!productCode) {
        skipped << [row: rowNumber, reason: 'blank product code', raw: row]
        return
    }

    String qtyRaw = cell(idxQuantity)
    Integer qty = parseQuantity(qtyRaw)
    if (qty == null) {
        skipped << [row: rowNumber, reason: "non-numeric quantity '${qtyRaw}'", raw: row]
        return
    }
    if (qty <= 0 && !includeZero) {
        skipped << [row: rowNumber, reason: "quantity ${qty} <= 0 (use --include-zero to keep)", raw: row]
        return
    }
    if (qty < 0) {
        skipped << [row: rowNumber, reason: "negative quantity ${qty}", raw: row]
        return
    }

    canonicalRows << [
            productCode   : productCode,
            productName   : idxProductName != null ? cell(idxProductName) : '',
            lotNumber     : idxLot != null ? cell(idxLot) : '',
            expirationDate: '',
            binLocation   : defaultBin ?: (idxBin != null ? cell(idxBin) : ''),
            obQoh         : '',
            quantity      : qty,
            comments      : idxComment != null && cell(idxComment) ? cell(idxComment) : comment,
    ]
}

// ---------------------------------------------------------------------------
// Report
// ---------------------------------------------------------------------------
println "Parsed ${dataRows.size()} data row(s):"
println "  ${canonicalRows.size()} row(s) to import"
println "  ${skipped.size()} row(s) skipped"
if (skipped) {
    // Summarize skip reasons
    Map<String, Integer> reasonCounts = [:].withDefault { 0 }
    skipped.each { reasonCounts[shortReason(it.reason)] += 1 }
    reasonCounts.each { r, c -> println "      - ${c}x ${r}" }
}
println ""

if (!canonicalRows) {
    println "Nothing to import after filtering. Exiting."
    return
}

// ===========================================================================
// Batch WITHOUT splitting a product across files.
// ===========================================================================
List<List<Map>> batches = batchRows(canonicalRows, batchSize)
println batchSize > 0 ?
        "Split into ${batches.size()} batch(es) of up to ${batchSize} row(s) each.\n" :
        "Batching disabled: writing a single file with ${canonicalRows.size()} row(s).\n"

// Write skipped-records report so nothing is silently dropped
File outputDir = new File(outputDirPath)

if (dryRun) {
    println "[dry-run] Would write ${batches.size()} batch file(s) to ${outputDir.absolutePath}"
    batches.eachWithIndex { List<Map> b, int i ->
        println "  batch ${String.format('%03d', i + 1)}: ${b.size()} row(s)"
    }
    return
}

outputDir.mkdirs()

// Skipped report
if (skipped) {
    File skipFile = new File(outputDir, 'skipped-records.csv')
    skipFile.withWriter('UTF-8') { w ->
        w.writeLine('sourceRow,reason,' + headers.collect { csvEscape(it) }.join(','))
        skipped.each { s ->
            w.writeLine("${s.row},${csvEscape(s.reason)}," + (s.raw as List).collect { csvEscape(it) }.join(','))
        }
    }
    println "Wrote skipped-records report: ${skipFile.name} (${skipped.size()} row(s))\n"
}

// ---------------------------------------------------------------------------
// Write batch files
// ---------------------------------------------------------------------------
List<File> csvBatchFiles = []
List<File> xlsBatchFiles = []
batches.eachWithIndex { List<Map> batch, int i ->
    String base = "inventory_batch_${String.format('%03d', i + 1)}"
    if (format in ['xls', 'both']) {
        File f = new File(outputDir, "${base}.xls")
        writeXls(f, batch, OB_COLUMNS)
        xlsBatchFiles << f
        println "Wrote ${f.name} (${batch.size()} row(s))"
    }
    if (format in ['csv', 'both']) {
        File f = new File(outputDir, "${base}.csv")
        writeApiCsv(f, batch, OB_COLUMNS)
        csvBatchFiles << f
        println "Wrote ${f.name} (${batch.size()} row(s))"
    }
}
println ""

// ===========================================================================
// Optional upload via the OpenBoxes inventory import API
// ===========================================================================
if (upload) {
    String baseUrl = opts['url']
    String facilityId = opts['facility-id']
    if (!baseUrl || !facilityId) {
        die("--upload requires --url <baseUrl> and --facility-id <locationId>.")
    }
    baseUrl = baseUrl.replaceAll('/+$', '')

    String cookie = opts['session-cookie']
    if (!cookie) {
        if (opts['username'] && opts['password']) {
            println "Logging in as ${opts['username']} ..."
            cookie = login(baseUrl, opts['username'], opts['password'])
            println "Obtained session cookie."
        } else {
            die("--upload needs auth: pass --session-cookie \"JSESSIONID=...\" " +
                    "or --username and --password.")
        }
    }

    if (!csvBatchFiles) {
        // We upload CSV; if the user chose --format xls, build CSVs on the fly.
        batches.eachWithIndex { List<Map> batch, int i ->
            File f = new File(outputDir, "inventory_batch_${String.format('%03d', i + 1)}.csv")
            writeApiCsv(f, batch, OB_COLUMNS)
            csvBatchFiles << f
        }
    }

    String importUrl = "${baseUrl}/api/facilities/${facilityId}/inventories/import"
    println "Uploading ${csvBatchFiles.size()} batch(es) to ${importUrl}\n"
    int ok = 0, failed = 0
    csvBatchFiles.eachWithIndex { File f, int i ->
        try {
            uploadCsv(importUrl, f, cookie)
            ok++
            println "  [${i + 1}/${csvBatchFiles.size()}] ${f.name} -> OK"
        } catch (Exception e) {
            failed++
            println "  [${i + 1}/${csvBatchFiles.size()}] ${f.name} -> FAILED: ${e.message}"
        }
    }
    println "\nUpload complete: ${ok} succeeded, ${failed} failed."
}

println "\nDone. Output in ${outputDir.absolutePath}"

// ===========================================================================
// Helper methods
// ===========================================================================

/** Group rows into batches of ~batchSize, never splitting one productCode across batches. */
static List<List<Map>> batchRows(List<Map> rows, int batchSize) {
    if (batchSize <= 0) {
        return [rows]
    }
    // Preserve first-seen order of products; keep all rows for a product together.
    Map<String, List<Map>> byProduct = [:]
    rows.each { Map r -> byProduct.computeIfAbsent(r.productCode as String, { [] }) << r }

    List<List<Map>> batches = []
    List<Map> current = []
    byProduct.values().each { List<Map> group ->
        if (!current.isEmpty() && current.size() + group.size() > batchSize) {
            batches << current
            current = []
        }
        current.addAll(group)
        // A single product with more rows than batchSize still stays whole (its own oversized batch).
        if (current.size() >= batchSize) {
            batches << current
            current = []
        }
    }
    if (!current.isEmpty()) {
        batches << current
    }
    return batches
}

/** Parse a quantity that may contain thousands separators / decimals. Returns rounded int or null. */
static Integer parseQuantity(String raw) {
    if (raw == null || raw.trim().isEmpty()) {
        return null
    }
    String cleaned = raw.trim().replaceAll(/[,\s]/, '')
    // Handle parenthesised negatives e.g. (5) -> -5 (accounting style)
    boolean paren = cleaned.startsWith('(') && cleaned.endsWith(')')
    if (paren) {
        cleaned = '-' + cleaned.substring(1, cleaned.length() - 1)
    }
    try {
        return Math.round(new BigDecimal(cleaned).doubleValue()) as Integer
    } catch (NumberFormatException ignored) {
        return null
    }
}

/** Write an OpenBoxes inventory import .xls (HSSF), columns by position, header row 0. */
static void writeXls(File file, List<Map> rows, List<List<String>> obColumns) {
    Workbook wb = new HSSFWorkbook()
    try {
        Sheet sheet = wb.createSheet('Sheet1')
        Row header = sheet.createRow(0)
        obColumns.eachWithIndex { List<String> col, int c ->
            header.createCell(c).setCellValue(col[1])   // XLS header label
        }
        rows.eachWithIndex { Map r, int i ->
            Row excelRow = sheet.createRow(i + 1)
            obColumns.eachWithIndex { List<String> col, int c ->
                String field = col[0]
                Cell cell = excelRow.createCell(c)
                def val = r[field]
                if (field == 'quantity') {
                    cell.setCellValue((val as Integer).doubleValue())   // numeric
                } else {
                    cell.setCellValue(val == null ? '' : val.toString())
                }
            }
        }
        file.withOutputStream { os -> wb.write(os) }
    } finally {
        wb.close()
    }
}

/** Write a CSV suitable for the inventory import API (headers camel-case on the server). */
static void writeApiCsv(File file, List<Map> rows, List<List<String>> obColumns) {
    // API path ignores "OB QOH"; keep the columns it uses. Order is not significant for CSV.
    List<List<String>> cols = obColumns.findAll { it[0] != 'obQoh' }
    file.withWriter('UTF-8') { w ->
        w.writeLine(cols.collect { csvEscape(it[2]) }.join(','))   // API CSV header
        rows.each { Map r ->
            w.writeLine(cols.collect { csvEscape(r[it[0]] == null ? '' : r[it[0]].toString()) }.join(','))
        }
    }
}

/** Minimal RFC-4180 CSV parser (handles quotes, embedded commas and newlines). */
static List<List<String>> parseCsv(String text) {
    final char QUOTE = '"'
    final char COMMA = ','
    final char CR = '\r'
    final char LF = '\n'

    List<List<String>> rows = []
    StringBuilder sb = new StringBuilder()
    boolean inQuotes = false
    int n = text.length()
    // Strip a leading UTF-8 BOM if present
    int start = text.startsWith('﻿') ? 1 : 0
    List<String> current = []
    for (int i = start; i < n; i++) {
        char ch = text.charAt(i)
        if (inQuotes) {
            if (ch == QUOTE) {
                if (i + 1 < n && text.charAt(i + 1) == QUOTE) {
                    sb.append(QUOTE); i++
                } else {
                    inQuotes = false
                }
            } else {
                sb.append(ch)
            }
        } else {
            if (ch == QUOTE) {
                inQuotes = true
            } else if (ch == COMMA) {
                current << sb.toString(); sb.setLength(0)
            } else if (ch == LF) {
                current << sb.toString(); sb.setLength(0)
                rows << current; current = []
            } else if (ch == CR) {
                // handle \r\n (wait for \n) and lone \r (end row now)
                if (!(i + 1 < n && text.charAt(i + 1) == LF)) {
                    current << sb.toString(); sb.setLength(0); rows << current; current = []
                }
            } else {
                sb.append(ch)
            }
        }
    }
    // last field/row
    if (sb.length() > 0 || !current.isEmpty()) {
        current << sb.toString()
        rows << current
    }
    // Drop fully-empty trailing rows
    return rows.findAll { !(it.size() == 1 && it[0].trim().isEmpty()) }
}

static String csvEscape(Object value) {
    String s = value == null ? '' : value.toString()
    if (s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')) {
        return '"' + s.replace('"', '""') + '"'
    }
    return s
}

static String normalize(String header) {
    return header == null ? '' : header.toLowerCase().replaceAll(/[^a-z0-9]/, '')
}

static String shortReason(String reason) {
    if (reason.startsWith('non-numeric')) return 'non-numeric quantity'
    if (reason.startsWith('quantity') && reason.contains('<= 0')) return 'quantity <= 0'
    if (reason.startsWith('negative')) return 'negative quantity'
    return reason
}

/** Form-login to OpenBoxes and return the "JSESSIONID=..." cookie header value. */
static String login(String baseUrl, String username, String password) {
    String body = "username=${URLEncoder.encode(username, 'UTF-8')}&password=${URLEncoder.encode(password, 'UTF-8')}"
    HttpURLConnection conn = (HttpURLConnection) new URL("${baseUrl}/auth/handleLogin").openConnection()
    conn.setInstanceFollowRedirects(false)
    conn.setRequestMethod('POST')
    conn.setDoOutput(true)
    conn.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded')
    conn.outputStream.withWriter('UTF-8') { it.write(body) }
    conn.connect()
    int code = conn.responseCode
    List<String> cookies = conn.getHeaderFields().get('Set-Cookie')
    conn.disconnect()
    if (!cookies) {
        throw new RuntimeException("Login did not return a session cookie (HTTP ${code}). " +
                "Check the URL/credentials, or pass --session-cookie manually.")
    }
    String jsession = cookies.collect { it.split(';')[0] }.find { it.startsWith('JSESSIONID=') }
    if (!jsession) {
        throw new RuntimeException("No JSESSIONID in login response. Pass --session-cookie manually.")
    }
    return jsession
}

/** POST a CSV batch to the inventory import API. Throws on non-2xx. */
static void uploadCsv(String importUrl, File csvFile, String cookie) {
    HttpURLConnection conn = (HttpURLConnection) new URL(importUrl).openConnection()
    conn.setRequestMethod('POST')
    conn.setDoOutput(true)
    conn.setRequestProperty('Content-Type', 'text/csv')
    conn.setRequestProperty('Cookie', cookie)
    conn.outputStream.withStream { os -> os.write(csvFile.getBytes()) }
    conn.connect()
    int code = conn.responseCode
    if (code < 200 || code >= 300) {
        String err
        try { err = conn.errorStream?.text } catch (Exception ignored) { err = '' }
        throw new RuntimeException("HTTP ${code}${err ? ': ' + err.take(500) : ''}")
    }
    conn.disconnect()
}

static void die(String message) {
    System.err.println("ERROR: ${message}")
    System.exit(1)
}

static void printUsage() {
    println '''\
PartsMasterToInventory.groovy (OBLS-903)
Transform an Excede Parts Master CSV into OpenBoxes inventory import batches.

USAGE:
  groovy PartsMasterToInventory.groovy --input <partsMaster.csv> [options]

INSPECT / DRY RUN:
  --list-columns            Print CSV headers + guessed column mapping, then exit
  --dry-run                 Parse, filter and batch; report counts but write nothing

INPUT / MAPPING:
  --input <file>            Parts Master CSV (required)
  --mapping <k=v,...>       Override column mapping, e.g.
                            --mapping productCode=PartNo,quantity=QtyOnHand
  --source-facility-col <h> Header of the warehouse/facility column (for filtering)
  --source-facility <val>   Keep only source rows whose facility column == val

OUTPUT:
  --output-dir <dir>        Output directory (default: inventory-output)
  --format <xls|csv|both>   xls = manual UI import, csv = API import (default: both)
  --batch-size <n>          Rows per batch, products never split (default: 100; 0 = one file)
  --include-zero            Keep rows with quantity <= 0 (default: skip them)
  --default-bin <name>      Force a bin location for every row (default: blank)
  --comment <text>          Comment applied to rows without their own comment

UPLOAD (optional, uses the CSV/API path which only raises stock, never zeroes):
  --upload                  Upload each batch after generating
  --url <baseUrl>           e.g. https://vvg.openboxes.com/openboxes
  --facility-id <id>        OpenBoxes location id to import into
  --session-cookie <c>      "JSESSIONID=..." from a logged-in browser session
  --username <u> --password <p>   Alternative: form-login to obtain the session

EXAMPLES:
  groovy PartsMasterToInventory.groovy --input pm.csv --list-columns
  groovy PartsMasterToInventory.groovy --input pm.csv --dry-run
  groovy PartsMasterToInventory.groovy --input pm.csv --output-dir out --batch-size 100
  groovy PartsMasterToInventory.groovy --input pm.csv --format csv --upload \\
         --url https://vvg.openboxes.com/openboxes --facility-id ff8080... \\
         --session-cookie "JSESSIONID=abc123"
'''
}
