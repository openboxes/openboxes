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

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.User
import org.pih.warehouse.integration.xml.acceptancestatus.AcceptanceStatus
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

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import java.text.SimpleDateFormat

class TmsIntegrationService {

    boolean transactional = true

    def grailsApplication
    def fileTransferService
    def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX")


    String serialize(final Object object, final Class clazz) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)
            Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
            marshaller.marshal(object, bufferedOutputStream)
            bufferedOutputStream.flush()
            return byteArrayOutputStream.toString()

        } catch (Exception e) {
            log.error("Error occurred while serializing object to XML: " + e.message, e)
            throw e
        }
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

    def uploadDeliveryOrder(StockMovement stockMovement) {
        Object deliveryOrder = createDeliveryOrder(stockMovement)
        String serializedOrder = serialize(deliveryOrder, org.pih.warehouse.integration.xml.order.Order.class)

        // transfer file to sftp server
        fileTransferService.storeMessage("CreateDeliveryOrder-${stockMovement?.identifier}.xml", serializedOrder)
    }


    def createDeliveryOrder(StockMovement stockMovement) {
        Map config = grailsApplication.config.openboxes.integration.order
        def defaultCurrencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode

        Order order = new Order();
        Header header = new Header(config.header.version, config.header.username,
                config.header.password, config.header.sequenceNumber, config.header.destinationApp);
        order.setHeader(header);
        order.setAction(config.action);
        order.setKnOrgDetails(new KNOrgDetails(config.organizationDetails.companyCode, config.organizationDetails.branchCode));

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setExtOrderId(stockMovement?.identifier);
        orderDetails.setDepartmentCode(config.orderDetails.departmentCode);
        orderDetails.setOrderType(config.orderDetails.orderType);
        orderDetails.setOrderProductType(config.orderDetails.orderProductType);
        orderDetails.setModeOfTransport(config.orderDetails.modeOfTransport);
        orderDetails.setServiceType(config.orderDetails.serviceType);
        orderDetails.setDeliveryTerms(config.orderDetails.deliveryTerms);

        orderDetails.setGoodsValue(new GoodsValue("${stockMovement.totalValue?:0}", defaultCurrencyCode));

        orderDetails.setTermsOfTrade(new TermsOfTrade(config.orderDetails.termsOfTrade.incoterm,
                new FreightName( config.orderDetails.termsOfTrade.freightName.name, config.orderDetails.termsOfTrade.freightName.name)));

        // FIXME Fix magic strings
        ArrayList <PartyType> partyTypes = new ArrayList<PartyType>();
        partyTypes.add(buildPartyType(stockMovement?.origin, stockMovement?.origin?.manager, "SHIPPER"))
        partyTypes.add(buildPartyType(stockMovement?.destination, stockMovement?.destination?.manager, "CONSIGNEE"))
        partyTypes.add(buildPartyType(stockMovement?.destination?.organization, stockMovement?.destination?.manager, "CUSTOMER"))
        orderDetails.setOrderParties(new OrderParties(partyTypes));

        // Start and end locations
        orderDetails.setOrderStartLocation(buildLocationInfo("1", stockMovement?.origin, stockMovement?.expectedShippingDate, null));
        orderDetails.setOrderEndLocation(buildLocationInfo("2", stockMovement?.destination, stockMovement?.expectedDeliveryDate, null));

        // Order cargo summary
        orderDetails.setOrderCargoSummary(new OrderCargoSummary(
                new UnitTypeQuantity("1.0"),
                new UnitTypeVolume("1.0", "cbm"),
                new UnitTypeWeight("1.0", "kg"),
                "false",
                "0"
        ));

        ArrayList itemList = new ArrayList<ItemDetails>();
        stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setCargoType("GEN_CATEGORY");
            itemDetails.setStackable("false");
            itemDetails.setSplittable("false");
            itemDetails.setDangerousGoodsFlag("false");
            itemDetails.setDescription(stockMovementItem?.product?.name);
            itemDetails.setHandlingUnit("DEFAULT");
            itemDetails.setQuantity(stockMovementItem.quantityShipped);
            itemDetails.setLength(new UnitTypeLength("1.0", "m"));
            itemDetails.setWidth(new UnitTypeLength("1.0", "m"));
            itemDetails.setHeight(new UnitTypeLength("1.0", "m"));
            itemDetails.setWeight(new UnitTypeWeight("1.0", "kg"));
            itemDetails.setActualVolume(new UnitTypeVolume("1.0", "cbm"));
            itemDetails.setActualWeight(new UnitTypeWeight("1.0", "kg"));
            itemDetails.setLdm("25");
            itemList.add(itemDetails)
        }

        orderDetails.setOrderCargoDetails(new CargoDetails(itemList));

//        RefType refType = new RefType("z09", "TEST REFERENCE");
//        RefType refType1 = new RefType("ADE", "A12345");
//        ArrayList<RefType> refTypes = new ArrayList<RefType>();
//        refTypes.add(refType);
//        refTypes.add(refType1);
//        orderDetails.setManageReferences(new ManageReferences(refTypes));

        if (stockMovement.comments) {
            Remark remark = new Remark(stockMovement.comments);
            ArrayList<Remark> remarks = new ArrayList<Remark>(Arrays.asList(remark));
            orderDetails.setManageRemarks(new ManageRemarks(remarks));
        }
        order.setOrderDetails(orderDetails);

        return order;
    }


    PartyType buildPartyType(Location location, User contactData, String type) {
        return buildPartyType(location?.organization, contactData, type)
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
    }

    LocationInfo buildLocationInfo(String stopSequence, Location location, Date expectedDate, String driverInstructions) {
        String expectedDateString = expectedDate ? dateFormatter.format(expectedDate) : null
        return new LocationInfo(
                stopSequence,
                location?.address ? buildAddress(location?.address) : null,
                new PlannedDateTime(expectedDateString, expectedDateString),
                driverInstructions
        );
    }

    Address buildAddress(org.pih.warehouse.core.Address address) {

        def defaultTimeZone = grailsApplication.config.openboxes.integration.order.address.timeZone?:null
        return new Address(
                address.description,
                address.address,
                address.city,
                address.stateOrProvince?:"",
                address.postalCode?:"",
                address.country?:"",
                defaultTimeZone)
    }
}
