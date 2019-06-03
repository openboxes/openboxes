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
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class DocumentTemplateService {

    boolean transactional = true

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
