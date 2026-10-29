package org.pih.warehouse.api.spec.product

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductSupplierPreferenceApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.PreferenceTypeTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductSupplierTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

class ProductSupplierPreferenceApiSpec extends ApiSpec {

    @Autowired
    ProductSupplierPreferenceApiWrapper productSupplierPreferenceApiWrapper

    @Shared
    Product supplierProduct

    @Shared
    PreferenceType preferenceType

    @Shared
    ProductSupplier productSupplier

    @Shared
    ProductSupplier productSupplierWithPreference

    @Shared
    ProductSupplierPreference preference

    @Override
    void setupData() {
        supplierProduct = new ProductTestBuilder().randomizedName().category(rootCategory).build(true)
        preferenceType = new PreferenceTypeTestBuilder().build(true)
        productSupplier = new ProductSupplierTestBuilder().product(supplierProduct).build(true)
        productSupplierWithPreference = new ProductSupplierTestBuilder().product(supplierProduct).build(true)
        preference = new ProductSupplierPreference(productSupplier: productSupplierWithPreference, preferenceType: preferenceType)
                .save(failOnError: true)
    }

    @Override
    void cleanupData() {
        ProductSupplierPreference.findAllByPreferenceType(preferenceType)*.delete()
        ProductSupplier.get(productSupplier.id)?.delete()
        ProductSupplier.get(productSupplierWithPreference.id)?.delete()
        preferenceType?.delete()
        supplierProduct?.delete()
    }

    void 'create product supplier preference should succeed when product supplier and preference type are provided'() {
        given:
        ProductSupplierPreference newPreference = new ProductSupplierPreference(
                productSupplier: productSupplier,
                preferenceType: preferenceType,
        )

        when:
        ProductSupplierPreference createdPreference = productSupplierPreferenceApiWrapper.createOK(newPreference)

        then:
        assert createdPreference.id != null
    }

    void 'create product supplier preference should fail when preference type is missing'() {
        given:
        ProductSupplierPreference preferenceWithoutType = new ProductSupplierPreference(productSupplier: productSupplier)

        expect:
        productSupplierPreferenceApiWrapper.create(preferenceWithoutType, HttpStatus.SC_BAD_REQUEST)
    }

    void 'update product supplier preference should succeed and return updated data'() {
        given:
        preference.comments = 'Updated preference'

        when:
        ProductSupplierPreference updatedPreference = productSupplierPreferenceApiWrapper.updateOK(preference)

        then:
        assert updatedPreference.id == preference.id
        assert updatedPreference.comments == 'Updated preference'
    }

    void 'delete product supplier preference should remove the preference'() {
        when:
        productSupplierPreferenceApiWrapper.deleteOK(preference.id)

        then:
        assert ProductSupplierPreference.withNewTransaction { ProductSupplierPreference.get(preference.id) } == null
    }

    void 'create or update batch should save all given preferences'() {
        given:
        ProductSupplierPreference newPreference = new ProductSupplierPreference(
                productSupplier: productSupplier,
                preferenceType: preferenceType,
        )
        preference.comments = 'Updated in batch'

        when:
        List<ProductSupplierPreference> savedPreferences =
                productSupplierPreferenceApiWrapper.createOrUpdateBatchOK([newPreference, preference])

        then:
        assert savedPreferences.size() == 2
        assert savedPreferences.every { it.id != null }
        ProductSupplierPreference updatedPreference = savedPreferences.find { it.id == preference.id }
        assert updatedPreference.comments == 'Updated in batch'
        ProductSupplierPreference createdPreference = savedPreferences.find { it.id != preference.id }
        assert createdPreference.comments == null
    }

    void 'create or update batch should fail when product supplier already has a default preference'() {
        given:
        ProductSupplierPreference secondDefaultPreference = new ProductSupplierPreference(
                productSupplier: productSupplierWithPreference,
                preferenceType: preferenceType,
        )

        expect:
        productSupplierPreferenceApiWrapper.createOrUpdateBatch([secondDefaultPreference], HttpStatus.SC_BAD_REQUEST)
    }
}
