package org.pih.warehouse.core

import org.junit.Ignore

// import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.Document

@Ignore
class DocumentTests {

    @Test
    void isImage_shouldReturnTrueWhenDocumentIsAnImage() {
        def document = new Document(filename:"image.jpg", contentType:"image/jpeg")
        mockDomain(Document, [document])
        assert document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenDocumentIsNotAnImage() {
        def document = new Document(filename:"document.pdf", contentType:"application/pdf")
        mockDomain(Document, [document])
        assert !document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenContentTypeIsEmpty() {
        def document = new Document(filename:"document.unknown", contentType:"")
        mockDomain(Document, [document])
        assert !document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenContentTypeIsNull() {
        def document = new Document(filename:"document.unknown", contentType:null)
        mockDomain(Document, [document])
        assert !document.isImage()
    }


    @Test
    void getSize_shouldReturnLengthWhenNotEmpty() {
        def document = new Document(filename:"document.unknown", fileContents: "testabc")
        mockDomain(Document, [document])
        assertEquals document?.size, 7
    }

    @Test
    void getSize_shouldReturnZeroWhenEmpty() {
        def document = new Document(filename:"document.unknown", fileContents: "")
        mockDomain(Document, [document])
        assertEquals document?.size, 0
    }

    @Test
    void getSize_shouldReturnZeroWhenNull() {
        def document = new Document(filename:"document.unknown", fileContents: null)
        mockDomain(Document, [document])
        assertEquals document?.size, 0


    }

}
