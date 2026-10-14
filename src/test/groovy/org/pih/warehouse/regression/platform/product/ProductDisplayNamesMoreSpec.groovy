// Companion spec: extra features for upstream's ProductDisplayNamesSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.product

import org.pih.warehouse.product.*
import grails.testing.gorm.DomainUnitTest
import java.time.Instant
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode

class ProductDisplayNamesMoreSpec extends Specification implements DomainUnitTest<Product> {

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.currentLocale >> Locale.ENGLISH
        LocalizationUtil.supportedLocales >> [Locale.ENGLISH, Locale.FRENCH, Locale.ITALIAN]
    }

    void 'getDisplayNameWithLocaleCode appends the display name and the current locale to the product name'() {
        given: 'a product with a display name in the current locale'
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'en-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.ENGLISH),
                new Synonym(name: 'en-alias', synonymTypeCode: SynonymTypeCode.ALTERNATE_NAME, locale: Locale.ENGLISH),
        ]

        expect:
        assert domain.getDisplayNameWithLocaleCode() == 'product-name (EN: en-name)'
        assert domain.getSynonyms(SynonymTypeCode.ALTERNATE_NAME)*.name == ['en-alias']
    }

    void 'getDisplayNameWithLocaleCode returns only the product name if there is no display name'() {
        given: 'a product with a display name in another locale only'
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'fr-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH),
        ]

        expect:
        assert domain.getDisplayNameWithLocaleCode() == 'product-name'
    }
}
