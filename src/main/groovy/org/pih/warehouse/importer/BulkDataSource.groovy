package org.pih.warehouse.importer

import grails.validation.Validateable

import org.pih.warehouse.core.http.ContentType

/**
 * An object containing some bulk data. Typically this source will be read in by a BulkDataReader, likely for
 * the purpose of being imported to the system.
 *
 * @param <SourceType> The class type of the object containing the bulk data. Often this will be a file, but some
 *                     content types support reading data in via other sources, such as Strings.
 */
interface BulkDataSource<SourceType> extends Validateable {

    /**
     * @return The object containing the bulk data.
     */
    SourceType getSource()

    /**
     * @return The content type associated with source object.
     */
    ContentType getContentType()
}
