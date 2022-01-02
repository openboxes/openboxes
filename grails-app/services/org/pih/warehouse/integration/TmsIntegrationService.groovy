/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import net.schmizz.sshj.sftp.SFTPException
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.EventTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.integration.xml.acceptancestatus.AcceptanceStatus
import org.pih.warehouse.integration.xml.execution.Execution
import org.pih.warehouse.integration.xml.execution.ExecutionStatus
import org.pih.warehouse.integration.xml.order.Address
import org.pih.warehouse.integration.xml.order.CargoDetails
import org.pih.warehouse.integration.xml.order.ContactData
import org.pih.warehouse.integration.xml.order.FreightName
import org.pih.warehouse.integration.xml.order.GoodsValue
import org.pih.warehouse.integration.xml.order.Header
import org.pih.warehouse.integration.xml.order.ItemDetails
import org.pih.warehouse.integration.xml.order.KNOrgDetails
import org.pih.warehouse.integration.xml.order.LocationInfo
import org.pih.warehouse.integration.xml.order.ManageReferences
import org.pih.warehouse.integration.xml.order.ManageRemarks
import org.pih.warehouse.integration.xml.order.Order
import org.pih.warehouse.integration.xml.order.OrderCargoSummary
import org.pih.warehouse.integration.xml.order.OrderDetails
import org.pih.warehouse.integration.xml.order.OrderParties
import org.pih.warehouse.integration.xml.order.PartyID
import org.pih.warehouse.integration.xml.order.PartyType
import org.pih.warehouse.integration.xml.order.Phone
import org.pih.warehouse.integration.xml.order.PlannedDateTime
import org.pih.warehouse.integration.xml.order.RefType
import org.pih.warehouse.integration.xml.order.Remark
import org.pih.warehouse.integration.xml.order.TermsOfTrade
import org.pih.warehouse.integration.xml.order.UnitTypeLength
import org.pih.warehouse.integration.xml.order.UnitTypeQuantity
import org.pih.warehouse.integration.xml.order.UnitTypeVolume
import org.pih.warehouse.integration.xml.order.UnitTypeWeight
import org.pih.warehouse.integration.xml.pod.DocumentUpload
import org.pih.warehouse.integration.xml.trip.Orders
import org.pih.warehouse.integration.xml.trip.Trip
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class TmsIntegrationService {

    boolean transactional = true

    def grailsApplication
    def shipmentService
    def fileTransferService
    def xsdValidatorService
    def notificationService
    def locationService
    def stockMovementService

    String serialize(final Object object, final Class clazz) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)
            Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE)
            marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            marshaller.marshal(object, bufferedOutputStream)
            bufferedOutputStream.flush()
            return byteArrayOutputStream.toString()

        } catch (Exception e) {
            log.error("Error occurred while serializing object to XML: " + e.message, e)
            throw e
        }
    }

    Object deserialize(String xmlContents) {
        // Convert XML message to message object
        JAXBContext jaxbContext = JAXBContext.newInstance("org.pih.warehouse.integration.xml.acceptancestatus:org.pih.warehouse.integration.xml.execution:org.pih.warehouse.integration.xml.pod:org.pih.warehouse.integration.xml.trip");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        // OBKN-372 Hack to allow JAXB to deserialize XML without xmlns
        Boolean resolveEmptyNamespace = grailsApplication.config.openboxes.integration.ftp.resolveEmptyNamespace.enabled
        if (resolveEmptyNamespace) {
            InputStream inputStream = new ByteArrayInputStream(xmlContents.bytes)
            xmlContents = xsdValidatorService.resolveEmptyNamespace(inputStream)
        }
        return unmarshaller.unmarshal(IOUtils.toInputStream(xmlContents))
    }

    Object deserialize(String xmlContents, final Class clazz) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream()
            BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)
            Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller()

            // OBKN-372 Hack to allow JAXB to deserialize XML without xmlns
            Boolean resolveEmptyNamespace = grailsApplication.config.openboxes.integration.ftp.resolveEmptyNamespace.enabled
            if (resolveEmptyNamespace) {
                InputStream inputStream = new ByteArrayInputStream(xmlContents.bytes)
                xmlContents = xsdValidatorService.resolveEmptyNamespace(inputStream)
            }
            return unmarshaller.unmarshal(new StringReader(xmlContents))

        } catch (Exception e) {
            log.error("Error occurred while deserializing XML to object: " + e.message, e)
            throw e
        }
    }

    boolean validateMessage(String xmlContents, Boolean forceValidation = Boolean.FALSE) {
        Boolean validationEnabled = grailsApplication.config.openboxes.integration.ftp.inbound.validate
        log.info "Validation enabled: ${validationEnabled}"
        if (validationEnabled || forceValidation) {
            return xsdValidatorService.validateXml(xmlContents)
        }
        return true
    }

    def handleMessages() {
        def message
        try {
            String directory = grailsApplication.config.openboxes.integration.ftp.inbound.directory
            List<String> subdirectories = grailsApplication.config.openboxes.integration.ftp.inbound.subdirectories
            def messages = fileTransferService.listMessages(directory, subdirectories)

            messages.eachWithIndex { msg, index ->
                log.info "Message ${index}: " + msg
            }

            messages = messages.findAll { it.isRegularFile && it.name.endsWith(".xml")}
            if (messages) {
                message = messages.pop()
                if (message) {
                    String xmlContents = fileTransferService.retrieveMessage(message.path)
                    log.info "Handling message ${message}:\n${xmlContents}"
                    handleMessage(xmlContents)

                    // Archive message on success so that other messages can be processed
                    Boolean archiveMessageOnSuccess = grailsApplication.config.openboxes.integration.ftp.inbound.archiveOnSuccess.enabled
                    if (archiveMessageOnSuccess) {
                        log.info "Archiving message ${message}"
                        archiveMessage(message.path)
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Message ${message?.name} not processed due to error: " + e.message, e)
            if (message) {
                failMessage(message?.path, e)

                // Archive message on failure so that other messages can be processed
                // Not recommended as this can lead to unexpected outcomes given that this will allow other messages to be processed
                Boolean archiveOnFailure = grailsApplication.config.openboxes.integration.ftp.inbound.archiveOnFailure.enabled
                if (archiveOnFailure) {
                    archiveMessage(message.path)
                }
            }
        }
    }

    def handleMessage(String xmlContents) {
        validateMessage(xmlContents)
        Object messageObject = deserialize(xmlContents)
        handleMessage(messageObject)
    }

    def handleMessage(DocumentUpload documentUpload) {
        log.info "Document upload: " + documentUpload
        String documentType = documentUpload.documentType
        String fileName = documentUpload.uploadDetails.documentName
        String fileContents = documentUpload.uploadDetails.documentFile
        String trackingNumber = documentUpload.orderId
        attachDocument(trackingNumber, documentType, fileName, fileContents)
    }

    def handleMessage(AcceptanceStatus acceptanceStatus) {
        log.info "Acceptance status " + acceptanceStatus.tripDetails.toString()
        List<String> trackingNumbers = acceptanceStatus.tripOrderDetails.orderId
        trackingNumbers.each { String trackingNumber ->
            acceptDeliveryOrder(trackingNumber)
        }
    }

    def handleMessage(Execution execution) {
        log.info "Trip execution " + execution.toString()
        SimpleDateFormat dateFormatter = new SimpleDateFormat(grailsApplication.config.openboxes.integration.defaultDateFormat)

        execution.executionStatus.each { ExecutionStatus executionStatus ->
            String trackingNumber = executionStatus.orderId
            String statusCode = executionStatus.status
            BigDecimal latitude = executionStatus?.geoData?.latitude
            BigDecimal longitude = executionStatus?.geoData?.longitude
            Date eventDate = dateFormatter.parse(executionStatus.dateTime)
            createEvent(trackingNumber, statusCode, eventDate, latitude, longitude)
            triggerOutboundInventoryUpdates(trackingNumber, statusCode)
            triggerInboundInventoryUpdates(trackingNumber, statusCode)
        }
    }

    def handleMessage(Trip trip) {
        log.info "Trip notification Orders Count" + trip?.tripOrderDetails?.orders?.size()
        List<Orders> orders = trip.tripOrderDetails.orders
        orders.each { Orders deliveryOrder ->
            log.info "Trip notification order" + deliveryOrder.toString()
            String identifier = deliveryOrder.extOrderId
            String trackingNumber = deliveryOrder.orderId
            associateStockMovementAndDeliveryOrder(identifier, trackingNumber)
        }
    }

    void uploadDeliveryOrders(Date requestedDeliveryDate) {
        // FIXME This should be replaced a getter that returns locations with a supported activity
        List<Location> locations = locationService.getLocationsSupportingActivity(ActivityCode.ENABLE_ETRUCKNOW_INTEGRATION)
        locations.each { Location origin ->
            List<StockMovement> stockMovements = getEligibleOutboundStockMovements(origin, requestedDeliveryDate)
            if (stockMovements) {
                log.info "Location ${origin} has ${stockMovements.size()} stock movements"
                stockMovements.each { StockMovement stockMovement ->
                    log.info "Send delivery order for stock movement ${stockMovement.identifier} ${stockMovement.requestedDeliveryDate}"
                    uploadDeliveryOrder(stockMovement)
                }
            }
            else {
                log.info "There are no delivery orders for location ${origin} on ${requestedDeliveryDate}"
            }
        }
    }

    List<StockMovement> getEligibleOutboundStockMovements(Location origin, Date requestedDeliveryDate) {
        StockMovement criteria = new StockMovement(origin: origin,
                stockMovementType: StockMovementType.OUTBOUND,
                requestedDeliveryDate: requestedDeliveryDate)
        List<StockMovement> stockMovements = stockMovementService.getStockMovements(criteria, [:])
        return stockMovements.findAll { StockMovement stockMovement -> !stockMovement.trackingNumber }
    }

    void createEvent(String trackingNumber, String statusCode, Date eventDate, BigDecimal latitude = null, BigDecimal longitude = null) {

        // Locate stock movement by tracking number
        StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber, Boolean.FALSE)
        if (!stockMovement) {
            throw new IllegalArgumentException("Unable to locate stock movement by tracking number ${trackingNumber}")
        }

        // Identify event type associated with status
        EventType eventType = EventType.findByCode(statusCode)
        if (!eventType) {
            throw new IllegalArgumentException("Status code ${statusCode} not associated with an event type")
        }

        createEvent(stockMovement, eventType, eventDate, latitude, longitude)
    }

    void createEvent(StockMovement stockMovement, EventTypeCode eventTypeCode, Date eventDate, BigDecimal latitude = null, BigDecimal longitude = null) {
        EventType eventType = EventType.findByEventCode(eventTypeCode)
        if (!eventType) {
            throw new IllegalArgumentException("No event type associated with event type code ${eventTypeCode}")
        }
        createEvent(stockMovement, eventType, eventDate, latitude, longitude)
    }

    void createEvent(StockMovement stockMovement, EventType eventType, Date eventDate, BigDecimal latitude = null, BigDecimal longitude = null) {

        Shipment shipment = stockMovement?.shipment

        // Validate status update - temporarily disabled
        //if (eventType.eventCode == EventTypeCode.COMPLETED && !shipment.hasShipped()) {
        //    throw new IllegalStateException("Unable to complete a shipment until it has been shipped")
        //}

        // Create new shipment event to represent status update
        Event event = shipment.events.find { it.eventType == eventType && it.eventDate == eventDate}
        if (!event) {
            log.info "Creating new event ${eventType} since it does not exist"
            // OBKN-378 TransientObjectException: object references an unsaved transient instance
            event = new Event(eventType: eventType, eventDate: eventDate, longitude: longitude, latitude: latitude)
            event.save(flush: true, failOnError: true)
            shipment.addToEvents(event)
            shipment.save(flush: true)
        }

        // If successful, send shipment status notification
        notificationService.sendShipmentStatusNotification(shipment, event, shipment.origin, [RoleType.ROLE_SHIPMENT_NOTIFICATION])

    }

    void associateStockMovementAndDeliveryOrder(String identifier, String trackingNumber) {
        // FIXME Need to ensure that stock movement is ready to receive notification i.e. shipment has been created
        StockMovement stockMovement = stockMovementService.getStockMovementByIdentifier(identifier, Boolean.FALSE)
        if (!stockMovement?.shipment) {
            stockMovement?.shipment = stockMovementService.createShipment(stockMovement, false)
        }
        stockMovementService.createOrUpdateTrackingNumber(stockMovement?.shipment, trackingNumber)
    }

    void acceptDeliveryOrder(String trackingNumber) {
        StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber, Boolean.FALSE)
        if (!stockMovement) {
            throw new Exception("Unable to locate stock movement by tracking number ${trackingNumber}")
        }

        createEvent(stockMovement, EventTypeCode.ACCEPTED, new Date())
        notificationService.sendShipmentAcceptedNotification(shipment, shipment.origin, [RoleType.ROLE_SHIPMENT_NOTIFICATION])
    }

    void attachDocument(String trackingNumber, String documentType, String fileName, String fileContents) {
        if (trackingNumber) {
            log.info "Looking up stock movement by tracking number ${trackingNumber}"
            StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber, Boolean.FALSE)
            if (stockMovement) {
                log.info "Attaching document ${fileName} to ${stockMovement.identifier}"
                Document document = new Document()
                if (documentType) {
                    document.documentNumber = "${documentType}-${trackingNumber}"
                }
                document.documentType = DocumentType.get(Constants.DEFAULT_DOCUMENT_TYPE_ID)
                document.name = fileName
                document.filename = fileName
                document.fileContents = fileContents.bytes

                // FIXME we need to figure out a way to detect the mimetype of the file
                document.contentType = "application/octet-stream"
                document.save(flush:true, failOnError: true)

                stockMovement.shipment.addToDocuments(document)
                stockMovement.shipment.save(flush:true)

            }
        }
    }

    def triggerOutboundInventoryUpdates(String trackingNumber, String status) {
        List outboundTransactionTriggerStatuses = grailsApplication.config.openboxes.integration.createOutboundTransactionOnStatusUpdate
        log.info "Triggering outbound inventory update ${trackingNumber} if ${status} in ${outboundTransactionTriggerStatuses}"
        if (status in outboundTransactionTriggerStatuses) {
            StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber, Boolean.FALSE)
            log.info ("Stock movement ${stockMovement?.identifier} with status ${stockMovement?.status} will be shipped")
            if (!stockMovement.hasBeenShipped() && stockMovement.stockMovementStatusCode >= StockMovementStatusCode.PACKED) {
                stockMovementService.transitionRequisitionBasedStockMovement(stockMovement, StockMovementStatusCode.DISPATCHED)
            }
            else {
                log.warn ("Stock movement ${stockMovement.identifier} with status ${stockMovement.status} cannot be dispatched")
            }
        }
    }

    def triggerInboundInventoryUpdates(String trackingNumber, String status) {
        List inboundTransactionTriggerStatuses = grailsApplication.config.openboxes.integration.createInboundTransactionOnStatusUpdate
        log.info "Triggering inbound inventory update ${trackingNumber} if ${status} in ${inboundTransactionTriggerStatuses}"
        if (status in inboundTransactionTriggerStatuses) {
            StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber, Boolean.FALSE)
            log.info "stockmovement ${stockMovement.hasBeenShipped()} ${stockMovement.isReceived} ${stockMovement?.stockMovementStatusCode}"
            if(stockMovement.hasBeenShipped() && !stockMovement.isReceived) {
                log.info ("Stock movement ${stockMovement.identifier} with status ${stockMovement.status} will be received")
                Shipment shipment = stockMovement.shipment
                if (shipment) {
                    String comment = "Shipment ${shipment.shipmentNumber} has been automatically received into ${shipment.destination}"
                    shipmentService.receiveShipments([shipment.id], comment, shipment?.createdBy?.id, shipment?.destination?.id, true)
                }
            }
            else {
                log.warn ("Stock movement ${stockMovement.identifier} with status ${stockMovement.status} has already been received")
            }

        }
    }

    def failMessage(String filePath, Exception exception) {
        try {
            log.info "Path ${filePath}"
            String stacktrace = ExceptionUtils.getStackTrace(exception);
            def filename = FilenameUtils.getBaseName(filePath)
            def path = FilenameUtils.getFullPathNoEndSeparator(filePath)
            log.info "Storing error message to ${filename} in directory ${path}"
            fileTransferService.storeMessage("${filename}-stacktrace.log", stacktrace, path)
        } catch (Exception e) {
            log.error("Unable to write error file to ftp server: " + e.message, e)
        }
    }

    def archiveMessage(String oldPath) {
        try {
            def filename = FilenameUtils.getName(oldPath)
            def destination = FilenameUtils.getFullPathNoEndSeparator(oldPath)
            def newPath = "${destination}/archive/${filename}"
            log.info("Archiving message ${oldPath} to ${newPath}")
            fileTransferService.moveMessage(oldPath, newPath)
        } catch (Exception e) {
            log.error("Unable to archive message " + e.message, e)
        }
    }

    def uploadDeliveryOrder(StockMovement stockMovement) {
        try {
            Object deliveryOrder = createDeliveryOrder(stockMovement)
            String xmlContents = serialize(deliveryOrder, org.pih.warehouse.integration.xml.order.Order.class)
            log.info "Uploading delivery order:\n" + xmlContents
            Boolean validationEnabled = grailsApplication.config.openboxes.integration.ftp.outbound.validate
            log.info "Validation enabled: ${validationEnabled}"
            if (validationEnabled) {
                xsdValidatorService.validateXml(xmlContents)
            }

            // transfer file to sftp server
            String filenameTemplate = grailsApplication.config.openboxes.integration.order.filename
            String destinationFile = String.format(filenameTemplate, stockMovement?.identifier ?: stockMovement?.id)
            String destinationDirectory = "${grailsApplication.config.openboxes.integration.ftp.outbound.directory}"
            fileTransferService.storeMessage(destinationFile, xmlContents, destinationDirectory)
            createEvent(stockMovement, EventTypeCode.UPLOADED, new Date())
        } catch (SFTPException e) {
            log.error("Unable to upload delivery to order to ftp server due to " + e.statusCode, e)
            throw e
        }
    }

    def createDeliveryOrder(StockMovement stockMovement) {
        Map config = grailsApplication.config.openboxes.integration.order
        def defaultCurrencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode

        // Create Order
        Order order = new Order();
        Header header = new Header(config.header.version, null, null,
                config.header.sequenceNumber, config.header.destinationApp);
        order.setHeader(header);
        order.setAction(config.action);
        order.setKnOrgDetails(buildOrganizationDetails(config));

        // Order Details
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setExtOrderId(stockMovement?.identifier);
        orderDetails.setDepartmentCode(config.orderDetails.departmentCode);
        orderDetails.setOrderType(config.orderDetails.orderType);
        orderDetails.setOrderProductType(config.orderDetails.orderProductType);
        orderDetails.setModeOfTransport(config.orderDetails.modeOfTransport);
        orderDetails.setServiceType(config.orderDetails.serviceType);
        orderDetails.setDeliveryTerms(config.orderDetails.deliveryTerms);
        orderDetails.setGoodsValue(new GoodsValue(stockMovement.totalValue?:0.00, defaultCurrencyCode));
        orderDetails.setTermsOfTrade(new TermsOfTrade(config.orderDetails.termsOfTrade.incoterm,
                new FreightName( config.orderDetails.termsOfTrade.freightName.term, config.orderDetails.termsOfTrade.freightName.name)));

        // Party Types
        // FIXME Fix magic strings
        ArrayList <PartyType> partyTypes = new ArrayList<PartyType>();
        partyTypes.add(buildPartyType(stockMovement?.destination?.organization, stockMovement?.destination?.manager, "CUSTOMER"))
        partyTypes.add(buildPartyType(stockMovement?.destination, stockMovement?.destination?.manager, "CONSIGNEE"))
        partyTypes.add(buildPartyType(stockMovement?.origin, stockMovement?.origin?.manager, "SHIPPER"))
        orderDetails.setOrderParties(new OrderParties(partyTypes));

        // Start and end locations
        Date requestedDeliveryDate = stockMovement.requestedDeliveryDate
        Date expectedShippingDate = stockMovement?.expectedShippingDate?:requestedDeliveryDate-1
        Date expectedDeliveryDate = stockMovement?.expectedDeliveryDate?:requestedDeliveryDate

        orderDetails.setOrderStartLocation(buildLocationInfo(1,
                stockMovement?.origin, expectedShippingDate, stockMovement.comments?:"N/A"));

        orderDetails.setOrderEndLocation(buildLocationInfo(2,
                stockMovement?.destination, expectedDeliveryDate, stockMovement.comments?:"N/A"));

        // Calculate total volume for stock movement
        Attribute volumeAttribute = Attribute.findByCode("VOLUME")
        BigDecimal totalVolume = stockMovement.getAggregateNumericValue(volumeAttribute)?:0.0
        String volumeUom = volumeAttribute?.unitOfMeasureClass?.baseUom?.code?:"cbm"

        // Calculate total volume for stock movement
        Attribute weightAttribute = Attribute.findByCode("WEIGHT")
        BigDecimal totalWeight = stockMovement.getAggregateNumericValue(weightAttribute)?:0.0
        String weightUom = weightAttribute?.unitOfMeasureClass?.baseUom?.code?:"kg"

        // Cargo Summary
        UnitTypeVolume unitTypeVolume = new UnitTypeVolume(formatDecimal(totalVolume), volumeUom)
        UnitTypeWeight unitTypeWeight = new UnitTypeWeight(formatDecimal(totalWeight), weightUom)
        orderDetails.setOrderCargoSummary(buildOrderCargoSummary(config,
                stockMovement.hasHazardousMaterial(),
                unitTypeVolume,
                unitTypeWeight))

        // Cargo Details
        List<ItemDetails> itemList = buildItemList(stockMovement, unitTypeVolume, unitTypeWeight)
        orderDetails.setOrderCargoDetails(new CargoDetails(itemList));

        // Remarks
        List<Remark> remarks = []
        if (stockMovement.comments) {
            remarks << new Remark(stockMovement.comments);
        }
        else {
            remarks << new Remark("No remarks")
        }
        orderDetails.setManageRemarks(new ManageRemarks(remarks));

        List<RefType> referenceTypes = []
        if (stockMovement?.identifier) {
            referenceTypes << new RefType("extOrderId", stockMovement?.identifier)
        }
        if (stockMovement?.shipment?.referenceNumbers) {
            stockMovement?.shipment?.referenceNumbers?.collect { ReferenceNumber referenceNumber ->
                referenceTypes << new RefType(referenceNumber.referenceNumberType.name, referenceNumber?.identifier)
            }
        }

        orderDetails.setManageReferences(new ManageReferences(referenceTypes))
        order.setOrderDetails(orderDetails);

        return order;
    }

    KNOrgDetails buildOrganizationDetails(Map config) {
        KNOrgDetails knOrgDetails = new KNOrgDetails(config.organizationDetails.companyCode, config.organizationDetails.branchCode)
        knOrgDetails.setLogicalReceiver(config.organizationDetails.logicalReceiver)
        knOrgDetails.setPhysicalReceiver(config.organizationDetails.physicalReceiver)
        knOrgDetails.setLogicalSender(config.organizationDetails.logicalSender)
        knOrgDetails.setPhysicalSender(config.organizationDetails.physicalSender)
        return knOrgDetails
    }

    List<ItemDetails> buildItemList(StockMovement stockMovement, UnitTypeVolume unitTypeVolume, UnitTypeWeight unitTypeWeight) {
        List itemList = new ArrayList<ItemDetails>();

        Map config = grailsApplication.config.openboxes.integration.order
        if (config.orderDetails.cargoDetails.enabled) {
            Attribute volumeAttribute = Attribute.findByCode("VOLUME")
            String volumeUom = volumeAttribute?.unitOfMeasureClass?.baseUom?.code?:"cbm"

            Attribute weightAttribute = Attribute.findByCode("WEIGHT")
            String weightUom = weightAttribute?.unitOfMeasureClass?.baseUom?.code?:"kg"

            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                ItemDetails itemDetails = new ItemDetails();
                itemDetails.setCargoType(config.orderDetails.cargoDetails.cargoType);
                itemDetails.setGrounded(config.orderDetails.cargoDetails.grounded);
                itemDetails.setStackable(config.orderDetails.cargoDetails.stackable);
                itemDetails.setSplittable(config.orderDetails.cargoDetails.splittable);
                itemDetails.setDangerousGoodsFlag(config.orderDetails.cargoDetails.dangerousGoodsFlag);
                itemDetails.setDescription(stockMovementItem?.product?.name);
                itemDetails.setQuantity("${stockMovementItem?.quantityShipped?:stockMovementItem.quantityRequired}");
                itemDetails.setHandlingUnit(config.orderDetails.cargoDetails.handlingUnit);
                itemDetails.setLength(new UnitTypeLength("1.0", "m"))
                itemDetails.setWidth(new UnitTypeLength("1.0", "m"))
                itemDetails.setHeight(new UnitTypeLength("1.0", "m"))
                itemDetails.setWeight(new UnitTypeWeight("1.0", "kg"))
                itemDetails.setActualWeight(new UnitTypeWeight("1.0", "kg"))
                itemDetails.setLdm("0")

                BigDecimal volumeValue = stockMovementItem.getNumericValue(volumeAttribute)?:0.0
                itemDetails.setActualVolume(new UnitTypeVolume(formatDecimal(volumeValue), volumeUom));

                BigDecimal weightValue = stockMovementItem.getNumericValue(weightAttribute)?:0.0
                itemDetails.setVolumetricWeight(new UnitTypeWeight(formatDecimal(weightValue), weightUom));

                itemList.add(itemDetails)
            }
        }
        else {
            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setCargoType(config.orderDetails.cargoDetails.cargoType);
            itemDetails.setGrounded(config.orderDetails.cargoDetails.grounded);
            itemDetails.setStackable(config.orderDetails.cargoDetails.stackable);
            itemDetails.setSplittable(config.orderDetails.cargoDetails.splittable);
            itemDetails.setDangerousGoodsFlag(config.orderDetails.cargoDetails.dangerousGoodsFlag);
            itemDetails.setDescription(stockMovement?.description);
            itemDetails.setQuantity("${stockMovement?.lineItems?.size()?:0}");
            itemDetails.setHandlingUnit(config.orderDetails.cargoDetails.handlingUnit);
            itemDetails.setLength(new UnitTypeLength("1.0", "m"))
            itemDetails.setWidth(new UnitTypeLength("1.0", "m"))
            itemDetails.setHeight(new UnitTypeLength("1.0", "m"))
            itemDetails.setWeight(new UnitTypeWeight("1.0", "kg"))
            itemDetails.setActualWeight(new UnitTypeWeight("1.0", "kg"))
            itemDetails.setLdm("0")

            itemDetails.setActualVolume(unitTypeVolume);
            itemDetails.setVolumetricWeight(unitTypeWeight);

            itemList.add(itemDetails)
        }
        return itemList
    }


    OrderCargoSummary buildOrderCargoSummary(Map config, Boolean dangerousGoodsFlag, UnitTypeVolume totalVolume, UnitTypeWeight totalWeight) {
        OrderCargoSummary orderCargoSummary = new OrderCargoSummary()
        orderCargoSummary.setDangerousGoodsFlag(config.orderDetails.cargoDetails.dangerousGoodsFlag)
        orderCargoSummary.setTotalPackagesOfDangerousGoods("0")
        orderCargoSummary.setTotalQuantity(new UnitTypeQuantity("1.0"))
        orderCargoSummary.setTotalVolume(totalVolume)
        orderCargoSummary.setTotalWeight(totalWeight)
        return orderCargoSummary
    }

    PartyType buildPartyType(Location location, User contactData, String type) {
        PartyType partyType = new PartyType();
        partyType.setPartyID(new PartyID(location?.locationNumber, location.name));
        partyType.setType(type);

        // Add contact information
        partyType.setContactData(buildContactData(contactData?:location?.manager))
        return partyType
    }

    PartyType buildPartyType(Organization organization, User contactData, String type) {
        PartyType partyType = new PartyType();
        partyType.setPartyID(new PartyID(organization?.code, organization?.name));
        partyType.setType(type);

        // Add contact information
        partyType.setContactData(buildContactData(contactData?:organization?.defaultLocation?.manager))
        return partyType
    }


    ContactData buildContactData(User user) {
        Map config = grailsApplication.config.openboxes.integration.partyType.contactData
        if (user) {
            return new ContactData(
                    user.firstName,
                    user?.lastName,
                    new Phone(config.phone.countryCode, user?.phoneNumber),
                    user?.email
            );
        }
        else {
            return new ContactData(
                    config.firstName,
                    config.lastName,
                    new Phone(config.phone.countryCode, config.phone.contactNumber),
                    config.emailAddress)
        }
    }

    LocationInfo buildLocationInfo(Integer stopSequence, Location location, Date expectedDate, String driverInstructions) {
        def dateFormatter = new SimpleDateFormat(grailsApplication.config.openboxes.integration.defaultDateFormat)
        String expectedDateString = expectedDate ? dateFormatter.format(expectedDate) : null
        return new LocationInfo(stopSequence,
                location?.address ? buildAddress(location?.address) : defaultAddress(),
                new PlannedDateTime(expectedDateString, expectedDateString),
                driverInstructions?:""
        );
    }

    Address buildAddress(org.pih.warehouse.core.Address address) {
        def defaultTimeZone = grailsApplication.config.openboxes.integration.defaultTimeZone?:null
        return new Address(
                address.description,
                address.address,
                address.city,
                address.stateOrProvince?:address?.city,
                address.postalCode?:"",
                address.country?:"",
                defaultTimeZone)
    }

    Address defaultAddress() {
        def defaultTimeZone = grailsApplication.config.openboxes.integration.defaultTimeZone?:null
        return new Address(
                "Null Address",
                "Null Street Address",
                "Null City",
                "Null State",
                "Null Zip",
                "UK",
                defaultTimeZone)
    }

    String formatDecimal(Number number) {
        DecimalFormat format = DecimalFormat.getNumberInstance()
        format.setMinimumFractionDigits(1)
        format.format(number)
    }

}
