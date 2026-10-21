package org.pih.warehouse.api.regression.platform.pages.document

import io.restassured.http.Cookie
import io.restassured.response.Response

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.product.Product

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The document pages through the running server: the file checks on the paths that store an uploaded file, and the
 * data export update, which only a superuser may use and which changes only a data export's name and query.
 * Runs as the admin user (a superuser) unless a spec logs in as someone else.
 */
class DocumentPathPagesSpec extends RegressionApiSpec {

    static final String MANAGER = 'test-document-paths-manager'
    static final byte[] HTML = '<html><body>page</body></html>'.bytes

    ServerPages pages
    List<String> documentIds = []

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    @Override
    void cleanupData() {
        Document.withNewTransaction {
            Product mainProduct = Product.get(product.id)
            documentIds.each { String id ->
                Document document = Document.get(id)
                if (document) {
                    mainProduct?.removeFromDocuments(document)
                    document.delete(flush: true)
                }
            }
        }
    }

    String saveDocument(DocumentCode documentCode, String filename, String contentType, byte[] contents) {
        String id = Document.withNewTransaction {
            DocumentType documentType = DocumentType.findByDocumentCode(documentCode) ?:
                    new DocumentType(name: documentCode.name(), documentCode: documentCode).save(failOnError: true)
            new Document(name: filename, filename: filename, extension: filename.tokenize('.').last(),
                    contentType: contentType, fileContents: contents, documentType: documentType).save(failOnError: true, flush: true).id
        }
        documentIds << id
        return id
    }

    Map stored(String id) {
        return Document.withNewSession {
            Document document = Document.get(id)
            [name: document.name, filename: document.filename, contentType: document.contentType,
             contents: new String(document.fileContents), documentCode: document.documentType?.documentCode]
        }
    }

    ServerPages pagesAs(String username) {
        Cookie session = authApiWrapper.loginOK(username, RegressionRoleFixtures.PASSWORD, facility.id).getDetailedCookie('JSESSIONID')
        return new ServerPages(serverPort, session)
    }

    void 'a data export is changed only by a superuser'() {
        given:
        String id = saveDocument(DocumentCode.DATA_EXPORT, 'export.sql', 'text/plain', 'select 1'.bytes)
        RegressionRoleFixtures.ensureUser(MANAGER, [RoleType.ROLE_MANAGER], [:])
        ServerPages manager = pagesAs(MANAGER)

        when:
        Response refused = manager.post('/dataExport/update', [id: id, name: 'by a manager', fileContents: 'select 2'])

        then:
        redirectPath(refused) == '/errors/handleForbidden'
        redirectPath(manager.get("/dataExport/editDialog/${id}")) == '/errors/handleForbidden'
        stored(id).subMap(['name', 'contents']) == [name: 'export.sql', contents: 'select 1']

        when:
        Response updated = pages.post('/dataExport/update', [id: id, name: 'Export', fileContents: 'select 2',
                contentType: 'text/html', filename: 'export.html'])

        then:
        redirectPath(updated) == '/dataExport/index'
        stored(id) == [name: 'Export', filename: 'export.sql', contentType: 'text/plain', contents: 'select 2',
                       documentCode: DocumentCode.DATA_EXPORT]
    }

    void 'the data export update leaves other documents alone'() {
        given:
        String id = saveDocument(DocumentCode.PRODUCT_MANUAL, 'manual.txt', 'text/plain', 'manual'.bytes)

        when:
        Response response = pages.post('/dataExport/update', [id: id, name: 'changed', fileContents: 'select 2'])

        then:
        redirectPath(response) == '/dataExport/index'
        stored(id).subMap(['name', 'contents']) == [name: 'manual.txt', contents: 'manual']
    }

    void 'replacing a document file refuses an html file'() {
        given:
        String id = saveDocument(DocumentCode.PRODUCT_MANUAL, 'manual.txt', 'text/plain', 'manual'.bytes)

        when:
        Response response = pages.request().multiPart('id', id).multiPart('fileContents', 'page.html', HTML, 'text/html')
                .post('/document/upload')

        then:
        redirectPath(response) == "/document/edit/${id}"
        stored(id).subMap(['filename', 'contentType', 'contents']) ==
                [filename: 'manual.txt', contentType: 'text/plain', contents: 'manual']
    }

    void 'the document form update does not take a file'() {
        given:
        String id = saveDocument(DocumentCode.PRODUCT_MANUAL, 'manual.txt', 'text/plain', 'manual'.bytes)

        when:
        Response response = pages.request().multiPart('id', id).multiPart('name', 'Manual')
                .multiPart('extension', 'txt').multiPart('contentType', 'text/plain')
                .multiPart('fileContents', 'page.html', HTML, 'text/html')
                .post('/document/update')

        then:
        redirectPath(response).startsWith('/document/list')
        stored(id).subMap(['name', 'contents']) == [name: 'Manual', contents: 'manual']
    }

    void 'editing a document on a record page refuses an html file'() {
        given:
        String id = saveDocument(DocumentCode.PRODUCT_MANUAL, 'manual.txt', 'text/plain', 'manual'.bytes)

        when:
        Response response = pages.request().multiPart('documentId', id).multiPart('name', 'renamed')
                .multiPart('fileContents', 'page.html', HTML, 'text/html')
                .post('/document/saveDocument')

        then:
        redirectPath(response) == "/document/edit/${id}"
        stored(id).subMap(['name', 'filename', 'contents']) == [name: 'manual.txt', filename: 'manual.txt', contents: 'manual']
    }

    void 'a document can still be added as a link only'() {
        given:
        String link = "https://example.org/${randomUtil.randomStringFieldValue('sheet')}.pdf"

        when:
        Response response = pages.request().multiPart('productId', product.id).multiPart('name', 'Supplier sheet')
                .multiPart('fileUri', link)
                .multiPart('fileContents', 'no-file.pdf', new byte[0], 'application/octet-stream')
                .post('/document/uploadDocument')
        String id = Document.withNewSession { Document.findByFileUri(link)?.id }
        if (id) {
            documentIds << id
        }

        then:
        redirectPath(response) == "/product/edit/${product.id}"
        id
    }
}
