package org.pih.warehouse.requisition

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.localization.MessageLocalizer

@Transactional
class RequisitionTemplateService {

    MessageLocalizer messageLocalizer

    List<Object> parseImportFile(InputStream inputStream, Requisition requisition, String delimiter, Integer skipLines) {
        List<Object> data = []
        if (requisition) {
            if (!inputStream) {
                throw new IllegalArgumentException("Must specify a file")
            }

            inputStream.toCsvReader('separatorChar': delimiter, 'skipLines': skipLines).eachLine { tokens ->
                println "line: " + tokens + " delimiter=" + delimiter
                println "ROW " + tokens
                if (tokens) {
                    data << tokens[0..3]
                }
            }

            log.info "Data: " + data
        }

        return data
    }

    List<String> validateImportData(List<Object> data) {
        List<String> errors = []

        // Imported data is in format: [Product code, Product name, Quantity, UoM]
        List<String> duplicationErrors = data
                // adding line numbers to the data so that we can report which lines have duplicates
                .withIndex(1)
                // grouping by product code, which is the first column in the data
                .groupBy { it[0][0] }
                // filtering out groups that only have one entry, since those are not duplicates
                .findAll { k, v -> v.size() > 1 }
                // creating error messages for each group of duplicates
                .collect { k, v ->
                    "${messageLocalizer.localize("requisitionTemplate.importDuplicatedLine.error.label", k, v*.second.join(', '))}"
                }

        if (duplicationErrors.size() > 0) {
            errors.addAll(duplicationErrors)
        }

        return errors
    }
}
