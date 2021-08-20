package org.pih.warehouse.xml.pod;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DocumentUploadTest {
    private DocumentUpload documentUpload;

    @Before
    public void setUp() {
        Header header = new Header("V1","20201008154348_SG3009200527","ETRUCKNOW");
        documentUpload = new DocumentUpload();
        documentUpload.setHeader( header );
        documentUpload.setAction("UPLOAD");
        documentUpload.setDocumentType("POD");
        UploadDetails uploadDetails = new UploadDetails();
        ArrayList <String> orderIds = new ArrayList<String>();
        orderIds.add("10088987");
        orderIds.add("10054782");
        uploadDetails.setOrderId(orderIds);
        Document document = new Document();
        document.setDocumentFile("/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAoHCBYWFRgVFhYYGRgZGhgYGBgaGBkZGRgYGBgZGhgYHBgcIS4lHB4rIRgYJjgmKy80NTU1GiQ7QDs0Py40NTEBDAwMEA8QHhISHzQrJSs0NDUxNDY0NDE0NDQ0NDQ0NDQ0NDQ0MTQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NP/AABEIAKIBNwMBIgACEQEDEQH/");
        document.setDocumentName("eTN Image");
        SourceType sourceType = new SourceType();
        sourceType.setDocument(document);
        uploadDetails.setSourceType(sourceType);
        documentUpload.setUploadDetails(uploadDetails);
    }

    @After
    public void tearDown() {  documentUpload = null;}

    @Test
    public void testObjectToXml() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentUpload.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(documentUpload, new File("/tmp/document_upload.xml"));
        marshaller.marshal(documentUpload, System.out);
    }

    @Test
    public void testXmlToObject() throws JAXBException {
        File xmlFile = new File("/tmp/document_upload.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DocumentUpload.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        DocumentUpload documentUpload = (DocumentUpload) unmarshaller.unmarshal(xmlFile);

        assertEquals( documentUpload.getAction(), "UPLOAD" );
        assertTrue ( documentUpload.getUploadDetails().getOrderId().contains("10088987"));
    }
}
