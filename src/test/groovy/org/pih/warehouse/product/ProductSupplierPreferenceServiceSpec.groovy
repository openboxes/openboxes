package org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

import org.pih.warehouse.core.PreferenceType

class ProductSupplierPreferenceServiceSpec extends Specification implements ServiceUnitTest<ProductSupplierPreferenceService>, DataTest {

    void setupSpec() {
        mockDomains(ProductSupplierPreference, ProductSupplier)
    }

    void setup() {
        service.productSupplierPreferenceDataService = Mock(ProductSupplierPreferenceDataService)
    }

    void 'save should persist preference and add it to product supplier preferences'() {
        given:
        ProductSupplier productSupplier = new ProductSupplier()
        ProductSupplierPreference preference = buildPreference(productSupplier)

        when:
        ProductSupplierPreference savedPreference = service.save(preference)

        then:
        assert savedPreference.id != null
        assert productSupplier.productSupplierPreferences.contains(savedPreference)
    }

    void 'save should throw validation exception when preference type is missing'() {
        given:
        ProductSupplierPreference preference = new ProductSupplierPreference(productSupplier: new ProductSupplier())

        when:
        service.save(preference)

        then:
        thrown(ValidationException)
    }

    void 'saveOrUpdateBatch should save and return all given preferences'() {
        given:
        List<ProductSupplierPreference> preferences = [
                buildPreference(new ProductSupplier()),
                buildPreference(new ProductSupplier()),
        ]

        when:
        List<ProductSupplierPreference> savedPreferences = service.saveOrUpdateBatch(preferences)

        then:
        assert savedPreferences == preferences
        assert savedPreferences.every { it.id != null }
    }

    void 'saveOrUpdateBatch should return empty list when no preferences are given'() {
        expect:
        assert service.saveOrUpdateBatch([]) == []
    }

    void 'delete should delete preference by id using data service'() {
        when:
        service.delete('1')

        then:
        1 * service.productSupplierPreferenceDataService.delete('1')
    }

    void 'delete should delete every given preference using data service'() {
        given:
        ProductSupplierPreference firstPreference = new ProductSupplierPreference()
        firstPreference.id = '1'
        ProductSupplierPreference secondPreference = new ProductSupplierPreference()
        secondPreference.id = '2'
        Set<ProductSupplierPreference> preferences = [firstPreference, secondPreference] as Set

        when:
        service.delete(preferences)

        then:
        1 * service.productSupplierPreferenceDataService.delete('1')
        1 * service.productSupplierPreferenceDataService.delete('2')
    }

    private ProductSupplierPreference buildPreference(ProductSupplier productSupplier) {
        return new ProductSupplierPreference(productSupplier: productSupplier, preferenceType: new PreferenceType(name: 'Primary'))
    }
}
