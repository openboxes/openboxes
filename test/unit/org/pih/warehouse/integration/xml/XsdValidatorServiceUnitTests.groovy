package org.pih.warehouse.integration.xml

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.integration.XsdValidatorService

class XsdValidatorServiceUnitTests extends GrailsUnitTestCase{
    XsdValidatorService xsdValidatorService = new XsdValidatorService()

    def acceptanceStatusXML = '''<?xml version="1.0" encoding="UTF-8"?>
        <AcceptanceStatus xmlns="http://www.etrucknow.com/edi/carrier/acceptance_status/v1">
          <Header>
            <Version>V1</Version>
            <SequenceNumber>20201008154348_SG3009200527</SequenceNumber>
            <SourceApp>ETRUCKNOW</SourceApp>
          </Header>
          <Action>ACCEPT</Action>
          <TripDetails>
            <TripID>10032021</TripID>
            <Carrier>
              <ID>MYSH01505</ID>
              <Name>RT</Name>
            </Carrier>
          </TripDetails>
          <TripOrderDetails>
            <OrderID>10032021-A</OrderID>
            <OrderID>10032021-B</OrderID>
          </TripOrderDetails>
        </AcceptanceStatus>'''

    protected void setUp() {
        super.setUp()
    }

    void testAcceptanceStatusHeader() {
        assertEquals(xsdValidatorService.getXsdType(acceptanceStatusXML), "AcceptanceStatus")
    }

    void testAcceptanceStatusValidator() {
        assertTrue(xsdValidatorService.validateXml(acceptanceStatusXML))
    }
}
