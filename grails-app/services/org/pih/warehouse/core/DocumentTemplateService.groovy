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

import fr.opensagres.xdocreport.converter.ConverterTypeTo
import fr.opensagres.xdocreport.converter.ConverterTypeVia
import fr.opensagres.xdocreport.converter.Options
import fr.opensagres.xdocreport.document.IXDocReport
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry
import fr.opensagres.xdocreport.template.IContext
import fr.opensagres.xdocreport.template.TemplateEngineKind
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata
import grails.gorm.transactions.Transactional
import groovy.text.Template
import org.grails.gsp.GroovyPagesTemplateEngine
import org.jxls.common.Context
import org.jxls.util.JxlsHelper
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.util.PdfUtil

@Transactional
class DocumentTemplateService {

    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    def forecastingService
    def requisitionService

    def renderGroovyServerPageDocumentTemplate(Document documentTemplate, Map model) {
        StringWriter output = new StringWriter()
        String templateContents = new String(documentTemplate.fileContents)
        Template template = groovyPagesTemplateEngine.createTemplate(templateContents, documentTemplate.name)
        template.make(model).writeTo(output)
        return output.toString()
    }

    def renderInvoiceTemplate(Document documentTemplate, Shipment shipmentInstance, ByteArrayOutputStream outputStream) {
        InputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)
        Context context = new Context()
        context.putVar("invoiceItems", shipmentInstance?.shipmentItems)
        context.putVar("datePrinted", Constants.EUROPEAN_DATE_FORMATTER.format(new Date()))
        context.putVar("requisition", shipmentInstance?.requisition)
        context.putVar("approvedRequisitionItems", shipmentInstance?.requisition?.requisitionItems?.findAll {
            it.status == RequisitionItemStatus.APPROVED
        })
        context.putVar("origin", shipmentInstance?.origin)
        context.putVar("destination", shipmentInstance?.destination)
        context.putVar("originRequisitionCount", requisitionService.getRequisitionCountInCurrentFiscalYear(shipmentInstance?.origin))
        context.putVar("destinationRequisitionCount", requisitionService.getRequisitionCountInCurrentFiscalYear(shipmentInstance?.destination))
        JxlsHelper.getInstance().processTemplateAtCell(inputStream, outputStream, context, "Sheet1!A1")
    }

    def renderOrderDocumentTemplate(Document documentTemplate, Order orderInstance, ConverterTypeTo targetDocumentType, OutputStream outputStream) {
        try {
            Boolean isVelocityTemplate = documentTemplate.filename.contains(".vm") || documentTemplate.filename.contains(".vtl")

            TemplateEngineKind templateEngineKind = isVelocityTemplate ?
                TemplateEngineKind.Velocity : TemplateEngineKind.Freemarker

            InputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(inputStream, templateEngineKind);

            // FIXME Need a better way to handle this generically (consider using config + dataService)
            IContext context = orderInstance ? createOrderContext(report, orderInstance) : report.createContext();

            ConverterTypeVia sourceDocumentType = (report.kind == "DOCX") ? ConverterTypeVia.DOCX4J :
                (report.kind == "ODT") ? ConverterTypeVia.ODFDOM : ConverterTypeVia.XWPF

            // Convert to the target type
            if (targetDocumentType && sourceDocumentType) {
                Options options = Options.getTo(targetDocumentType).via(sourceDocumentType);
                report.convert(context, options, outputStream);
            }
            // Process document in place
            else {
                report.process(context, outputStream)
            }
            log.info "Report dump: " + report.dump()
        } finally {
            PdfUtil.restoreBaseFonts()
        }
    }

    private createOrderContext(IXDocReport report, Order orderInstance) {

        // Add data to the context
        def orderItems = orderInstance?.orderItems?.findAll {
            it.orderItemStatusCode != OrderItemStatusCode.CANCELED
        }?.collect { OrderItem orderItem ->
            return [
                    code                : orderItem?.product?.productCode,
                    type                : "Item",
                    status              : orderItem?.orderItemStatusCode?:"",
                    description         : orderItem?.description ?: orderItem?.product?.name,
                    quantity            : orderItem?.quantity ?: 0,
                    supplierCode        : orderItem?.productSupplier?.supplierCode ?: "",
                    manufacturer        : orderItem?.productSupplier?.manufacturer?.name ?: "",
                    manufacturerCode    : orderItem?.productSupplier?.manufacturerCode ?: "",
                    unitOfMeasure       : orderItem?.unitOfMeasure ?: "",
                    unitPrice           : orderItem?.unitPrice ?: "",
                    totalPrice          : orderItem?.totalPrice() ?: "",
                    expectedShippingDate: orderItem?.estimatedReadyDate ? orderItem?.estimatedReadyDate?.format("MMM dd, yyyy") : ""
            ]
        }
        def orderAdjustments = orderInstance?.orderAdjustments?.findAll { !it.canceled }?.collect { OrderAdjustment orderAdjustment ->
            return [
                    code                : orderAdjustment?.orderAdjustmentType?.code?:"",
                    type                : "Adjustment",
                    status              : "",
                    description         : orderAdjustment?.description?:orderAdjustment?.orderAdjustmentType?.description?:"",
                    quantity            : 1,
                    supplierCode        : "",
                    manufacturer        : "",
                    manufacturerCode    : "",
                    unitOfMeasure       : "",
                    unitPrice           : "",
                    totalPrice          : orderAdjustment?.totalAdjustments ?: "",
                    expectedShippingDate: ""
            ]
        }

        def order = orderInstance.toJson()
        order.dateOrdered = orderInstance.dateOrdered
        order.dateApproved = orderInstance.dateApproved
        order.dateCompleted = orderInstance.dateCompleted
        order.currencyCode = orderInstance.currencyCode?:""
        order.exchangeRate = orderInstance.exchangeRate?:""
        order.total = orderInstance.total?:""
        order.subtotal = orderInstance.subtotal?:""
        order.totalAdjustments = orderInstance.totalAdjustments?:""
        order.description = orderInstance.description?:""
        order.orderedBy = orderInstance.orderedBy?:""
        order.paymentTerm = orderInstance.paymentTerm?:""

        def nullAddress = [address:"", address2: "", city: "", stateOrProvince: "", description: ""]
        Organization vendor = orderInstance.originParty
        order.vendor = vendor?.properties?:[:]
        order.vendor.address = vendor?.defaultLocation?.address?:nullAddress

        Organization buyer = orderInstance.destinationParty
        order.buyer = buyer?.properties?:[:]
        order.buyer.address = buyer?.defaultLocation?.address?:nullAddress

        Location deliverto = orderInstance.destination
        order.deliverto = deliverto?.properties?:[:]
        order.deliverto.address = deliverto?.address?:nullAddress

        order['deliverto.address'] = deliverto?.address?.address ?: ""
        order['deliverto.address2'] = deliverto?.address?.address2 ?: ""
        order['deliverto.city'] = deliverto?.address?.city ?: ""
        order['deliverto.stateOrProvince'] = deliverto?.address?.stateOrProvince ?: ""
        order['deliverto.postalCode'] = deliverto?.address?.postalCode ?: ""
        order['deliverto.country'] = deliverto?.address?.country ?: ""

        order['origin.address'] = order?.origin?.address?.address ?: "No Address on File"
        order['origin.address2'] = order?.origin?.address?.address2 ?: ""
        order['origin.city'] = order?.origin?.address?.city ?: ""
        order['origin.stateOrProvince'] = order?.origin?.address?.stateOrProvince ?: ""
        order['origin.postalCode'] = order?.origin?.address?.postalCode ?: ""
        order['origin.country'] = order?.origin?.address?.country ?: ""

        // Add order item fields to metadata
        FieldsMetadata metadata = report.createFieldsMetadata();
        metadata.addFieldAsList("orderItems.code")
        metadata.addFieldAsList("orderItems.description")
        metadata.addFieldAsList("orderItems.quantity")
        metadata.addFieldAsList("orderItems.supplierCode")
        metadata.addFieldAsList("orderItems.manufacturer")
        metadata.addFieldAsList("orderItems.manufacturerCode")
        metadata.addFieldAsList("orderItems.unitOfMeasure")
        metadata.addFieldAsList("orderItems.unitPrice")
        metadata.addFieldAsList("orderItems.totalPrice")
        metadata.addFieldAsList("orderItems.expectedShippingDate")

        // Add order adjustment fields to metadata
        metadata.addFieldAsList("orderAdjustments.code")
        metadata.addFieldAsList("orderAdjustments.description")
        metadata.addFieldAsList("orderAdjustments.totalPrice")

        IContext context = report.createContext();
        context.put("order", order)
        context.put("orderItems", orderItems);
        context.put("orderAdjustments", orderAdjustments);
        return context
    }

    // TODO: make it generic for requistions and orders
    def renderRequisitionDocumentTemplate(Document documentTemplate, Requisition requisitionInstance, ConverterTypeTo targetDocumentType, OutputStream outputStream) {
        try {
            InputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(inputStream, TemplateEngineKind.Freemarker)

            IContext context = createRequisitionContext(report, requisitionInstance)

            ConverterTypeVia sourceDocumentType = (report.kind == "DOCX") ? ConverterTypeVia.DOCX4J :
                (report.kind == "ODT") ? ConverterTypeVia.ODFDOM : ConverterTypeVia.XWPF

            // Convert to the target type
            if (targetDocumentType && sourceDocumentType) {
                Options options = Options.getTo(targetDocumentType).via(sourceDocumentType)
                report.convert(context, options, outputStream)
            }
            // Process document in place
            else {
                report.process(context, outputStream)
            }
            log.info "Report dump: " + report.dump()
        } finally {
            PdfUtil.restoreBaseFonts()
        }
    }

    private createRequisitionContext(IXDocReport report, Requisition requisitionInstance) {
        def requisitionItems = requisitionInstance?.requisitionItems?.collect { RequisitionItem requisitionItem ->
            def demand
            def quantityOnHand
            if (requisitionInstance?.destination?.isDownstreamConsumer()) {
                // if request FROM downstream consumer (location without managed inventory but supporting submitting requests),
                // then pull demand from origin to that location
                demand = forecastingService.getDemand(requisitionInstance.origin, requisitionInstance.destination, requisitionItem.product)
                quantityOnHand = requisitionItem?.quantityCounted ?: 0
            } else {
                // if request is NOT FROM downstream consumer, then pull demand outgoing FROM destination to all other locations
                demand = forecastingService.getDemand(requisitionInstance.destination, null, requisitionItem.product)
                quantityOnHand = demand?.quantityOnHand ?: 0
            }
            return [
                code                    : requisitionItem?.product?.productCode,
                name                    : requisitionItem?.product?.name,
                status                  : requisitionItem?.status,
                unitOfMeasure           : requisitionItem?.product?.unitOfMeasure ?: "EA",
                requestorMonthlyDemand  : demand?.monthlyDemand ?: 0,
                requestorQuantityOnHand : quantityOnHand,
                quantityRequested       : requisitionItem?.quantity,
                quantityIssued          : requisitionItem.isQuantityIssued() ? requisitionItem?.quantityIssued : ""
            ]
        }

        def requisition = requisitionInstance.toJson()
        requisition.remove('requisitionTemplate')
        requisition.remove('requisitionItems')
        requisition.requestNumber = requisitionInstance.requestNumber

        FieldsMetadata metadata = report.createFieldsMetadata()
        metadata.addFieldAsList("requisitionItems.code")
        metadata.addFieldAsList("requisitionItems.name")
        metadata.addFieldAsList("requisitionItems.status")
        metadata.addFieldAsList("requisitionItems.unitOfMeasure")
        metadata.addFieldAsList("requisitionItems.requestorMonthlyDemand")
        metadata.addFieldAsList("requisitionItems.requestorQuantityOnHand")
        metadata.addFieldAsList("requisitionItems.quantityRequested")
        metadata.addFieldAsList("requisitionItems.quantityIssued")

        IContext context = report.createContext()
        context.put("requisition", requisition)
        context.put("requisitionItems", requisitionItems)
        return context
    }
}
