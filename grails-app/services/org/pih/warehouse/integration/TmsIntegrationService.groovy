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

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.User
import org.pih.warehouse.integration.xml.acceptancestatus.AcceptanceStatus
import org.pih.warehouse.integration.xml.execution.Execution
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
import org.pih.warehouse.integration.xml.trip.Trip
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.shipping.ReferenceNumber

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.validation.SchemaFactory
import java.text.SimpleDateFormat

class TmsIntegrationService {

    boolean transactional = true

    def grailsApplication
    def fileTransferService
    def xsdValidatorService

    String serialize(final Object object, final Class clazz) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)
            Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE)
            marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            //SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //schema = factory.newSchema((new File(xsdLocation)));


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
        InputStream inputStream = IOUtils.toInputStream(xmlContents)
        return unmarshaller.unmarshal(inputStream)
    }

    Object deserialize(String xmlContents, final Class clazz) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream()
            BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)
            Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller()
            return unmarshaller.unmarshal(new StringReader(xmlContents))

        } catch (Exception e) {
            log.error("Error occurred while deserializing XML to object: " + e.message, e)
            throw e
        }
    }

    def handleMessage(Object messageObject) {

        // Publish message to event bus
        if (messageObject instanceof DocumentUpload) {
            grailsApplication.mainContext.publishEvent(new DocumentUploadEvent(messageObject))
        } else if (messageObject instanceof AcceptanceStatus) {
            grailsApplication.mainContext.publishEvent(new AcceptanceStatusEvent(messageObject))
        } else if (messageObject instanceof Execution) {
            grailsApplication.mainContext.publishEvent(new TripExecutionEvent(messageObject))
        } else if (messageObject instanceof Trip) {
            grailsApplication.mainContext.publishEvent(new TripNotificationEvent(messageObject))
        }

    }


    def uploadDeliveryOrder(StockMovement stockMovement) {
        Object deliveryOrder = createDeliveryOrder(stockMovement)
        String xmlContents = serialize(deliveryOrder, org.pih.warehouse.integration.xml.order.Order.class)

        Boolean validationEnabled = grailsApplication.config.openboxes.integration.ftp.outbound.validate
        log.info "Validation enabled: ${validationEnabled}"
        if (validationEnabled) {
            xsdValidatorService.validateXml(xmlContents)
        }

        // transfer file to sftp server
        String filenameTemplate = grailsApplication.config.openboxes.integration.order.filename
        String destinationFile = String.format(filenameTemplate, stockMovement?.identifier?:stockMovement?.id)
        String destinationDirectory = "${grailsApplication.config.openboxes.integration.ftp.outbound.directory}"
        fileTransferService.storeMessage(destinationFile, xmlContents, destinationDirectory)
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
        orderDetails.setGoodsValue(new GoodsValue(stockMovement.totalValue?:0, defaultCurrencyCode));
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
        BigDecimal totalVolume = stockMovement.getAggregateNumericValue(volumeAttribute)?:0
        String volumeUom = volumeAttribute?.unitOfMeasureClass?.baseUom?.code?:"cbm"

        // Calculate total volume for stock movement
        Attribute volumeWeight = Attribute.findByCode("WEIGHT")
        BigDecimal totalWeight = stockMovement.getAggregateNumericValue(volumeWeight)?:0
        String weightUom = volumeWeight?.unitOfMeasureClass?.baseUom?.code?:"kg"

        // Cargo Summary
        UnitTypeVolume unitTypeVolume = new UnitTypeVolume(totalVolume.toString(), volumeUom)
        UnitTypeWeight unitTypeWeight = new UnitTypeWeight(totalWeight.toString(), weightUom)
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

                BigDecimal volumeValue = stockMovementItem.getNumericValue(volumeAttribute)?:0
                itemDetails.setActualVolume(new UnitTypeVolume(volumeValue.toString(), volumeUom));

                BigDecimal weightValue = stockMovementItem.getNumericValue(weightAttribute)?:0
                itemDetails.setVolumetricWeight(new UnitTypeWeight(weightValue.toString(), weightUom));

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

    PartyType buildPartyType(Organization organization, User contactData, String type) {
        PartyType partyType = new PartyType();
        partyType.setPartyID(new PartyID(organization?.code, organization?.name));
        partyType.setType(type);

        // Add contact information
        User contact = contactData?:organization?.defaultLocation?.manager
        if (contact) {
            partyType.setContactData(new ContactData(contact.firstName, contact?.lastName,
                    new Phone(null, contact?.phoneNumber), contact?.email));
        }
        return partyType
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

}
