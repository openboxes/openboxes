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
 * CsvToInventoryImport.groovy
 * ------------------------------------------------------------------------------
 * A GENERIC, source-agnostic tool that transforms an arbitrary inventory CSV
 * (whatever columns your source system exports) into OpenBoxes inventory import
 * files (quantity-on-hand baseline), split into batches.
 *
 * This is stage 1 (TRANSFORM) of a two-step flow. It only writes files so you can
 * review exactly what will be imported. Stage 2 (IMPORT) is a separate script,
 * ImportInventoryBatches.groovy, which uploads the reviewed .csv batches to OpenBoxes.
 *
 * There is NO source-specific logic in this script. All knowledge about the
 * source file - which column is the product code, which is the quantity, how to
 * filter rows - lives in an external JSON mapping config that you supply at
 * runtime with --config (or inline with --mapping / --filter). The script itself
 * only knows about OpenBoxes: its import columns, file formats, batching rules
 * and quantity semantics. See mapping.example.json and README.md.
 *
 * Two output formats, both derived from the same canonical rows:
 *
 *   xls  - Matches the OpenBoxes inventory import template
 *          (grails-app/conf/templates/inventory.xls). Upload manually via
 *          "Record Inventory > Import Inventory". Columns are read BY POSITION
 *          (A..H) by InventoryExcelImporter, so column order matters, not the
 *          header text. The source quantity goes in column G (Physical QOH).
 *
 *   csv  - For the API path: POST {baseUrl}/api/facilities/{facilityId}/inventories/import
 *          with Content-Type: text/csv. Header cells are camel-cased by the server
 *          (CSVUtils.toCamelCase), so "Product Code" -> productCode, "Quantity" ->
 *          quantity. The API path only RAISES stock (never zeroes) and is the
 *          safest option for reconciliation.
 *
 * Why "Physical QOH" (not "OB QOH"): OpenBoxes treats the imported quantity as the
 * ABSOLUTE target on-hand. It reads the current QoH from the database and creates
 * an adjustment for the difference (target - current). So we always populate the
 * quantity as the source value and leave "OB QOH" blank.
 *
 * SAFETY NOTE (batching): for every product present in an import file, OpenBoxes
 * zeroes out any OTHER bin/lot of that product that is NOT in the same file. This
 * tool therefore never splits a single product's rows across two batches.
 *
 * Requirements: Groovy 2.5+ / 3.x with internet access on first run (Grape pulls
 * Apache POI). Everything else uses the JDK.
 *
 * Quick start:
 *   groovy CsvToInventoryImport.groovy --input data.csv --list-columns
 *   groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --dry-run
 *   groovy CsvToInventoryImport.groovy --input data.csv --config mapping.json --output-dir out
 */

// Resolve from Maven Central explicitly. Older Groovy defaults to JCenter (shut down), which
// causes "unresolved dependency: org.apache.poi#poi ... not found". Grape pulls POI's transitive
// dependencies automatically. On first run this downloads to ~/.groovy/grapes.
@GrabResolver(name = 'central', root = 'https://repo1.maven.org/maven2/', m2compatible = true)
@Grab(group = 'org.apache.poi', module = 'poi', version = '5.2.5')
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

// NOTE: the JSON mapping config is parsed by the small built-in parseJson() below rather than
// groovy.json.JsonSlurper, so the script depends only on POI - some minimal Groovy installs do
// not bundle the groovy-json module.

// ---------------------------------------------------------------------------
// Canonical OpenBoxes inventory columns (order matters for the XLS template).
// This is the ONLY built-in schema - it describes OpenBoxes, not any source.
// Each entry: [canonical field, XLS header, API CSV header]
//   - the XLS is read by column position (A..H)
//   - the API CSV by camel-cased header name
// ---------------------------------------------------------------------------
final List<List<String>> OB_COLUMNS = [
        ['productCode', 'Product code', 'Product Code'],
        ['productName', 'Product name', 'Product'],
        ['lotNumber', 'Lot number', 'Lot Number'],
        ['expirationDate', 'Expiration date', 'Expiration Date'],
        ['binLocation', 'Bin location', 'Bin Location'],
        ['obQoh', 'OB QOH', 'Quantity On Hand'],   // always left blank; OB computes current
        ['quantity', 'Physical QOH', 'Quantity'],   // <-- the source QoH (absolute target)
        ['comments', 'Comment', 'Comments'],
]

