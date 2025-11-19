package unit.org.pih.warehouse.product

import grails.testing.gorm.DomainUnitTest
import java.time.Instant
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode

class ProductDisplayNamesSpec extends Specification implements DomainUnitTest<Product> {

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.currentLocale >> Locale.ENGLISH
        LocalizationUtil.supportedLocales >> [Locale.ENGLISH, Locale.FRENCH, Locale.ITALIAN]
    }

    void 'getDisplayNames returns all defined synonyms of a product'() {
        given: 'a list of synonyms for the product'
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'en-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.ENGLISH),
                new Synonym(name: 'fr-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH),
        ]

        expect: 'all defined synonyms to be returned, including a default based on currentLocale'
        assert domain.getDisplayNames() == [
                'default': 'en-name',
                'en': 'en-name',
                'fr': 'fr-name',
                // Note that even though Italian is a supported locale, it's not present because there's no synonym
        ]

        and:
        assert domain.getDisplayNameOrDefaultName() == 'en-name'
    }

    void 'getDisplayName returns the default synonyms of a product'() {
        given: 'a list of synonyms for the product'
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'en-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.ENGLISH),
                new Synonym(name: 'fr-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH),
        ]

        expect: 'the default synonym can be fetched'
        assert domain.getDisplayName() == 'en-name'
    }

    void 'getDisplayName returns the default synonyms of a product'() {
        given: 'a list of synonyms for the product'
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'en-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.ENGLISH),
                new Synonym(name: 'fr-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH),
        ]

        expect: 'a specific locale synonym can be fetched'
        assert domain.getDisplayName(Locale.FRENCH) == 'fr-name'
    }

    void 'getDisplayNames returns the default synonym of a product'() {
        given: 'a list of synonyms for the product'
        domain.name = 'product-name'
        domain.synonyms = [
                new Synonym(name: 'en-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.ENGLISH),
                new Synonym(name: 'fr-name', synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH),
        ]

        expect:
        assert domain.getDisplayNameOrDefaultName() == 'en-name'
    }

    void 'getDisplayNameOrDefaultName returns product name if there are no synonyms'() {
        given: 'a product with no synonyms'
        domain.name = 'product-name'
        domain.synonyms = []

        expect:
        assert domain.getDisplayNameOrDefaultName() == 'product-name'
    }
}
