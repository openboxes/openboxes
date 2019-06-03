/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.pih.warehouse.shipping.Shipment

class Doc4jController {

    def fileService
    def documentService
    def shipmentService
	def documentTemplateService


	def downloadSomething = {
		Shipment shipment = Shipment.get(params.id);
		Document document = Document.get("ff8081816b1157be016b116663b20001")
		def filename = "${document.name} - " + shipment?.name?.trim() + "." + document.extension
		response.setHeader("Content-disposition", "attachment; filename=\"${filename}\".pdf");
		//response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		log.info "document.contentType " + document.contentType
		//response.setContentType(document.contentType)
		response.setContentType("application/pdf")

		documentTemplateService.renderDocumentTemplate(document, shipment, response.outputStream)
		//response.outputStream << tempFile.readBytes()
	}

	def downloadShipmentManifestDocx = {
		Shipment shipment = Shipment.get(params.id);
		Document document = Document.get("ff8081816b1157be016b1aefde020002")
		def filename = "${document.name} - " + shipment?.name?.trim() + "." + document.extension
		response.setHeader("Content-disposition", "attachment; filename=\"${filename}\".pdf");
		//response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		log.info "document.contentType " + document.contentType
		//response.setContentType(document.contentType)
		response.setContentType("application/pdf")

		documentTemplateService.renderDocumentTemplate(document, shipment, response.outputStream)
		//response.outputStream << tempFile.readBytes()
	}


    def downloadLetter = {
        def shipmentInstance = Shipment.get(params.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        def tempFile = fileService.generateLetterAsDocx(shipmentInstance)
        def filename = "Certificate of Donation - " + shipmentInstance?.name?.trim() + ".docx"
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        response.outputStream << tempFile.readBytes()
    }

    def downloadLetterAsPdf = {

        def shipmentInstance = Shipment.get(params.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        def filename = "Certificate of Donation - " + shipmentInstance?.name?.trim() + ".pdf"
        fileService.generateLetterAsPdf(shipmentInstance, response.outputStream)

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.setContentType("application/pdf")
        return
    }


    /**
     *
     */
    def downloadPackingList = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        // For some reason, this needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)
        render ""

        def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".xls"
        log.info("filename " + filename)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        documentService.generatePackingList(response.outputStream, shipmentInstance)
        return

    }

    def downloadCertificateOfDonation = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        // Tis needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)
        render ""

        def filename = "Certificate of Donation - " + shipmentInstance?.shipmentNumber + ".xls"
        log.info("filename " + filename)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        documentService.generateCertificateOfDonation(response.outputStream, shipmentInstance)
        return

    }

}
