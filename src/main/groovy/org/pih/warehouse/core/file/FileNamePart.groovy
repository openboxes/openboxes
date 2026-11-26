package org.pih.warehouse.core.file

/**
 * Placeholders for file name parts to be filled in by {@link FileNameGenerator} when the file name is generated.
 */
enum FileNamePart {

    /**
     * The current time (when the file name is generated). Formatted the same as any other Instant.
     */
    CURRENT_TIME,
}
