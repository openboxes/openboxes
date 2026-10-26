package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.gsp.GroovyPagesTemplateEngine
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.pih.warehouse.MessageTagLib
import org.pih.warehouse.core.localization.MessageLocalizer
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The document paths that store an uploaded file (uploadDocument, saveDocument, upload) refuse the same files.
 */
class DocumentControllerUploadChecksSpec extends Specification implements ControllerUnitTest<DocumentController>, DataTest {

    static final byte[] PNG = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D] as byte[]
    static final byte[] HTML = '<html><body>page</body></html>'.bytes
    static final byte[] SVG = '<svg xmlns="http://www.w3.org/2000/svg"></svg>'.bytes

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
        controller.messageLocalizer = Stub(MessageLocalizer) {
            localize(*_) >> { args -> args[0] as String }
        }
        controller.groovyPagesTemplateEngine = Stub(GroovyPagesTemplateEngine)
        actAsSuperuser(false)
        request.method = 'POST'
    }

    private void actAsSuperuser(boolean superuser) {
        controller.userService = Stub(UserService) {
            isSuperuser(_) >> superuser
        }
    }

    private static Document document(DocumentCode documentCode, String filename, String contentType, byte[] contents) {
        DocumentType documentType = documentCode ?
                new DocumentType(name: documentCode.name(), documentCode: documentCode).save(validate: false) : null
        return new Document(name: filename, filename: filename, extension: filename.tokenize('.').last(),
                contentType: contentType, fileContents: contents, documentType: documentType).save(validate: false, flush: true)
    }

    private void attach(String filename, String contentType, byte[] contents) {
        request.addFile(new MockMultipartFile('fileContents', filename, contentType, contents))
    }

    private static DocumentCommand command(Map properties, String filename = null, String contentType = null, byte[] contents = null) {
        DocumentCommand command = new DocumentCommand(properties)
        if (filename) {
            command.fileContents = new MockMultipartFile('fileContents', filename, contentType, contents)
        }
        return command
    }

    @Unroll
    void "saveDocument refuses #description and leaves the document unchanged"() {
        given:
        Document stored = document(DocumentCode.SHIPPING_DOCUMENT, 'packing.pdf', 'application/pdf', 'pdf'.bytes)
        params.documentId = stored.id

        when:
        controller.saveDocument(command([name: 'renamed', typeId: stored.documentType.id], uploadName, uploadType, uploadBytes))

        then:
        flash.message == 'document.uploadNotAllowed.message'
        response.redirectedUrl == "/document/edit/${stored.id}"
        Document.get(stored.id).with { [name, filename, contentType, new String(fileContents)] } ==
                ['packing.pdf', 'packing.pdf', 'application/pdf', 'pdf']

        where:
        description               | uploadName   | uploadType      | uploadBytes
        'an html file'            | 'page.html'  | 'text/html'     | HTML
        'an svg image'            | 'image.svg'  | 'image/svg+xml' | SVG
        'a page named as a png'   | 'photo.png'  | 'image/png'     | HTML
        'a png sent as text'      | 'photo.txt'  | 'text/plain'    | PNG
    }

    void "saveDocument stores an accepted file"() {
        given:
        Document stored = document(DocumentCode.SHIPPING_DOCUMENT, 'packing.pdf', 'application/pdf', 'pdf'.bytes)
        params.documentId = stored.id

        when:
        controller.saveDocument(command([typeId: stored.documentType.id], 'photo.png', 'image/png', PNG))

        then:
        flash.message != 'document.uploadNotAllowed.message'
        Document.get(stored.id).with { [filename, contentType, fileContents] } == ['photo.png', 'image/png', PNG]
    }

    void "saveDocument keeps refusing template types, as uploadDocument does"() {
        given:
        Document stored = document(DocumentCode.SHIPPING_DOCUMENT, 'packing.pdf', 'application/pdf', 'pdf'.bytes)
        DocumentType templateType = new DocumentType(name: 'Label', documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        params.documentId = stored.id

        when:
        controller.saveDocument(command([typeId: templateType.id]))

        then:
        flash.message == 'document.templateTypeNotAllowed.message'
        Document.get(stored.id).documentType.documentCode == DocumentCode.SHIPPING_DOCUMENT
    }

    @Unroll
    void "upload refuses #description as the new file of a document"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.pdf', 'application/pdf', 'pdf'.bytes)
        params.id = stored.id
        attach(uploadName, uploadType, uploadBytes)

        when:
        controller.upload()

        then:
        flash.message == 'document.uploadNotAllowed.message'
        response.redirectedUrl == "/document/edit/${stored.id}"
        Document.get(stored.id).with { [filename, contentType, new String(fileContents)] } == ['manual.pdf', 'application/pdf', 'pdf']

        where:
        description               | uploadName   | uploadType      | uploadBytes
        'an html file'            | 'page.html'  | 'text/html'     | HTML
        'an svg image'            | 'image.svg'  | 'image/svg+xml' | SVG
        'a page named as a png'   | 'photo.png'  | 'image/png'     | HTML
    }

    void "upload replaces a document's file with an accepted one"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.pdf', 'application/pdf', 'pdf'.bytes)
        params.id = stored.id
        attach('photo.png', 'image/png', PNG)

        when:
        controller.upload()

        then:
        response.redirectedUrl == "/document/edit/${stored.id}"
        Document.get(stored.id).with { [filename, extension, contentType, fileContents] } == ['photo.png', 'png', 'image/png', PNG]
    }

    void "upload replaces a label template with a file of the same type, stored as plain bytes"() {
        given:
        Document stored = document(DocumentCode.ZEBRA_TEMPLATE, 'label.zpl', 'text/plain', '^XA^XZ'.bytes)
        params.id = stored.id
        attach('label-v2.zpl', 'application/octet-stream', '^XA^FDnew^FS^XZ'.bytes)

        when:
        controller.upload()

        then:
        response.redirectedUrl == "/document/edit/${stored.id}"
        Document.get(stored.id).with { [filename, extension, contentType, new String(fileContents)] } ==
                ['label-v2.zpl', 'zpl', 'application/octet-stream', '^XA^FDnew^FS^XZ']
    }

    @Unroll
    void "upload refuses a #uploadName file as the new file of a #documentCode document"() {
        given:
        Document stored = document(documentCode, storedName, 'text/plain', 'stored'.bytes)
        params.id = stored.id
        attach(uploadName, 'text/html', HTML)

        actAsSuperuser(true)

        when:
        controller.upload()

        then:
        flash.message == 'document.replacementTypeNotAllowed.message'
        new String(Document.get(stored.id).fileContents) == 'stored'

        where:
        documentCode                | storedName   | uploadName
        DocumentCode.ZEBRA_TEMPLATE | 'label.zpl'  | 'label.html'
        DocumentCode.DATA_EXPORT    | 'export.sql' | 'export.html'
    }

    void "upload replaces a template stored without an extension by a file with its filename's extension"() {
        given:
        Document stored = document(DocumentCode.ZEBRA_TEMPLATE, 'label.zpl', 'text/plain', '^XA^XZ'.bytes)
        stored.extension = null
        stored.save(validate: false, flush: true)
        params.id = stored.id
        attach('label.zpl', 'application/octet-stream', '^XA^FDnew^FS^XZ'.bytes)

        when:
        controller.upload()

        then:
        new String(Document.get(stored.id).fileContents) == '^XA^FDnew^FS^XZ'
        Document.get(stored.id).contentType == 'application/octet-stream'
    }

    @Unroll
    void "replacing the query of a data export as #role"() {
        given:
        Document stored = document(DocumentCode.DATA_EXPORT, 'export.sql', 'text/plain', 'select 1'.bytes)
        params.id = stored.id
        attach('export.sql', 'application/sql', 'select 2'.bytes)
        actAsSuperuser(superuser)

        when:
        controller.upload()

        then:
        new String(Document.get(stored.id).fileContents) == query

        where:
        role               | superuser | query
        'a manager'        | false     | 'select 1'
        'a superuser'      | true      | 'select 2'
    }

    @Unroll
    void "save refuses #description for a new document"() {
        given:
        DocumentType manual = new DocumentType(name: 'Manual', documentCode: DocumentCode.PRODUCT_MANUAL).save(validate: false)
        params['documentType.id'] = manual.id
        attach(uploadName, uploadType, uploadBytes)

        when:
        controller.save()

        then:
        flash.message == 'document.uploadNotAllowed.message'
        Document.count() == 0

        where:
        description             | uploadName  | uploadType      | uploadBytes
        'an html file'          | 'page.html' | 'text/html'     | HTML
        'an svg image'          | 'image.svg' | 'image/svg+xml' | SVG
        'a page named as a png' | 'photo.png' | 'image/png'     | HTML
    }

    void "save creates a label template from its own file type, stored as plain bytes"() {
        given:
        DocumentType label = new DocumentType(name: 'Label', documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        params['documentType.id'] = label.id
        attach('label.zpl', 'text/html', '^XA^XZ'.bytes)

        when:
        controller.save()

        then:
        Document.count() == 1
        Document.list()[0].with { [filename, extension, contentType] } == ['label.zpl', 'zpl', 'application/octet-stream']
    }

    void "only a superuser creates a data export"() {
        given:
        DocumentType export = new DocumentType(name: 'Export', documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        params['documentType.id'] = export.id
        attach('export.sql', 'application/sql', 'select 1'.bytes)

        when:
        controller.save()

        then:
        flash.message == 'document.dataExportNotAllowed.message'
        Document.count() == 0
    }

    void "update never takes a file"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.pdf', 'application/pdf', 'pdf'.bytes)
        params.id = stored.id
        params.name = 'Manual'
        params.extension = 'pdf'
        params.contentType = 'application/pdf'
        attach('page.html', 'text/html', HTML)

        when:
        controller.update()

        then:
        Document.get(stored.id).with { [name, contentType, new String(fileContents)] } == ['Manual', 'application/pdf', 'pdf']
    }

    @Unroll
    void "update refuses the file type #newExtension #newContentType"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.pdf', 'application/pdf', 'pdf'.bytes)
        params.id = stored.id
        params.extension = newExtension
        params.contentType = newContentType

        when:
        controller.update()

        then:
        flash.message == 'document.uploadNotAllowed.message'
        Document.get(stored.id).with { [extension, contentType] } == ['pdf', 'application/pdf']

        where:
        newExtension | newContentType
        'pdf'     | 'text/html'
        'html'    | 'text/html'
        'svg'     | 'image/svg+xml'
    }

    void "update keeps a document from becoming a data export unless a superuser asks"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'notes.txt', 'text/plain', 'select 1'.bytes)
        DocumentType export = new DocumentType(name: 'Export', documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        params.id = stored.id
        params['documentType.id'] = export.id

        when:
        controller.update()

        then:
        flash.message == 'document.dataExportNotAllowed.message'
        Document.get(stored.id).documentType.documentCode == DocumentCode.PRODUCT_MANUAL
    }

    void "saveDocument does not make a record's document a data export"() {
        given:
        Document stored = document(DocumentCode.SHIPPING_DOCUMENT, 'packing.pdf', 'application/pdf', 'pdf'.bytes)
        DocumentType export = new DocumentType(name: 'Export', documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        params.documentId = stored.id

        when:
        controller.saveDocument(command([typeId: export.id, requestId: 'request-1']))

        then:
        flash.message == 'document.templateTypeNotAllowed.message'
        response.redirectedUrl == '/requisition/show/request-1'
        Document.get(stored.id).documentType.documentCode == DocumentCode.SHIPPING_DOCUMENT
    }

    void "saveDocument does not change a data export through a record's document form"() {
        given:
        Document stored = document(DocumentCode.DATA_EXPORT, 'export.txt', 'text/plain', 'select 1'.bytes)
        DocumentType manual = new DocumentType(name: 'Manual', documentCode: DocumentCode.PRODUCT_MANUAL).save(validate: false)
        params.documentId = stored.id
        actAsSuperuser(true)

        when:
        controller.saveDocument(command([typeId: manual.id], 'export.txt', 'text/plain', 'select 2'.bytes))

        then:
        flash.message == 'document.templateTypeNotAllowed.message'
        Document.get(stored.id).with { [documentType.documentCode, new String(fileContents)] } == [DocumentCode.DATA_EXPORT, 'select 1']
    }

    void "saveDocument returns to the requisition after editing a request's document"() {
        given:
        Document stored = document(DocumentCode.SHIPPING_DOCUMENT, 'packing.pdf', 'application/pdf', 'pdf'.bytes)
        params.documentId = stored.id

        when:
        controller.saveDocument(command([typeId: stored.documentType.id, requestId: 'request-1', name: 'Packing']))

        then:
        response.redirectedUrl == '/requisition/show/request-1'
        Document.get(stored.id).name == 'Packing'
    }

    void "update ignores file contents sent as text"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.txt', 'text/plain', 'manual'.bytes)
        params.id = stored.id
        params.fileContents = '<html><body>page</body></html>'

        when:
        controller.update()

        then:
        new String(Document.get(stored.id).fileContents) == 'manual'
    }

    void "save does not take a file sent as text, or no file at all"() {
        given:
        DocumentType manual = new DocumentType(name: 'Manual', documentCode: DocumentCode.PRODUCT_MANUAL).save(validate: false)
        params['documentType.id'] = manual.id
        params.fileContents = '<html><body>page</body></html>'
        params.contentType = 'text/html'
        params.filename = 'page.html'

        when:
        controller.save()

        then:
        Document.count() == 0
    }

    void "upload replaces a template with no known file type by any file, stored as plain bytes"() {
        given:
        Document stored = document(DocumentCode.ZEBRA_TEMPLATE, 'label', 'text/plain', '^XA^XZ'.bytes)
        stored.extension = null
        stored.save(validate: false, flush: true)
        params.id = stored.id
        attach('label.zpl', 'text/html', '^XA^FDnew^FS^XZ'.bytes)

        when:
        controller.upload()

        then:
        Document.get(stored.id).with { [extension, contentType, new String(fileContents)] } ==
                ['zpl', 'application/octet-stream', '^XA^FDnew^FS^XZ']
    }

    void "update chooses a document type by its id and never changes the type itself"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'manual.pdf', 'application/pdf', 'pdf'.bytes)
        DocumentType manual = stored.documentType
        params.id = stored.id
        params['documentType.id'] = manual.id
        params['documentType.documentCode'] = 'DATA_EXPORT'
        params['documentType.name'] = 'Renamed'

        when:
        controller.update()

        then:
        DocumentType.get(manual.id).with { [documentCode, name] } == [DocumentCode.PRODUCT_MANUAL, 'PRODUCT_MANUAL']
        Document.get(stored.id).documentType.id == manual.id
    }

    void "save chooses a document type by its id and never changes the type itself"() {
        given:
        DocumentType manual = new DocumentType(name: 'Manual', documentCode: DocumentCode.PRODUCT_MANUAL).save(validate: false)
        params['documentType.id'] = manual.id
        params['documentType.documentCode'] = 'DATA_EXPORT'
        attach('notes.txt', 'text/plain', 'select 1'.bytes)

        when:
        controller.save()

        then:
        DocumentType.get(manual.id).documentCode == DocumentCode.PRODUCT_MANUAL
        Document.list()*.documentType*.id == [manual.id]
    }

    void "update refuses an unaccepted file type also for a document that is only a link"() {
        given:
        Document stored = document(DocumentCode.PRODUCT_MANUAL, 'sheet.pdf', 'application/pdf', new byte[0])
        params.id = stored.id
        params.extension = 'html'
        params.contentType = 'text/html'

        when:
        controller.update()

        then:
        flash.message == 'document.uploadNotAllowed.message'
        Document.get(stored.id).with { [extension, contentType] } == ['pdf', 'application/pdf']
    }

    void "upload checks the content of a replacement for a template of an accepted type"() {
        given:
        Document stored = document(DocumentCode.INVOICE_TEMPLATE, 'invoice.png', 'image/png', PNG)
        params.id = stored.id
        attach('invoice.png', 'image/png', HTML)

        when:
        controller.upload()

        then:
        flash.message == 'document.replacementTypeNotAllowed.message'
        Document.get(stored.id).fileContents == PNG
    }
}
