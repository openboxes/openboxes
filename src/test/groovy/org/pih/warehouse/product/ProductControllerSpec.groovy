package org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.MessageTagLib
import org.pih.warehouse.core.ProductPrice

class ProductControllerSpec extends Specification implements ControllerUnitTest<ProductController>, DataTest {

    Closure doWithSpring() {
        return {
            defaultValidator(LocalValidatorFactoryBean)
        }
    }

    void setupSpec() {
        mockDomains(Product, ProductPackage, ProductPrice, Category, ProductType)
    }

    void setup() {
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        if (!applicationContext.containsBean('productValidator')) {
            applicationContext.beanFactory.registerSingleton('productValidator', Stub(ProductValidator) {
                validate(_) >> true
            })
        }
    }

    @Unroll
    void "savePackage preserves price #price when existing package is #existing"() {
        given:
        Product product = createProduct()
        if (existing) {
            ProductPackage productPackage = new ProductPackage(product: product, quantity: 1,
                    productPrice: new ProductPrice(price: 1.00)).save(validate: false)
            product.addToPackages(productPackage)
            params.id = productPackage.id
        }
        params['product.id'] = product.id
        params.quantity = '1'
        params.price = price

        when:
        controller.savePackage()

        then:
        assert product.packages.size() == 1
        assert product.packages.first().productPrice.price == new BigDecimal(price ?: '0')
        assert response.redirectedUrl == "/product/edit/${product.id}"

        where:
        price    | existing
        '3.4666' | false
        '3.4666' | true
        '0.0001' | false
        '0.0001' | true
        '0'      | false
        '0'      | true
        '12.34'  | false
        '12.34'  | true
        ''       | false
        ''       | true
    }

    @Unroll
    void "savePackage rejects invalid price #price"() {
        given:
        Product product = createProduct()
        params['product.id'] = product.id
        params.quantity = '1'
        params.price = price

        when:
        controller.savePackage()

        then:
        assert ProductPackage.count() == 0
        assert flash.message.startsWith(message)
        assert response.redirectedUrl == "/product/edit/${product.id}"

        where:
        price      | message
        'invalid'  | 'Could not parse unit price with value: invalid.'
        '-1'       | 'Wrong unit price value: -1'
    }

    private Product createProduct() {
        Category category = new Category(name: "Test category").save(failOnError: true)
        ProductType productType = new ProductType(name: "Test type",
                productTypeCode: ProductTypeCode.GOOD).save(failOnError: true)
        return new Product(name: "Test product", category: category, productType: productType)
                .save(failOnError: true)
    }
}