// Canonical fields a mapping config may set (obQoh/expirationDate are managed by OB rules).
final List<String> MAPPABLE_FIELDS =
        ['productCode', 'productName', 'quantity', 'binLocation', 'lotNumber', 'comments']
final List<String> REQUIRED_FIELDS = ['productCode', 'quantity']

// Output formats this script can write. Add a new one here plus a branch in the write loop
// (and, for API-importable formats, teach ImportInventoryBatches.groovy to read it).
final List<String> SUPPORTED_FORMATS = ['xls', 'csv']

// ===========================================================================
// Arg parsing
// ===========================================================================
Map<String, String> opts = [:]
Set<String> flags = [] as Set
final Set<String> FLAG_NAMES = ['include-zero', 'dry-run', 'list-columns', 'clean', 'help'] as Set

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

if ('help' in flags || (!opts && !flags)) {
    printUsage()
    return
}

String inputPath = opts['input']
if (!inputPath) {
    die("--input <data.csv> is required. Use --help for usage.")
}
File inputFile = new File(inputPath)
if (!inputFile.exists()) {
    die("Input file not found: ${inputFile.absolutePath}")
}

boolean listColumns = 'list-columns' in flags
boolean dryRun = 'dry-run' in flags

// ---------------------------------------------------------------------------
// Load the mapping config (JSON). Everything source-specific comes from here.
// CLI options override config values.
// ---------------------------------------------------------------------------
Map config = [:]
if (opts['config']) {
    File configFile = new File(opts['config'])
    if (!configFile.exists()) {
        die("Config file not found: ${configFile.absolutePath}")
    }
    try {
        config = parseJson(configFile.getText('UTF-8')) as Map
    } catch (Exception e) {
        die("Could not parse config JSON (${configFile.name}): ${e.message}")
    }
}
Map configMapping = (config['mapping'] ?: [:]) as Map
Map configFilter = (config['filter'] ?: [:]) as Map
Map configOptions = (config['options'] ?: [:]) as Map

// Resolve options (CLI wins over config wins over built-in default)
String outputDirPath = opts['output-dir'] ?: (configOptions['outputDir'] ?: 'inventory-output')
// Output formats. Accepts a JSON list (["xls","csv"]), a comma-separated string ("xls,csv"),
// or a single value; "both" expands to every supported format. New writers (e.g. json, xml)
// only need to be added to SUPPORTED_FORMATS and given a branch in the write loop below.
def formatRaw = opts['format'] ?: configOptions['format'] ?: SUPPORTED_FORMATS
List rawFormatTokens = (formatRaw instanceof List) ? (formatRaw as List) : formatRaw.toString().split(',').toList()
List<String> formats = []
rawFormatTokens.each { def token ->
    String t = token.toString().trim().toLowerCase()
    if (!t) {
        return
    }
    List<String> toAdd = (t == 'both') ? SUPPORTED_FORMATS : [t]
    toAdd.each { String f -> if (!formats.contains(f)) formats << f }
}
if (!formats) {
    formats = new ArrayList<String>(SUPPORTED_FORMATS)
}
List<String> unsupportedFormats = formats.findAll { !(it in SUPPORTED_FORMATS) }
if (unsupportedFormats) {
    die("Unsupported output format(s): ${unsupportedFormats.join(', ')}. Supported: ${SUPPORTED_FORMATS.join(', ')}.")
}
int batchSize = (opts['batch-size'] ?: (configOptions['batchSize'] ?: 100)) as int
boolean includeZero = ('include-zero' in flags) || (configOptions['includeZero'] as boolean)
String defaultBin = opts['default-bin'] ?: (configOptions['defaultBin'] ?: null)
String comment = opts['comment'] ?: (configOptions['comment'] ?:
        "Inventory import ${new Date().format('yyyy-MM-dd')}")

