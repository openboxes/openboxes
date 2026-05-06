package org.pih.warehouse.importer

import org.pih.warehouse.core.date.EpochDate

/**
 * The result of reading in some source object containing bulk data to be imported to the system.
 *
 * Used in part one of the import process where we convert rows in the file to this simple format. This allows us
 * to structure the imported data in exactly the same way, no matter what we imported from (CSV, XLSX...). This
 * saves us from needing to define a separate data binder (part two of the import) for each file type.
 */
class BulkDataReaderResult {

    /**
     * The resulting rows that were extracted from the file.
     * The keys of the column map are the field names as specified in the data reader configuration.
     */
    List<Map<String, BulkDataCell>> rows = []

    /**
     * The epoch date to use when parsing date fields.
     *
     * This is required for importing Excel files and can likely be ignored for all other scenarios.
     */
    EpochDate epochDate = EpochDate.UNIX_EPOCH
}
