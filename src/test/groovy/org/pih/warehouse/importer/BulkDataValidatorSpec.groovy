package org.pih.warehouse.importer

import grails.validation.Validateable
import org.springframework.context.support.DefaultMessageSourceResolvable
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.localization.LocalizableMessage
import org.pih.warehouse.core.localization.MessageLocalizer

@Unroll
class BulkDataValidatorSpec extends Specification {

    // Chosen arbitrarily. This will be stubbed so all that matters is that we're consistent.
    private static final BulkDataType BULK_DATA_TYPE = BulkDataType.PERSON

    private static final String VALUE_THAT_FAILS_VALIDATION = "invalid"

    BulkDataValidator bulkDataValidator

    void setup() {
        MessageLocalizer messageLocalizerStub = Stub(MessageLocalizer) {
            localize(_ as DefaultMessageSourceResolvable) >> "LOCALIZED MESSAGE - CONSTRAINT VIOLATION"
            localize(_ as LocalizableMessage) >> "LOCALIZED MESSAGE - CUSTOM VIOLATION"
        }

        BulkDataImportComponentResolver componentResolverStub = Stub(BulkDataImportComponentResolver) {
            getBulkDataValidatorConfigurer(BULK_DATA_TYPE) >> new BulkDataValidatorConfigurerForTest()
        }

        bulkDataValidator = new BulkDataValidator(componentResolverStub, messageLocalizerStub)
    }

    void "validate should return nothing when no validation is triggered"() {
        given: "the data being validated"
        BulkDataType bulkDataType = BULK_DATA_TYPE
        List<ImportableForTest> data = [
                new ImportableForTest(stringField: "validValue", integerField: 1),
                new ImportableForTest(stringField: "otherValidValue", integerField: 2345),
        ]

        when: "validate is called"
        List<BulkDataError> errors = bulkDataValidator.validate(bulkDataType, data)

        then:
        assert errors.size() == 0
    }

    void "validate should return an error when the object's constraint validation is triggered"() {
        given: "the data being validated"
        BulkDataType bulkDataType = BULK_DATA_TYPE
        List<ImportableForTest> data = [
                new ImportableForTest(stringField: "validValue", integerField: 1),
                new ImportableForTest(stringField: null, integerField: 1),
        ]

        when: "validate is called"
        List<BulkDataError> errors = bulkDataValidator.validate(bulkDataType, data)

        then:
        assert errors.size() == 1
        assert errors[0].row == 1
        assert errors[0].fieldName == "stringField"
        assert errors[0].severity == BulkDataErrorSeverity.ERROR
        assert errors[0].localizedMessage == "LOCALIZED MESSAGE - CONSTRAINT VIOLATION"
    }

    void "validate should return an error when custom validation is triggered on the object"() {
        given: "the data being validated"
        BulkDataType bulkDataType = BULK_DATA_TYPE
        List<ImportableForTest> data = [
                new ImportableForTest(stringField: "validValue", integerField: 1),
                new ImportableForTest(stringField: "validValue", integerField: -1),
        ]

        when: "validate is called"
        List<BulkDataError> errors = bulkDataValidator.validate(bulkDataType, data)

        then:
        assert errors.size() == 1
        assert errors[0].row == 1
        assert errors[0].fieldName == "integerField"
        assert errors[0].severity == BulkDataErrorSeverity.WARNING
        assert errors[0].localizedMessage == "LOCALIZED MESSAGE - CUSTOM VIOLATION"
    }

    void "validate should return both errors when both a constraint violation and custom validation are triggered"() {
        given: "the data being validated"
        BulkDataType bulkDataType = BULK_DATA_TYPE
        List<ImportableForTest> data = [
                new ImportableForTest(stringField: VALUE_THAT_FAILS_VALIDATION, integerField: null),
        ]

        when: "validate is called"
        List<BulkDataError> errors = bulkDataValidator.validate(bulkDataType, data)

        then:
        assert errors.size() == 2

        BulkDataError stringFieldError = errors.find { it.fieldName == "stringField" }
        assert stringFieldError != null
        assert stringFieldError.row == 0
        assert stringFieldError.severity == BulkDataErrorSeverity.ERROR
        assert stringFieldError.localizedMessage == "LOCALIZED MESSAGE - CUSTOM VIOLATION"

        BulkDataError integerFieldError = errors.find { it.fieldName == "integerField" }
        assert integerFieldError != null
        assert integerFieldError.row == 0
        assert integerFieldError.severity == BulkDataErrorSeverity.ERROR
        assert integerFieldError.localizedMessage == "LOCALIZED MESSAGE - CONSTRAINT VIOLATION"
    }

    /**
     * A simple importable, validatable object to use in tests.
     */
    class ImportableForTest implements Importable, Validateable {

        String stringField
        Integer integerField

        static constraints = {
            stringField(nullable: false)
            integerField(nullable: false)
        }
    }

    /**
     * A simple configurer of custom validation for use in tests.
     * The validation itself is arbitrary. We just define something so that we can make assertions on it.
     */
    class BulkDataValidatorConfigurerForTest implements ConfiguresBulkDataValidator<ImportableForTest> {

        @Override
        BulkDataType getBulkDataType() {
            return BULK_DATA_TYPE
        }

        @Override
        BulkDataErrors customValidateRow(ImportableForTest row) {
            BulkDataErrors errors = new BulkDataErrors()
            if (row.stringField == VALUE_THAT_FAILS_VALIDATION) {
                errors.addFieldError("stringField", BulkDataErrorSeverity.ERROR, "some.code")
            }
            if (row.integerField != null && row.integerField < 0) {
                errors.addFieldError("integerField", BulkDataErrorSeverity.WARNING, "some.other.code")
            }
            return errors
        }
    }
}
