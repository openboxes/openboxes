package unit.org.pih.warehouse.product

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.pih.warehouse.PagedResultList
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductController
import org.pih.warehouse.product.ProductService
import spock.lang.Specification

@TestFor(ProductController)
@Mock([Product, Category, ProductService])
class ProductControllerTests extends Specification {

    void setup() {
		def currentDate = new Date()
		def category = new Category(id: "123", name: "category 123").save(flush: true)
		new Category(id: "123", name: "category 123").save(flush: true)
		new Product(id:"1234", name: "product 1234", category: category, lastUpdated: currentDate, dateCreated: currentDate).save(flush: true)
		new Product(id:"1236", name: "product 1236", category: category, lastUpdated: currentDate, dateCreated: currentDate).save(flush: true)
	}

	void "test product list size"() {
		expect:
		Product.count() == 2
	}

	void "test list should contain two products"() {
		when:
		controller.productService = [
				getProducts: { arg1, arg2, arg3, arg4, arg5 ->
					return new PagedResultList(Product.list(), 2)
				}
		]
		def model = controller.list()

		then:
		model.productInstanceTotal == 2
		model.productInstanceList.size() == 2
		model.productInstanceList.get(0).name == "product 1234"
		model.productInstanceList.get(1).name == "product 1236"
	}

	// TODO: Add test for save, import and export Product
}
