package org.pih.warehouse.exporter

import org.springframework.validation.FieldError
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.validation.ObjectValidationResult

@Unroll
class BulkDataWriterConfigValidatorSpec extends Specification {

    @Shared
    BulkDataWriterConfigValidator validator

    void setup() {
        validator = new BulkDataWriterConfigValidator()
    }

    void "validate succeeds with all default values and no field config"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig()

        expect:
        assert validator.validate(config).isValid()
    }

    void "validate succeeds with a delimiter: #delimiter and addHeaderRow: #addHeaderRow"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                delimiter: delimiter,
                addHeaderRow: addHeaderRow,
        )

        expect:
        assert validator.validate(config).isValid()

        where:
        delimiter | addHeaderRow
        ","       | true
        ","       | false
        ":"       | true
        "\t"      | true
        "‍"     | true
    }

    void "validate succeeds with fields that do not specify a columnIndex"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "field1",
                                headerPlainText: "Field1 Header",
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "field2",
                                headerPlainText: "Field2 Header",
                        ),
                ],
        )

        expect:
        assert validator.validate(config).isValid()
    }

    void "validate succeeds with fields that specify a columnIndex"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "field1",
                                headerPlainText: "Field1 Header",
                                columnIndex: 0,
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "field2",
                                headerPlainText: "Field2 Header",
                                columnIndex: 1,
                        ),
                ],
        )

        expect:
        assert validator.validate(config).isValid()
    }

    void "validate fails with a null in the field config"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [null],
        )

        when:
        ObjectValidationResult result = validator.validate(config, false)

        then:
        assert result.errors.size() == 1
        assert result.errors[0] instanceof FieldError

        FieldError error = result.errors[0] as FieldError
        assert error.field == "fields"
        assert error.defaultMessage == "Null field configs are not allowed."
    }

    void "validate fails when we have duplicate columnIndex values in the field config"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "field1",
                                headerPlainText: "Field1 Header",
                                columnIndex: 0,
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "field2",
                                headerPlainText: "Field2 Header",
                                columnIndex: 0,
                        ),
                ],
        )

        when:
        ObjectValidationResult result = validator.validate(config, false)

        then:
        assert result.errors.size() == 1
        assert result.errors[0] instanceof FieldError

        FieldError error = result.errors[0] as FieldError
        assert error.field == "fields"
        assert error.defaultMessage == "Fields config contains duplicate columnIndex values: [0]."
    }

    void "validate fails when we only define columnIndex for some fields"() {
        given:
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "field1",
                                headerPlainText: "Field1 Header",
                                columnIndex: 0,
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "field2",
                                headerPlainText: "Field2 Header",
                                columnIndex: null,  // Invalid since we've defined a columnIndex for another field.
                        ),
                ],
        )

        when:
        ObjectValidationResult result = validator.validate(config, false)

        then:
        assert result.errors.size() == 1
        assert result.errors[0] instanceof FieldError

        FieldError error = result.errors[0] as FieldError
        assert error.field == "fields"
        assert error.defaultMessage == "All field configs must either define a unique columnIndex, or all must " +
                "leave the field null."
    }
}
