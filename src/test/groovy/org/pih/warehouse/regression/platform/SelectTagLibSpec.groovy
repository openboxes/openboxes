package org.pih.warehouse.regression.platform

import spock.lang.PendingFeature

import org.pih.warehouse.*
import grails.gorm.PagedResultList
import grails.testing.gorm.DataTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.taglib.GrailsTagException
import spock.lang.Ignore
import spock.lang.Specification

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.BudgetCodeFilterCommand
import org.pih.warehouse.core.BudgetCodeService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.OrganizationService
import org.pih.warehouse.core.PaymentMethodType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustmentType
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.CategoryService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.Shipper

class SelectTagLibSpec extends Specification implements TagLibUnitTest<SelectTagLib>, DataTest {

    LocationType depotType
    LocationType binType
    Location depot

    void setupSpec() {
        mockDomains(Location, LocationType, Organization, Product, Category, Tag, ProductType, ProductCatalog,
                ProductGroup, UnitOfMeasure, UnitOfMeasureClass, PaymentTerm, PaymentMethodType, GlAccountType,
                GlAccount, OrderAdjustmentType, Shipper, User, Person, ProductSupplier, TransactionType, Order,
                OrderItem, PreferenceType, BudgetCode, Requisition, Shipment, ProductPackage)
        mockTagLib(FormatTagLib)
        mockTagLib(MessageTagLib)
    }

