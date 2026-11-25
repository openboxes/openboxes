package org.pih.warehouse.importer

import org.springframework.stereotype.Component

/**
 * Reads in files containing bulk data to be imported into the system.
 */
@Component
class BulkDataImporter {

    final BulkDataBinder bulkDataDataBinder
    final BulkDataValidator bulkDataValidator
    final BulkDataImportComponentResolver componentResolver

    BulkDataImporter(final BulkDataBinder bulkDataDataBinder,
                     final BulkDataValidator bulkDataValidator,
                     final BulkDataImportComponentResolver componentResolver) {
        this.bulkDataDataBinder = bulkDataDataBinder
        this.bulkDataValidator = bulkDataValidator
        this.componentResolver = componentResolver
    }

    /**
     * Imports a bulk data source object (such as a file), binding its rows to a strongly
     * typed {@link Importable} object.
     */
    BulkDataImportResult importBulkDataSource(BulkDataImportRequest request) {
        BulkDataErrorSeverity stopProcessingOn = request.stopProcessingOn

        BulkDataImportResult importResult = new BulkDataImportResult()

        // STEP 1: Read in the source object
        BulkDataReaderResult readerResult = readFile(request)

        List<BulkDataError> readErrors = readerResult.readErrors
        importResult.importErrors.addAll(readErrors)
        if (shouldStopProcessing(readErrors, stopProcessingOn)) {
            return importResult
        }

        // STEP 2: Bind the rows to a strongly typed object
        BulkDataBinderResult binderResult = bindData(request, readerResult)
        importResult.boundRows = binderResult.boundRows

        List<BulkDataError> bindErrors = binderResult.bindErrors
        importResult.importErrors.addAll(bindErrors)
        if (shouldStopProcessing(bindErrors, stopProcessingOn)) {
            return importResult
        }

        // STEP 3: Validate each row
        BulkDataValidatorResult validatorResult = validate(request, binderResult)

        List<BulkDataError> validationErrors = validatorResult.validationErrors
        importResult.importErrors.addAll(validationErrors)
        if (shouldStopProcessing(validationErrors, stopProcessingOn)) {
            return importResult
        }

        // STEP 4: Persist the data
        if (request.persistData) {
            // TODO: Support persisting the data in binderResult.boundRows to the DB
        }

        return importResult
    }

    /**
     * Read in the file, formatting it into a map of rows for further processing.
     */
    private BulkDataReaderResult readFile(BulkDataImportRequest request) {
        BulkDataSource source = request.source

        BulkDataReader reader = componentResolver.getBulkDataReader(source.contentType)
        if (!reader) {
            throw new RuntimeException("No bulk data reader was found for content type ${source.contentType}")
        }

        BulkDataReaderConfig readerConfigOverride = request.readerConfigOverride
        return readerConfigOverride == null ?
                reader.read(source, request.dataImportType) :
                reader.read(source, readerConfigOverride)
    }

    /**
     * Bind the rows to a list of strongly-typed Importable objects.
     */
    private BulkDataBinderResult bindData(BulkDataImportRequest request, BulkDataReaderResult readerResult) {
        BulkDataBinderConfig dataBinderConfigOverride = request.dataBinderConfigOverride
        return dataBinderConfigOverride == null ?
                bulkDataDataBinder.bindData(request.dataImportType, readerResult) :
                bulkDataDataBinder.bindData(dataBinderConfigOverride, readerResult)
    }

    /**
     * Validate each of the Importable objects.
     */
    private BulkDataValidatorResult validate(BulkDataImportRequest request, BulkDataBinderResult binderResult) {
        // Because "columnByFieldName" is the only validator config, and because it is simply the inverse of the
        // "columnMapping" reader config, we don't require a validator config override to be provided. We build it
        // automatically here if a "columnMapping" was specified.
        BulkDataReaderConfig readerConfigOverride = request.readerConfigOverride
        if (readerConfigOverride) {
            BulkDataValidatorConfig validatorConfigOverride = new BulkDataValidatorConfig(
                    columnByFieldName: readerConfigOverride.columnMapping.collectEntries {
                        k, v -> [v, k]
                    } as Map<String, String>,
            )
            return bulkDataValidator.validate(request.dataImportType, validatorConfigOverride, binderResult.boundRows)
        }

        return bulkDataValidator.validate(request.dataImportType, binderResult.boundRows)
    }

    private boolean shouldStopProcessing(List<BulkDataError> errors, BulkDataErrorSeverity stopProcessingOn) {
        BulkDataErrorSeverity highestSeverity = errors.severity.max()
        return highestSeverity ? highestSeverity.isSameSeverityOrHigher(stopProcessingOn) : false
    }
}
