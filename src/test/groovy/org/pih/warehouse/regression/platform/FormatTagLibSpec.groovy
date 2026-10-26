package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.shipping.ShipmentStatusCode

class FormatTagLibSpec extends Specification implements TagLibUnitTest<FormatTagLib> {

    Date marchFifth = new GregorianCalendar(2026, Calendar.MARCH, 5, 14, 30, 15).time

    void setupSpec() {
        mockTagLib(MessageTagLib)
        mockTagLib(org.grails.plugins.web.taglib.ValidationTagLib)
    }

    void setup() {
        config.openboxes.locale.defaultLocale = 'en'
        config.openboxes.locale.custom.enabled = false
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }
    }

    void 'date formats with the default or a given pattern and ignores a missing date'() {
        expect:
        applyTemplate('<format:date obj="${d}"/>', [d: marchFifth]) == '05/Mar/2026'
        applyTemplate('<format:date obj="${d}" format="yyyy-MM-dd"/>', [d: marchFifth]) == '2026-03-05'
        applyTemplate('<format:date obj="${null}"/>') == ''
    }

    void 'datetime formats in the session timezone when there is one'() {
        expect:
        applyTemplate('<format:datetime obj="${d}"/>', [d: marchFifth]) == '05/Mar/2026 14:30:15'
        applyTemplate('<format:datetime obj="${null}"/>') == ''

        when:
        session.timezone = TimeZone.getTimeZone('UTC')
        Date utcNoon = Date.from(java.time.Instant.parse('2026-03-05T12:00:00Z'))

        then:
        applyTemplate('<format:datetime obj="${d}"/>', [d: utcNoon]) == '05/Mar/2026 12:00:00'
    }

    void 'expirationDate shows month and year, or never for products that do not expire'() {
        given:
        messageSource.addMessage('default.never.label', Locale.ENGLISH, 'Never')

        expect:
        applyTemplate('<format:expirationDate obj="${d}"/>', [d: marchFifth]) == '05/Mar/2026'
        applyTemplate('<format:expirationDate obj="${null}"/>') == 'Never'
    }

    void 'product shows the name localized to the requested, user or default locale'() {
        given:
        def product = [name: 'Bandage <large>|fr:Pansement']

        expect: 'escaped default name'
        applyTemplate('<format:product product="${p}"/>', [p: product]) == 'Bandage &lt;large&gt;'
        applyTemplate('<format:product product="${p}" locale="${l}"/>', [p: product, l: Locale.FRENCH]) == 'Pansement'
        applyTemplate('<format:product product="${p}" locale="${null}"/>', [p: product]) == 'Bandage &lt;large&gt;'
        applyTemplate('<format:product product="${null}"/>') == ''

        when: 'the user prefers French'
        session.user = [locale: Locale.FRENCH]

        then:
        applyTemplate('<format:product product="${p}"/>', [p: product]) == 'Pansement'
    }

    void 'displayName prefers the display name for the locale and can prefix the product code'() {
        given:
        def product = [name: 'Paracetamol 500mg', productCode: 'P1', displayNames: [default: 'Paracetamol', fr: 'Paracetamol FR']]
        def unnamed = [name: 'Gauze & tape', productCode: 'G1', displayNames: [:]]

        expect:
        applyTemplate('<format:displayName product="${p}"/>', [p: product]) == 'Paracetamol'
        applyTemplate('<format:displayName product="${p}" locale="fr"/>', [p: product]) == 'Paracetamol FR'
        applyTemplate('<format:displayName product="${p}" showProductCode="${true}"/>', [p: product]) == 'P1 Paracetamol'
        applyTemplate('<format:displayName product="${p}"/>', [p: unnamed]) == 'Gauze &amp; tape'
        applyTemplate('<format:displayName product="${null}"/>') == ''
    }

    void 'displayName with a tooltip renders the display name template'() {
        given:
        views['/taglib/_productDisplayName.gsp'] =
                '${product.name}|${product.productCode}|${productSupplier.code}|${displayName}|${showProductCode}'
        def product = [name: 'Paracetamol 500mg', productCode: 'P1', displayNames: [default: 'Paracetamol']]

        when:
        String html = applyTemplate('<format:displayName product="${p}" productSupplier="${s}" showTooltip="${true}"/>',
                [p: product, s: [name: 'Source', code: 'S-1']])

        then:
        html == 'Paracetamol 500mg|P1|S-1|Paracetamol|false'
    }

    void 'displayNameWithColor wraps the display name in the product color'() {
        expect:
        applyTemplate('<format:displayNameWithColor product="${p}"/>',
                [p: [name: 'Aspirin', color: '#ff0000', displayNames: [:]]]) ==
                "<span style='color: #ff0000'> Aspirin </span>"
        applyTemplate('<format:displayNameWithColor product="${p}"/>', [p: [name: 'Aspirin', displayNames: [:]]]) ==
                "<span style='color: inherit'> Aspirin </span>"
    }

    void 'category shows the localized category name, optionally shortened'() {
        given:
        def category = [name: 'Medical supplies and consumables for surgery|fr:Fournitures']

        expect:
        applyTemplate('<format:category category="${c}"/>', [c: category]) ==
                'Medical supplies and consumables for surgery'
        applyTemplate('<format:category category="${c}" shorten="${true}"/>', [c: category]) ==
                'Medical supplies and consumables fo...'
        applyTemplate('<format:category category="${c}" shorten="${true}"/>', [c: [name: 'Drugs']]) == 'Drugs'
        applyTemplate('<format:category category="${c}" locale="${l}"/>', [c: category, l: Locale.FRENCH]) ==
                'Fournitures'
        applyTemplate('<format:category category="${null}"/>') == ''
    }

    void 'metadata localizes strings, enums, named objects and other objects'() {
        given:
        messageSource.addMessage('enum.ShipmentStatusCode.PENDING', Locale.ENGLISH, 'Pending')
        messageSource.addMessage('enum.ShipmentStatusCode.PENDING', Locale.FRENCH, 'En attente')

        expect:
        applyTemplate('<format:metadata obj="${o}"/>', [o: 'Box|fr:Boite']) == 'Box'
        applyTemplate('<format:metadata obj="${o}" locale="${l}"/>', [o: 'Box|fr:Boite', l: Locale.FRENCH]) == 'Boite'
        applyTemplate('<format:metadata obj="${o}"/>', [o: ShipmentStatusCode.PENDING]) == 'Pending'
        applyTemplate('<format:metadata obj="${o}" locale="${l}"/>', [o: ShipmentStatusCode.PENDING, l: Locale.FRENCH]) ==
                'En attente'
        applyTemplate('<format:metadata obj="${o}"/>', [o: new Named(name: 'Air <freight>|fr:Avion')]) ==
                'Air &lt;freight&gt;'
        applyTemplate('<format:metadata obj="${o}"/>', [o: 42]) == '42'
        applyTemplate('<format:metadata obj="${null}"/>') == ''
    }

    void 'metadata uses the session locale before the user locale'() {
        given:
        session.locale = Locale.FRENCH
        session.user = [locale: Locale.ENGLISH]

        expect:
        applyTemplate('<format:metadata obj="${o}"/>', [o: 'Box|fr:Boite']) == 'Boite'
    }

    static class Named {
        String name
    }
}
