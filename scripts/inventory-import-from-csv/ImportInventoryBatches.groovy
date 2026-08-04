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
 * ImportInventoryBatches.groovy
 * ------------------------------------------------------------------------------
 * Stage 2 (IMPORT) of the CSV -> OpenBoxes inventory flow. Uploads inventory batch
 * CSV files - the ones produced and reviewed from CsvToInventoryImport.groovy - to
 * an OpenBoxes instance via its inventory import API:
 *
 *   POST {url}/api/facilities/{facilityId}/inventories/import
 *   Content-Type: text/csv
 *
 * This is deliberately separate from the transform step so you review exactly what
 * will be imported. It uploads whatever .csv batch files are on disk, so any manual
 * edits you made after reviewing are exactly what gets sent.
 *
 * The API path only RAISES stock (it never zeroes anything) and each file is one
 * atomic baseline+adjustment transaction. By default the run ABORTS on the first
 * rejected batch - most commonly a product code that does not exist in OpenBoxes -
 * so a problem is loud rather than silently skipped. Batches already imported stay
 * applied; pass --continue-on-error to push the rest anyway.
 *
 * No third-party dependencies (no Apache POI, no groovy-json) - pure JDK, so it runs
 * on any Groovy 2.5+ / 3.x.
 *
 * Quick start:
 *   # See what would be uploaded, in what order (no upload)
 *   groovy ImportInventoryBatches.groovy --input-dir out --dry-run
 *
 *   # Upload every inventory_batch_*.csv in ./out
 *   groovy ImportInventoryBatches.groovy --input-dir out \
 *          --url https://your-openboxes-host/openboxes --facility-id <locationId> \
 *          --username <user> --password <pass>
 */

// ===========================================================================
// Arg parsing
// ===========================================================================
Map<String, String> opts = [:]
Set<String> flags = [] as Set
final Set<String> FLAG_NAMES = ['dry-run', 'continue-on-error', 'help'] as Set

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

boolean dryRun = 'dry-run' in flags
boolean continueOnError = 'continue-on-error' in flags

// ---------------------------------------------------------------------------
// Resolve the batch files: either every inventory_batch_*.csv in --input-dir,
// or an explicit comma-separated list via --input-files.
// ---------------------------------------------------------------------------
List<File> batchFiles = []
if (opts['input-files']) {
    opts['input-files'].split(',').each { String path ->
        File f = new File(path.trim())
        if (!f.exists()) die("Batch file not found: ${f.absolutePath}")
        batchFiles << f
    }
} else if (opts['input-dir']) {
    File dir = new File(opts['input-dir'])
    if (!dir.isDirectory()) die("Not a directory: ${dir.absolutePath}")
    batchFiles = (dir.listFiles({ File f ->
        f.isFile() && f.name ==~ /inventory_batch_\d+\.csv/
    } as FileFilter) as List<File>).sort { it.name }
    if (!batchFiles) {
        die("No inventory_batch_*.csv files found in ${dir.absolutePath}.\n" +
                "Run CsvToInventoryImport.groovy first, or point --input-files at specific files.")
    }
} else {
    die("Provide --input-dir <dir> (uses inventory_batch_*.csv) or --input-files <a.csv,b.csv>.")
}

// Report the plan (with record counts) so a --dry-run is a real review step.
println "Found ${batchFiles.size()} batch file(s):"
int totalRows = 0
batchFiles.each { File f ->
    int rows = countCsvRecords(f)
    totalRows += rows
    println "  ${f.name}  (${rows} row(s))"
}
println "Total: ${totalRows} row(s) across ${batchFiles.size()} batch(es)\n"

if (dryRun) {
    String target = opts['url'] && opts['facility-id'] ?
            "${opts['url'].replaceAll('/+$', '')}/api/facilities/${opts['facility-id']}/inventories/import" :
            "<url>/api/facilities/<facility-id>/inventories/import"
    println "[dry-run] Would POST each of the above to:\n  ${target}"
    return
}