    void setup() {
        config.openboxes.locale.defaultLocale = 'en'
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }
        depotType = save(new LocationType(name: 'Depot', locationTypeCode: LocationTypeCode.DEPOT))
        binType = save(new LocationType(name: 'Bin', locationTypeCode: LocationTypeCode.BIN_LOCATION))
        depot = save(new Location(name: 'Main Depot', locationType: depotType,
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.PICK_STOCK.id] as Set))
        session.warehouse = depot
    }

    private static <T> T save(T entity) {
        entity.save(validate: false, flush: true)
        return entity
    }

    /** Parses the rendered options into [value, text, selected] maps. */
    private static List<Map> options(String html) {
        (html =~ /<option ([^>]*)>(.*?)<\/option>/).collect { all, attributes, text ->
            String value = (attributes =~ /value="([^"]*)"/).with { it.find() ? it.group(1) : null }
            [value: value, text: text, selected: attributes.contains('selected')]
        }
    }

    private static List<String> texts(String html) {
        options(html)*.text
    }

    void 'selectAjax requires an id and a url'() {
        when:
        tagLib.selectAjax([url: '/api/x'], null)

        then:
        GrailsTagException e = thrown()
        e.message.contains('[id]')

        when:
        tagLib.selectAjax([id: 'x'], null)

        then:
        e = thrown(GrailsTagException)
        e.message.contains('[url]')
    }

    void 'selectAjax renders a select2 field with the selected values and the search parameter'() {
        given:
        Product aspirin = save(new Product(name: 'Aspirin <500mg>', productCode: 'AS1'))

        when:
        String html = applyTemplate(
                '<g:selectAjax id="prod" name="productId" url="/api/products" searchParameter="q" multiple="true" value="${values}"/>',
                [values: [aspirin]])

        then:
        html.contains('id="prod"')
        html.contains('name="productId"')
        html.contains('multiple')
        html.contains("<option value='${aspirin.id}' selected='true'>Aspirin &lt;500mg&gt;</option>")
        html.contains('url: "/api/products"')
        html.contains('q: params.term')
        html.contains('text: item.q')
        html.contains('minimumInputLength: "3"')
        html.contains('delay: "300"')
    }

    void 'selectProductAjax searches the product API and shows the product code with the name'() {
        when:
        String html = applyTemplate('<g:selectProductAjax/>')

        then:
        html.contains('id="products-select"')
        html.contains('name="products-select"')
        html.contains('url: "/api/products/search"')
        html.contains("text: '[' + item.productCode + '] ' + item.name")
        !html.contains('multiple')
    }

    void 'selectCategory adds the current category when the service did not list it'() {
        given:
        Category root = save(new Category(name: 'Root'))
        Category child = save(new Category(name: 'Child', parentCategory: root))
        tagLib.categoryService = Mock(CategoryService)

        when:
        String html = applyTemplate('<g:selectCategory name="category" value="${value}" includeParentCategories="${false}"/>',
                [value: child.id])

        then:
        1 * tagLib.categoryService.getCategoriesOptions(false) >> [[id: 'other', label: 'Other']]
        List<Map> opts = options(html)
        opts*.value == ['other', child.id]
        opts[1].text == 'Root &gt; Child'
        opts[1].selected
    }

    void 'selectCategory does not duplicate a category the service already listed'() {
        given:
        Category cat = save(new Category(name: 'Drugs'))
        tagLib.categoryService = Mock(CategoryService)

        when:
        String html = applyTemplate('<g:selectCategory name="category" value="${value}"/>', [value: cat.id])

        then:
        1 * tagLib.categoryService.getCategoriesOptions(true) >> [[id: cat.id, label: 'Drugs']]
        options(html)*.value == [cat.id]
    }

    void 'enum based selects list every constant of their enum'() {
        when:
        String html = applyTemplate("<g:${tag} name='x'/>")

        then:
        options(html)*.value == expected*.toString()

        where:
        tag                                  | expected
        'selectEntityTypeCode'               | org.pih.warehouse.core.EntityTypeCode.values()
        'selectRequisitionStatus'            | org.pih.warehouse.requisition.RequisitionStatus.list()
        'selectRatingType'                   | org.pih.warehouse.core.RatingTypeCode.list()
        'selectCommodityClass'               | org.pih.warehouse.requisition.CommodityClass.list()
        'selectRequisitionType'              | org.pih.warehouse.requisition.RequisitionType.list()
        'selectProductAssociationTypeCode'   | org.pih.warehouse.product.ProductAssociationTypeCode.list()
        'selectCancelReasonCode'             | org.pih.warehouse.core.ReasonCode.list()
        'selectRequestReasonCode'            | org.pih.warehouse.core.ReasonCode.listRequestReasonCodes()
        'selectChangeQuantityReasonCode'     | org.pih.warehouse.core.ReasonCode.listRequisitionQuantityChangeReasonCodes()
        'selectSubstitutionReasonCode'       | org.pih.warehouse.core.ReasonCode.listRequisitionSubstitutionReasonCodes()
        'selectInventoryAdjustmentReasonCode'| org.pih.warehouse.core.ReasonCode.listInventoryAdjustmentReasonCodes()
    }

    void 'enum options are labelled with the localized enum message, request statuses with their name'() {
        given:
        def codes = org.pih.warehouse.core.EntityTypeCode.values()
        messageSource.addMessage("enum.EntityTypeCode.${codes[0]}", Locale.ENGLISH, 'First type')

        when:
        List<String> labels = texts(applyTemplate("<g:selectEntityTypeCode name='x'/>"))

        then:
        labels[0] == 'First type'
        labels[1] == "enum.EntityTypeCode.${codes[1]}"
        texts(applyTemplate("<g:selectRequisitionStatus name='x'/>")) ==
                org.pih.warehouse.requisition.RequisitionStatus.list()*.name()
    }

    void 'tag, catalog, product type and product family selects list their entities with counts'() {
        given:
        Product p1 = save(new Product(name: 'P1'))
        Product p2 = save(new Product(name: 'P2'))
        Tag tag = save(new Tag(tag: 'cold chain', products: [p1, p2] as Set))
        save(new Tag(tag: 'antibiotic', products: [p1] as Set))
        ProductCatalog catalog = save(new ProductCatalog(name: 'Essentials'))
        save(new ProductType(name: 'Medicine'))
        save(new ProductType(name: 'Device'))
        save(new ProductGroup(name: 'Analgesics'))

        expect:
        texts(applyTemplate('<g:selectTag name="t"/>')) == ['antibiotic (1)', 'cold chain (2)']
        texts(applyTemplate('<g:selectTags name="t"/>')) == ['antibiotic (1)', 'cold chain (2)']
        applyTemplate('<g:selectTags name="t"/>').contains('multiple')
        texts(applyTemplate('<g:selectCatalogs name="c"/>')) == ['Essentials (null)']
        options(applyTemplate('<g:selectCatalogs name="c"/>'))*.value == [catalog.id]
        texts(applyTemplate('<g:selectProductType name="pt"/>')) == ['Device', 'Medicine']
        texts(applyTemplate('<g:selectProductFamily name="pf"/>')) == ['Analgesics']
        tag.id
    }

    void 'simple domain selects list their entities by name'() {
        given:
        save(new PaymentMethodType(name: 'wire'))
        save(new PaymentMethodType(name: 'Cash'))
        save(new PaymentTerm(name: 'Net 30'))
        save(new GlAccountType(code: 'EXP'))
        save(new GlAccount(code: '6000', description: 'Supplies', active: true))
        save(new GlAccount(code: '7000', description: 'Retired', active: false))
        save(new OrderAdjustmentType(name: 'Freight'))
        save(new Shipper(name: 'zeta'))
        save(new Shipper(name: 'Alpha'))
        save(new PreferenceType(name: 'Preferred'))
        save(new Product(name: 'Active product', active: true))
        save(new Product(name: 'Inactive product', active: false))

        expect:
        texts(applyTemplate('<g:selectPaymentMethodType name="x"/>')) == ['Cash', 'wire']
        texts(applyTemplate('<g:selectPaymentTerm name="x"/>')) == ['Net 30']
        texts(applyTemplate('<g:selectGlAccountType name="x"/>')) == ['EXP']
        texts(applyTemplate('<g:selectGlAccount name="x"/>')) == ['6000 Supplies']
        texts(applyTemplate('<g:selectOrderAdjustmentTypes name="x"/>')) == ['Freight']
        texts(applyTemplate('<g:selectShipper name="x"/>')) == ['Alpha', 'zeta']
        texts(applyTemplate('<g:selectPreferenceType name="x"/>')) == ['Preferred']
        texts(applyTemplate('<g:selectProduct name="x"/>')) == ['Active product']
    }

    void 'selectUnitOfMeasure lists the units of the requested class'() {
        given:
        UnitOfMeasureClass quantity = save(new UnitOfMeasureClass(name: 'Quantity', type: UnitOfMeasureType.QUANTITY))
        UnitOfMeasureClass volume = save(new UnitOfMeasureClass(name: 'Volume', type: UnitOfMeasureType.VOLUME))
        UnitOfMeasure each = save(new UnitOfMeasure(name: 'Each', code: 'EA', uomClass: quantity))
        UnitOfMeasure litre = save(new UnitOfMeasure(name: 'Litre', code: 'L', uomClass: volume))

        expect:
        options(applyTemplate('<g:selectUnitOfMeasure name="u"/>'))*.value == [each.id]
        options(applyTemplate('<g:selectUnitOfMeasure name="u" unitOfMeasureType="VOLUME"/>'))*.value == [litre.id]
    }

    void 'selectCurrency lists currencies by code and preselects the base currency'() {
        given:
        UnitOfMeasureClass currency = save(new UnitOfMeasureClass(name: 'Currency', type: UnitOfMeasureType.CURRENCY))
        UnitOfMeasure usd = save(new UnitOfMeasure(name: 'Dollar', code: 'USD', uomClass: currency))
        save(new UnitOfMeasure(name: 'Euro', code: 'EUR', uomClass: currency))
        currency.baseUom = usd
        save(currency)

        when:
        List<Map> opts = options(applyTemplate('<g:selectCurrency name="c"/>'))

        then:
        opts.collect { [it.value, it.text] } == [['USD', 'Dollar USD'], ['EUR', 'Euro EUR']]
        opts.find { it.selected }.value == 'USD'

        when: 'an explicit value wins'
        opts = options(applyTemplate('<g:selectCurrency name="c" value="EUR"/>'))

        then:
        opts.find { it.selected }.value == 'EUR'
    }

    void 'selectCurrency renders an empty select without a currency class'() {
        expect:
        options(applyTemplate('<g:selectCurrency name="c"/>')).isEmpty()
    }

    void 'selectProductSupplier lists the active sources of a product from a supplier'() {
        given:
        Product product = save(new Product(name: 'Gloves'))
        Organization supplier = save(new Organization(name: 'Acme', code: 'ACME'))
        Organization maker = save(new Organization(name: 'Maker', code: 'MK'))
        save(new ProductSupplier(code: 'PS1', supplierCode: 'S-1', product: product, supplier: supplier,
                manufacturer: maker, manufacturerCode: 'M-1', active: true))
        save(new ProductSupplier(code: 'PS2', supplierCode: 'S-2', product: product, supplier: supplier, active: true))
        save(new ProductSupplier(code: 'PS3', supplierCode: 'S-3', product: product, supplier: supplier, active: false))

        when:
        String html = applyTemplate('<g:selectProductSupplier name="ps" product="${product}" supplier="${supplier}"/>',
                [product: product, supplier: supplier])

        then:
        texts(html).sort() == ['PS1 - S-1 - Maker - M-1', 'PS2 - S-2 -  - ']
    }

    void 'selectProductPackage lists the product packages largest first after an EA/1 option'() {
        given:
        UnitOfMeasure box = save(new UnitOfMeasure(name: 'Box', code: 'BX'))
        Product product = save(new Product(name: 'Bandage'))
        ProductPackage small = save(new ProductPackage(name: 'small', uom: box, quantity: 10, product: product))
        ProductPackage large = save(new ProductPackage(name: 'large', uom: box, quantity: 100, product: product))
        product.packages = [small, large] as Set
        save(product)

        when:
        List<Map> opts = options(applyTemplate('<g:selectProductPackage name="pkg" product="${p}"/>', [p: product]))

        then:
        opts.collect { [it.value, it.text] } ==
                [['null', 'EA/1'], [large.id, 'BX/100 -- Box'], [small.id, 'BX/10 -- Box']]
    }

    @PendingFeature(reason = 'selectProductPackage calls g.select without from for a product without packages')
    void 'selectProductPackage posts the chosen package as productPackage.id, the parameter changeQuantity reads'() {
        given:
        Product product = save(new Product(name: 'Gauze'))

        when:
        String html = applyTemplate('<g:selectProductPackage name="productPackage.id" product="${p}"/>', [p: product])

        then:
        html.contains('name="productPackage.id"')
        options(html).collect { [it.value, it.text] } == [['null', 'EA/1']]
    }

    void 'selectOrganization asks the organization service for the requested roles'() {
        given:
        Organization org = save(new Organization(name: 'Acme', code: 'ACME'))
        tagLib.organizationService = Mock(OrganizationService)

        when:
        String html = applyTemplate('<g:selectOrganization name="o" roleTypes="${roles}" active="${true}"/>',
                [roles: ['ROLE_SUPPLIER']])

        then:
        1 * tagLib.organizationService.selectOrganizations(['ROLE_SUPPLIER'], true, null) >> [org]
        options(html).collect { [it.value, it.text] } == [[org.id, 'Acme']]
    }

    void 'selectBudgetCode filters by active flag and current value'() {
        given:
        BudgetCode code = save(new BudgetCode(code: 'BC-1', name: 'Budget'))
        tagLib.budgetCodeService = Mock(BudgetCodeService)

        when:
        String html = applyTemplate('<g:selectBudgetCode name="b" active="true" value="${v}"/>', [v: code.id])

        then:
        1 * tagLib.budgetCodeService.getBudgetCodes({ BudgetCodeFilterCommand c ->
            c.active && c.budgetCodeIds == [code.id] && !c.paginationEnabled
        }) >> new PagedResultList(null).with {
            resultList = [code]
            totalCount = 1
            it
        }
        options(html).collect { [it.value, it.text, it.selected] } == [[code.id, 'BC-1', true]]
    }

    void 'selectOrderItems requires a valid order'() {
        when:
        tagLib.selectOrderItems([orderId: 'missing'], null)

        then:
        thrown(IllegalArgumentException)
    }

    void 'selectOrderItems lists the order items by product name'() {
        given:
        Product product = save(new Product(name: 'Syringe'))
        Order order = save(new Order(name: 'PO'))
        OrderItem item = save(new OrderItem(order: order, product: product))
        order.addToOrderItems(item)
        save(order)

        when:
        String html = applyTemplate('<g:selectOrderItems name="oi" orderId="${id}"/>', [id: order.id])

        then:
        options(html).collect { [it.value, it.text] } == [[item.id, 'Syringe']]
    }

    void 'selectShipment labels shipments with number, item count and route'() {
        given:
        Location other = save(new Location(name: 'Clinic', locationType: depotType))
        Shipment shipment = save(new Shipment(name: 'Boxes', shipmentNumber: 'S-1', origin: depot, destination: other))
        shipment.metaClass.getShipmentItemCount = { -> 3 }
        tagLib.shipmentService = Mock(ShipmentService)

        when:
        String html = applyTemplate('<g:selectShipment name="s" statusCode="PENDING"/>')

        then:
        1 * tagLib.shipmentService.getShipmentsByLocation(null, depot, ShipmentStatusCode.PENDING) >> [shipment]
        texts(html) == ['S-1 Boxes - 3 items (Main Depot to Clinic)']
    }

    void 'selectDepot lists active depots grouped by type and preselects the current location'() {
        given:
        Location second = save(new Location(name: 'Annex', locationType: depotType, active: true))
        save(new Location(name: 'Bin A', locationType: binType, active: true))
        save(new Location(name: 'Closed', locationType: depotType, active: false))

        when:
        List<Map> opts = options(applyTemplate('<g:selectDepot name="d"/>'))

        then:
        opts*.text == ['Annex', 'Main Depot']
        opts.find { it.selected }.value == depot.id
        second.id
    }

    void 'user, person and recipient selects list active people'() {
        given:
        save(new User(firstName: 'Zed', lastName: 'Z', username: 'zed', active: true, email: 'zed@example.org'))
        save(new User(firstName: 'Amy', lastName: 'A', username: 'amy', active: true))
        save(new User(firstName: 'Old', lastName: 'O', username: 'old', active: false))
        Person bob = save(new Person(firstName: 'Bob', lastName: 'B', active: true, email: 'bob@example.org'))

        expect:
        texts(applyTemplate('<g:selectUser name="u"/>')) == ['Amy A (amy)', 'Zed Z (zed)']
        texts(applyTemplate('<g:selectPerson name="p"/>')) == ['Amy A', 'Bob B', 'Zed Z']
        options(applyTemplate('<g:selectRecipient name="r"/>'))*.value == ['bob@example.org', 'zed@example.org']
        options(applyTemplate('<g:selectPersonViaAjax name="p" value="${v}"/>', [v: bob.id]))*.value == [bob.id]
        options(applyTemplate('<g:selectPersonViaAjax name="p"/>')).isEmpty()
    }

    void 'selectBinLocation lists the bins of a location that supports bins'() {
        given:
        save(new Location(name: 'b-2', locationType: binType, parentLocation: depot, active: true))
        save(new Location(name: 'A-1', locationType: binType, parentLocation: depot, active: true))
        save(new Location(name: 'gone', locationType: binType, parentLocation: depot, active: false))

        expect:
        texts(applyTemplate('<g:selectBinLocation name="bin"/>')) == ['A-1', 'b-2']
    }

    void 'bin selects fall back to a hidden field when the location does not support bins'() {
        given:
        Location clinic = save(new Location(name: 'Clinic', locationType: depotType,
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id] as Set))
        session.warehouse = clinic

        when:
        String html = applyTemplate("<g:${tag} id='bin' name='binId' value='v1'/>")

        then:
        html.contains('default.notSupported.label')
        html.contains('type="hidden"')
        html.contains('name="binId"')
        html.contains('value="v1"')

        where:
        tag << ['selectBinLocation', 'selectBinLocationWithOptGroup']
    }

    void 'selectBinLocationWithOptGroup groups bins by zone'() {
        given:
        LocationType zoneType = save(new LocationType(name: 'Zone', locationTypeCode: LocationTypeCode.ZONE))
        Location zone = save(new Location(name: 'Cold', locationType: zoneType, parentLocation: depot))
        Location inZone = save(new Location(name: 'C-1', locationType: binType, parentLocation: depot, zone: zone))
        Location loose = save(new Location(name: 'A-1', locationType: binType, parentLocation: depot))
        tagLib.locationService = Mock(LocationService)

        when:
        String html = applyTemplate('<g:selectBinLocationWithOptGroup name="bin" value="${v}"/>', [v: inZone.id])

        then:
        1 * tagLib.locationService.getBinLocations(depot) >> [inZone, loose]
        html.contains('<optgroup label="Cold">')
        options(html).collect { [it.value, it.text, it.selected] } ==
                [[inZone.id, 'Cold: C-1', true], [loose.id, 'A-1', false]]
    }

    void 'selectBinLocationByLocation and selectZoneLocationByLocation use the location service'() {
        given:
        Location bin = save(new Location(name: 'A-1', locationType: binType, parentLocation: depot))
        Location zone = save(new Location(name: 'Zone 1', locationType: binType, parentLocation: depot))
        tagLib.locationService = Mock(LocationService)

        when:
        String bins = applyTemplate('<g:selectBinLocationByLocation id="${id}" name="bin"/>', [id: depot.id])
        String zones = applyTemplate('<g:selectZoneLocationByLocation id="${id}" name="zone"/>', [id: depot.id])
        String none = applyTemplate('<g:selectZoneLocationByLocation id="nope" name="zone"/>')

        then:
        1 * tagLib.locationService.getBinLocations(depot) >> [bin]
        1 * tagLib.locationService.getZones(depot) >> [zone]
        options(bins).collect { [it.value, it.text] } == [['', ''], [bin.id, 'A-1']]
        bins.contains('chzn-select-deselect')
        options(zones).collect { [it.value, it.text] } == [['null', ''], [zone.id, 'Zone 1']]
        options(none).collect { it.value } == ['null']
    }

    void 'selectLocation lists locations from the service, optionally filtered by activity'() {
        given:
        Location shipper = save(new Location(name: 'Sender', locationType: depotType,
                supportedActivities: [ActivityCode.SEND_STOCK.id] as Set))
        tagLib.locationService = Mock(LocationService)

        when:
        String all = applyTemplate('<g:selectLocation name="l"/>')
        String sending = applyTemplate('<g:selectLocation name="l" activityCode="${code}"/>', [code: ActivityCode.SEND_STOCK])
        String given = applyTemplate('<g:selectLocation name="l" from="${[]}"/>')

        then:
        2 * tagLib.locationService.getLocations(null, [withOrganization: null]) >> [shipper, depot]
        texts(all) == ['Main Depot', 'Sender']
        texts(sending) == ['Sender']
        options(given).isEmpty()
    }

    void 'selectLocationViaAjax only renders the selected location'() {
        expect:
        options(applyTemplate('<g:selectLocationViaAjax name="l" value="${v}"/>', [v: depot.id]))*.value == [depot.id]
        options(applyTemplate('<g:selectLocationViaAjax name="l"/>')).isEmpty()
    }

    void 'selectLocationWithOptGroup groups all locations by type'() {
        given:
        Location bin = save(new Location(name: 'A-1', locationType: binType))
        tagLib.locationService = Mock(LocationService)

        when:
        String html = applyTemplate('<g:selectLocationWithOptGroup name="l" noSelection="${[\'\': \'Choose\']}"/>')

        then:
        1 * tagLib.locationService.getAllLocations() >> [depot, bin]
        html.indexOf('<optgroup label="Depot">') < html.indexOf('<optgroup label="Bin">')
        options(html)*.text == ['Choose', 'Main Depot', 'A-1']
    }

    void 'transaction, shipment and supplier location selects label locations with their type'() {
        given:
        Organization org = save(new Organization(name: 'Acme', code: 'ACME'))
        Location supplier = save(new Location(name: 'Vendor', locationType: depotType, organization: org))
        tagLib.locationService = Mock(LocationService)

        when:
        String destinations = applyTemplate('<g:selectTransactionDestination name="l"/>')
        String sources = applyTemplate('<g:selectTransactionSource name="l"/>')
        String origins = applyTemplate('<g:selectShipmentOrigin name="l"/>')
        String shipTo = applyTemplate('<g:selectShipmentDestination name="l"/>')
        String suppliers = applyTemplate('<g:selectOrderSupplier name="l"/>')

        then:
        1 * tagLib.locationService.getTransactionDestinations(depot) >> [supplier]
        1 * tagLib.locationService.getTransactionSources(depot) >> [supplier]
        1 * tagLib.locationService.getShipmentOrigins() >> [supplier]
        1 * tagLib.locationService.getShipmentDestinations() >> [supplier]
        1 * tagLib.locationService.getOrderSuppliers(depot) >> [supplier, depot]
        texts(destinations) == ['Vendor [Depot]']
        texts(sources) == ['Vendor [Depot]']
        texts(origins) == ['Vendor [Depot]']
        texts(shipTo) == ['Vendor [Depot]']
        texts(suppliers) == ['ACME - Vendor', 'Main Depot']
    }

    void 'selectTransactionType hides transaction types the current location does not support'() {
        given:
        [Constants.CONSUMPTION_TRANSACTION_TYPE_ID, Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID,
         Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID,
         Constants.TRANSFER_IN_TRANSACTION_TYPE_ID].each { String id ->
            TransactionType type = new TransactionType(name: "type-${id}", transactionCode: TransactionCode.DEBIT)
            type.id = id
            type.save(validate: false, flush: true)
        }

        when:
        List<String> values = options(applyTemplate('<g:selectTransactionType name="t"/>'))*.value

        then: 'the depot supports none of consume, adjust or send'
        values == [Constants.TRANSFER_IN_TRANSACTION_TYPE_ID]

        when:
        depot.supportedActivities = [ActivityCode.CONSUME_STOCK.id, ActivityCode.ADJUST_INVENTORY.id,
                                     ActivityCode.SEND_STOCK.id] as Set
        save(depot)
        values = options(applyTemplate('<g:selectTransactionType name="t" transactionCode="${c}"/>',
                [c: TransactionCode.DEBIT]))*.value

        then:
        values.size() == 5
    }

    void 'selectRequisitionTemplate lists templates of the current location sorted by destination'() {
        given:
        Location ward = save(new Location(name: 'Ward', locationType: depotType))
        Location annex = save(new Location(name: 'Annex', locationType: depotType))
        Requisition t1 = save(new Requisition(name: 'Weekly', origin: depot, destination: ward,
                commodityClass: org.pih.warehouse.requisition.CommodityClass.MEDICATION))
        Requisition t2 = save(new Requisition(name: 'Monthly', origin: depot, destination: annex))
        tagLib.requisitionService = Mock(RequisitionService)

        when:
        String html = applyTemplate('<g:selectRequisitionTemplate name="r"/>')

        then:
        1 * tagLib.requisitionService.getAllRequisitionTemplates({ it.isTemplate && it.origin == depot }, { it.max == -1 && it.offset == 0 }) >> [t1, t2]
        texts(html)[0] == 'Monthly - Main Depot - Annex ()'
        texts(html)[1].startsWith('Weekly - Main Depot - Ward (')
    }

    void 'selectContainer renders the pending shipments template'() {
        given:
        tagLib.shipmentService = Mock(ShipmentService)
        views['/taglib/_selectContainer.gsp'] = '${attrs.from.size()} shipments, placeholder ${attrs["data-placeholder"]}'

        when:
        String html = applyTemplate('<g:selectContainer name="c"/>')

        then:
        1 * tagLib.shipmentService.getPendingShipments(depot) >> [new Shipment(name: 'one')]
        html == '1 shipments, placeholder Select an Option'
    }

    void 'selectTimezone lists the available timezones'() {
        when:
        String html = applyTemplate('<g:selectTimezone name="tz" value="UTC"/>')

        then:
        html.contains('chzn-select-deselect')
        options(html).find { it.selected }.value == 'UTC'
        options(html)*.value.contains('America/New_York')
    }

    @Ignore("upstream 635b225b0 (#6152) dropped the localization mode locale 'ach' from supportedLocales")
    void 'selectLocale lists the supported locales except the localization mode locale'() {
        when:
        List<String> values = options(applyTemplate('<g:selectLocale name="locale"/>'))*.value

        then:
        values.contains('en')
        values.contains('fr')
        !values.contains('ach')
        values == values.sort(false)
    }

    static class Kind implements Comparable<Kind> {
        String id
        String name
        int compareTo(Kind other) { id <=> other.id }
    }

    static class Item {
        Kind kind
        String label
        String toString() { "item ${label}" }
    }

    void 'selectWithOptGroup supports keys, plain option values and disabled selects'() {
        given:
        Kind warm = new Kind(id: 'warm', name: 'Warm')
        def items = [new Item(kind: warm, label: 'red'), new Item(kind: warm, label: 'orange')]

        when:
        String html = applyTemplate(
                '<g:selectWithOptGroup name="c" from="${items}" keys="${[\'k1\', \'k2\']}" groupBy="kind" optionValue="label" value="k2" disabled="true"/>',
                [items: items])

        then:
        html.startsWith('<select name="c" ')
        html.contains('disabled="disabled"')
        html.contains('id="c"')
        html.contains('<optgroup label="Warm">')
        options(html).collect { [it.value, it.text, it.selected] } == [['k1', 'red', false], ['k2', 'orange', true]]
    }

    void 'selectWithOptGroup renders grouped and ungrouped items with closures and multiple values'() {
        given:
        Kind warm = new Kind(id: 'warm', name: 'Warm')
        Kind cold = new Kind(id: 'cold', name: 'Cold')
        def items = [new Item(kind: warm, label: 'red'), new Item(kind: cold, label: 'blue'), new Item(label: 'grey')]

        when:
        String html = applyTemplate(
                '<g:selectWithOptGroup name="c" from="${items}" groupBy="kind" optionKey="${{ it.label }}" value="${[\'red\', \'grey\']}" optionValue="${{ it.label.toUpperCase() }}"/>',
                [items: items])

        then:
        html.contains('multiple="multiple"')
        html.indexOf('label="Cold"') < html.indexOf('label="Warm"')
        options(html).collect { [it.value, it.text, it.selected] } ==
                [['blue', 'BLUE', false], ['red', 'RED', true], ['grey', 'GREY', true]]
    }

    void 'selectWithOptGroup labels options from a message prefix or the item itself'() {
        given:
        [Locale.default, Locale.ENGLISH, Locale.US, request.locale].each {
            messageSource.addMessage('color.red', it, 'Red color')
        }
        Kind warm = new Kind(id: 'warm', name: 'Warm')
        def items = [new Item(kind: warm, label: 'red'), new Item(kind: warm, label: 'teal')]

        when:
        String html = applyTemplate(
                '<g:selectWithOptGroup name="c" from="${items}" groupBy="kind" optionKey="label" valueMessagePrefix="color"/>',
                [items: items])

        then:
        texts(html)[0] == 'Red color'
        texts(html)[1] in ['teal', 'color.teal']

        when:
        html = applyTemplate('<g:selectWithOptGroup name="c" from="${items}" groupBy="kind"/>', [items: items])

        then:
        texts(html) == ['item red', 'item teal']
    }
}
