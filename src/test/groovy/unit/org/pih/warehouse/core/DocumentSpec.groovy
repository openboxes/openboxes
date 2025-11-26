package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest

import spock.lang.Specification

import org.pih.warehouse.core.Document

class DocumentSpec extends Specification implements DomainUnitTest<Document> {

    void isImage_shouldReturnTrueWhenDocumentIsAnImage() {
        when:
        Document document = new Document(filename:"image.jpg", contentType:"image/jpeg")

        then:
        assert document.isImage()
    }

    void isImage_shouldReturnFalseWhenDocumentIsNotAnImage() {
        when:
        def document = new Document(filename:"document.pdf", contentType:"application/pdf")

        then:
        assert !document.isImage()
    }

    void isImage_shouldReturnFalseWhenContentTypeIsEmpty() {
        when:
        Document document = new Document(filename:"document.unknown", contentType:"")

        then:
        assert !document.isImage()
    }

    void isImage_shouldReturnFalseWhenContentTypeIsNull() {
        when:
        Document document = new Document(filename:"document.unknown", contentType:null)

        then:
        assert !document.isImage()
    }

    void getSize_shouldReturnLengthWhenNotEmpty() {
        when:
        Document document = new Document(filename:"document.unknown", fileContents: "testabc".bytes)

        then:
        assert document?.size == 7
    }

    void getSize_shouldReturnZeroWhenEmpty() {
        when:
        Document document = new Document(filename:"document.unknown", fileContents: "".bytes)

        then:
        assert document?.size == 0
    }

    void getSize_shouldReturnZeroWhenNull() {
        when:
        Document document = new Document(filename:"document.unknown", fileContents: null)

        then:
        assert document?.size == 0
    }
}
