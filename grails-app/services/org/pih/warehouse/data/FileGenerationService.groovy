package org.pih.warehouse.data

import com.google.zxing.BarcodeFormat
import grails.plugins.rendering.pdf.PdfRenderingService
import org.pih.warehouse.product.BarcodeService

class FileGenerationService {

    PdfRenderingService pdfRenderingService
    BarcodeService barcodeService

    String generateBarcodeFileUri(String shipmentNumber) {
        File tempBarcodeFile = File.createTempFile("barcode-${shipmentNumber}", ".png")

        tempBarcodeFile.withOutputStream { os ->
            barcodeService.renderImage(os, shipmentNumber, 100, 30, BarcodeFormat.CODE_128)
        }

        return "file://" + tempBarcodeFile.absolutePath
    }

    byte[] generatePdfFromTemplate(String templateName, Map model) {
        ByteArrayOutputStream generatedPdfStream = pdfRenderingService.render(
                template: templateName,
                model: model
        )

        return generatedPdfStream.toByteArray()
    }
}
