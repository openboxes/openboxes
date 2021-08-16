package org.pih.warehouse.xml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class StatusTest {
    private TripExecution tripExecution;

    @Before
    public void setUp() {
        Header header = new Header("V1", "eTrucknow","eTrucknow","20201008154348_SG3009200527","ETRUCKNOW");
        ExecutionStatus executionStatus = new ExecutionStatus();
        executionStatus.setStatus("PICKUP");
        executionStatus.setOrderId("1003343");
        executionStatus.setDateTime("2020-10-07T15:43:48+01:00");
        ExecutionStatus secondStatus = new ExecutionStatus();
        secondStatus.setOrderId("55555555");
        secondStatus.setStatus("DELIVERY");
        secondStatus.setDateTime("2020-10-08T15:43:48+01:00");

        List <ExecutionStatus> list = new ArrayList<>();
        list.add(executionStatus);
        list.add(secondStatus);

        tripExecution = new TripExecution("10032021", header, list);
    }

    @After
    public void tearDown() {
        tripExecution = null;
    }

    @Test
    public void testObjectToXml() throws JAXBException, FileNotFoundException {
        JAXBContext jaxbContext = JAXBContext.newInstance(TripExecution.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(tripExecution, new File("./uploads/new_status.xml"));
        marshaller.marshal(tripExecution, System.out);
    }

    @Test
    public void testXmlToObject() throws  JAXBException, FileNotFoundException {
        File xmlFile = new File("./uploads/new_status.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TripExecution.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        TripExecution tripExecution = (TripExecution) unmarshaller.unmarshal(xmlFile);

        assertEquals( tripExecution.getTripID() , "10032021");
        assertEquals( tripExecution.getExecutionStatus().get(0).getStatus() , "PICKUP");
    }


}
