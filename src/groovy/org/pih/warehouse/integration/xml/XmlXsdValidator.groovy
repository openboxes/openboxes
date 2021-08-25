package org.pih.warehouse.integration.xml

import groovy.util.slurpersupport.GPathResult
import org.springframework.util.ResourceUtils
import org.xml.sax.SAXException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

public class XmlXsdValidator {
    public static boolean validateXmlSchema( String xsdPath, String xmlString ) {
        try {

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
           //  File xsd = ResourceUtils.getFile("classpath:" + xsdPath)
            File xsd = ResourceUtils.getFile(xsdPath)
            Schema schema = factory.newSchema(xsd)
            Validator validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(sanitizeXmlString(xmlString))))

        } catch (IOException e) {
            System.out.println("XSD Validator Error (" + xsdPath + "): " + e.getMessage())
            return false
        } catch (SAXException e) {
            System.out.println("SAX Validation Error (" + xsdPath + "): " + e.getMessage())
            return false
        }

        return true
    }

    public static String sanitizeXmlString(String xmlIn) {
        return xmlIn.replaceAll("(?:>)(\\s*)<", "><")
    }

    public static boolean isStringInXml(String needle, String xmlHaystack) {
        return (sanitizeXmlString(xmlHaystack).indexOf(sanitizeXmlString(needle)) != -1)
    }

    public static GPathResult parseXML(String xmlIn) {
        return new XmlSlurper().parseText(sanitizeXmlString(xmlIn))
    }

}
