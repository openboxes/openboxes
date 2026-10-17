package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.InventoryLevelImportDataService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService

class InventoryLevelControllerSpec extends Specification implements ControllerUnitTest<InventoryLevelController>, DataTest {

    Location depot
    Location store
    Inventory inventory
    Inventory storeInventory
    Product product
    Product other

    void setupSpec() {
        mockDomains(Location, Inventory, InventoryLevel, Product, Category)
    }

    void setup() {
        depot = new Location(name: 'Main depot').save(validate: false)
        inventory = new Inventory(warehouse: depot).save(validate: false)
        depot.inventory = inventory
        depot.save(validate: false)
        store = new Location(name: 'Store').save(validate: false)
        storeInventory = new Inventory(warehouse: store).save(validate: false)
        store.inventory = storeInventory
        store.save(validate: false)
        product = new Product(name: 'Paracetamol', productCode: 'PC1').save(validate: false)
        other = new Product(name: 'Ibuprofen', productCode: 'IB1').save(validate: false)
        session.warehouse = depot
        request.method = 'POST'

        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code }]
        controller.productService = Mock(ProductService)
        controller.dataService = Mock(DataService)
        controller.documentService = Mock(DocumentService)
        controller.inventoryLevelImportDataService = Mock(InventoryLevelImportDataService)
    }

    private InventoryLevel level(Product p, Inventory inv, Map props = [:]) {
        InventoryLevel level = new InventoryLevel([product: p, inventory: inv] + props)
        level.save(validate: false)
        return level
    }

    void 'index redirects to list'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/inventoryLevel/list'
    }

    void 'list filters by location and product search'() {
        given:
        level(product, inventory)
        level(other, inventory)
        level(product, storeInventory)
        params['location.id'] = depot.id

        when:
        def model = controller.list()

        then:
        model.inventoryLevelInstanceTotal == 2
        params.max == 10

        when:
        params.q = 'para'
        model = controller.list()

        then:
        1 * controller.productService.searchProducts(['para'] as String[], null) >> [product]
        model.inventoryLevelInstanceList*.product == [product]
    }

    void 'list exports the levels as CSV'() {
        given:
        level(product, inventory)
        params.format = 'csv'
        params.max = '1'

        when:
        controller.list()

        then:
        1 * controller.inventoryLevelImportDataService.exportInventoryLevels(_) >> 'code\nPC1'
        params.max == null
        response.contentType.startsWith('text/csv')
        response.getHeader('Content-disposition').contains('inventoryLevels.csv')
        response.text == 'code\nPC1'
    }

    void 'create binds the params'() {
        given:
        params.minQuantity = '4'

        when:
        def model = controller.create()

        then:
        model.inventoryLevelInstance.minQuantity == 4
    }

    void 'save refuses a duplicate level'() {
        given:
        level(product, inventory)
        params['product.id'] = product.id
        params['location.id'] = depot.id

        when:
        controller.save()

        then:
        flash.error.startsWith("Inventory level already exists for 'Paracetamol' in location 'Main depot'")
        response.redirectedUrl == "/product/edit/${product.id}"
        InventoryLevel.count() == 1
    }

    void 'save creates a level for the location inventory'() {
        given:
        params['product.id'] = product.id
        params['location.id'] = depot.id
        params.minQuantity = '2'

        when:
        controller.save()

        then:
        InventoryLevel created = InventoryLevel.findByProduct(product)
        created.inventory == inventory
        created.minQuantity == 2
        flash.message == 'default.created.message'
        response.redirectedUrl == "/product/edit/${product.id}"
    }

    void 'save re-renders the form for an invalid level'() {
        given:
        params['product.id'] = product.id
        params['location.id'] = depot.id
        params.minQuantity = '-3'

        when:
        controller.save()

        then:
        view == '/inventoryLevel/create'
        model.inventoryLevelInstance.errors.hasFieldErrors('minQuantity')
    }

    void 'show and edit find the level, edit also by product'() {
        given:
        InventoryLevel existing = level(product, inventory)

        when:
        params.id = existing.id
        def model = controller.show()

        then:
        model.inventoryLevelInstance == existing

        when:
        params.id = product.id
        model = controller.edit()

        then:
        model.inventoryLevelInstance == existing

        when:
        params.id = 'missing'
        controller.show()

        then:
        response.redirectedUrl == '/inventoryLevel/list'

        when:
        response.reset()
        controller.edit()

        then:
        response.redirectedUrl == '/inventoryLevel/create'
        flash.message == 'default.not.found.message'
    }

    void 'update rejects stale versions and invalid values'() {
        given:
        InventoryLevel existing = level(product, inventory)
        existing.version = 4
        existing.save(validate: false)
        params.id = existing.id
        params.version = '1'

        when:
        controller.update()

        then:
        view == '/inventoryLevel/edit'
        model.inventoryLevelInstance.errors.hasFieldErrors('version')

        when:
        response.reset()
        params.remove('version')
        params.maxQuantity = '-1'
        controller.update()

        then:
        view == '/inventoryLevel/edit'
        model.inventoryLevelInstance.errors.hasFieldErrors('maxQuantity')

        when:
        response.reset()
        params.id = 'missing'
        controller.update()

        then:
        response.redirectedUrl == '/inventoryLevel/list'
    }

    void 'delete removes the level and returns to the product'() {
        given:
        InventoryLevel existing = level(product, inventory)

        when:
        params.id = existing.id
        controller.delete()

        then:
        flash.message == 'default.deleted.message'
        response.redirectedUrl == "/product/edit/${product.id}"

        when:
        response.reset()
        params.id = 'missing'
        controller.delete()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/inventoryLevel/list'
    }

    void 'dialog renders the form for a new or existing level'() {
        given:
        views['/inventoryLevel/_form.gsp'] = '${inventoryLevelInstance?.minQuantity}|${productInstance?.name}'
        InventoryLevel existing = level(product, inventory, [minQuantity: 6])
        params.id = existing.id
        params.productId = product.id

        when:
        controller.dialog()

        then:
        response.text == '6|Paracetamol'
    }

    void 'dialog reports a missing level'() {
        given:
        views['/inventoryLevel/_form.gsp'] = '${inventoryLevelInstance?.minQuantity}|${productInstance?.name}'
        params.id = 'missing'

        when:
        controller.dialog()

        then:
        flash.message == 'default.not.found.message'
        response.text == '|'
    }

    void 'markAsSupported and markAsNotSupported set the status of the selected products'() {
        given:
        InventoryLevel existing = level(product, inventory, [status: InventoryStatus.NOT_SUPPORTED])
        params['product.id'] = [product.id, other.id] as String[]

        when:
        controller.markAsSupported()

        then:
        existing.status == InventoryStatus.SUPPORTED
        InventoryLevel.findByProduct(other)?.status == InventoryStatus.SUPPORTED
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"

        when:
        response.reset()
        controller.markAsNotSupported()

        then:
        existing.status == InventoryStatus.NOT_SUPPORTED
        InventoryLevel.findByProduct(other).status == InventoryStatus.NOT_SUPPORTED
    }

    void '#action without products returns to the browser'() {
        when:
        controller."$action"()

        then:
        flash.message == 'default.updated.message'
        response.redirectedUrl == '/inventory/browse'

        where:
        action << ['markAsSupported', 'markAsNotSupported']
    }

    void 'markAsNonInventoried flags each selected product'() {
        given:
        params['product.id'] = [product.id, other.id] as String[]
        params.product = [id: [product.id, other.id] as String[]]

        when:
        controller.markAsNonInventoried()

        then:
        InventoryLevel.findAllByStatus(InventoryStatus.SUPPORTED_NON_INVENTORY)*.product.toSet() == [product, other] as Set
        response.redirectedUrl == '/inventory/browse'
    }

    @PendingFeature(reason = 'OBAF reads params.product.id as a list and iterates the id characters')
    void 'markAsNonInventoried flags a single selected product'() {
        given:
        Product selected = (1..12).collect { new Product(name: "Product ${it}", productCode: "P${it}").save(validate: false) }.last()
        assert selected.id.toString().size() > 1
        params['product.id'] = selected.id
        params.product = [id: selected.id]

        when:
        controller.markAsNonInventoried()

        then:
        InventoryLevel.findAllByStatus(InventoryStatus.SUPPORTED_NON_INVENTORY)*.product == [selected]
        response.redirectedUrl == '/inventory/browse'
    }

    void 'export renders JSON, CSV and Excel, or 404 when nothing matches'() {
        given:
        level(product, inventory)
        params.id = product.id
        params['location.id'] = depot.id

        when:
        response.format = 'json'
        controller.export()

        then:
        JSON.parse(response.text).data.size() == 1

        when:
        response.reset()
        response.format = 'csv'
        controller.export()

        then:
        1 * controller.inventoryLevelImportDataService.exportInventoryLevels(_) >> 'code\nPC1'
        response.getHeader('Content-disposition').contains('inventory-levels.csv')
        response.text == 'code\nPC1'

        when:
        response.reset()
        response.format = 'xls'
        controller.export()

        then:
        1 * controller.dataService.transformObjects(_, InventoryLevel.PROPERTIES) >> [[code: 'PC1']]
        1 * controller.documentService.generateExcel(_, [[code: 'PC1']])
        response.getHeader('Content-disposition').contains('inventory-levels.xls')

        when:
        response.reset()
        params.id = other.id
        controller.export()

        then:
        response.status == 404
        response.text == 'Resource not found'
    }
}
