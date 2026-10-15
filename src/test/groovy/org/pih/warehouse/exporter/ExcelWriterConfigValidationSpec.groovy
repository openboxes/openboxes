package org.pih.warehouse.exporter

import grails.testing.gorm.DataTest
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Checks that calling validate() on a writer config actually runs BulkDataWriterConfigValidator.
 * BulkDataWriterConfigValidatorSpec covers the validator's rules; this covers how it is hooked in.
 */
@Unroll
class ExcelWriterConfigValidationSpec extends Specification implements DataTest {

    void setupSpec() {
        // ObjectValidatable.validate() fetches the javax "defaultValidator" bean and the config's ObjectValidator
        // bean from the app context. The DataTest context registers neither, so define them here.
        defineBeans {
            defaultValidator(LocalValidatorFactoryBean)
            bulkDataWriterConfigValidator(BulkDataWriterConfigValidator)
        }
    }

    void "validate rejects an ExcelWriterConfig whose fields have #description"() {
        given:
        ExcelWriterConfig config = new ExcelWriterConfig(fields: fields)

        expect: "the custom validator runs, not only the Grails constraints"
        assert !config.validate()
        assert config.errors.getFieldErrors("fields")

        where:
        description                      | fields
        "duplicate column indexes"       | [new BulkDataWriterFieldConfig(fieldName: "field1", columnIndex: "A"),
                                            new BulkDataWriterFieldConfig(fieldName: "field2", columnIndex: "A")]
        "both indexed and ordinal fields" | [new BulkDataWriterFieldConfig(fieldName: "field1", columnIndex: "A"),
                                            new BulkDataWriterFieldConfig(fieldName: "field2")]
    }

    void "validate accepts a well-formed ExcelWriterConfig"() {
        given:
        ExcelWriterConfig config = new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "field1", columnIndex: "A"),
                        new BulkDataWriterFieldConfig(fieldName: "field2", columnIndex: "B"),
                ],
        )

        expect:
        assert config.validate()
    }

    // Separate scope (already broken on develop): Validateable's private static findConstraintsEvaluator() is only
    // woven into BulkDataWriterConfig, where the trait is applied, so validate() on the subclass throws a
    // MissingMethodException. Remove @PendingFeature once fixed.
    @PendingFeature
    void "validate rejects a CsvWriterConfig whose fields have duplicate column indexes"() {
        given:
        CsvWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "field1", columnIndex: "A"),
                        new BulkDataWriterFieldConfig(fieldName: "field2", columnIndex: "A"),
                ],
        )

        expect:
        assert !config.validate()
        assert config.errors.getFieldErrors("fields")
    }
}
