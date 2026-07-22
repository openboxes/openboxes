package org.pih.warehouse.importer

import grails.validation.Validateable
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.http.ContentType

@Unroll
class BulkDataImporterSpec extends Specification {

    // Chosen arbitrarily. This will be stubbed so all that matters is that we're consistent.
    private static final BulkDataType BULK_DATA_TYPE = BulkDataType.PERSON

    @Shared
    CsvReader readerStub

    @Shared
    BulkDataBinder dataBinderStub

    @Shared
    BulkDataValidator validatorStub

    @Shared
    BulkDataImporter importer

    void setup() {
        readerStub = Stub(CsvReader)
        dataBinderStub = Stub(BulkDataBinder)
        validatorStub = Stub(BulkDataValidator)
        BulkDataImportComponentResolver componentResolverStub = Stub(BulkDataImportComponentResolver) {
            getBulkDataReader(ContentType.CSV) >> readerStub
        }

        importer = new BulkDataImporter(dataBinderStub, validatorStub, componentResolverStub)
    }

    void "importBulkDataSource should succeed when no errors are thrown"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=ERROR"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
        )

        and: "The stubbed reader result containing no errors/warnings"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
                readErrors: new BulkDataErrors(errors: []),
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing no errors/warnings"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
                bindErrors: new BulkDataErrors(errors: []),
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        and: "The stubbed validator result containing no errors/warnings"
        BulkDataValidatorResult validatorResult = new BulkDataValidatorResult(
                validationErrors: new BulkDataErrors(errors: []),
        )
        validatorStub.validate(BULK_DATA_TYPE, binderResult.boundRows) >> validatorResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<Importable> rows = result.boundRows
        assert rows[0]["stringField"] == "ABC"
        assert rows[0]["integerField"] == 0

        and: "No errors are returned"
        assert !result.importErrors.hasErrors()
    }

    void "importBulkDataSource should error when a read error is thrown"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=ERROR"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
        )

        and: "The stubbed reader result containing an error"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
                readErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.ERROR),
                ]),
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is not bound"
        assert result.boundRows.empty

        and: "The reader error is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.ERROR
    }

    void "importBulkDataSource should error when a read warning is thrown and stopProcessingOn=WARNING"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=WARNING"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
                stopProcessingOn: BulkDataErrorSeverity.WARNING
        )

        and: "The stubbed reader result containing a warning"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
                readErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.WARNING),
                ])
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is not bound"
        assert result.boundRows.empty

        and: "The reader warning is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.WARNING
    }

    void "importBulkDataSource should error when a data binding error is thrown"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=ERROR"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
        )

        and: "The stubbed reader result containing no errors/warnings"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing an error"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
                bindErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.ERROR),
                ]),
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<ImportableForTest> boundRows = result.boundRows as List<ImportableForTest>
        assert boundRows[0].stringField == "ABC"
        assert boundRows[0].integerField == 0

        and: "The data binder error is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.ERROR
    }

    void "importBulkDataSource should error when a data binding warning is thrown and stopProcessingOn=WARNING"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=WARNING"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
                stopProcessingOn: BulkDataErrorSeverity.WARNING,
        )

        and: "The stubbed reader result containing no errors/warnings"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing a warning"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
                bindErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.WARNING),
                ]),
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<ImportableForTest> boundRows = result.boundRows as List<ImportableForTest>
        assert boundRows[0].stringField == "ABC"
        assert boundRows[0].integerField == 0

        and: "The data binder warning is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.WARNING
    }

    void "importBulkDataSource should error when a validation error is thrown"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
        )

        and: "The stubbed reader result containing no errors/warnings"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing no errors/warnings"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        and: "The stubbed validator result containing an error"
        BulkDataValidatorResult validatorResult = new BulkDataValidatorResult(
                validationErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.ERROR),
                ]),
        )
        validatorStub.validate(BULK_DATA_TYPE, binderResult.boundRows) >> validatorResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<ImportableForTest> boundRows = result.boundRows as List<ImportableForTest>
        assert boundRows[0].stringField == "ABC"
        assert boundRows[0].integerField == 0

        and: "The validator error is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.ERROR
    }

    void "importBulkDataSource should error when a validation warning is thrown and stopProcessingOn=WARNING"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=WARNING"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
                stopProcessingOn: BulkDataErrorSeverity.WARNING,
        )

        and: "The stubbed reader result containing no errors/warnings"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing no errors/warnings"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        and: "The stubbed validator result containing a warning"
        BulkDataValidatorResult validatorResult = new BulkDataValidatorResult(
                validationErrors: new BulkDataErrors(errors: [
                        new BulkDataError(
                                row: 1, column: 0, fieldName: "stringField", severity: BulkDataErrorSeverity.WARNING),
                ]),
        )
        validatorStub.validate(BULK_DATA_TYPE, binderResult.boundRows) >> validatorResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<ImportableForTest> boundRows = result.boundRows as List<ImportableForTest>
        assert boundRows[0].stringField == "ABC"
        assert boundRows[0].integerField == 0

        and: "The validator warning is returned"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors[0].row == 1
        assert errors[0].column == "0"
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.WARNING
    }

    void "importBulkDataSource should succeed when only warnings are thrown and stopProcessingOn=ERROR"() {
        given: "A source object"
        StringSource csvString = new StringSource(
                source: "stringField,integerField\n" +
                        "ABC,0",
                contentType: ContentType.CSV,
        )

        and: "A request object with stopProcessingOn=ERROR"
        BulkDataImportRequest request = new BulkDataImportRequest(
                source: csvString,
                dataImportType: BULK_DATA_TYPE,
        )

        and: "The stubbed reader result containing a warning"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(
                rows: [[
                        "stringField": new BulkDataCell(row: 1, column: 0, fieldName: "stringField", value: "ABC"),
                        "integerField": new BulkDataCell(row: 1, column: 1, fieldName: "integerField", value: "0"),
                ]],
                readErrors: new BulkDataErrors(errors: [
                        new BulkDataError(severity: BulkDataErrorSeverity.WARNING, localizedMessage: "READ"),
                ]),
        )
        readerStub.read(csvString, BULK_DATA_TYPE) >> readerResult

        and: "The stubbed data binder result containing a warning"
        BulkDataBinderResult binderResult = new BulkDataBinderResult(
                boundRows: [new ImportableForTest(stringField: "ABC", integerField: 0)],
                bindErrors: new BulkDataErrors(errors: [
                        new BulkDataError(severity: BulkDataErrorSeverity.WARNING, localizedMessage: "BIND"),
                ]),
        )
        dataBinderStub.bindData(BULK_DATA_TYPE, readerResult) >> binderResult

        and: "The stubbed validator result containing a warning"
        BulkDataValidatorResult validatorResult = new BulkDataValidatorResult(
                validationErrors: new BulkDataErrors(errors: [
                        new BulkDataError(severity: BulkDataErrorSeverity.WARNING, localizedMessage: "VALIDATE"),
                ]),
        )
        validatorStub.validate(BULK_DATA_TYPE, binderResult.boundRows) >> validatorResult

        when:
        BulkDataImportResult result = importer.importBulkDataSource(request)

        then: "The data is bound successfully"
        List<ImportableForTest> boundRows = result.boundRows as List<ImportableForTest>
        assert boundRows[0].stringField == "ABC"
        assert boundRows[0].integerField == 0

        and: "all warnings are present"
        List<BulkDataError> errors = result.importErrors.allErrors
        assert errors.size() == 3
        assert errors.any { it.localizedMessage == "READ" }
        assert errors.any { it.localizedMessage == "BIND" }
        assert errors.any { it.localizedMessage == "VALIDATE" }
    }

    /**
     * A simple importable, validatable object to use in tests.
     */
    static class ImportableForTest implements Importable, Validateable {
        // We use the default constraints, meaning neither field is nullable.
        String stringField
        Integer integerField
    }
}
