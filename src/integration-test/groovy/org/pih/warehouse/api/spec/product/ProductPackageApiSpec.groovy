package org.pih.warehouse.api.spec.product

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductPackageApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.UnitOfMeasureTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductSupplierTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductPackageCommand
import org.pih.warehouse.product.ProductSupplier

class ProductPackageApiSpec extends ApiSpec {

    @Autowired
    ProductPackageApiWrapper productPackageApiWrapper

    @Shared
    Product supplierProduct

    @Shared
    ProductSupplier productSupplier

    @Shared
    UnitOfMeasure packageUom

    @Override
    void setupData() {
        supplierProduct = new ProductTestBuilder().randomizedName().category(rootCategory).build(true)
        productSupplier = new ProductSupplierTestBuilder().product(supplierProduct).build(true)
        packageUom = new UnitOfMeasureTestBuilder().build(true)
    }

    @Override
    void cleanupData() {
        ProductSupplier productSupplierToDelete = ProductSupplier.get(productSupplier.id)
        productSupplierToDelete.defaultProductPackage = null
        productSupplierToDelete.productPackages*.delete()
        productSupplierToDelete.delete()
        packageUom?.delete()
        supplierProduct?.delete()
    }

    void 'create product package should create default package with given uom, quantity and price'() {
        given:
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: packageUom,
                productPackageQuantity: 10,
                productPackagePrice: 25.0,
        )

        when:
        ProductPackage createdPackage = productPackageApiWrapper.createOK(command)

        then:
        assert createdPackage.id != null
        assert createdPackage.uom.id == packageUom.id
        assert createdPackage.quantity == 10
        assert createdPackage.productPrice.price == 25.0
        assert ProductPackage.withNewTransaction { ProductPackage.get(createdPackage.id).productSupplier.id } == productSupplier.id
    }

    void 'create product package should fail when package quantity is negative'() {
        given:
        ProductPackageCommand command = new ProductPackageCommand(
                productSupplier: productSupplier,
                uom: packageUom,
                productPackageQuantity: -1,
        )

        expect:
        productPackageApiWrapper.create(command, HttpStatus.SC_BAD_REQUEST)
    }
}
