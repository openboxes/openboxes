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
import groovy.text.Template
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class DocumentTemplateService {

    boolean transactional = true
    GroovyPagesTemplateEngine groovyPagesTemplateEngine

    def renderGroovyServerPageDocumentTemplate(Document documentTemplate, Map model) {
        StringWriter output = new StringWriter()
        String templateContents = new String(documentTemplate.fileContents)
        Template template = groovyPagesTemplateEngine.createTemplate(templateContents, documentTemplate.name)
        template.make(model).writeTo(output)
        return output.toString()
    }

    def renderOrderDocumentTemplate(Document documentTemplate, Order orderInstance, ConverterTypeTo targetDocumentType, OutputStream outputStream) {

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

    }

    def createOrderContext(IXDocReport report, Order orderInstance) {

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
}
