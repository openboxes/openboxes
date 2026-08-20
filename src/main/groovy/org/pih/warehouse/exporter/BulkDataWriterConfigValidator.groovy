package org.pih.warehouse.exporter

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

/**
 * Validates that a writer config is formatted correctly.
 */
@Component
class BulkDataWriterConfigValidator extends ObjectValidator<BulkDataWriterConfig> {

    @Override
    protected ObjectValidationResult doValidate(BulkDataWriterConfig toValidate) {
        return new ObjectValidationResult(
                validateFields(toValidate),
        )
    }

    private ObjectError validateFields(BulkDataWriterConfig toValidate) {
        List<BulkDataWriterFieldConfig> fields = toValidate.fields
        if (!fields) {
            return null
        }

        Set<String> indexes = []
        List<String> duplicates = []
        boolean usingOrdinalIndexes = false
        for (field in fields) {
            if (field == null) {
                return rejectField("fields", "Null field configs are not allowed.")
            }

            String columnIndex = field.columnIndex
            if (StringUtils.isNotBlank(columnIndex)) {
                if (indexes.contains(columnIndex)) {
                    duplicates.add(columnIndex)
                    continue
                }

                indexes.add(columnIndex)
                continue
            }

            usingOrdinalIndexes = true
        }

        if (duplicates) {
            return rejectField("fields", "Fields config contains duplicate columnIndex values: ${duplicates}.")
        }

        if (indexes && usingOrdinalIndexes) {
            return rejectField("fields",
                    "All field configs must either define a unique columnIndex, or all must leave the field null.")
        }

        return null
    }
}
