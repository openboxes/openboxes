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

class TmsIntegrationService {

    boolean transactional = true


    def createDeliveryOrder(StockMovement stockMovement) {
        Order order = new Order();
        Header header = new Header("V1", "TestName", "TestPWD", "20201008154348_SG3009200527", "ETRUCKNOW");
        order.setHeader(header);
        order.setAction("CREATE");
        order.setKnOrgDetails(new KNOrgDetails("MYKN", "MYKUL"));

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setExtOrderId(stockMovement?.identifier);
        orderDetails.setDepartmentCode("MYKUL");
        orderDetails.setOrderType("NORMAL");
        orderDetails.setOrderProductType("NORMAL");
        orderDetails.setModeOfTransport("FTL");
        orderDetails.setServiceType("Pharma");
        orderDetails.setDeliveryTerms("Shipper");
        orderDetails.setGoodsValue(new GoodsValue("500", "EUR"));
        orderDetails.setTermsOfTrade(new TermsOfTrade("DAP", new FreightName("10", "Shipper")));

        PartyType partyType = new PartyType();
        partyType.setPartyID(new PartyID("MYSH01505", "RT"));
        partyType.setType("CONSIGNEE");
        partyType.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"));

        PartyType partyType2 = new PartyType();
        partyType2.setPartyID(new PartyID("MYSH01505", "RT"));
        partyType2.setType("SHIPPER");
        partyType2.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"));

        PartyType partyType3 = new PartyType();
        partyType3.setPartyID(new PartyID("MYSH01505", "RT"));
        partyType3.setType("CUSTOMER");
        partyType3.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"));

        ArrayList <PartyType> partyTypes = new ArrayList<PartyType>();
        partyTypes.add(partyType);
        partyTypes.add(partyType2);
        partyTypes.add(partyType3);

        orderDetails.setOrderParties(new OrderParties(partyTypes));

        Address dummyAddress = new Address("Main Address", "Test Street 1", "Test City", "Test State", "10500", "DC", "Asia/Kolkata" );
        LocationInfo orderStartLocation = new LocationInfo(
                "1",
                dummyAddress,
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        );
        orderDetails.setOrderStartLocation(orderStartLocation);
        LocationInfo orderEndLocation = new LocationInfo(
                "3",
                dummyAddress,
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        );
        orderDetails.setOrderEndLocation(orderEndLocation);
        orderDetails.setOrderCargoSummary(new OrderCargoSummary(
                new UnitTypeQuantity("1.0"),
                new UnitTypeVolume("1.0", "cbm"),
                new UnitTypeWeight("200", "kg"),
                "true",
                "1"
        ));

        ArrayList itemList = new ArrayList<ItemDetails>();
        stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setCargoType("GEN_CATEGORY");
            itemDetails.setStackable("false");
            itemDetails.setSplittable("false");
            itemDetails.setDangerousGoodsFlag("true");
            itemDetails.setDescription(stockMovementItem?.product?.name);
            itemDetails.setHandlingUnit("BLUE-PALLETS");
            itemDetails.setQuantity();
            itemDetails.setLength(new UnitTypeLength("1.0", "m"));
            itemDetails.setWidth(new UnitTypeLength("1.0", "m"));
            itemDetails.setHeight(new UnitTypeLength("1.0", "m"));
            itemDetails.setWeight(new UnitTypeWeight("200.0", "kg"));
            itemDetails.setActualVolume(new UnitTypeVolume("1.0", "cbm"));
            itemDetails.setActualWeight(new UnitTypeWeight("200.0", "kg"));
            itemDetails.setLdm("25");
            itemList.add(itemDetails)
        }

        orderDetails.setOrderCargoDetails(new CargoDetails(itemList));
        RefType refType = new RefType("z09", "TEST REFERENCE");
        RefType refType1 = new RefType("ADE", "A12345");
        ArrayList<RefType> refTypes = new ArrayList<RefType>();
        refTypes.add(refType);
        refTypes.add(refType1);
        orderDetails.setManageReferences(new ManageReferences(refTypes));

        Remark remark = new Remark("Lorem Ipsum");
        ArrayList <Remark> remarks = new ArrayList<Remark>(Arrays.asList(remark));
        orderDetails.setManageRemarks(new ManageRemarks(remarks));
        order.setOrderDetails(orderDetails);
        return order;
    }
}
