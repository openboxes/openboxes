package org.pih.warehouse.importer

import grails.databinding.BindUsing
import grails.databinding.DataBindingSource
import java.time.Instant

import org.pih.warehouse.core.validation.ObjectValidatable

class BulkDataImportRequest implements ObjectValidatable {

    /**
     * Contains the bulk data to import.
     *
     * This will be the file itself for a {@link MultipartFileSource}.
     *
     * This will be a String for a {@link StringSource} but note that a string source must also specify
     * the {@link org.pih.warehouse.core.http.ContentType} contentType field in the root of the request payload.
     */
    @BindUsing({ obj, DataBindingSource source -> BulkDataSourceBuilder.build(source) })
    BulkDataSource source

    /**
     * The feature associated with the data being imported.
     * Is used to fetch the default bulk data configuration.
     */
    BulkDataType dataImportType

    /**
     * If we receive a bulk data error with this severity or higher, will will stop processing
     * and not proceed to the next step of the import.
     *
     * For example, if stopProcessingOn==ERROR and we receive an error with severity WARNING, we will still
     * track and return the error but it will not stop further processing.
     */
    BulkDataErrorSeverity stopProcessingOn = BulkDataErrorSeverity.ERROR

    /**
     * True if we should persist the imported data to the database after successfully completing the import.
     */
    boolean persistData = false

    /**
     * If persisting the data (ie persistData==true), this datetime can be used by the data-binder to set
     * certain date-related fields.
     */
    Instant importDate = Instant.now()

    /**
     * For providing custom configuration to the bulk data reader.
     * Typically it is better to rely on the config defined in a {@link ConfiguresBulkDataReader} implementation.
     */
    BulkDataReaderConfig readerConfigOverride

    /**
     * For providing custom configuration to the bulk data binder.
     * Typically it is better to rely on the config defined in a {@link ConfiguresBulkDataBinder} implementation.
     */
    BulkDataBinderConfig dataBinderConfigOverride

    static constraints = {
        readerConfigOverride(nullable: true)
        dataBinderConfigOverride(nullable: true)
    }
}
