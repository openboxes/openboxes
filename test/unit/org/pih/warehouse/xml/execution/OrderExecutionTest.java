package org.pih.warehouse.xml.execution;

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

public class OrderExecutionTest {
    private Execution orderExecution;

    @Before
    public void setUp() {
        Header header = new Header("V1", "20201008154348_SG3009200527","ETRUCKNOW");
        ExecutionStatus executionStatus = new ExecutionStatus(
                "1003343",
                "PICKUP",
                "2020-10-07T15:43:48+01:00",
                new GeoData(50.957263f, 4.593913f)
        );
        ExecutionStatus secondStatus = new ExecutionStatus();
        secondStatus.setOrderId("55555555");
        secondStatus.setStatus("PICKUP");
        secondStatus.setDateTime("2020-10-08T15:43:48+01:00");

        ExecutionStatus thirdStatus = new ExecutionStatus();
        thirdStatus.setOrderId("55555555");
        thirdStatus.setStatus("PICKUP");
        thirdStatus.setDateTime("2020-10-08T15:43:48+01:00");

        List <ExecutionStatus> list = new ArrayList<>();
        list.add(executionStatus);
        list.add(secondStatus);
        list.add(thirdStatus);

        orderExecution = new Execution("10032021", header, list);
    }

    @After
    public void tearDown() {
        orderExecution = null;
    }

    @Test
    public void testObjectToXml() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Execution.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(orderExecution, new File("/tmp/order_execution.xml"));
        marshaller.marshal(orderExecution, System.out);
    }

    @Test
    public void testXmlToObject() throws  JAXBException {
        File xmlFile = new File("/tmp/order_execution.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Execution.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Execution orderExecution = (Execution) unmarshaller.unmarshal(xmlFile);

        assertEquals( orderExecution.getTripID() , "10032021");
        assertEquals( orderExecution.getExecutionStatus().get(0).getStatus() , "PICKUP");
    }
}
