// Vendored from org.grails.plugins:csv:1.0.1 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org csv-1.0.1-sources.jar. Unmodified except this header.
/*
 * Copyright 2010 Les Hazlewood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package grails.plugins.csv

import au.com.bytecode.opencsv.CSVParser
import au.com.bytecode.opencsv.CSVReader

/**
 * Utility class for adding CSV parsing capability to core Java/Groovy classes (String, File, InputStream, Reader).
 *
 * @since 0.1
 * @author Les Hazlewood
 */
class CSVReaderUtils {

    public static void eachLine(CSVReader csvReader, Closure c) {
        try {
            String[] tokens = csvReader.readNext()
            while (tokens) {
                c.doCall(tokens);
                tokens = csvReader.readNext();
            }
        } finally {
            csvReader.close()
        }
    }

    public static void eachLine(File file, Closure c) {
        eachLine(toCsvReader(file, null), c);
    }

    public static void eachLine(InputStream is, Closure c) {
        eachLine(toCsvReader(is, null), c);
    }

    public static void eachLine(Reader r, Closure c) {
        eachLine(toCsvReader(r, null), c);
    }

    public static void eachLine(String csv, Closure c) {
        eachLine(toCsvReader(csv, null), c);
    }

    public static CSVReader toCsvReader(File file, def settingsMap) {
        return toCsvReader(new FileInputStream(file), settingsMap)
    }

    public static CSVReader toCsvReader(InputStream is, def settingsMap) {
        def charset = settingsMap?.get('charset')
        InputStreamReader reader;
        if (charset) {
            reader = new InputStreamReader(is, charset)
        } else {
            reader = new InputStreamReader(is)
        }
        return toCsvReader(reader, settingsMap);
    }

    public static CSVReader toCsvReader(Reader r, def settingsMap) {

        char separatorChar = (settingsMap?.get('separatorChar') ?: CSVParser.DEFAULT_SEPARATOR) as char
        char quoteChar = (settingsMap?.get('quoteChar') ?: CSVParser.DEFAULT_QUOTE_CHARACTER) as char
        char escapeChar = (settingsMap?.get('escapeChar') ?: CSVParser.DEFAULT_ESCAPE_CHARACTER) as char
        int skipLines = Math.max(0, (settingsMap?.get('skipLines') ?: 0) as int)

        boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES
        def mapValue = settingsMap?.get('strictQuotes')
        if (mapValue != null) {
            strictQuotes = mapValue as boolean
        }

        boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE
        mapValue = settingsMap?.get('ignoreLeadingWhiteSpace')
        if (mapValue != null) {
            ignoreLeadingWhiteSpace = mapValue as boolean
        }

        return new CSVReader(r, separatorChar, quoteChar, escapeChar, skipLines, strictQuotes, ignoreLeadingWhiteSpace)
    }

    public static CSVReader toCsvReader(String s, def settingsMap) {
        return toCsvReader(new StringReader(s), settingsMap)
    }


}
