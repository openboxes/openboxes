package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import grails.testing.web.controllers.ControllerUnitTest
import org.junit.Ignore

// import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.Document
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class DocumentTests extends Specification implements DomainUnitTest<Document> {



    @Test
    void isImage_shouldReturnTrueWhenDocumentIsAnImage() {
        when:
        def document = new Document(filename:"image.jpg", contentType:"image/jpeg")
        mockDomain(Document, [document])
        then:
        assert document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenDocumentIsNotAnImage() {
        when:
        def document = new Document(filename:"document.pdf", contentType:"application/pdf")
        mockDomain(Document, [document])
        then:
        assert !document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenContentTypeIsEmpty() {
        when:
        def document = new Document(filename:"document.unknown", contentType:"")
        mockDomain(Document, [document])
        then:
        assert !document.isImage()
    }

    @Test
    void isImage_shouldReturnFalseWhenContentTypeIsNull() {
        when:
        def document = new Document(filename:"document.unknown", contentType:null)
        mockDomain(Document, [document])
        then:
        assert !document.isImage()
    }


    @Test
    void getSize_shouldReturnLengthWhenNotEmpty() {
        when:
        def document = new Document(filename:"document.unknown", fileContents: "testabc".bytes)
        mockDomain(Document, [document])
        then:
        assert document?.size == 7
    }

    @Test
    void getSize_shouldReturnZeroWhenEmpty() {
        when:
        def document = new Document(filename:"document.unknown", fileContents: "")
        mockDomain(Document, [document])
        then:
        assert document?.size == 0
    }

    @Test
    void getSize_shouldReturnZeroWhenNull() {
        when:
        def document = new Document(filename:"document.unknown", fileContents: null)
        mockDomain(Document, [document])
        then:
        assert document?.size == 0
    }

}
