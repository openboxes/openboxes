package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.DefaultGrailsTagDateHelper
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.springframework.context.MessageSource
import spock.lang.Specification

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductActivityCode
import org.pih.warehouse.product.ProductField
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment

/**
 * Covers the JsonController actions that look up domain data: autocompletes, barcode scans,
 * requisition item editing and localization editing.
 */
class JsonControllerLookupSpec extends Specification implements ControllerUnitTest<JsonController>, DataTest {

    Closure doWithSpring() {{ ->
        grailsTagDateHelper(DefaultGrailsTagDateHelper)
    }}

    Location warehouse
    Product aspirin
    Product aspen
    Product inactive
    Category medicines
    User user
    Person requester

    void setupSpec() {
        mockDomains(Location, LocationType, Inventory, Product, Category, Tag, ProductGroup, ProductType, ProductPackage,
                InventoryItem, Person, User, Requisition, RequisitionItem, Localization, Shipment, Container, Order, OrderItem)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
                getLocalizedString(_) >> { String s -> s }
            }, LocalizationService)
        }
        controller.localizationService = Stub(LocalizationService) {
            getLocalizedString(_) >> { String s -> s }
        }

        warehouse = new Location(name: "Main Warehouse", active: true).save(validate: false)
        warehouse.inventory = new Inventory(warehouse: warehouse).save(validate: false)
        medicines = new Category(name: "Medicines").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01", active: true, unitOfMeasure: "EA", category: medicines).save(validate: false)
        aspen = new Product(name: "Aspen Extract", productCode: "XA02", active: true, category: medicines).save(validate: false)
        inactive = new Product(name: "Aspartame", productCode: "AS99", active: false, category: medicines).save(validate: false)
        requester = new Person(firstName: "Ana", lastName: "Silva", email: "ana@example.org", active: true).save(validate: false)
        user = new User(username: "clerk", firstName: "Zed", lastName: "Clerk", locale: Locale.ENGLISH).save(validate: false)
        session.user = user
        session.warehouse = warehouse
    }

    private Requisition requisition(Map props = [:]) {
        Requisition requisition = new Requisition([
                name                 : "Stock list",
                origin               : warehouse,
                destination          : new Location(name: "Ward").save(validate: false),
                requestedBy          : requester,
                dateRequested        : new Date(),
                requestedDeliveryDate: new Date()
        ] + props)
        requisition.save(failOnError: true)
        return requisition
    }

    void "findProductCodes matches active products by code or name prefix"() {
        when:
        params.term = "AS"
        controller.findProductCodes()

        then:
        response.json*.value == ["AS01", "XA02"]
        response.json*.label.contains("AS01 Aspirin")
    }

    void "findProductNames and autoSuggest suggest names of active products"() {
        when:
        params.term = "asp"
        controller.findProductNames()

        then:
        response.json*.value as Set == ["Aspirin", "Aspen Extract"] as Set

        when:
        response.reset()
        params.term = "01"
        params.field = "productCode"
        controller.autoSuggest()

        then:
        response.json*.label == ["AS01"]
    }

    void "findTags and autoSuggestProductGroups suggest distinct names"() {
        given:
        new Tag(tag: "cold chain").save(validate: false)
        new Tag(tag: "cold storage").save(validate: false)
        new ProductGroup(name: "Pain relief tablets").save(validate: false)
        new ProductGroup(name: "Pain gel").save(validate: false)

        when:
        params.term = "cold"
        controller.findTags()

        then:
        response.json*.value as Set == ["cold chain", "cold storage"] as Set

        when:
        response.reset()
        params.term = "pain tab"
        controller.autoSuggestProductGroups()

        then:
        response.json*.value == ["Pain relief tablets"]
    }

    void "findPrograms prefers prefix matches, then substring matches, then echoes the term"() {
        given:
        requisition(recipientProgram: "Malaria control")
        requisition(recipientProgram: "HIV Malaria")

        when:
        params.term = "Mal"
        controller.findPrograms()

        then:
        response.json*.value == ["Malaria control"]

        when:
        response.reset()
        params.term = "aria"
        controller.findPrograms()

        then:
        response.json*.value as Set == ["Malaria control", "HIV Malaria"] as Set

        when:
        response.reset()
        params.term = "TB"
        controller.findPrograms()

        then:
        response.json*.value == ["TB"]
    }

    void "findLocations lists active top level locations, optionally filtered by activity"() {
        given:
        LocationType depot = new LocationType(name: "Depot", supportedActivities: [ActivityCode.MANAGE_INVENTORY.id] as Set).save(validate: false)
        LocationType office = new LocationType(name: "Office", supportedActivities: [] as Set).save(validate: false)
        warehouse.locationType = depot
        warehouse.save(validate: false)
        new Location(name: "Main Office", active: true, locationType: office).save(validate: false)
        new Location(name: "Main Closed", active: false, locationType: depot).save(validate: false)

        when:
        params.term = "Main"
        controller.findLocations()

        then:
        response.json*.text == ["Main Office", "Main Warehouse"]

        when:
        response.reset()
        params.activityCode = "MANAGE_INVENTORY"
        controller.findLocations()

        then:
        response.json*.text == ["Main Warehouse"]
    }

    void "person searches match first name, last name or email"() {
        given:
        new Person(firstName: "Bruno", lastName: "Anaya", email: "bruno@example.org", active: true).save(validate: false)
        new Person(firstName: "Anabel", lastName: "Old", active: false).save(validate: false)

        when:
        params.term = "ana"
        controller.searchPersonByName()

        then:
        response.json*.value as Set == ["Ana Silva", "Bruno Anaya"] as Set
        response.json.find { it.value == "Ana Silva" }.label == "Ana Silva ana@example.org"

        when:
        response.reset()
        controller.findPersonByName()
        def results = response.json

        then: "the last entry offers to create a new person"
        results*.label == ["Ana Silva", "Bruno Anaya", "Create new record for ana"]
        results[0].desc == "ana@example.org"
        results[2].id == "new"
    }

    void "createPerson splits the name into first and last name"() {
        when:
        params.name = name
        controller.createPerson()

        then:
        response.json.value == expected
        Person.get(response.json.id).lastName == lastName

        where:
        name               | expected           | lastName
        "Joao Santos"      | "Joao Santos"      | "Santos"
        "Maria Jose Lopes" | "Maria Jose Lopes" | "Lopes"
    }

    void "createPerson requires two or three names"() {
        when:
        params.name = name
        controller.createPerson()

        then:
        Exception e = thrown()
        e.message == message

        where:
        name          | message
        "Cher"        | "Person must have at least two names"
        "A B C D"     | "Person must have at most three names"
    }

    void "getInventoryItem and getInventoryItems render inventory items"() {
        given:
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "LOT-1",
                expirationDate: Date.parse("yyyy-MM-dd", "2031-06-30")).save(validate: false)
        controller.inventoryService = Mock(InventoryService)

        when:
        params.id = lot.id
        controller.getInventoryItem()

        then:
        response.json.lotNumber == "LOT-1"
        response.json.productName == "Aspirin"
        response.json.expirationDate == "06/30/2031"

        when:
        response.reset()
        params['product.id'] = aspirin.id
        controller.getInventoryItems()

        then:
        1 * controller.inventoryService.getInventoryItemsByProduct(aspirin) >> [[lotNumber: "LOT-1"]]
        response.json[0].lotNumber == "LOT-1"
    }

    void "getQuantity looks up the lot of the product at the current inventory"() {
        given:
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "LOT-1").save(validate: false)
        controller.inventoryService = Mock(InventoryService)

        when:
        params.productId = aspirin.id
        params.lotNumber = "LOT-1"
        controller.getQuantity()

        then:
        1 * controller.inventoryService.findInventoryItemByProductAndLotNumber(aspirin, "LOT-1") >> lot
        1 * controller.inventoryService.getQuantity(warehouse.inventory, lot) >> 42
        response.text == "42"

        when:
        response.reset()
        params.lotNumber = "MISSING"
        controller.getQuantity()

        then:
        1 * controller.inventoryService.findInventoryItemByProductAndLotNumber(aspirin, "MISSING") >> null
        response.text == "N/A"
    }

    void "findLotsByName labels matching lots with their quantity"() {
        given:
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "ABC-1").save(validate: false)
        new InventoryItem(product: aspen, lotNumber: "ABC-2").save(validate: false)
        controller.inventoryService = Stub(InventoryService) {
            getQuantityForInventory(warehouse.inventory) >> [(lot): 15]
        }

        when:
        params.term = "abc"
        controller.findLotsByName()
        def results = response.json

        then:
        results*.lotNumber as Set == ["ABC-1", "ABC-2"] as Set
        results.find { it.lotNumber == "ABC-1" }.label == "Aspirin --- Item: ABC-1 --- Qty: 15 --- "
        results.find { it.lotNumber == "ABC-2" }.label == "Aspen Extract --- Item: ABC-2 --- Qty: 0 --- "
    }

    void "findLotsByName without a term renders an empty list"() {
        when:
        controller.findLotsByName()

        then:
        response.json.size() == 0
    }

    void "findInventoryItems lists lots in stock with a search summary first"() {
        given:
        InventoryItem inStock = new InventoryItem(product: aspirin, lotNumber: "L1",
                expirationDate: Date.parse("yyyy-MM-dd", "2031-01-15")).save(validate: false)
        InventoryItem empty = new InventoryItem(product: aspirin, lotNumber: "L2").save(validate: false)
        controller.inventoryService = Stub(InventoryService) {
            findInventoryItems("asp") >> [inStock, empty]
            getQuantityByInventoryItemMap(warehouse, _) >> [(inStock): 20]
        }

        when:
        params.term = "asp"
        controller.findInventoryItems()
        def results = response.json

        then:
        results.size() == 2
        results.find { it.id == "null" }.value == "Searching for 'asp'"
        def item = results.find { it.id == inStock.id }
        item.lotNumber == "L1"
        item.quantity == 20
        item.productName == "AS01 Aspirin"
        item.label == "AS01 Aspirin x 20 EA"
        item.expirationDate == "Jan 2031"
    }

    void "findInventoryItems refuses to compute quantities for too many matches"() {
        given:
        config.openboxes.shipping.search.maxResults = 1
        controller.inventoryService = Stub(InventoryService) {
            findInventoryItems("asp") >> [new InventoryItem(product: aspirin), new InventoryItem(product: aspen)]
        }

        when:
        params.term = "asp"
        controller.findInventoryItems()

        then:
        response.json.size() == 1
        response.json[0].id == "null"
        response.json[0].value.contains("asp")
    }

    void "findInventoryItems reports when nothing was found"() {
        when:
        controller.findInventoryItems()

        then:
        response.json.size() == 1
        response.json[0].id == "null"
    }

    void "findProductByName ranks code matches first and lists the product lots"() {
        given:
        new InventoryItem(product: aspirin, lotNumber: "L9", expirationDate: Date.parse("yyyy-MM-dd", "2031-02-01")).save(validate: false)
        controller.productService = Stub(ProductService) {
            searchProducts(["AS01"] as String[], []) >> [aspen, aspirin, aspirin]
        }

        when:
        params.term = "AS01"
        controller.findProductByName()
        def results = response.json

        then:
        results*.id == [aspirin.id, aspen.id]
        results[0].label == "AS01 Aspirin [EA]"
        results[0].inventoryItems*.lotNumber == ["L9"]
        results[0].inventoryItems[0].expirationDate == "02/2031"
        results[1].inventoryItems.size() == 0
    }

    void "findProductByName can skip quantities and reports when nothing matches"() {
        given:
        controller.productService = Stub(ProductService) {
            searchProducts(_, _) >> []
        }

        when:
        params.term = "zzz"
        params.skipQuantity = "true"
        controller.findProductByName()

        then:
        response.json.size() == 1
        response.json[0].value == null
    }

    void "findRequestItems searches products, product groups and categories"() {
        given:
        new ProductGroup(name: "Medicines for pain").save(validate: false)

        when:
        params.term = "medic"
        controller.findRequestItems()
        def results = response.json

        then:
        results*.type as Set == ["ProductGroup", "Category"] as Set
        results.find { it.type == "Category" }.value == "Category:${medicines.id}"
        results.find { it.type == "Category" }.label == "Medicines(Category)"
    }

    void "findRequestItems reports when nothing matches"() {
        when:
        params.term = "zzz"
        controller.findRequestItems()

        then:
        response.json.size() == 1
        response.json[0].value == null
    }

    void "searchProduct and searchProductPackages fall back to a broader search"() {
        given:
        ProductService productService = Mock()
        controller.productService = productService
        params.term = "asp"

        when:
        controller."$action"()

        then:
        1 * productService.searchProductAndProductGroup("asp") >> []
        1 * productService.searchProductAndProductGroup("asp", true) >> [[aspirin.id], [aspen.id]]
        1 * productService.getProducts([aspirin.id, aspen.id] as String[]) >> [aspen, aspirin]
        response.json*.value == ["AS01 Aspirin (EA/1)", "XA02 Aspen Extract (EA/1)"]
        response.json*.type == ["Product", "Product"]

        where:
        action << ["searchProduct", "searchProductPackages"]
    }

    void "globalSearch ignores short terms"() {
        given:
        config.openboxes.typeahead.minLength = 3
        controller.productService = Mock(ProductService)

        when:
        params.term = "as"
        controller.globalSearch()

        then:
        0 * controller.productService._
        response.json.size() == 0
    }

    void "globalSearch hides products whose type is not searchable or not in stock"() {
        given:
        config.openboxes.typeahead.minLength = 2
        ProductType searchable = new ProductType(name: "Searchable", supportedActivities: [ProductActivityCode.SEARCHABLE] as Set).save(validate: false)
        ProductType hidden = new ProductType(name: "Hidden", supportedActivities: [] as Set).save(validate: false)
        Product noStock = new Product(name: "Aspic", productCode: "AS03", productType: searchable).save(validate: false)
        Product secret = new Product(name: "Asparagus", productCode: "AS04", productType: hidden).save(validate: false)
        controller.productService = Stub(ProductService) {
            searchProducts(["AS"] as String[], []) >> [aspirin, noStock, secret]
        }
        controller.productAvailabilityService = Stub(ProductAvailabilityServiceStub) {
            getQuantityAvailableToPromiseByProduct(warehouse, _) >> [(aspirin.id): 5]
        }

        when:
        params.term = "AS"
        controller.globalSearch()

        then:
        response.json.size() == 1
        response.json[0].id == aspirin.id
        response.json[0].label == "AS01 Aspirin  [5 EA]"
        response.json[0].url == "/product/redirect/${aspirin.id}"
    }

    void "scanBarcode resolves products, requisitions, shipments and purchase orders"() {
        given:
        requisition(requestNumber: "REQ-1")
        new Shipment(name: "Shipment", shipmentNumber: "SHP-1").save(validate: false)
        new Order(name: "PO", orderNumber: "PO-1").save(validate: false)

        when:
        params.barcode = barcode
        controller.scanBarcode()

        then:
        response.json.type == type
        response.json.barcode == barcode
        (response.json.url ?: "").contains(urlPart)

        where:
        barcode | type             | urlPart
        "AS01"  | "stock card"     | "/inventoryItem/showStockCard/"
        "REQ-1" | "requisition"    | "/requisition/show/"
        "SHP-1" | "shipment"       | "/shipment/showDetails/"
        "PO-1"  | "purchase order" | "/purchaseOrder/show/"
        "NONE"  | null             | ""
    }

    void "getOrderItem renders the order line"() {
        given:
        Order order = new Order(name: "PO", orderNumber: "PO-1").save(validate: false)
        OrderItem item = new OrderItem(order: order, product: aspirin, quantity: 4, unitPrice: 1.25).save(validate: false)

        when:
        params.id = item.id
        controller.getOrderItem()

        then:
        response.json.id == item.id
        response.json.quantity == 4
        response.json.unitPrice == 1.25
    }

    void "checkIfProductFieldRemoved reports whether the new type drops displayed fields"() {
        given:
        ProductType all = new ProductType(name: "All", displayedFields: [] as Set).save(validate: false)
        ProductType two = new ProductType(name: "Two", displayedFields: [ProductField.BRAND_NAME, ProductField.ABC_CLASS] as Set).save(validate: false)
        ProductType one = new ProductType(name: "One", displayedFields: [ProductField.BRAND_NAME] as Set).save(validate: false)

        when:
        params.oldTypeId = types[0].call(all, two, one)?.id
        params.newTypeId = types[1].call(all, two, one)?.id
        controller.checkIfProductFieldRemoved()

        then:
        response.json.fieldRemoved == removed

        where:
        types                                          | removed
        [{ a, t, o -> t }, { a, t, o -> o }]           | true
        [{ a, t, o -> o }, { a, t, o -> t }]           | false
        [{ a, t, o -> a }, { a, t, o -> o }]           | true
        [{ a, t, o -> t }, { a, t, o -> a }]           | false
    }

    void "requisition items can be added, listed, updated and removed"() {
        given:
        Requisition requisition = requisition()

        when: "adding a product"
        params['requisition.id'] = requisition.id
        params['product.id'] = aspirin.id
        params.quantity = "5"
        params.orderIndex = "1"
        controller.addToRequisitionItems()
        String itemId = response.json.data.id

        then:
        response.json.success == true
        response.json.data.quantity == 5
        response.json.data.product.productCode == "AS01"
        requisition.requisitionItems.size() == 1

        when: "adding a second product with the default quantity"
        response.reset()
        params.remove("quantity")
        params.orderIndex = "0"
        params['product.id'] = aspen.id
        controller.addToRequisitionItems()

        then:
        response.json.data.quantity == 1

        when: "listing the items in sort order"
        response.reset()
        params.id = requisition.id
        controller.getRequisitionItems()

        then:
        response.json.aaData*.product.productCode == ["XA02", "AS01"]

        when: "updating quantities"
        response.reset()
        request.json = ([items: [[id: itemId, quantity: "9"]]] as JSON).toString()
        controller.updateRequisitionItems()

        then:
        RequisitionItem.get(itemId).quantity == 9
        response.forwardedUrl.startsWith("/json/getRequisitionItems")

        when: "reordering"
        response.reset()
        params["requisitionItem[]"] = [itemId, RequisitionItem.findByProduct(aspen).id]
        controller.sortRequisitionItems()

        then:
        RequisitionItem.get(itemId).orderIndex == 0
        RequisitionItem.findByProduct(aspen).orderIndex == 1
        response.contentType.startsWith("text/plain")

        when: "removing an item"
        response.reset()
        params.id = itemId
        controller.removeRequisitionItem()

        then:
        response.json.success == true
        requisition.requisitionItems.size() == 1
    }

    void "requisition item actions reject unknown ids"() {
        when:
        params['requisition.id'] = "missing"
        controller.addToRequisitionItems()

        then:
        response.status == 400
        response.json.errors[0] == "Unable to find requisition with ID missing"

        when:
        response.reset()
        params['requisition.id'] = requisition().id
        params['product.id'] = "missing"
        controller.addToRequisitionItems()

        then:
        response.status == 400
        response.json.errors[0] == "Unable to find product with ID missing"

        when:
        response.reset()
        params.id = "missing"
        controller.getRequisitionItems()

        then:
        response.json.success == false

        when:
        response.reset()
        request.json = '{"items": []}'
        controller.updateRequisitionItems()

        then:
        response.status == 400

        when:
        response.reset()
        controller.removeRequisitionItem()

        then:
        response.status == 400
        response.json.errors[0] == "Unable to find requisition item with ID missing"
    }

    void "getLocalization returns the stored localization or builds one from the message source"() {
        given:
        Localization stored = new Localization(code: "app.title", locale: "fr", text: "Titre").save(failOnError: true)
        controller.messageSource = Stub(MessageSource) {
            getMessage("app.hello", null, "Hello", _) >> "Hello there"
        }

        when:
        params.id = stored.id
        controller.getLocalization()

        then:
        response.json.code == "app.title"
        response.json.translation == "Titre"

        when:
        response.reset()
        params.id = "missing"
        params.code = "app.hello"
        params.resolvedMessage = "Hello"
        controller.getLocalization()

        then:
        response.json.code == "app.hello"
        response.json.text == "Hello there"
        response.json.locale == "en"
    }

    void "saveLocalization creates or updates the user's translation and deleteLocalization removes it"() {
        when:
        request.json = '{"code": "app.title", "translation": "Title"}'
        controller.saveLocalization()

        then:
        response.json.success == true
        Localization.findByCode("app.title").text == "Title"
        Localization.findByCode("app.title").locale == "en"

        when:
        response.reset()
        params.id = Localization.findByCode("app.title").id
        controller.deleteLocalization()

        then:
        response.json.success == true
        Localization.countByCode("app.title") == 0
    }

    void "saveLocalization updates the user's existing translation of the code"() {
        given:
        new Localization(code: "app.title", locale: "en", text: "Title").save(failOnError: true)

        when:
        request.json = '{"code": "app.title", "translation": "Main title"}'
        controller.saveLocalization()

        then:
        response.json.success == true
        Localization.countByCode("app.title") == 1
        Localization.findByCode("app.title").text == "Main title"
    }

    void "sortContainers stores the new container order"() {
        given:
        Shipment shipment = new Shipment(name: "S").save(validate: false)
        Container first = new Container(name: "Box 1", shipment: shipment, sortOrder: 0).save(validate: false)
        Container second = new Container(name: "Box 2", shipment: shipment, sortOrder: 1).save(validate: false)

        when:
        params["container[]"] = [second.id, first.id]
        controller.sortContainers()

        then:
        Container.get(second.id).sortOrder == 0
        Container.get(first.id).sortOrder == 1
        response.contentType.startsWith("text/plain")
    }
}

interface ProductAvailabilityServiceStub {
    def getQuantityAvailableToPromiseByProduct(def location, def products)
}
