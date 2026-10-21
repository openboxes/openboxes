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

import java.nio.charset.StandardCharsets

@Transactional
class DataExportController {

    def dataService
    def userService
    def index() {
        List<Document> documents = Document.findAllByDocumentCode(DocumentCode.DATA_EXPORT)
        [documents: documents]
    }

    def render() {
        Document document = findDataExport(params.id)
        if (!document) {
            response.status = 404
            render(text: "Data export not found", contentType: "text/plain", encoding: "UTF-8")
            return
        }
        String query = document.fileContents ? new String(document.fileContents, StandardCharsets.UTF_8) : null
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
        Document document = findDataExport(params.id)
        if (!document) {
            response.status = 404
            render(text: "Data export not found", contentType: "text/plain", encoding: "UTF-8")
            return
        }
        render(template: "editDialog", model: [documentInstance:document])
    }

    /**
     * Updates a data export's name and query. Only DATA_EXPORT documents can be changed here, and only those
     * two fields; anything else in the request is ignored.
     */
    def update() {
        // Also required by a role rule (runtime.groovy); checked here too, as a deployment may replace those rules
        if (!userService.isSuperuser(session.user)) {
            redirect(controller: "errors", action: "handleForbidden")
            return
        }
        Document documentInstance = findDataExport(params.id)
        if (documentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (documentInstance.version > version) {
                    flash.message = "Another user has updated this Document while you were editing"
                    redirect(action: "index")
                    return
                }
            }

            String fileContents = params.fileContents instanceof String ? params.fileContents : null
            if (fileContents) {
                documentInstance.fileContents = fileContents.getBytes(StandardCharsets.UTF_8)
            }
            if (params.name instanceof String && params.name) {
                documentInstance.name = params.name
            }

            if (documentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
            } else {
                flash.message = "${warehouse.message(code: 'document.cannotSave.message', args: [documentInstance.errors])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
        }
        redirect(action: "index")
    }

    private static Document findDataExport(Object id) {
        Document document = id ? Document.get(id) : null
        return document?.documentType?.documentCode == DocumentCode.DATA_EXPORT ? document : null
    }
}