// Source bin values that genuinely mean "no location" and should become a blank bin (e.g. a
// "NONE"/"UNASSIGNED" sentinel). Off by default. Only for values that mean "no bin" - a real
// staging/virtual bin should be mapped through as-is (and must exist in the depot). Supply per
// source via config options.binBlankValues (a JSON array) or --bin-blank-values a,b.
// Matched case-insensitively after trimming.
Set<String> binBlankValues = [] as Set
if (opts['bin-blank-values']) {
    opts['bin-blank-values'].split(',').each { binBlankValues << it.trim().toLowerCase() }
} else if (configOptions['binBlankValues'] instanceof List) {
    (configOptions['binBlankValues'] as List).each { binBlankValues << it.toString().trim().toLowerCase() }
}

// ===========================================================================
// Read the input CSV
// ===========================================================================
List<List<String>> rawRows = parseCsv(inputFile.getText('UTF-8'))
if (rawRows.size() < 2) {
    die("CSV appears to have no data rows: ${inputFile.absolutePath}")
}
List<String> headers = rawRows[0]
List<List<String>> dataRows = rawRows[1..-1]

Map<String, Integer> headerIndex = [:]
headers.eachWithIndex { String h, int idx -> headerIndex[normalize(h)] = idx }

// ---------------------------------------------------------------------------
// --list-columns: show the CSV headers so you can build a mapping config. This
// makes no assumptions about the source - it just lists what's there.
// ---------------------------------------------------------------------------
if (listColumns) {
    println "CSV columns detected in ${inputFile.name}:"
    headers.eachWithIndex { String h, int idx -> println "  [${idx}] ${h}" }
    println ""
    println "Build a --config mapping.json (see mapping.example.json). Map at least:"
    println "  productCode, quantity   (required)"
    println "  productName, binLocation, lotNumber, comments   (optional)"
    return
}

// ---------------------------------------------------------------------------
// Build mapping: canonical field -> source header. From config, then inline
// --mapping overrides. No auto-detection: mappings are always explicit.
// ---------------------------------------------------------------------------
Map<String, String> mapping = [:]
configMapping.each { k, v ->
    if (v != null && v.toString().trim()) {
        if (!(k in MAPPABLE_FIELDS)) {
            die("Config mapping has unknown field '${k}'. Valid fields: ${MAPPABLE_FIELDS.join(', ')}")
        }
        mapping[k as String] = v.toString()
    }
}
if (opts['mapping']) {
    opts['mapping'].split(',').each { String pair ->
        List<String> kv = pair.split('=', 2) as List
        if (kv.size() != 2) {
            die("Bad --mapping entry '${pair}'. Expected field=Header.")
        }
        String field = kv[0].trim()
        if (!(field in MAPPABLE_FIELDS)) {
            die("--mapping has unknown field '${field}'. Valid fields: ${MAPPABLE_FIELDS.join(', ')}")
        }
        mapping[field] = kv[1].trim()
    }
}

// Row filter (optional): keep only rows where <column> == <value>.
String filterColumn = configFilter['column'] ?: null
String filterValue = configFilter['equals'] != null ? configFilter['equals'].toString() : null
if (opts['filter']) {
    List<String> kv = opts['filter'].split('=', 2) as List
    if (kv.size() != 2) {
        die("Bad --filter '${opts['filter']}'. Expected Column=Value.")
    }
    filterColumn = kv[0].trim()
    filterValue = kv[1].trim()
}

// Validate mappings resolve to real CSV columns
if (!mapping) {
    die("No column mapping provided. Pass --config <mapping.json> or --mapping field=Header,...\n" +
            "Run with --list-columns to see the CSV's columns.")
}
List<String> missing = REQUIRED_FIELDS.findAll { !mapping[it] }
if (missing) {
    die("Mapping is missing required field(s): ${missing.join(', ')}.\n" +
            "CSV columns are: ${headers.join(', ')}")
}
mapping.each { field, header ->
    if (!headerIndex.containsKey(normalize(header))) {
        die("Mapping for '${field}' points at column '${header}', which is not in the CSV.\n" +
                "CSV columns are: ${headers.join(', ')}")
    }
}
if (filterColumn && !headerIndex.containsKey(normalize(filterColumn))) {
    die("Filter column '${filterColumn}' is not in the CSV. Columns: ${headers.join(', ')}")
}

