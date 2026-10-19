package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.core.Document
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The product details for the app link images and documents only by http or https URLs.
 */
class MobileProductApiControllerLinksSpec extends Specification implements ControllerUnitTest<MobileProductApiController>, DataTest {

    void setupSpec() {
        mockDomains(Document)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
    }

    private static Document document(String filename, String fileUri) {
        return new Document(name: filename, filename: filename, contentType: 'image/png', fileUri: fileUri,
                fileContents: fileUri ? null : 'png'.bytes).save(validate: false, flush: true)
    }

    void "documentLinks keeps http and https links and the download link of a stored file"() {
        given:
        Document remote = document('remote.png', 'https://images.example.org/remote.png')
        Document plain = document('plain.png', 'http://images.example.org/plain.png')
        Document stored = document('stored.png', null)

        when:
        List<Map> links = MobileProductApiController.documentLinks([remote, plain, stored])

        then:
        links*.id == [remote.id, plain.id, stored.id]
        links[0] == [id: remote.id, name: 'remote.png', contentType: 'image/png', uri: 'https://images.example.org/remote.png']
        links[1].uri == 'http://images.example.org/plain.png'
        links[2].uri.startsWith('http')
        links[2].uri.contains("/document/download/${stored.id}")
    }

    @Unroll
    void "documentLinks leaves out a document linked as #fileUri"() {
        given:
        Document kept = document('kept.png', 'https://images.example.org/kept.png')
        Document dropped = document('dropped.png', fileUri)

        expect:
        MobileProductApiController.documentLinks([dropped, kept])*.id == [kept.id]

        where:
        fileUri << [
                'javascript:alert(1)',
                'JavaScript:alert(1)',
                'data:image/png;base64,AAAA',
                'file:///etc/hosts',
                'ftp://images.example.org/x.png',
                'intent://scan/#Intent;scheme=zxing;end',
                'https:///no-host.png',
                '//images.example.org/x.png',
        ]
    }

    void "documentLinks keeps a link to a host name with an underscore"() {
        given:
        Document internal = document('internal.png', 'http://ob_app:8080/openboxes/document/download/1')

        expect:
        MobileProductApiController.documentLinks([internal])*.id == [internal.id]
    }

    void "documentLinks of no documents is nothing"() {
        expect:
        MobileProductApiController.documentLinks(null) == null
        MobileProductApiController.documentLinks([]) == []
    }
}
