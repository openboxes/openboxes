package org.pih.warehouse.importer

/**
 * Enumerates the different severity levels of import / bulk data binding errors that can occur.
 */
enum BulkDataErrorSeverity {

    /**
     * Signifies that a row/col/field was able to be bound but there were issues that
     * the user should be made aware of. Attempting to persist this data should still succeed.
     */
    WARNING(10),

    /**
     * Signifies that a row/col/field was unable to be bound. Attempting to persist this data will
     * fail or create invalid state.
     */
    ERROR(20),

    /**
     * A numerical index representing the severity level. Higher is more severe.
     */
    private int severityIndex

    BulkDataErrorSeverity(int severityIndex) {
        this.severityIndex = severityIndex
    }

    /**
     * @return True if the severity is the same or higher than the given BulkDataErrorSeverity.
     */
    boolean isSameSeverityOrHigher(BulkDataErrorSeverity other) {
        return this.severityIndex >= other.severityIndex
    }
}
