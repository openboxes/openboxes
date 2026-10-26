package org.pih.warehouse.inventory

import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.BulkDocumentCommand
import org.pih.warehouse.core.DocumentCommand
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The stock movement document uploads refuse the files uploadDocument refuses, and store nothing when they do.
 */
class StockMovementControllerUploadSpec extends Specification implements ControllerUnitTest<StockMovementController> {

    static final byte[] PNG = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D] as byte[]
    static final byte[] HTML = '<html><body>page</body></html>'.bytes

    StockMovement stockMovement = new StockMovement()

    void setup() {
        controller.stockMovementService = Mock(StockMovementService) {
            getStockMovement(_) >> stockMovement
        }
        params.id = 'movement-1'
        request.method = 'POST'
    }

    private static MockMultipartFile file(String filename, String contentType, byte[] contents) {
        return new MockMultipartFile('fileContents', filename, contentType, contents)
    }

    @Unroll
    void "uploadDocument refuses #filename"() {
        when:
        controller.uploadDocument(new DocumentCommand(fileContents: file(filename, contentType, contents)))

        then:
        0 * controller.stockMovementService.saveDocument(*_)
        response.status == 400
        response.json.errorMessage

        where:
        filename    | contentType     | contents
        'page.html' | 'text/html'     | HTML
        'image.svg' | 'image/svg+xml' | '<svg/>'.bytes
        'photo.png' | 'image/png'     | HTML
    }

    void "uploadDocument refuses an empty or missing file"() {
        when:
        controller.uploadDocument(new DocumentCommand(fileContents: file('notes.txt', 'text/plain', new byte[0])))

        then:
        0 * controller.stockMovementService.saveDocument(*_)
        response.status == 400
    }

    void "uploadDocuments refuses an empty list"() {
        when:
        controller.uploadDocuments(new BulkDocumentCommand(filesContents: []))

        then:
        0 * controller.stockMovementService.saveDocuments(*_)
        response.status == 400
    }

    void "uploadDocument stores an accepted file"() {
        given:
        MockMultipartFile png = file('photo.png', 'image/png', PNG)

        when:
        controller.uploadDocument(new DocumentCommand(fileContents: png))

        then:
        1 * controller.stockMovementService.saveDocument(png, stockMovement)
        response.status == 200
    }

    void "uploadDocuments stores nothing when one of the files is refused"() {
        when:
        controller.uploadDocuments(new BulkDocumentCommand(filesContents: [file('photo.png', 'image/png', PNG),
                file('page.html', 'text/html', HTML)]))

        then:
        0 * controller.stockMovementService.saveDocuments(*_)
        response.status == 400
    }

    void "uploadDocuments stores accepted files"() {
        given:
        List files = [file('photo.png', 'image/png', PNG), file('notes.txt', 'text/plain', 'notes'.bytes)]

        when:
        controller.uploadDocuments(new BulkDocumentCommand(filesContents: files))

        then:
        1 * controller.stockMovementService.saveDocuments(files, stockMovement)
        response.status == 200
    }
}
