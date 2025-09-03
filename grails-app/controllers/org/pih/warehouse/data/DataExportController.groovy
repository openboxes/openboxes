/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.data

import grails.converters.JSON
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import grails.gorm.transactions.Transactional

import java.nio.charset.Charset

@Transactional
class DataExportController {

    def dataService
    def index() {
        List<Document> documents = Document.findAllByDocumentCode(DocumentCode.DATA_EXPORT)
        [documents: documents]
    }

    def render() {
        Document document = Document.get(params.id)
        String query = new String(document.fileContents, Charset.defaultCharset());
        if (query) {
            def data = dataService.executeQuery(query)
            if (params.format == "csv") {
                String csv = dataService.generateCsv(data)
                response.setHeader("Content-disposition", "attachment; filename=\"${document.name}.csv\"")
                render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
                return
            }
            render dataService.executeQuery(query) as JSON
            return
        }
        render document as JSON
    }

    def editDialog() {
        Document document = Document.get(params.id)
        render(template: "editDialog", model: [documentInstance:document])
    }

    def update() {
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (documentInstance.version > version) {
                    documentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'document.label', default: 'Document')] as Object[], "Another user has updated this Document while you were editing")
                    render(view: "edit", model: [documentInstance: documentInstance])
                    return
                }
            }

            String fileContents = params.remove("fileContents")
            if (fileContents) {
                documentInstance.fileContents = fileContents.bytes
            }

            documentInstance.properties = params

            if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
            }
            else {
                // render errors somehow
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
        }
        redirect(action: "index")
    }


}
