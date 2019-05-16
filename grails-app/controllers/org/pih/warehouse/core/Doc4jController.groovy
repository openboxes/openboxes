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
import fr.opensagres.xdocreport.converter.ConverterTypeTo
import fr.opensagres.xdocreport.converter.ConverterTypeVia
import fr.opensagres.xdocreport.converter.Options
import fr.opensagres.xdocreport.document.IXDocReport
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry
import fr.opensagres.xdocreport.template.IContext
import fr.opensagres.xdocreport.template.TemplateEngineKind
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata


class Doc4jController {

    def fileService
    def documentService
    def shipmentService


	def downloadSomething = {


		Document document = Document.get("ff8081816ac15134016ac159595a0001")

		InputStream inputStream = new ByteArrayInputStream(document.fileContents)
		IXDocReport report = XDocReportRegistry.getRegistry().
				loadReport(inputStream, TemplateEngineKind.Freemarker);

		//Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);

		def shipment = Shipment.get(params.id);
		// Add properties to the context
		IContext context = report.createContext();

		def sender = [name: "full name", email: "2", address: "1", phone: "test"]
		//ctx.put("shipment", shipment);
		context.put("sender", sender)
		// instruct XDocReport to inspect InvoiceRow entity as well
		// which is given as a list and iterated in a table
//		FieldsMetadata metadata = report.createFieldsMetadata();
//		metadata.load("r", InvoiceRow.class, true);
//		ctx.put("r", invoice.getInvoiceRows());


		//def tempFile = fileService.generateLetterAsDocx(shipment)
		def filename = "${document.name} - " + shipment?.name?.trim() + "." + document.extension
		response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
		//response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		//response.outputStream << tempFile.readBytes()

		report.process(context, response.outputStream)
		// Write the PDF file to output stream
		//report.convert(ctx, options, response.outputStream);
		//out.close();

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
