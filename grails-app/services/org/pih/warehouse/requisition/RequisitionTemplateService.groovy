package org.pih.warehouse.requisition

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

@Transactional
class RequisitionTemplateService {

    ApplicationTagLib getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
    }

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
        ApplicationTagLib g = getApplicationTagLib()
        List<String> errors = []

        List<String> duplicationErrors = data.withIndex(1)
                .groupBy { it[0][0] }
                .findAll { k, v -> v.size() > 1 }
                .collect { k, v ->
                    "${g.message(code: "requisitionTemplate.importDuplicatedLine.error.label", default: "Duplicated product in the stocklist (${k}) in lines: ${v*.second.join(', ')}", args: [k, v*.second.join(', ')])}"
                }

        if (duplicationErrors.size() > 0) {
            errors.addAll(duplicationErrors)
        }

        return errors
    }
}
