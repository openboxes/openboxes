package org.pih.warehouse.data

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.pih.warehouse.MessageTagLib
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.UserService
import spock.lang.Specification

/**
 * The data export actions work on data export documents only, and update changes only a data export's name and query.
 */
class DataExportControllerUpdateSpec extends Specification implements ControllerUnitTest<DataExportController>, DataTest {

    Document export
    Document label

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }
        DocumentType exportType = new DocumentType(name: "Export", documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        DocumentType labelType = new DocumentType(name: "Label", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        export = new Document(name: "Products", filename: "products.sql", extension: "sql", contentType: "text/plain",
                fileContents: "select 1".bytes, documentType: exportType).save(validate: false, flush: true)
        label = new Document(name: "Label", filename: "label.zpl", extension: "zpl", contentType: "text/plain",
                fileContents: "^XA^XZ".bytes, documentType: labelType).save(validate: false, flush: true)
        controller.dataService = Mock(DataService)
        actAsSuperuser(true)
        request.method = 'POST'
    }

    private void actAsSuperuser(boolean superuser) {
        controller.userService = Stub(UserService) {
            isSuperuser(_) >> superuser
        }
    }

    void "update changes a data export's name and query"() {
        given:
        params.id = export.id
        params.name = "Products (active)"
        params.fileContents = "select 2"

        when:
        controller.update()

        then:
        response.redirectedUrl == "/dataExport/index"
        Document.get(export.id).with { [name, new String(fileContents)] } == ["Products (active)", "select 2"]
    }

    void "update stores the query as UTF-8"() {
        given:
        String query = "select 'Caf\u00e9 \u00fcber \u4e2d'"
        params.id = export.id
        params.fileContents = query

        when:
        controller.update()

        then:
        Document.get(export.id).fileContents == query.getBytes('UTF-8')
    }

    void "render reads the query as UTF-8"() {
        given:
        String query = "select 'Caf\u00e9 \u00fcber \u4e2d'"
        export.fileContents = query.getBytes('UTF-8')
        export.save(validate: false, flush: true)
        params.id = export.id
        params.format = "csv"

        when:
        controller.render()

        then:
        1 * controller.dataService.executeQuery(query) >> []
        1 * controller.dataService.generateCsv([]) >> ""
    }

    void "update ignores every other field in the request"() {
        given:
        DocumentType otherType = new DocumentType(name: "Other", documentCode: DocumentCode.SHIPPING_DOCUMENT).save(validate: false)
        params.id = export.id
        params.fileContents = "select 2"
        params.contentType = "text/html"
        params.filename = "products.html"
        params.extension = "html"
        params.fileUri = "https://elsewhere.example.com/x"
        params.documentNumber = "barcodeLabel:product"
        params['documentType.id'] = otherType.id
        params.documentType = [id: otherType.id]

        when:
        controller.update()

        then:
        Document.get(export.id).with {
            [new String(fileContents), contentType, filename, extension, fileUri, documentNumber, documentType.documentCode]
        } == ["select 2", "text/plain", "products.sql", "sql", null, null, DocumentCode.DATA_EXPORT]
    }

    void "update refuses a document that is not a data export"() {
        given:
        params.id = label.id
        params.name = "changed"
        params.fileContents = "select 2"

        when:
        controller.update()

        then:
        response.redirectedUrl == "/dataExport/index"
        Document.get(label.id).with { [name, new String(fileContents)] } == ["Label", "^XA^XZ"]
    }

    void "render runs only data export documents"() {
        given:
        params.id = label.id

        when:
        controller.render()

        then:
        0 * controller.dataService._
        response.status == 404
    }

    void "editDialog answers not found for a document that is not a data export"() {
        given:
        params.id = label.id

        when:
        controller.editDialog()

        then:
        response.status == 404
    }

    void "update needs a superuser even without the role rule"() {
        given:
        actAsSuperuser(false)
        params.id = export.id
        params.fileContents = "select 2"

        when:
        controller.update()

        then:
        response.redirectedUrl == "/errors/handleForbidden"
        new String(Document.get(export.id).fileContents) == "select 1"
    }
}
