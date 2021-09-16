package org.pih.warehouse.integration.xml

import groovy.util.slurpersupport.GPathResult
import org.apache.commons.logging.LogFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.util.xml.SimpleSaxErrorHandler
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

public class XmlXsdValidator {

    static final LOG = LogFactory.getLog(this)

    public static void validateXmlSchema(String xsdPath, String xmlContents) {
        StringWriter stringWriter = new StringWriter()
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            File xsdFile = new ClassPathResource(xsdPath)?.file
            Schema schema = factory.newSchema(xsdFile)
            Validator validator = schema.newValidator()
            validator.setErrorHandler(new SimpleSaxErrorHandler(LOG))
            validator.validate(new StreamSource(new StringReader(XmlXsdValidator.sanitizeXmlString(xmlContents))))
        } catch (SAXParseException e) {
            LOG.error("Error occurred while validating XML against XSD (" + xsdPath + "): " + e.getMessage(), e)
            throw e
        } catch (SAXException e) {
            LOG.error("Error occurred while validating XML against XSD (" + xsdPath + "): " + e.getMessage(), e)
            throw e
        }
    }

    public static String sanitizeXmlString(String xmlIn) {
        return xmlIn.replaceAll("(?:>)(\\s*)<", "><")
    }

    public static boolean isStringInXml(String needle, String xmlHaystack) {
        return (sanitizeXmlString(xmlHaystack).indexOf(sanitizeXmlString(needle)) != -1)
    }

    public static GPathResult parseXml(String xmlIn) {
        return new XmlSlurper().parseText(sanitizeXmlString(xmlIn))
    }

}
