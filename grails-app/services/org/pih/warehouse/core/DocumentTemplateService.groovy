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
import fr.opensagres.xdocreport.template.ITemplateEngine
import fr.opensagres.xdocreport.template.TemplateEngineKind
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata
import fr.opensagres.xdocreport.template.freemarker.FreemarkerTemplateEngine
import groovy.text.Template
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
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

    def renderDocumentTemplate(Document documentTemplate, Map model, TemplateEngineKind templateEngineKind, ConverterTypeTo targetDocumentType, OutputStream outputStream) {
        InputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)
        IXDocReport report = XDocReportRegistry.getRegistry().
                loadReport(inputStream, templateEngineKind);

        // Add properties to the context
        IContext context = report.createContext(model);

        log.info "Set field metadata"
        FieldsMetadata metadata = report.createFieldsMetadata();
        metadata.addFieldAsList("orderItem.code");
        metadata.addFieldAsList("orderItem.description");
        metadata.addFieldAsList("orderItem.quantity");
        metadata.addFieldAsList("orderItem.unit");
        metadata.addFieldAsList("orderItem.price");
        metadata.addFieldAsList("orderItem.total");

        log.info "Add line items"
        def orderItems = model.order.orderItems.collect { OrderItem orderItem ->
            [
                    code       : orderItem.product?.productCode,
                    description: orderItem.product?.name,
                    quantity   : orderItem.quantity,
                    unit       : orderItem?.product?.unitOfMeasure,
                    price      : orderItem?.product?.pricePerUnit,
                    total   : orderItem.quantity * orderItem?.product?.pricePerUnit
            ]
        }

        log.info "Add line items to context"
        context.put("orderItems", orderItems);

        ConverterTypeVia sourceDocumentType = report.getKind() == "DOCX" ? ConverterTypeVia.DOCX4J :
                report.getKind() == "ODT" ? ConverterTypeVia.ODFDOM : ConverterTypeVia.XWPF
        Options options = Options.getTo(targetDocumentType).via(sourceDocumentType);

        report.convert(context, options, outputStream);
    }


    def renderDocumentTemplate(Document document, Shipment shipment, OutputStream outputStream) {

        InputStream inputStream = new ByteArrayInputStream(document.fileContents)
        IXDocReport report = XDocReportRegistry.getRegistry().
                loadReport(inputStream, TemplateEngineKind.Freemarker);

        // Add properties to the context
        IContext context = report.createContext();

        def destination = [
                name           : shipment?.destination?.name,
                streetAddress  : shipment?.destination?.address?.address,
                city           : shipment?.destination?.address?.city,
                stateOrProvince: shipment?.destination?.address?.stateOrProvince,
                postalCode     : shipment?.destination?.address?.postalCode,
                country        : shipment?.destination?.address?.country
        ]
        def origin = [
                name           : shipment?.origin?.name,
                streetAddress  : shipment?.destination?.address?.address,
                city           : shipment?.destination?.address?.city,
                stateOrProvince: shipment?.destination?.address?.stateOrProvince,
                postalCode     : shipment?.destination?.address?.postalCode,
                country        : shipment?.destination?.address?.country
        ]
        def shipmentData = [
                name                : shipment?.name,
                shipmentNumber      : shipment?.shipmentNumber,
                expectedShippingDate: shipment?.expectedShippingDate,
                totalValue          : shipment?.calculateTotalValue(),
                actualShippingDate  : shipment?.actualShippingDate,
                status              : shipment?.currentStatus?.name
        ]
        context.put("origin", origin)
        context.put("destination", destination)
        context.put("shipment", shipmentData);

        // instruct XDocReport to inspect InvoiceRow entity as well
        // which is given as a list and iterated in a table
        FieldsMetadata metadata = report.createFieldsMetadata();
        //metadata.load("r", ShipmentItem.class, true);
        metadata.addFieldAsList("r.description");
        metadata.addFieldAsList("r.quantity");
        metadata.addFieldAsList("r.unit");
        metadata.addFieldAsList("r.price");
        metadata.addFieldAsList("r.rowtotal");

        def shipmentItems = shipment.shipmentItems.collect { ShipmentItem shipmentItem ->
            [
                    description: shipmentItem?.product?.productCode + " " + shipmentItem?.product?.name,
                    quantity   : shipmentItem.quantity,
                    unit       : shipmentItem?.product?.unitOfMeasure,
                    price      : shipmentItem?.product?.pricePerUnit,
                    rowtotal   : shipmentItem.quantity * shipmentItem?.product?.pricePerUnit
            ]
        }
        context.put("r", shipmentItems);


//        FieldsMetadata metadata = new FieldsMetadata();
//        metadata.addFieldAsList( "developers.Name" );
//        metadata.addFieldAsList( "developers.LastName" );
//        metadata.addFieldAsList( "developers.Mail" );

        //report.process(context, outputStream)

        // Write the PDF file to output stream
        //Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);
        Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.DOCX4J);
        report.convert(context, options, outputStream);

    }
}
