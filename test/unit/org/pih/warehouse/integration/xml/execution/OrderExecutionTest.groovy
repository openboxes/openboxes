package org.pih.warehouse.integration.xml.execution

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.integration.XsdValidatorService

import java.text.SimpleDateFormat

import static org.junit.Assert.*
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

public class OrderExecutionTest {

    private Execution orderExecution
    private final String ACCEPTANCE_STATUS_XSD = "xsd/Execution.xsd"
    XsdValidatorService xsdValidatorService = new XsdValidatorService()
    def grailsApplication = new DefaultGrailsApplication()

    @Before
    public void setUp() {

        grailsApplication.config.openboxes.integration.defaultDateFormat = "yyyy-MM-dd'T'hh:mm:ssXXX"

        Header header = new Header("V1", "20201008154348_SG3009200527","ETRUCKNOW");
        ExecutionStatus executionStatus = new ExecutionStatus(
                "1003343",
                "PICKUP",
                "2020-10-08T11:12:23+01:00",
                new GeoData(50.957263f, 4.593913f)
        )
        ExecutionStatus secondStatus = new ExecutionStatus()
        secondStatus.setOrderId("55555555")
        secondStatus.setStatus("IN_TRANSIT")
        secondStatus.setDateTime("2020-10-08T14:56:12+01:00")

        ExecutionStatus thirdStatus = new ExecutionStatus()
        thirdStatus.setOrderId("55555555")
        thirdStatus.setStatus("DELIVERY")
        thirdStatus.setDateTime("2020-10-08T18:43:48+01:00")

        List <ExecutionStatus> list = new ArrayList()
        list.add(executionStatus)
        list.add(secondStatus)
        list.add(thirdStatus)

        orderExecution = new Execution("10032021", header, list)
    }

    @After
    public void tearDown() {
        orderExecution = null
    }

    @Test
    public void testObjectToXml() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Execution.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(orderExecution, new File("/tmp/order_execution.xml"))
        marshaller.marshal(orderExecution, System.out)
    }

    @Test
    public void testXmlToObject() throws  JAXBException {
        File xmlFile = new File("/tmp/order_execution.xml")

        //assertTrue(xsdValidatorService.validateXml(xmlFile.text))

        JAXBContext jaxbContext = JAXBContext.newInstance(Execution.class)
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
        Execution orderExecution = (Execution) unmarshaller.unmarshal(xmlFile)

        assertEquals(orderExecution.tripID, "10032021")
        assertEquals(orderExecution.executionStatus.first().status, "PICKUP")
        assertEquals(orderExecution.executionStatus.first().dateTime.toString(), "2020-10-08T11:12:23+01:00")

        assertEquals(orderExecution.executionStatus.last().status, "DELIVERY")
        assertEquals(orderExecution.executionStatus.last().dateTime.toString(), "2020-10-08T18:43:48+01:00")

    }

    @Test
    public void parse24HourDate() {
        println grailsApplication.config.openboxes.integration.defaultDateFormat
        SimpleDateFormat dateFormatter = new SimpleDateFormat(grailsApplication.config.openboxes.integration.defaultDateFormat)
        Date eventDate = dateFormatter.parse("2020-10-08T14:56:12+01:00")

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"))
        calendar.setTime(eventDate)
        println "eventDate " + dateFormatter.format(eventDate)

        assertEquals(13, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(56, calendar.get(Calendar.MINUTE))
        assertEquals(12, calendar.get(Calendar.SECOND))
        assertEquals(8, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(Calendar.PM, calendar.get(Calendar.AM_PM))
    }

    @Test
    public void parseZuluDate() {
        println grailsApplication.config.openboxes.integration.defaultDateFormat
        SimpleDateFormat dateFormatter = new SimpleDateFormat(grailsApplication.config.openboxes.integration.defaultDateFormat)
        Date eventDate = dateFormatter.parse("2020-10-08T23:56:12Z")

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"))
        calendar.setTime(eventDate)
        println "eventDate " + dateFormatter.format(eventDate)

        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(56, calendar.get(Calendar.MINUTE))
        assertEquals(12, calendar.get(Calendar.SECOND))
        assertEquals(8, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(Calendar.PM, calendar.get(Calendar.AM_PM))
    }

}
