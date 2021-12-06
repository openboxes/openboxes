package org.pih.warehouse.integration.xml

import grails.test.GrailsUnitTestCase
import groovy.util.slurpersupport.GPathResult
import org.pih.warehouse.integration.XsdValidatorService

class XsdValidatorServiceUnitTests extends GrailsUnitTestCase{
    XsdValidatorService xsdValidatorService = new XsdValidatorService()

    def acceptanceStatusXml = '''<?xml version="1.0" encoding="UTF-8"?><AcceptanceStatus xmlns="http://www.etrucknow.com/edi/carrier/acceptance_status/v1">
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
    String noNamespaceXml = '''<?xml version="1.0" encoding="UTF-8"?>
        <AcceptanceStatus xmlns="">
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
        assertEquals(xsdValidatorService.getXsdType(acceptanceStatusXml), "AcceptanceStatus")
    }

    void testAcceptanceStatusValidator() {
        assertTrue(xsdValidatorService.validateXml(acceptanceStatusXml))
    }

    void testResolveNameSpace() {
        InputStream inputStream = new ByteArrayInputStream(noNamespaceXml.bytes)
        String namespaceXml = xsdValidatorService.resolveEmptyNamespace(inputStream)
        GPathResult acceptanceStatus = new XmlSlurper(false, true).parseText(namespaceXml)
        String namespace = xsdValidatorService.namespaceMap.get(acceptanceStatus.name())

        assertNotNull namespace
        assertTrue namespaceXml.contains(namespace)
        assertEquals namespaceXml, acceptanceStatusXml
    }
}
