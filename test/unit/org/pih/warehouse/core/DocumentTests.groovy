package org.pih.warehouse.core

import grails.test.GrailsUnitTestCase
import org.junit.Test

class DocumentTests extends GrailsUnitTestCase {

    @Test
    void test_isImage_shouldReturnTrueWhenDocumentIsAnImage() {
        def document = new Document(filename:"image.jpg", contentType:"image/jpeg")
        mockDomain(Document, [document])
        assert document.isImage()
    }

    @Test
    void test_isImage_shouldReturnFalseWhenDocumentIsNotAnImage() {
        def document = new Document(filename:"document.pdf", contentType:"application/pdf")
        mockDomain(Document, [document])
        assert !document.isImage()
    }

    @Test
    void test_isImage_shouldReturnFalseWhenContentTypeIsEmpty() {
        def document = new Document(filename:"document.unknown", contentType:"")
        mockDomain(Document, [document])
        assert !document.isImage()
    }

    @Test
    void test_isImage_shouldReturnFalseWhenContentTypeIsNull() {
        def document = new Document(filename:"document.unknown", contentType:null)
        mockDomain(Document, [document])
        assert !document.isImage()
    }


    @Test // FIXME
    void getSize_shouldReturnLengthWhenNotEmpty() {
        def document = new Document(filename:"document.unknown", fileContents: "testabc")
        mockDomain(Document, [document])
        assertEquals document?.size, 7
    }

    @Test
    void test_getSize_shouldReturnZeroWhenEmpty() {
        def document = new Document(filename:"document.unknown", fileContents: "")
        mockDomain(Document, [document])
        assertEquals document?.size, 0
    }

    @Test
    void test_getSize_shouldReturnZeroWhenNull() {
        def document = new Document(filename:"document.unknown", fileContents: null)
        mockDomain(Document, [document])
        assertEquals document?.size, 0


    }

}
