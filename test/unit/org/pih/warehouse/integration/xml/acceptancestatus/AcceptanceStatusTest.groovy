package org.pih.warehouse.integration.xml.acceptancestatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test
import org.pih.warehouse.integration.xml.XmlXsdValidator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert
import static org.junit.Assert.assertEquals

public class AcceptanceStatusTest {
    private AcceptanceStatus acceptanceStatus;
    // TODO: need to figure out a place for the XSDs
    private final String ACCEPTANCE_STATUS_XSD = "xsd/AcceptanceStatus.xsd"

    @Before
    public void setUp() {
        Header header = new Header("V1", "eTrucknow","eTrucknow","20201008154348_SG3009200527","ETRUCKNOW");
        Carrier carrier = new Carrier();
        carrier.setId("MYSH01505");
        carrier.setName("RT");
        TripDetails tripDetails = new TripDetails();
        tripDetails.setTripId("TripID");
        tripDetails.setCarrier(carrier);
        TripOrderDetails tripOrderDetails = new TripOrderDetails();
        List<String> orderList = new ArrayList();
        orderList.add("10032021-A");
        orderList.add("10032021-B");
        tripOrderDetails.setOrderId(orderList);
        acceptanceStatus = new AcceptanceStatus( header, "ACCEPT", tripDetails, tripOrderDetails);
    }

    @After
    public void tearDown() { acceptanceStatus = null;}

    @Test
    public void testAcceptanceStatusToXml() throws JAXBException, FileNotFoundException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AcceptanceStatus.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(acceptanceStatus, new File("/tmp/acceptance_status.xml"));
        marshaller.marshal(acceptanceStatus, System.out);
    }

    @Test
    public void testXmlToAcceptanceStatus() throws JAXBException, FileNotFoundException {
        File xmlFile = new File("/tmp/acceptance_status.xml");
        String xmlFileContents = xmlFile.text

        JAXBContext jaxbContext = JAXBContext.newInstance(AcceptanceStatus.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AcceptanceStatus acceptanceStatus = (AcceptanceStatus) unmarshaller.unmarshal(xmlFile);

        assertEquals( acceptanceStatus.getAction() , "ACCEPT");
        assertEquals( acceptanceStatus.getTripDetails().getCarrier().getId() , "MYSH01505");
        try {
            XmlXsdValidator.validateXmlSchema(ACCEPTANCE_STATUS_XSD, xmlFileContents)
        } catch (Exception e) {
            e.printStackTrace()
            Assert.fail("Should not throw exception")
        }
    }
}
