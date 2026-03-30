package org.pih.warehouse.core

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

    void "isAllowedFile should accept allowed extensions and content types"() {
        expect:
        Document.isAllowedFile(filename, contentType) == expected

        where:
        filename        | contentType                                                                  | expected
        "photo.jpg"     | "image/jpeg"                                                                 | true
        "photo.jpeg"    | "image/jpeg"                                                                 | true
        "image.png"     | "image/png"                                                                  | true
        "image.gif"     | "image/gif"                                                                  | true
        "report.pdf"    | "application/pdf"                                                            | true
        "data.csv"      | "text/csv"                                                                   | true
        "sheet.xls"     | "application/vnd.ms-excel"                                                   | true
        "sheet.xlsx"    | "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"           | true
        "notes.txt"     | "text/plain"                                                                   | true
        "doc.docx"      | "application/vnd.openxmlformats-officedocument.wordprocessingml.document"      | true
        "icon.svg"      | "image/svg+xml"                                                                | true
        "photo.webp"    | "image/webp"                                                                   | true
        "PHOTO.JPG"     | "image/jpeg"                                                                   | true
    }

    void "isAllowedFile should reject disallowed extensions"() {
        expect:
        !Document.isAllowedFile(filename, contentType)

        where:
        filename        | contentType
        "virus.exe"     | "application/x-msdownload"
        "script.sh"     | "application/x-sh"
        "page.html"     | "text/html"
        "archive.zip"   | "application/zip"
    }

    void "isAllowedFile should reject mismatched extension and content type"() {
        expect:
        !Document.isAllowedFile(filename, contentType)

        where:
        filename        | contentType
        "fake.pdf"      | "application/x-msdownload"
        "fake.exe"      | "application/pdf"
        "report.pdf"    | "text/csv"
        "data.csv"      | "application/pdf"
        "photo.jpg"     | "application/pdf"
    }

    void "isAllowedFile should reject null or missing values"() {
        expect:
        !Document.isAllowedFile(filename, contentType)

        where:
        filename   | contentType
        null       | "application/pdf"
        "test.pdf" | null
        null       | null
    }

    void "isAllowedFile should reject content that contradicts the claimed type"() {
        given:
        // GIF magic bytes (GIF89a)
        byte[] gifBytes = [0x47, 0x49, 0x46, 0x38, 0x39, 0x61] as byte[]

        expect:
        // claimed jpeg, but bytes are GIF
        !Document.isAllowedFile("photo.jpg", "image/jpeg", new ByteArrayInputStream(gifBytes))
    }

    void "isAllowedFile should accept content that matches the claimed type"() {
        given:
        // GIF magic bytes (GIF89a)
        byte[] gifBytes = [0x47, 0x49, 0x46, 0x38, 0x39, 0x61] as byte[]

        expect:
        Document.isAllowedFile("image.gif", "image/gif", new ByteArrayInputStream(gifBytes))
    }

    void "isAllowedFile should accept when content sniffing returns null"() {
        given:
        // PDF magic bytes -- guessContentTypeFromStream returns null for these
        byte[] pdfBytes = "%PDF-1.4".bytes

        expect:
        Document.isAllowedFile("report.pdf", "application/pdf", new ByteArrayInputStream(pdfBytes))
    }
}
