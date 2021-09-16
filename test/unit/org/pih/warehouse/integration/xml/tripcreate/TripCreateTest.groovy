package org.pih.warehouse.integration.xml.tripcreate

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.integration.XsdValidatorService
import org.pih.warehouse.integration.xml.order.Order

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

public class TripCreateTest {
    private TripCreate trip
    XsdValidatorService xsdValidatorService = new XsdValidatorService()

    @Before
    public void setUp() {
        trip = new TripCreate()
        Header header = new Header("V1", "20201008154348_SG3009200527", "ETRUCKNOW" )
        trip.setHeader(header)
        trip.setAction("CREATE")
        trip.setTripId("Trip12345")
        trip.setExternalTripId("")
        trip.setKnOrgDetails(new KNOrgDetails("MYKN", "MYKUL"))

        TripDetails tripDetails = new TripDetails()
        tripDetails.setTripType("REGULAR")

        Carrier carrier = new Carrier()
        PartyId partyId = new PartyId("MYSH454", "RT")
        ContactData contactData = new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com")
        carrier.setPartyId(partyId)
        carrier.setContactData(contactData)
        tripDetails.setCarrier(carrier)
        tripDetails.setCarrierInstructions("Please arrive at least 15 minutes early")
        tripDetails.setAdditionalConditions("Special conditions apply to this order")

        VehicleDetails vehicleDetails = new VehicleDetails()
        vehicleDetails.setVehicleTypeCode("")
        vehicleDetails.setVehicleModelCode("")
        vehicleDetails.setProperties(
                new Properties(
                        new WeightProperties(3000.0, "kg"),
                        new VolumeProperties(1, "cbm"),
                        new DimensionProperties(4, "m"),
                        new DimensionProperties(1.8,"m"),
                        new DimensionProperties(2,"m")
                ))
        tripDetails.setVehicleDetails(vehicleDetails)
        DriverDetails driverDetails = new DriverDetails()
        IdProof idProof = new IdProof()
        idProof.setIdType("4343")
        idProof.setIdNumber("3434343")
        driverDetails.setIdProof(idProof)
        driverDetails.setAddress( new Address("Main Address", "Test Street 1", "Test City", "Test State","10500","DE","Asia/Kolkata"))
        driverDetails.setContactData(new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com") )
        tripDetails.setDriverDetails(driverDetails)
        TripCargoSummary tripCargoSummary = new TripCargoSummary(
                new UnitTypeQuantity(1.0),
                new UnitTypeVolume(1.0, "cbm"),
                new UnitTypeWeight(200.0, "kg")
        )
        tripDetails.setTripCargoSummary(tripCargoSummary)

        trip.setTripDetails(tripDetails)

        TripOrderDetails tripOrderDetails = new TripOrderDetails()
        Orders orders = new Orders()
        orders.setOrderId("123456789")
        orders.setExtOrderId("external_ID")
        orders.setDepartmentCode("MYKUL")
        orders.setOrderType("NORMAL")
        orders.setModeOfTransport("FTL")
        orders.setServiceType("Pharma")
        orders.setDeliveryTerms("Shipper")
        orders.setGoodsValue( new GoodsValue(500, "EUR"))
        orders.setTypeOfBusiness("DOMESTIC")
        orders.setTermsOfTrade(
                new TermsOfTrade("DAP", new FreightName("10","Shipper"))
        )

        List <PartyType> orderPartiesList = new ArrayList() {{
            add(new PartyType( new PartyId("MYSH01505", "RT"), "CUSTOMER",new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com")));
            add(new PartyType( new PartyId("MYSH01505", "RT"), "SHIPPER",new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com")));
            add(new PartyType( new PartyId("MYSH01505", "RT"), "CONSIGNEE",new ContactData("Vijetha", "Kakarlapudi", new Phone("60", "9989570124"), "vijetha.kakarlapudi@gmail.com")));

        }}
        orders.setOrderParties( new OrderParties(orderPartiesList))

        LocationInfo orderStartLocation = new LocationInfo(
                1,
                new Address("Main Address", "Test Street 1", "Test City", "Test State","10500","DE","Asia/Kolkata"),
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        )
        LocationInfo orderEndLocation = new LocationInfo(
                3,
                new Address("Main Address", "Test Street 1", "Test City", "Test State","10500","DE","Asia/Kolkata"),
                new PlannedDateTime("2020-10-08T15:43:48+01:00", "2020-10-08T15:43:48+01:00"),
                "Instructions"
        )
        orders.setOrderStartLocation(orderStartLocation)
        orders.setOrderEndLocation(orderEndLocation)

        orders.setOrderCargoSummary(
                new OrderCargoSummary(
                        new UnitTypeQuantity(1.0),
                        new UnitTypeVolume(1.0, "cbm"),
                        new UnitTypeWeight(200.0, "kg"),
                        "1",
                        2
                )
        )

        ItemDetails firstItem = new ItemDetails()
        firstItem.setCargoType("GEN_CATEGORY")
        firstItem.setStackable("0")
        firstItem.setSplittable("0")
        firstItem.setDangerousGoodsFlag("1")
        firstItem.setDescription("Description")
        firstItem.setHandlingUnit("BLUE-PALLETS")
        firstItem.setQuantity(5)
        firstItem.setLength(new DimensionProperties(1.0, "m"))
        firstItem.setWidth(new DimensionProperties(1.0, "m"))
        firstItem.setHeight(new DimensionProperties(1.0, "m"))
        firstItem.setWeight(new UnitTypeWeight(200.0, "kg"))
        firstItem.setActualVolume(new UnitTypeVolume(1.0, "cbm"))
        firstItem.setActualWeight(new UnitTypeWeight(200.0, "kg"))
        firstItem.setLdm(25)

        ItemDetails secondItem = new ItemDetails()
        secondItem.setCargoType("GEN_CATEGORY")
        secondItem.setStackable("0")
        secondItem.setSplittable("0")
        secondItem.setDangerousGoodsFlag("1")
        secondItem.setDescription("Description")
        secondItem.setHandlingUnit("BLUE-PALLETS")
        secondItem.setQuantity(5)
        secondItem.setLength(new DimensionProperties(1.0, "m"))
        secondItem.setWidth(new DimensionProperties(1.0, "m"))
        secondItem.setHeight(new DimensionProperties(1.0, "m"))
        secondItem.setWeight(new UnitTypeWeight(200.0, "kg"))
        secondItem.setActualVolume(new UnitTypeVolume(1.0, "cbm"))
        secondItem.setActualWeight(new UnitTypeWeight(200.0, "kg"))
        secondItem.setLdm(25)

        ArrayList <ItemDetails> items = new ArrayList<ItemDetails>(){{
            add(firstItem)
            add(secondItem)
        }}

        orders.setOrderCargoDetails(new CargoDetails(items))

        ArrayList<RefType> refTypeArrayList = new ArrayList<RefType>(){{
            add( new RefType("z09", "TEST REFERENCE"));
            add( new RefType("ADE", "A12345"))
        }}

        orders.setManageReferences(new ManageReferences(refTypeArrayList))

        Remark remark = new Remark("Lorem Ipsum")
        ArrayList<Remark> remarks = new ArrayList<Remark>(Arrays.asList(remark))
        orders.setManageRemarks(new ManageRemarks(remarks))

        tripOrderDetails.setOrders([orders])

        trip.setTripOrderDetails(tripOrderDetails)

    }

    @After
    public void tearDown() {
        trip = null
    }

    @Test
    public void testTripObjectToXml() throws JAXBException, FileNotFoundException {
        JAXBContext jaxbContext = JAXBContext.newInstance(TripCreate.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(trip, new File("/tmp/new_trip.xml"))
        marshaller.marshal(trip, System.out)
    }

    @Test
    public void testXmlToTripObject() throws JAXBException, FileNotFoundException {
        File xmlFile = new File("/tmp/new_trip.xml")
        assertTrue(xsdValidatorService.validateXml(xmlFile.text))

        JAXBContext jaxbContext = JAXBContext.newInstance(TripCreate.class)
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
        TripCreate trip = (TripCreate) unmarshaller.unmarshal(xmlFile)

        assertEquals(trip.getAction(), "CREATE")
        assertEquals(trip.getTripDetails().getTripType(), "REGULAR")
    }
}
