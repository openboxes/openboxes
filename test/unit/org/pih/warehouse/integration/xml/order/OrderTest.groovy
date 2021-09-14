package org.pih.warehouse.integration.xml.order;

import org.junit.After;
import org.junit.Before;
import org.junit.Test
import org.pih.warehouse.integration.XsdValidatorService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue;

public class OrderTest {
    private Order order;
    private final String ACCEPTANCE_STATUS_XSD = "xsd/Order.xsd"
    XsdValidatorService xsdValidatorService = new XsdValidatorService()

    @Before
    public void setUp() {
        order = new Order()
        Header header = new Header("V1", "TestName", "TestPWD", "20201008154348_SG3009200527", "ETRUCKNOW")
        order.setHeader(header)
        order.setAction("CREATE")
        order.setKnOrgDetails(new KNOrgDetails("MYKN", "MYKUL"))

        OrderDetails orderDetails = new OrderDetails()
        orderDetails.setExtOrderId("343432443")
        orderDetails.setDepartmentCode("MYKUL")
        orderDetails.setOrderType("NORMAL")
        orderDetails.setOrderProductType("NORMAL")
        orderDetails.setModeOfTransport("FTL")
        orderDetails.setServiceType("Pharma")
        orderDetails.setDeliveryTerms("Shipper")
        orderDetails.setGoodsValue(new GoodsValue("500", "EUR"))
        orderDetails.setTermsOfTrade(new TermsOfTrade("DAP", new FreightName("10", "Shipper")))

        PartyType partyType = new PartyType()
        partyType.setPartyID(new PartyID("MYSH01505", "RT"))
        partyType.setType("CONSIGNEE")
        partyType.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"))

        PartyType partyType2 = new PartyType()
        partyType2.setPartyID(new PartyID("MYSH01505", "RT"))
        partyType2.setType("SHIPPER")
        partyType2.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"))

        PartyType partyType3 = new PartyType()
        partyType3.setPartyID(new PartyID("MYSH01505", "RT"))
        partyType3.setType("CUSTOMER")
        partyType3.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com"))

        ArrayList <PartyType> partyTypes = new ArrayList()
        partyTypes.add(partyType)
        partyTypes.add(partyType2)
        partyTypes.add(partyType3)

        orderDetails.setOrderParties(new OrderParties(partyTypes))

        Address dummyAddress = new Address("Main Address", "Test Street 1", "Test City", "Test State", "10500", "DC", "Asia/Kolkata" )
        LocationInfo orderStartLocation = new LocationInfo(
                dummyAddress,
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        )
        orderDetails.setOrderStartLocation(orderStartLocation)
        LocationInfo orderEndLocation = new LocationInfo(
                dummyAddress,
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        )
        orderDetails.setOrderEndLocation(orderEndLocation)
        orderDetails.setOrderCargoSummary(new OrderCargoSummary(
                new UnitTypeQuantity("1.0"),
                new UnitTypeVolume("1.0", "cbm"),
                new UnitTypeWeight("200", "kg"),
                "true",
                "1"
        ))
        //OrderCargoDetails
        ItemDetails firstItem = new ItemDetails()
        firstItem.setCargoType("GEN_CATEGORY")
        firstItem.setStackable("false")
        firstItem.setSplittable("false")
        firstItem.setDangerousGoodsFlag("true")
        firstItem.setDescription("Description")
        firstItem.setHandlingUnit("BLUE-PALLETS")
        firstItem.setQuantity("5")
        firstItem.setLength(new UnitTypeLength("1.0", "m"))
        firstItem.setWidth(new UnitTypeLength("1.0", "m"))
        firstItem.setHeight(new UnitTypeLength("1.0", "m"))
        firstItem.setWeight(new UnitTypeWeight("200.0", "kg"))
        firstItem.setActualVolume(new UnitTypeVolume("1.0", "cbm"))
        firstItem.setActualWeight(new UnitTypeWeight("200.0", "kg"))
        firstItem.setLdm("25")

        ItemDetails secondItem = new ItemDetails()
        secondItem.setCargoType("GEN_CATEGORY")
        secondItem.setStackable("false")
        secondItem.setSplittable("false")
        secondItem.setDangerousGoodsFlag("true")
        secondItem.setDescription("Description")
        secondItem.setHandlingUnit("BLUE-PALLETS")
        secondItem.setQuantity("5")
        secondItem.setLength(new UnitTypeLength("1.0", "m"))
        secondItem.setWidth(new UnitTypeLength("1.0", "m"))
        secondItem.setHeight(new UnitTypeLength("1.0", "m"))
        secondItem.setWeight(new UnitTypeWeight("200.0", "kg"))
        secondItem.setActualVolume(new UnitTypeVolume("1.0", "cbm"))
        secondItem.setActualWeight(new UnitTypeWeight("200.0", "kg"))
        secondItem.setLdm("25")

        ArrayList items = new ArrayList<ItemDetails>()
        items.add(firstItem)
        items.add(secondItem)

        orderDetails.setOrderCargoDetails(new CargoDetails(items))
        RefType refType = new RefType("z09", "TEST REFERENCE")
        RefType refType1 = new RefType("ADE", "A12345")
        ArrayList<RefType> refTypes = new ArrayList<RefType>()
        refTypes.add(refType)
        refTypes.add(refType1)
        orderDetails.setManageReferences(new ManageReferences(refTypes))

        Remark remark = new Remark("Lorem Ipsum")
        ArrayList <Remark> remarks = new ArrayList<Remark>(Arrays.asList(remark))
        orderDetails.setManageRemarks(new ManageRemarks(remarks))
        order.setOrderDetails(orderDetails)

    }

    @After
    public void tearDown() {
        order = null;
    }

    @Test
    public void testObjectToXml() throws JAXBException, FileNotFoundException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Order.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(order, new File("/tmp/new_order.xml"))
        marshaller.marshal(order, System.out)
    }

    @Test
    public void testXmlToObject() throws  JAXBException, FileNotFoundException {
        File xmlFile = new File("/tmp/new_order.xml")

        assertTrue(xsdValidatorService.validateXml(xmlFile.text))

        JAXBContext jaxbContext = JAXBContext.newInstance(Order.class)
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
        Order order = (Order) unmarshaller.unmarshal(xmlFile)

        assertEquals( order.getAction() , "CREATE")
        assertEquals( order.getOrderDetails().getExtOrderId() , "343432443")
        assertEquals(order.getOrderDetails().getOrderParties().getPartyTypes().size(), 3)
    }
}