println "Using column mapping:"
MAPPABLE_FIELDS.each { String f -> if (mapping[f]) println "  ${f.padRight(16)} <- ${mapping[f]}" }
if (filterColumn) println "Filtering rows where '${filterColumn}' == '${filterValue}'"
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
Integer idxFilter = filterColumn ? headerIndex[normalize(filterColumn)] : null

List<Map> canonicalRows = []
List<Map> skipped = []       // [row, reason, raw]

dataRows.eachWithIndex { List<String> row, int i ->
    int rowNumber = i + 2   // 1-based, +1 for header
    def cell = { Integer idx -> idx != null && idx < row.size() ? (row[idx] ?: '').trim() : '' }

    if (idxFilter != null && !cell(idxFilter).equalsIgnoreCase(filterValue)) {
        return   // silently skip rows that don't match the filter (not an error)
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
    if (qty < 0) {
        skipped << [row: rowNumber, reason: "negative quantity ${qty}", raw: row]
        return
    }
    if (qty == 0 && !includeZero) {
        skipped << [row: rowNumber, reason: "quantity 0 (use --include-zero to keep)", raw: row]
        return
    }

    String binValue = idxBin != null ? cell(idxBin) : ''
    if (binValue && binBlankValues.contains(binValue.toLowerCase())) {
        binValue = ''   // source "no location" placeholder -> blank bin
    }

    canonicalRows << [
            productCode   : productCode,
            productName   : idxProductName != null ? cell(idxProductName) : '',
            lotNumber     : idxLot != null ? cell(idxLot) : '',
            expirationDate: '',
            binLocation   : defaultBin ?: binValue,
            obQoh         : '',
            quantity      : qty,
            comments      : idxComment != null && cell(idxComment) ? cell(idxComment) : comment,
    ]
}

// Optional cap on the number of import rows, for quick test runs (--max / options.max).
int maxRows = (opts['max'] ?: configOptions['max'] ?: 0) as int
boolean capped = false
if (maxRows > 0 && canonicalRows.size() > maxRows) {
    canonicalRows = canonicalRows[0..<maxRows]
    capped = true
}

// ---------------------------------------------------------------------------
// Report
// ---------------------------------------------------------------------------
println "Parsed ${dataRows.size()} data row(s):"
println "  ${canonicalRows.size()} row(s) to import" + (capped ? " (capped at --max ${maxRows})" : "")
println "  ${skipped.size()} row(s) skipped"
if (skipped) {
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

File outputDir = new File(outputDirPath)

if (dryRun) {
    println "[dry-run] Would write ${batches.size()} batch file(s) to ${outputDir.absolutePath}"
    batches.eachWithIndex { List<Map> b, int i ->
        println "  batch ${String.format('%03d', i + 1)}: ${b.size()} row(s)"
    }
    return
}

outputDir.mkdirs()

// Clear (or warn about) batch/report files left by a previous run. Files are otherwise
// overwritten by name, so a shorter run would leave stale higher-numbered batches behind
// that the importer (--input-dir) would still pick up.
List<File> stale = (outputDir.listFiles({ File f ->
    f.isFile() && (f.name ==~ /inventory_batch_\d+\.(csv|xls)/ || f.name == 'skipped-records.csv')
} as FileFilter) ?: []) as List<File>
if (stale) {
    if ('clean' in flags) {
        stale.each { it.delete() }
        println "Removed ${stale.size()} file(s) from a previous run in ${outputDir.name}/\n"
    } else {
        println "WARNING: ${outputDir.name}/ already contains ${stale.size()} batch/report file(s) from a"
        println "         previous run. Same-named files are overwritten, but extras are NOT removed, so a"
        println "         shorter run leaves stale inventory_batch_*.csv that the importer would still upload."
        println "         Re-run with --clean, or use an empty --output-dir, to be safe.\n"
    }
}

// Skipped report - nothing is silently dropped.
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
// Write batch files. Review these before importing. The .csv batches are the
// canonical / API-ready form consumed by ImportInventoryBatches.groovy; the
// .xls batches are for the manual "Record Inventory > Import Inventory" UI.
// ---------------------------------------------------------------------------
batches.eachWithIndex { List<Map> batch, int i ->
    String base = "inventory_batch_${String.format('%03d', i + 1)}"
    if ('xls' in formats) {
        File f = new File(outputDir, "${base}.xls")
        writeXls(f, batch, OB_COLUMNS)
        println "Wrote ${f.name} (${batch.size()} row(s))"
    }
    if ('csv' in formats) {
        File f = new File(outputDir, "${base}.csv")
        writeApiCsv(f, batch, OB_COLUMNS)
        println "Wrote ${f.name} (${batch.size()} row(s))"
    }
}

println "\nDone. Review the output in ${outputDir.absolutePath}"
println "To import the .csv batches via the API:"
println "  groovy ImportInventoryBatches.groovy --input-dir ${outputDir.path} --url <baseUrl> --facility-id <id> --username <u> --password <p>"

// ===========================================================================
// Helper methods
// ===========================================================================

/** Group rows into batches of ~batchSize, never splitting one productCode across batches. */
static List<List<Map>> batchRows(List<Map> rows, int batchSize) {
    if (batchSize <= 0) {
        return [rows]
    }
    // Group by product, preserving first-seen order. (Avoid Map.computeIfAbsent with a closure:
    // older Groovy does not coerce the closure to a java.util.function.Function.)
    Map<String, List<Map>> byProduct = [:]
    rows.each { Map r ->
        String key = r.productCode as String
        List<Map> group = byProduct.get(key)
        if (group == null) {
            group = []
            byProduct.put(key, group)
        }
        group << r
    }

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
    if (sb.length() > 0 || !current.isEmpty()) {
        current << sb.toString()
        rows << current
    }
    return rows.findAll { !(it.size() == 1 && it[0].trim().isEmpty()) }
}

// ---------------------------------------------------------------------------
// Minimal dependency-free JSON parser (objects, arrays, strings, numbers,
// booleans, null) - avoids needing the groovy-json module. Returns Map / List /
// String / Number / Boolean / null.
// ---------------------------------------------------------------------------
static Object parseJson(String s) {
    int[] p = [0]
    Object v = jsonValue(s, p)
    jsonWs(s, p)
    if (p[0] != s.length()) {
        throw new RuntimeException("Unexpected trailing content at position ${p[0]}")
    }
    return v
}

static void jsonWs(String s, int[] p) {
    while (p[0] < s.length() && Character.isWhitespace(s.charAt(p[0]))) p[0]++
}

static Object jsonValue(String s, int[] p) {
    jsonWs(s, p)
    if (p[0] >= s.length()) throw new RuntimeException("Unexpected end of JSON")
    char c = s.charAt(p[0])
    if (c == '{') return jsonObject(s, p)
    if (c == '[') return jsonArray(s, p)
    if (c == '"') return jsonString(s, p)
    if (c == 't') { jsonLiteral(s, p, 'true'); return Boolean.TRUE }
    if (c == 'f') { jsonLiteral(s, p, 'false'); return Boolean.FALSE }
    if (c == 'n') { jsonLiteral(s, p, 'null'); return null }
    return jsonNumber(s, p)
}

static Map jsonObject(String s, int[] p) {
    Map m = [:]
    p[0]++ // consume {
    jsonWs(s, p)
    if (s.charAt(p[0]) == '}') { p[0]++; return m }
    while (true) {
        jsonWs(s, p)
        String key = jsonString(s, p)
        jsonWs(s, p)
        if (s.charAt(p[0]) != ':') throw new RuntimeException("Expected ':' at position ${p[0]}")
        p[0]++
        m[key] = jsonValue(s, p)
        jsonWs(s, p)
        char c = s.charAt(p[0])
        if (c == ',') { p[0]++; continue }
        if (c == '}') { p[0]++; break }
        throw new RuntimeException("Expected ',' or '}' at position ${p[0]}")
    }
    return m
}

static List jsonArray(String s, int[] p) {
    List a = []
    p[0]++ // consume [
    jsonWs(s, p)
    if (s.charAt(p[0]) == ']') { p[0]++; return a }
    while (true) {
        a << jsonValue(s, p)
        jsonWs(s, p)
        char c = s.charAt(p[0])
        if (c == ',') { p[0]++; continue }
        if (c == ']') { p[0]++; break }
        throw new RuntimeException("Expected ',' or ']' at position ${p[0]}")
    }
    return a
}

static String jsonString(String s, int[] p) {
    if (s.charAt(p[0]) != '"') throw new RuntimeException("Expected string at position ${p[0]}")
    p[0]++
    StringBuilder sb = new StringBuilder()
    while (true) {
        if (p[0] >= s.length()) throw new RuntimeException("Unterminated string")
        char c = s.charAt(p[0]++)
        if (c == '"') break
        if (c == '\\') {
            char e = s.charAt(p[0]++)
            switch (e) {
                case '"': sb.append('"' as char); break
                case '\\': sb.append('\\' as char); break
                case '/': sb.append('/' as char); break
                case 'n': sb.append('\n' as char); break
                case 't': sb.append('\t' as char); break
                case 'r': sb.append('\r' as char); break
                case 'b': sb.append('\b' as char); break
                case 'f': sb.append('\f' as char); break
                case 'u':
                    String hex = s.substring(p[0], p[0] + 4); p[0] += 4
                    sb.append((char) Integer.parseInt(hex, 16)); break
                default: throw new RuntimeException("Invalid escape '\\${e}' at position ${p[0]}")
            }
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

static void jsonLiteral(String s, int[] p, String lit) {
    if (!s.startsWith(lit, p[0])) throw new RuntimeException("Expected '${lit}' at position ${p[0]}")
    p[0] += lit.length()
}

static Object jsonNumber(String s, int[] p) {
    int start = p[0]
    while (p[0] < s.length()) {
        char ch = s.charAt(p[0])
        if (Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '.' || ch == 'e' || ch == 'E') {
            p[0]++
        } else {
            break
        }
    }
    String num = s.substring(start, p[0])
    if (num.isEmpty()) throw new RuntimeException("Invalid JSON value at position ${start}")
    if (num.contains('.') || num.contains('e') || num.contains('E')) return new BigDecimal(num)
    return Long.parseLong(num)
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
    if (reason.startsWith('quantity 0')) return 'quantity 0'
    if (reason.startsWith('negative')) return 'negative quantity'
    if (reason.startsWith('blank')) return 'blank product code'
    return reason
}

static void die(String message) {
    System.err.println("ERROR: ${message}")
    System.exit(1)
}

static void printUsage() {
    println '''\
CsvToInventoryImport.groovy
Transform an arbitrary inventory CSV into OpenBoxes inventory import batches,
driven by an external column-mapping config (no source-specific logic baked in).

USAGE:
  groovy CsvToInventoryImport.groovy --input <data.csv> --config <mapping.json> [options]

INSPECT / DRY RUN:
  --list-columns            Print the CSV's headers (to help build a config), then exit
  --dry-run                 Parse, filter and batch; report counts but write nothing

INPUT / MAPPING:
  --input <file>            Source inventory CSV (required)
  --config <mapping.json>   JSON column-mapping config (see mapping.example.json)
  --mapping <k=v,...>       Inline mapping/override, e.g. productCode=PartNo,quantity=QtyOnHand
  --filter <Column=Value>   Keep only source rows where Column == Value

OUTPUT:
  --output-dir <dir>        Output directory (default: inventory-output)
  --clean                   Remove old inventory_batch_*/skipped-records files first (default:
                            warn if the output dir has leftovers from a previous run)
  --format <list>           Comma-separated output formats: xls (manual UI import), csv (API
                            import). e.g. --format csv or --format xls,csv. "both" = all.
                            Config may use a JSON list: "format": ["xls","csv"]. Default: all.
  --batch-size <n>          Rows per batch, products never split (default: 100; 0 = one file)
  --max <n>                 Cap the number of import rows (for quick test runs; 0 = no cap)
  --include-zero            Keep rows with quantity 0 (default: skip them)
  --default-bin <name>      Force a bin location for every row (default: blank)
  --bin-blank-values <a,b>  Source bin values that truly mean "no bin" -> blank (e.g. NONE).
                            Do NOT use for real staging/virtual bins; map those through as-is.
  --comment <text>          Comment applied to rows without their own comment

Config values are overridden by the equivalent command-line options.

This script only TRANSFORMS. Review the generated .csv batches, then import them with
the companion script:
  groovy ImportInventoryBatches.groovy --input-dir <output-dir> --url <baseUrl> \\
         --facility-id <id> --username <u> --password <p>
'''
}
