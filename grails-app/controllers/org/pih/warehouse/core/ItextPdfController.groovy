package org.pih.warehouse.core

import org.pih.warehouse.shipping.Shipment

class ItextPdfController {

    def itextPdfService

    def exportPackingListPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)
        //  Location currentLocation = Location.get(session.warehouse.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }
        def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".pdf"
        itextPdfService.exportPackingListPdf(shipmentInstance, response.outputStream)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")
        // response.outputStream = outputStream;
        return;
    }

    def downloadPackingListPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id);

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        // For some reason, this needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)

        def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".pdf"
        itextPdfService.generatePackingList(response.outputStream, shipmentInstance)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")
        return;
    }

    def certificateOfDonationPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id);

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        def filename = "Certificate of Donation - " + shipmentInstance?.shipmentNumber + ".pdf"
        itextPdfService.generateCertificateOfDonation(response.outputStream, shipmentInstance)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")

        return;
    }
}
