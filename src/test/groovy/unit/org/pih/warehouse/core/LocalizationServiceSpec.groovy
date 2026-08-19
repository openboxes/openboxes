package unit.org.pih.warehouse.core

import grails.testing.services.ServiceUnitTest
import org.grails.core.io.ResourceLocator
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.LocalizationService

/**
 * Test how LocalizationService resolves the message bundle to use for a given locale.
 */
@Unroll
class LocalizationServiceSpec extends Specification implements ServiceUnitTest<LocalizationService> {

    ResourceLocator grailsResourceLocatorStub

    void setup() {
        grailsResourceLocatorStub = Stub(ResourceLocator)
        service.grailsResourceLocator = grailsResourceLocatorStub
    }

    /**
     * Make the given bundles the only ones that can be resolved from the classpath. Anything else resolves to null,
     * which is what the Grails resource locator does for a URI that doesn't exist.
     */
    void mockBundles(Map<String, String> bundlesByFilename) {
        bundlesByFilename.each { String filename, String contents ->
            grailsResourceLocatorStub.findResourceForURI("classpath:${filename}") >> new ByteArrayResource(contents.bytes)
        }
        grailsResourceLocatorStub.findResourceForURI(_) >> null
    }

    void 'getMessagesProperties should use the bundle of the given locale when it exists'() {
        given:
        mockBundles([
                'messages.properties'      : 'greeting=hello',
                'messages_es.properties'   : 'greeting=hola',
                'messages_es_MX.properties': 'greeting=que onda',
        ])

        expect:
        service.getMessagesProperties(new Locale('es', 'MX')).greeting == 'que onda'
    }

    void 'getMessagesProperties should fall back to the language bundle when there is no country specific bundle'() {
        given:
        mockBundles([
                'messages.properties'   : 'greeting=hello',
                'messages_fr.properties': 'greeting=bonjour',
        ])

        expect:
        service.getMessagesProperties(new Locale('fr', 'FR')).greeting == 'bonjour'
    }

    void 'getMessagesProperties should fall back to the default bundle for locale #locale when no bundle exists'() {
        given: 'the default bundle is the only one we ship'
        mockBundles(['messages.properties': 'greeting=hello'])

        expect: 'we get the default messages instead of failing'
        service.getMessagesProperties(locale).greeting == 'hello'

        where:
        locale << [
                new Locale('en'),
                new Locale('en', 'US'),
                new Locale('pl', 'PL'),
                new Locale('null'),  // a locale parsed from the literal string "null"
                new Locale(''),
                null,
        ]
    }

    void 'getMessagesProperties should return no messages when no bundle at all can be resolved'() {
        given:
        mockBundles([:])

        expect:
        service.getMessagesProperties(new Locale('fr', 'FR')).isEmpty()
    }

    void 'getMessagesProperties should skip a bundle that resolves to a resource that does not exist'() {
        given:
        Resource missingResource = new ClassPathResource('messages_de_DE.properties')
        grailsResourceLocatorStub.findResourceForURI('classpath:messages_de_DE.properties') >> missingResource
        grailsResourceLocatorStub.findResourceForURI('classpath:messages_de.properties') >> new ByteArrayResource('greeting=guten tag'.bytes)
        grailsResourceLocatorStub.findResourceForURI(_) >> null

        expect:
        !missingResource.exists()
        service.getMessagesProperties(new Locale('de', 'DE')).greeting == 'guten tag'
    }
}
