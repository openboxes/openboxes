package org.pih.warehouse.integration.xml.pod;

import org.junit.After;
import org.junit.Before;
import org.junit.Test
import org.pih.warehouse.integration.XsdValidatorService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DocumentUploadTest {
    private DocumentUpload documentUpload;
    private final String DOCUMENT_UPLOAD_XSD = "xsd/DocumentUpload.xsd"
    XsdValidatorService xsdValidatorService = new XsdValidatorService()

    @Before
    public void setUp() {
        Header header = new Header("V1","20201008154348_SG3009200527","ETRUCKNOW")
        documentUpload = new DocumentUpload()
        documentUpload.setHeader( header )
        documentUpload.setAction("UPLOAD")
        documentUpload.setDocumentType("POD")
        documentUpload.setOrderId("10088987")
        UploadDetails uploadDetails = new UploadDetails()
        uploadDetails.setDocumentName("eTN Image")
        uploadDetails.setDocumentFile("/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAoHCBYWFRgVFhYYGRgZGhgYGBgaGBkZGRgYGBgZGhgYHBgcIS4lHB4rIRgYJjgmKy80NTU1GiQ7QDs0Py40NTEBDAwMEA8QHhISHzQrJSs0NDUxNDY0NDE0NDQ0NDQ0NDQ0NDQ0MTQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NP/AABEIAKIBNwMBIgACEQEDEQH/")
        documentUpload.setUploadDetails(uploadDetails)
    }

    @After
    public void tearDown() {  documentUpload = null;}

    @Test
    public void testObjectToXml() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentUpload.class)
        Marshaller marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(documentUpload, new File("/tmp/document_upload.xml"))
        marshaller.marshal(documentUpload, System.out)
    }

    @Test
    public void testXmlToObject() throws JAXBException {
        File xmlFile = new File("/tmp/document_upload.xml")

        assertTrue(xsdValidatorService.validateXml(xmlFile.text))

        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentUpload.class)
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
        DocumentUpload documentUpload = (DocumentUpload) unmarshaller.unmarshal(xmlFile)

        assertEquals( documentUpload.getAction(), "UPLOAD" )
        assertTrue ( documentUpload.getOrderId().contains("10088987"))
    }
}
