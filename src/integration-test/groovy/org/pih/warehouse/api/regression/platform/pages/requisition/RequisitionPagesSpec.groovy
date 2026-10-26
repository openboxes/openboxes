package org.pih.warehouse.api.regression.platform.pages.requisition

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The legacy requisition server pages (RequisitionController, RequisitionItemController,
 * RequisitionTemplateController): a ward requests stock from the main facility, which reviews, picks and issues it;
 * stock list templates are edited, imported and exported.
 */
class RequisitionPagesSpec extends RegressionApiSpec {

    ServerPages pages

    Location ward

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
        ward = new LocationTestBuilder().name(randomUtil.randomStringFieldValue('Ward')).asFacility().build(true)
    }

    Product newProduct(String prefix = 'Requested product') {
        return Product.withNewTransaction {
            new ProductTestBuilder()
                    .name(randomUtil.randomStringFieldValue(prefix))
                    .category(rootCategory)
                    .build(true)
        }
    }

    /** A requisition from the ward to the main facility with the given lines; returns its id. */
    String requisition(Map<Product, Integer> lines = [(product): 5], Map extra = [:]) {
        return Requisition.withNewTransaction {
            Requisition requisition = new Requisition([
                    name                 : randomUtil.randomStringFieldValue('Requisition'),
                    requestNumber        : randomUtil.randomStringFieldValue('REQ'),
                    type                 : RequisitionType.STOCK,
                    status               : RequisitionStatus.CREATED,
                    origin               : Location.get(facility.id),
                    destination          : Location.get(ward.id),
                    requestedBy          : Person.get(User.findByUsername(username).id),
                    dateRequested        : new Date(),
                    requestedDeliveryDate: new Date() + 1] + extra)
            lines.eachWithIndex { Product requested, Integer quantity, int index ->
                requisition.addToRequisitionItems(new RequisitionItem(product: Product.get(requested.id),
                        quantity: quantity, substitutable: false, orderIndex: index))
            }
            requisition.save(failOnError: true, flush: true)
            requisition.id
        }
    }

    def <T> T withRequisition(String id, Closure<T> closure) {
        return Requisition.withNewSession { closure(Requisition.get(id)) }
    }

    String firstItemId(String requisitionId) {
        return withRequisition(requisitionId) { it.requisitionItems.sort { it.orderIndex }.first().id }
    }

    void 'the requisition list'() {
        given:
        requisition()

        expect:
        page(pages.get('/requisition/list')) != null
        redirectPath(pages.get('/requisition/index')) == '/requisition/list'
    }

    void 'the header and details are saved'() {
        given:
        String id = requisition()

        when:
        Response header = pages.post("/requisition/saveHeader/${id}", [description: 'Monthly request'])
        Response details = pages.post("/requisition/saveDetails/${id}", [description: 'Monthly request, revised',
                redirectAction: 'edit'])

        then:
        redirectPath(header) == "/requisition/edit/${id}"
        redirectPath(details) == "/requisition/edit/${id}"
        withRequisition(id) { it.description } == 'Monthly request, revised'
    }

    void 'requisitions export as csv'() {
        given:
        String id = requisition()
        String requestNumber = withRequisition(id) { it.requestNumber }

        when:
        Response requisitions = pages.get('/requisition/exportRequisitions')
        Response items = pages.get('/requisition/exportRequisitionItems')
        Response exported = pages.get('/requisition/export')

        then:
        requisitions.statusCode == 200
        requisitions.contentType.startsWith('text/csv')
        requisitions.body.asString().contains(requestNumber)
        items.statusCode == 200
        items.body.asString().contains(product.productCode)
        exported.statusCode == 200
        exported.body.asString().contains(requestNumber)
    }

    void 'requisition items are reviewed one by one'() {
        given:
        Product other = newProduct()
        String id = requisition([(product): 5, (other): 2])
        String itemId = firstItemId(id)

        expect:
        page(pages.get("/requisition/showRequisitionItems/${id}")).contains(product.productCode)
        page(pages.get("/requisition/editRequisitionItem/${id}", ['requisitionItem.id': itemId, actionType: 'show']))
                .contains(product.productCode)
        redirectPath(pages.get("/requisition/nextRequisitionItem/${id}", ['requisitionItem.id': itemId])) ==
                "/requisition/editRequisitionItem/${id}"
        redirectPath(pages.get("/requisition/previousRequisitionItem/${id}", ['requisitionItem.id': itemId])) ==
                "/requisition/editRequisitionItem/${id}"

        when: 'the quantity is changed'
        String changed = page(pages.post("/requisition/saveRequisitionItem/${id}", ['requisitionItem.id': itemId,
                'product.id': product.id, quantity: '3', reasonCode: 'STOCKOUT', comments: 'Short']))

        then:
        changed.contains(product.productCode)

        when:
        String undone = page(pages.post("/requisition/undoChanges/${id}", ['requisitionItem.id': itemId]))

        then:
        undone != null

        when:
        String approved = page(pages.post("/requisition/approveQuantity/${id}", ['requisitionItem.id': itemId]))
        String saved = page(pages.post("/requisition/saveRequisitionItem/${id}", ['requisitionItem.id': itemId,
                'product.id': product.id, quantity: '5']))
        String undoneFromList = page(pages.post("/requisition/undoChangesFromList/${id}",
                ['requisitionItem.id': itemId]))

        then:
        approved != null
        saved != null
        undoneFromList.contains(product.productCode)

        when: 'no product chosen'
        String refused = page(pages.post("/requisition/saveRequisitionItem/${id}", ['requisitionItem.id': itemId,
                quantity: '5']))

        then:
        refused != null
    }

    void 'saving requisition items from JSON reports errors'() {
        when:
        Response saved = pages.postJson('/requisition/saveRequisitionItems', '{}')

        then:
        saved.statusCode == 200
        saved.jsonPath().getBoolean('success') == false
    }

    void 'stock list items are added, sorted, imported, exported and removed'() {
        given:
        Product second = newProduct('Second item')
        String id = requisition([:], [isTemplate: true])

        when:
        Response added = pages.post("/requisitionTemplate/addToRequisitionItems/${id}",
                [multipleProductCodes: "${product.productCode}, ${second.productCode}, NO-SUCH-CODE"])

        then:
        redirectPath(added) == "/requisitionTemplate/edit/${id}"
        withRequisition(id) { it.requisitionItems*.product*.productCode.sort() } ==
                [product.productCode, second.productCode].sort()

        expect:
        redirectPath(pages.get("/requisitionTemplate/changeSortOrderAlpha/${id}")) == "/requisitionTemplate/edit/${id}"
        redirectPath(pages.get("/requisitionTemplate/changeSortOrderChrono/${id}")) == "/requisitionTemplate/edit/${id}"

        when:
        Response exported = pages.get("/requisitionTemplate/export/${id}")

        then:
        exported.statusCode == 200
        exported.contentType.startsWith('text/csv')
        exported.body.asString().readLines()[0].startsWith('"Product Code","Product Name","Quantity","UOM"')
        exported.body.asString().contains(second.productCode)
        pages.get('/requisitionTemplate/export/unknown').statusCode == 404

        when: 'quantities are imported'
        String preview = page(pages.post("/requisitionTemplate/importData/${id}",
                [csv: "Product Code,Product Name,Quantity,UOM\n${product.productCode},x,7,EA\nNO-SUCH-CODE,x,1,EA\n${second.productCode},x,many,EA"]))
        Response imported = pages.post("/requisitionTemplate/doImport/${id}")

        then:
        preview.contains(product.productCode)
        redirectPath(imported) == "/requisitionTemplate/batch/${id}"
        withRequisition(id) { it.requisitionItems.find { it.product.id == product.id }.quantity } == 7

        when:
        String removedId = withRequisition(id) { it.requisitionItems.find { it.product.id == second.id }.id }
        Response removed = pages.post("/requisitionTemplate/removeFromRequisitionItems/${id}",
                ['requisitionItem.id': removedId])

        then:
        redirectPath(removed) == "/requisitionTemplate/edit/${id}"
        withRequisition(id) { it.requisitionItems*.product*.id } == [product.id]
    }

    void 'the delivery note prints the requisition'() {
        given:
        String id = requisition()

        expect:
        page(pages.get("/deliveryNote/print/${id}", [sortOrder: 'sortOrder']))
                .contains(withRequisition(id) { it.requestNumber })
    }

    @PendingFeature(reason = 'the template chooser, pending items and change pages fail to render')
    void 'the template chooser, pending items and the change page render'() {
        given:
        requisition([(product): 3], [isTemplate: true])
        String id = requisition([(product): 5])
        String itemId = firstItemId(id)

        expect:
        page(pages.get('/requisition/chooseTemplate')) != null
        page(pages.get('/requisitionItem/listPending')) != null
        page(pages.get("/requisitionItem/change/${itemId}")).contains(product.productCode)
        redirectPath(pages.get('/requisitionItem/change/unknown')) == '/requisitionItem/list'
    }

    @PendingFeature(reason = 'Generate picklist answers 500')
    void 'generating a picklist goes back to the picking page with a message'() {
        given:
        String id = requisition()
        Requisition.withNewTransaction {
            Requisition requisition = Requisition.get(id)
            requisition.verifiedBy = Person.get(User.findByUsername(username).id)
            requisition.save(failOnError: true, flush: true)
        }

        when:
        Response generated = pages.get("/requisition/generatePicklist/${id}")

        then:
        redirectPath(generated) == "/requisition/pick/${id}"

        when: 'the browser follows the redirect'
        String pick = page(pages.get("/requisition/pick/${id}"))

        then:
        pick.contains('Generating a picklist from this menu is not supported. Pick the items on this page instead.')
    }
}