// ---------------------------------------------------------------------------
// Validate upload args and resolve auth.
// ---------------------------------------------------------------------------
String baseUrl = opts['url']
String facilityId = opts['facility-id']
if (!baseUrl || !facilityId) {
    die("--url <baseUrl> and --facility-id <locationId> are required to upload " +
            "(or pass --dry-run to just review the batches).")
}
baseUrl = baseUrl.replaceAll('/+$', '')

String cookie = opts['session-cookie']
if (!cookie) {
    if (opts['username'] && opts['password']) {
        println "Logging in as ${opts['username']} ..."
        cookie = login(baseUrl, opts['username'], opts['password'])
        println "Obtained session cookie.\n"
    } else {
        die("Auth required: pass --session-cookie \"JSESSIONID=...\" or --username and --password.")
    }
}

// ---------------------------------------------------------------------------
// Upload, fail-fast by default.
// ---------------------------------------------------------------------------
String importUrl = "${baseUrl}/api/facilities/${facilityId}/inventories/import"
println "Uploading ${batchFiles.size()} batch(es) to ${importUrl}\n"
int ok = 0
for (int i = 0; i < batchFiles.size(); i++) {
    File f = batchFiles[i]
    try {
        uploadCsv(importUrl, f, cookie)
        ok++
        println "  [${i + 1}/${batchFiles.size()}] ${f.name} -> OK"
    } catch (Exception e) {
        println "  [${i + 1}/${batchFiles.size()}] ${f.name} -> FAILED: ${e.message}"
        if (!continueOnError) {
            die("Aborting: batch ${f.name} was rejected by OpenBoxes.\n" +
                    "  ${ok} batch(es) before it were already imported.\n" +
                    "  This usually means a row references a product code that does not exist.\n" +
                    "  Fix the data (or re-transform) and re-run, or pass --continue-on-error " +
                    "to push the remaining batches anyway.")
        }
    }
}
println "\nUpload complete: ${ok}/${batchFiles.size()} succeeded."

// ===========================================================================
// Helpers
// ===========================================================================

/** Count data records (excluding the header) in a CSV, respecting quoted newlines. */
static int countCsvRecords(File file) {
    String text = file.getText('UTF-8')
    int records = 0
    boolean inQuotes = false
    boolean sawContent = false
    int n = text.length()
    for (int i = 0; i < n; i++) {
        char ch = text.charAt(i)
        if (ch == ('"' as char)) {
            inQuotes = !inQuotes
            sawContent = true
        } else if (ch == ('\n' as char) && !inQuotes) {
            records++
            sawContent = false
        } else if (ch != ('\r' as char)) {
            if (!Character.isWhitespace(ch)) sawContent = true
        }
    }
    if (sawContent) records++   // last line without trailing newline
    return Math.max(0, records - 1)   // minus header row
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
ImportInventoryBatches.groovy
Upload reviewed inventory batch CSV files (from CsvToInventoryImport.groovy) to
OpenBoxes via POST /api/facilities/{facilityId}/inventories/import.

USAGE:
  groovy ImportInventoryBatches.groovy --input-dir <dir> --url <baseUrl> \\
         --facility-id <id> (--session-cookie <c> | --username <u> --password <p>) [options]

INPUT:
  --input-dir <dir>         Directory of inventory_batch_*.csv files (sorted by name)
  --input-files <a,b,...>   Alternative: explicit comma-separated list of CSV files

TARGET / AUTH:
  --url <baseUrl>           e.g. https://your-openboxes-host/openboxes
  --facility-id <id>        OpenBoxes location id to import into
  --session-cookie <c>      "JSESSIONID=..." from a logged-in browser session
  --username <u> --password <p>   Alternative: form-login to obtain the session

OPTIONS:
  --dry-run                 List the batches (and row counts) that would be uploaded; no upload
  --continue-on-error       Keep uploading after a rejected batch (default: abort on the first,
                            e.g. a product code that does not exist)

Notes:
  - The API only RAISES stock; it never zeroes existing quantities.
  - Each file is one atomic transaction. Keep all rows for a product in one file (the
    transform script guarantees this) so nothing gets unintentionally zeroed.
'''
}
