// Companion spec: extra features for upstream's RequisitionItemSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.requisition

import spock.lang.PendingFeature

import org.pih.warehouse.requisition.*
import grails.testing.gorm.DomainUnitTest
import grails.validation.ValidationException
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RequisitionItemMoreSpec extends Specification implements DomainUnitTest<RequisitionItem> {

    Class[] getDomainClassesToMock() {
        return [RequisitionItem, Requisition, Picklist, PicklistItem] as Class[]
    }

    void setup() {
        domain.requisition = new Requisition()
    }

    void "RequisitionItem.getStatus() should return APPROVED for a child item even when it is canceled"() {
        given:
        domain.parentRequisitionItem = new RequisitionItem()
        domain.quantity = 2
        domain.quantityCanceled = 2

        expect:
        domain.isCanceled()
        domain.status == RequisitionItemStatus.APPROVED
    }

    void "RequisitionItem.getStatus() should return #status for quantity #quantity, canceled #quantityCanceled and approved #quantityApproved"() {
        given:
        domain.quantity = quantity
        domain.quantityCanceled = quantityCanceled
        domain.quantityApproved = quantityApproved

        expect:
        domain.status == status

        where:
        quantity | quantityCanceled | quantityApproved || status
        5        | 0                | 0                || RequisitionItemStatus.PENDING
        5        | 0                | 5                || RequisitionItemStatus.APPROVED
        5        | 5                | 0                || RequisitionItemStatus.CANCELED
        5        | 2                | 3                || RequisitionItemStatus.APPROVED
        5        | 2                | 0                || RequisitionItemStatus.PENDING
    }

    void "RequisitionItem.getStatus() should prefer SUBSTITUTED over CHANGED when a substitution item exists"() {
        given:
        domain.quantity = 5
        domain.quantityCanceled = 5
        domain.substitutionItem = new RequisitionItem()

        expect:
        domain.isChanged()
        domain.isSubstituted()
        domain.status == RequisitionItemStatus.SUBSTITUTED
    }

    void "RequisitionItem.getChange() and getSubstitution() should prefer #preferredByChange and #preferredBySubstitution respectively"() {
        given:
        RequisitionItem modification = new RequisitionItem(comment: 'modification')
        RequisitionItem substitution = new RequisitionItem(comment: 'substitution')
        domain.modificationItem = hasModification ? modification : null
        domain.substitutionItem = hasSubstitution ? substitution : null

        expect:
        domain.change?.comment == preferredByChange
        domain.substitution?.comment == preferredBySubstitution

        where:
        hasModification | hasSubstitution || preferredByChange | preferredBySubstitution
        true            | true            || 'modification'    | 'substitution'
        true            | false           || 'modification'    | 'modification'
        false           | true            || 'substitution'    | 'substitution'
        false           | false           || null              | null
    }

    void "RequisitionItem.undoChanges() should reset the cancellation and approval fields"() {
        given:
        domain.quantity = 5
        domain.quantityApproved = 3
        domain.quantityCanceled = 2
        domain.cancelReasonCode = ReasonCode.STOCKOUT.name()
        domain.cancelComments = 'comment'

        when:
        domain.undoChanges()

        then:
        domain.quantityApproved == 0
        domain.quantityCanceled == 0
        domain.cancelReasonCode == null
        domain.cancelComments == null
        domain.status == RequisitionItemStatus.PENDING
    }

    void "RequisitionItem.undoChanges() on a child item should undo the changes of its parent instead"() {
        given:
        RequisitionItem parent = new RequisitionItem(quantity: 5, quantityCanceled: 5, cancelReasonCode: 'STOCKOUT')
        parent.requisition = domain.requisition
        domain.parentRequisitionItem = parent
        domain.quantityApproved = 4

        when:
        domain.undoChanges()

        then:
        parent.quantityCanceled == 0
        parent.cancelReasonCode == null
        domain.quantityApproved == 4
    }

    void "RequisitionItem.undoChanges() should remove a quantity change and a substitution from the requisition"() {
        given:
        Requisition requisition = domain.requisition
        domain.product = productWithId('p1')
        domain.quantity = 10
        requisition.addToRequisitionItems(domain)
        domain.save(validate: false, flush: true)

        and:
        RequisitionItem modification = new RequisitionItem(product: domain.product, quantity: 4)
        requisition.addToRequisitionItems(modification)
        modification.save(validate: false, flush: true)
        domain.modificationItem = modification

        and:
        RequisitionItem substitution = new RequisitionItem(product: productWithId('p2'), quantity: 1,
                requisitionItemType: RequisitionItemType.SUBSTITUTION)
        requisition.addToRequisitionItems(substitution)
        domain.addToRequisitionItems(substitution)
        substitution.save(validate: false, flush: true)
        domain.substitutionItem = substitution

        when:
        domain.undoChanges()

        then:
        domain.modificationItem == null
        domain.substitutionItem == null
        !domain.requisitionItems
        requisition.requisitionItems == [domain] as Set
        modification.disableRefresh
        substitution.disableRefresh
    }

    void "RequisitionItem.changeQuantity() should create a #expectedType modification item for the new quantity"() {
        given:
        ProductPackage originalPackage = new ProductPackage(name: 'original', quantity: 10)
        ProductPackage newPackage = changePackage ? new ProductPackage(name: 'new', quantity: 20) : null
        domain.product = productWithId('p1')
        domain.productPackage = originalPackage
        domain.quantity = 5
        domain.orderIndex = 7

        when:
        domain.changeQuantity(3, newPackage, ReasonCode.STOCKOUT.name(), 'comment')

        then:
        domain.quantityCanceled == 5
        domain.cancelReasonCode == ReasonCode.STOCKOUT.name()
        domain.isChanged()
        // OBAF: getStatus() reports REDUCED/INCREASED before CHANGED when the modification lowers/raises the quantity
        domain.status == RequisitionItemStatus.REDUCED

        and:
        RequisitionItem modification = domain.modificationItem
        modification.requisitionItemType == expectedType
        modification.parentRequisitionItem.is(domain)
        modification.requisition.is(domain.requisition)
        modification.product.is(domain.product)
        modification.productPackage.name == expectedPackageName
        modification.quantity == 3
        modification.quantityApproved == 3
        modification.orderIndex == 7

        where:
        changePackage || expectedType                        | expectedPackageName
        false         || RequisitionItemType.QUANTITY_CHANGE | 'original'
        true          || RequisitionItemType.PACKAGE_CHANGE  | 'new'
    }

    void "RequisitionItem.changeQuantity() should cancel the whole item without a modification when the new quantity is zero"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5

        when:
        domain.changeQuantity(0, ReasonCode.STOCKOUT.name(), 'none left')

        then:
        domain.modificationItem == null
        domain.quantityCanceled == 5
        domain.cancelComments == 'none left'
        domain.isCanceled()
        domain.status == RequisitionItemStatus.CANCELED
    }

    void "RequisitionItem.changeQuantity() should reject an unchanged quantity and package"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5

        when:
        domain.changeQuantity(5, ReasonCode.STOCKOUT.name(), 'comment')

        then:
        ValidationException exception = thrown()
        domain.errors.getFieldError('quantity').code == 'requisitionItem.mustChangeQuantityOrPackage.message'
        domain.modificationItem == null
        domain.quantityCanceled == null
    }

    void "RequisitionItem.chooseSubstitute() should cancel the item and add a substitution item"() {
        given:
        ProductPackage originalPackage = new ProductPackage(name: 'original', quantity: 10)
        Product substitute = productWithId('p2')
        domain.product = productWithId('p1')
        domain.productPackage = originalPackage
        domain.quantity = 5
        domain.orderIndex = 3

        when:
        domain.chooseSubstitute(substitute, null, 4, ReasonCode.SUBSTITUTION.name(), 'substituted', sortOrder)

        then:
        domain.quantityCanceled == 5
        domain.cancelReasonCode == ReasonCode.SUBSTITUTION.name()
        domain.isSubstituted()
        domain.hasSubstitution()
        domain.status == RequisitionItemStatus.SUBSTITUTED

        and:
        RequisitionItem substitution = domain.substitutionItem
        domain.requisitionItems == [substitution] as Set
        domain.substitutionItems == [substitution] as Set
        substitution.requisitionItemType == RequisitionItemType.SUBSTITUTION
        substitution.product.is(substitute)
        substitution.productPackage.is(originalPackage)
        substitution.quantity == 4
        substitution.quantityApproved == 4
        substitution.orderIndex == expectedOrderIndex
        substitution.parentRequisitionItem.is(domain)
        substitution.isSubstitution()

        where:
        sortOrder || expectedOrderIndex
        null      || 3
        9         || 9
    }

    void "RequisitionItem.chooseSubstitute() should reject #field when product is #newProduct, quantity is #newQuantity and reason code is #reasonCode"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5

        when:
        domain.chooseSubstitute(newProduct, null, newQuantity, reasonCode, 'comment')

        then:
        thrown(ValidationException)
        domain.errors.getFieldError(field) != null
        domain.substitutionItem == null
        domain.quantityCanceled == null

        where:
        newProduct          | newQuantity | reasonCode                     || field
        null                | 1           | ReasonCode.SUBSTITUTION.name() || 'product'
        productWithId('p2') | 0           | ReasonCode.SUBSTITUTION.name() || 'quantity'
        productWithId('p2') | -1          | ReasonCode.SUBSTITUTION.name() || 'quantity'
        productWithId('p2') | 1           | null                           || 'cancelReasonCode'
        productWithId('p2') | 1           | ''                             || 'cancelReasonCode'
    }

    void "RequisitionItem.cancelQuantity() should cancel the full quantity and remove the picklist items"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5
        domain.save(validate: false, flush: true)
        PicklistItem picklistItem = addPick(domain, 2)
        Picklist picklist = picklistItem.picklist
        domain.addToPicklistItems(picklistItem)

        when:
        domain.cancelQuantity(ReasonCode.STOCKOUT.name(), 'gone')

        then:
        domain.quantityCanceled == 5
        domain.cancelReasonCode == ReasonCode.STOCKOUT.name()
        domain.cancelComments == 'gone'
        !domain.picklistItems
        !picklist.picklistItems
        PicklistItem.count() == 0
    }

    void "RequisitionItem.cancelQuantity() should reject an item that is already canceled"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5
        domain.quantityCanceled = 5
        domain.cancelReasonCode = ReasonCode.STOCKOUT.name()

        when:
        domain.cancelQuantity(ReasonCode.STOCKOUT.name(), 'again')

        then:
        thrown(ValidationException)
        domain.errors.globalErrors*.code == ['requisitionItem.alreadyCancelled.message']
        domain.cancelComments == null
    }

    void "RequisitionItem.cancelQuantity() should reject a missing reason code"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5

        when:
        domain.cancelQuantity(null, 'comment')

        then:
        thrown(ValidationException)
        domain.errors.getFieldError('cancelReasonCode').code == 'requisitionItem.reasonCode.invalid'
        domain.quantityCanceled == null
    }

    void "RequisitionItem.validate() should require a cancel reason code when quantity canceled is #quantityCanceled and reason is #reasonCode"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5
        domain.quantityCanceled = quantityCanceled
        domain.cancelReasonCode = reasonCode

        expect:
        domain.validate() == valid
        domain.errors.hasFieldErrors('quantityCanceled') == !valid

        where:
        quantityCanceled | reasonCode || valid
        null             | null       || true
        0                | null       || true
        1                | null       || false
        1                | ''         || false
        1                | 'STOCKOUT' || true
    }

    void "RequisitionItem.calculateQuantityPicked() should sum the picked quantities of the item"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 10
        domain.save(validate: false, flush: true)
        addPick(domain, 3)
        addPick(domain, 4)

        expect:
        domain.calculateQuantityPicked() == 7
        domain.totalQuantityPicked() == 7
        domain.calculateQuantityRemaining() == 3
        domain.calculatePercentagePicked() == 70
        domain.calculatePercentageRemaining() == 30
        domain.isPartiallyFulfilled()
        !domain.isFulfilled()
    }

    void "RequisitionItem.calculateQuantityPicked() should return zero when nothing is picked"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 10
        domain.save(validate: false, flush: true)

        expect:
        domain.calculateQuantityPicked() == 0
        domain.calculateQuantityRemaining() == 10
        !domain.isPartiallyFulfilled()
        !domain.isFulfilled()
    }

    void "RequisitionItem.calculateQuantityPicked() should sum the picks of the substitution items instead of its own"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 10
        domain.save(validate: false, flush: true)
        addPick(domain, 9)

        and:
        RequisitionItem substitution1 = new RequisitionItem(requisitionItemType: RequisitionItemType.SUBSTITUTION)
        RequisitionItem substitution2 = new RequisitionItem(requisitionItemType: RequisitionItemType.SUBSTITUTION)
        [substitution1, substitution2].each {
            domain.addToRequisitionItems(it)
            it.save(validate: false, flush: true)
        }
        addPick(substitution1, 2)
        addPick(substitution2, 5)

        expect:
        domain.calculateQuantityPicked() == 7
    }

    @PendingFeature(reason = 'OBAF adds a null sum for an unpicked substitution; the swallowed NPE drops later picks')
    void "RequisitionItem.calculateQuantityPicked() should count a substitution without picks as nothing, whatever its position"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 10
        domain.save(validate: false, flush: true)

        and:
        // the unpicked substitution sorts first or last (order index and id agree, whichever the set sorts by)
        RequisitionItem unpicked = new RequisitionItem(requisitionItemType: RequisitionItemType.SUBSTITUTION,
                orderIndex: unpickedFirst ? 0 : 1)
        unpicked.id = unpickedFirst ? 'sub-a' : 'sub-z'
        RequisitionItem picked = new RequisitionItem(requisitionItemType: RequisitionItemType.SUBSTITUTION,
                orderIndex: unpickedFirst ? 1 : 0)
        picked.id = unpickedFirst ? 'sub-z' : 'sub-a'
        [unpicked, picked].each {
            domain.addToRequisitionItems(it)
            it.save(validate: false, flush: true)
        }
        addPick(picked, 5)

        expect:
        domain.calculateQuantityPicked() == 5

        where:
        unpickedFirst << [true, false]
    }

    void "RequisitionItem.isFulfilled() should be true when the picked quantity covers the total quantity"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 4
        domain.save(validate: false, flush: true)
        addPick(domain, 4)

        expect:
        domain.isFulfilled()
        !domain.isPartiallyFulfilled()
        domain.calculateQuantityRemaining() == 0
    }

    void "RequisitionItem.calculateQuantityRemaining() should never be negative"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 4
        domain.quantityCanceled = 2
        domain.save(validate: false, flush: true)
        addPick(domain, 3)

        expect:
        domain.calculateQuantityRemaining() == 0
    }

    void "RequisitionItem.retrievePicklistItemsSortedByBinName() should order picks by bin location name"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 4
        domain.save(validate: false, flush: true)
        addPick(domain, 1, 'B-2')
        addPick(domain, 2, 'A-1')
        addPick(domain, 3, 'C-3')

        expect:
        domain.retrievePicklistItems()*.quantity.sort() == [1, 2, 3]
        domain.retrievePicklistItemsSortedByBinName()*.quantity == [2, 1, 3]
    }

    void "RequisitionItem.getPickablePicklistItems() should only return picks from pickable inventory and bins"() {
        given:
        InventoryItem recalled = Stub(InventoryItem) { isPickable() >> false }
        PicklistItem pickable = new PicklistItem(quantity: 1)
        PicklistItem notPickable = new PicklistItem(quantity: 2, inventoryItem: recalled)
        domain.picklistItems = [pickable, notPickable] as Set

        expect:
        domain.pickablePicklistItems*.quantity == [1]
    }

    void "RequisitionItem.calculatePercentageCanceled() and calculatePercentageCompleted() should use the total quantity"() {
        given:
        domain.quantity = quantity
        domain.quantityCanceled = quantityCanceled
        domain.quantityApproved = quantityApproved

        expect:
        domain.calculatePercentageCanceled() == percentageCanceled
        domain.calculatePercentageCompleted() == percentageCompleted
        domain.isCompleted() == (percentageCompleted >= 100)

        where:
        quantity | quantityCanceled | quantityApproved || percentageCanceled | percentageCompleted
        null     | null             | null             || 0                  | 0
        0        | 0                | 0                || 0                  | 0
        4        | 1                | 0                || 25                 | 25
        4        | 1                | 3                || 25                 | 100
        4        | 0                | 2                || 0                  | 50
    }

    void "RequisitionItem.isCanceledDuringPick() should use the picks of the item itself when there is no modification"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 4
        domain.requisition.status = RequisitionStatus.PICKED
        domain.save(validate: false, flush: true)
        if (picked) {
            addPick(domain, picked)
        }

        expect:
        domain.isCanceledDuringPick() == canceled

        where:
        picked || canceled
        0      || true
        2      || false
    }

    void "RequisitionItem.isIncreased() and isReduced() should compare against the modification item quantity #modifiedQuantity"() {
        given:
        domain.quantity = 5
        domain.modificationItem = new RequisitionItem(quantity: modifiedQuantity)

        expect:
        domain.isIncreased() == increased
        domain.isReduced() == reduced

        where:
        modifiedQuantity || increased | reduced
        8                || true      | false
        2                || false     | true
        5                || false     | false
    }

    void "RequisitionItem.isIncreased() and isReduced() should compare against the picked quantity #picked once picking started"() {
        given:
        domain.product = productWithId('p1')
        domain.quantity = 5
        // OBAF: isIncreased()/isReduced() compare against the picks only from PICKED on (was PICKING)
        domain.requisition.status = RequisitionStatus.PICKED
        domain.save(validate: false, flush: true)
        addPick(domain, picked)

        expect:
        domain.isIncreased() == increased
        domain.isReduced() == reduced

        where:
        picked || increased | reduced
        7      || true      | false
        3      || false     | true
        5      || false     | false
    }

    void "RequisitionItem.isIncreased() and isReduced() should both be false before picking without a modification"() {
        given:
        domain.quantity = 5
        domain.requisition.status = RequisitionStatus.VERIFYING

        expect:
        !domain.isIncreased()
        !domain.isReduced()
    }

    void "RequisitionItem predicates should reflect a #description item"() {
        given:
        domain.quantity = 5
        domain.quantityApproved = quantityApproved
        domain.quantityCanceled = quantityCanceled
        domain.modificationItem = modified ? new RequisitionItem(quantity: 2) : null

        expect:
        domain.isPending() == pending
        domain.isCanceledOrSubstituted() == canceledOrSubstituted
        domain.canUndoChanges() == !pending
        domain.canApproveQuantity() == pending
        domain.canChangeQuantity() == pending
        domain.canCancelQuantity() == pending
        domain.canChooseSubstitute() == pending

        where:
        description | quantityApproved | quantityCanceled | modified || pending | canceledOrSubstituted
        'new'       | 0                | 0                | false    || true    | false
        'approved'  | 5                | 0                | false    || false   | false
        'canceled'  | 0                | 5                | false    || false   | true
        'changed'   | 0                | 5                | true     || false   | false
    }

    void "RequisitionItem.isParentRequisitionPending() and isRejected() should follow the requisition status #status"() {
        given:
        domain.requisition.status = status

        expect:
        domain.isParentRequisitionPending() == pending
        domain.isParentRequisitionRejected() == rejected
        domain.isRejected() == rejected

        where:
        status                             || pending | rejected
        RequisitionStatus.CREATED          || true    | false
        RequisitionStatus.PENDING          || true    | false
        RequisitionStatus.PENDING_APPROVAL || true    | false
        RequisitionStatus.VERIFYING        || false   | false
        RequisitionStatus.REJECTED         || false   | true
    }

    void "RequisitionItem.isQuantityIssued() should be #issued for an approved item when the requisition status is #status"() {
        given:
        domain.quantity = 5
        domain.quantityApproved = 5
        domain.requisition.status = status

        expect:
        domain.isQuantityIssued() == issued

        where:
        status                      || issued
        RequisitionStatus.ISSUED    || true
        RequisitionStatus.PICKING   || false
        RequisitionStatus.VERIFYING || false
    }

    void "RequisitionItem.isQuantityIssued() should be false for an item that is not approved"() {
        given:
        domain.quantity = 5
        domain.requisition.status = RequisitionStatus.ISSUED

        expect:
        !domain.isQuantityIssued()
    }

    void "RequisitionItem.isSubstitution() should be #substitution when the parent changed is #parentChanged and the product differs is #differentProduct"() {
        given:
        RequisitionItem parent = new RequisitionItem(product: productWithId('p1'), quantity: 5)
        if (parentChanged) {
            parent.quantityCanceled = 5
            parent.modificationItem = domain
        }
        domain.parentRequisitionItem = parent
        domain.product = differentProduct ? productWithId('p2') : productWithId('p1')

        expect:
        domain.isSubstitution() == substitution

        where:
        parentChanged | differentProduct || substitution
        true          | true             || true
        true          | false            || false
        false         | true             || false
    }

    void "RequisitionItem.isSubstitution() should be false without a parent item"() {
        expect:
        !domain.isSubstitution()
    }

    void "RequisitionItem.hasSubstitution() should be #hasSubstitution for child type #childType and substitution item #withSubstitutionItem"() {
        given:
        if (childType) {
            domain.addToRequisitionItems(new RequisitionItem(requisitionItemType: childType))
        }
        domain.substitutionItem = withSubstitutionItem ? new RequisitionItem() : null

        expect:
        domain.hasSubstitution() == hasSubstitution

        where:
        childType                           | withSubstitutionItem || hasSubstitution
        null                                | false                || false
        RequisitionItemType.QUANTITY_CHANGE | false                || false
        RequisitionItemType.SUBSTITUTION    | false                || true
        null                                | true                 || true
    }

    void "RequisitionItem.calculateQuantityRevised() and calculateQuantityRequired() should return #revised and #required"() {
        given:
        domain.quantity = 10
        domain.quantityCanceled = quantityCanceled
        domain.modificationItem = modifiedQuantity != null ? new RequisitionItem(quantity: modifiedQuantity) : null

        expect:
        domain.calculateQuantityRevised() == revised
        domain.calculateQuantityRequired() == required

        where:
        modifiedQuantity | quantityCanceled || revised | required
        6                | 10               || 6       | 6
        null             | 4                || 6       | 6
        null             | 0                || null    | 10
        null             | null             || null    | 10
    }

    void "RequisitionItem.getMonthlyDemand() should be #monthlyDemand for quantity #quantity over #period days"() {
        given:
        domain.quantity = quantity
        domain.requisition.replenishmentPeriod = period

        expect:
        domain.monthlyDemand == monthlyDemand

        where:
        quantity | period || monthlyDemand
        10       | 30     || 10
        10       | 14     || 22
        7        | 7      || 30
        10       | 0      || null
        10       | null   || null
    }

    void "RequisitionItem.getTotalCost() should multiply the quantity by the unit price when there is one"() {
        given:
        domain.quantity = 4
        domain.product = new Product(pricePerUnit: pricePerUnit)

        expect:
        domain.totalCost == totalCost

        where:
        pricePerUnit || totalCost
        2.5          || 10.0
        null         || null
        0            || null
    }

    void "RequisitionItem.getQuantityIssued() should sum the shipment items of this item"() {
        given:
        domain.id = 'ri-1'
        RequisitionItem other = itemWith('ri-2', 0, RequisitionItemType.ORIGINAL)
        other.requisition = domain.requisition
        domain.requisition.shipments = [shipmentWith([
                shipmentItem(domain, 3),
                shipmentItem(other, 50),
                shipmentItem(domain, 4),
        ])] as Set

        expect:
        domain.quantityIssued == 7
        other.quantityIssued == 50
    }

    void "RequisitionItem.getQuantityIssued() should be zero without a shipment"() {
        expect:
        domain.quantityIssued == 0
    }

    void "RequisitionItem.getQuantityIssued() should sum the quantities issued for its substitution items"() {
        given:
        domain.id = 'ri-1'
        RequisitionItem substitution1 = itemWith('ri-2', 0, RequisitionItemType.SUBSTITUTION)
        RequisitionItem substitution2 = itemWith('ri-3', 0, RequisitionItemType.SUBSTITUTION)
        [substitution1, substitution2].each {
            it.requisition = domain.requisition
            domain.addToRequisitionItems(it)
        }
        domain.requisition.shipments = [shipmentWith([
                shipmentItem(domain, 100),
                shipmentItem(substitution1, 2),
                shipmentItem(substitution2, 3),
        ])] as Set

        expect:
        domain.quantityIssued == 5
    }

    void "RequisitionItem.getQuantityAdjusted() should compare the issued quantity to the requested quantity"() {
        given:
        domain.id = 'ri-1'
        domain.quantity = 10
        RequisitionItem modification = itemWith('ri-2', 0, RequisitionItemType.QUANTITY_CHANGE)
        modification.quantity = 6
        modification.requisition = domain.requisition
        RequisitionItem untouched = itemWith('ri-3', 0, RequisitionItemType.ORIGINAL)
        untouched.quantity = 4
        untouched.requisition = domain.requisition
        domain.requisition.shipments = [shipmentWith([
                shipmentItem(modification, 6),
                shipmentItem(untouched, 5),
        ])] as Set

        when:
        domain.modificationItem = modification

        then:
        domain.quantityAdjusted == -4
        untouched.quantityAdjusted == 1
    }

    void "RequisitionItem.getQuantityAdjusted() should compare the substitution items' issued quantity to the requested quantity"() {
        given:
        domain.id = 'ri-1'
        domain.quantity = 10
        RequisitionItem substitution = itemWith('ri-2', 0, RequisitionItemType.SUBSTITUTION)
        substitution.requisition = domain.requisition
        domain.addToRequisitionItems(substitution)
        domain.requisition.shipments = [shipmentWith([shipmentItem(substitution, 12)])] as Set

        expect:
        domain.quantityAdjusted == 2
    }

    void "RequisitionItem.getShipmentItems() should return the shipment items matching the item id"() {
        given:
        domain.id = 'ri-1'
        RequisitionItem other = new RequisitionItem()
        other.id = 'ri-2'
        ShipmentItem mine = shipmentItem(domain, 1)
        domain.requisition.shipments = [shipmentWith([mine, shipmentItem(other, 2)])] as Set

        expect:
        domain.shipmentItems as List == [mine]
    }

    void "RequisitionItem.next() and previous() should walk the requisition items and wrap around"() {
        given:
        // Items are matched with ==, which for this Comparable class means compareTo(), so give them distinct ids
        RequisitionItem first = itemWith('a', 0, RequisitionItemType.ORIGINAL)
        RequisitionItem second = itemWith('b', 0, RequisitionItemType.ORIGINAL)
        RequisitionItem third = itemWith('c', 0, RequisitionItemType.ORIGINAL)
        [first, second, third].each { it.requisition = domain.requisition }
        first.comment = 'first'
        second.comment = 'second'
        third.comment = 'third'
        domain.requisition.requisitionItems = new LinkedHashSet([first, second, third])

        expect:
        first.next().comment == 'second'
        second.next().comment == 'third'
        third.next().comment == 'first'
        first.previous().comment == 'third'
        second.previous().comment == 'first'
        third.previous().comment == 'second'
    }

    void "RequisitionItem.newInstance() should return a new, empty item"() {
        given:
        domain.quantity = 5

        when:
        RequisitionItem copy = domain.newInstance()

        then:
        !copy.is(domain)
        copy.quantity == null
        copy.requisitionItemType == RequisitionItemType.ORIGINAL
    }

    void "RequisitionItem.compareTo() should sort by order index, then item type, then id"() {
        given:
        RequisitionItem a = itemWith('a', 1, RequisitionItemType.ORIGINAL)
        RequisitionItem b = itemWith('b', 0, RequisitionItemType.SUBSTITUTION)
        RequisitionItem c = itemWith('c', 0, RequisitionItemType.ORIGINAL)
        RequisitionItem d = itemWith('d', 0, RequisitionItemType.SUBSTITUTION)

        expect:
        [a, d, b, c].sort()*.id == ['c', 'b', 'd', 'a']
        a.compareTo(itemWith('a', 1, RequisitionItemType.ORIGINAL)) == 0
    }

    void "RequisitionItem.toString() should combine the product code, status, type and quantity"() {
        given:
        domain.product = new Product(productCode: 'AB12')
        domain.quantity = 3

        expect:
        domain.toString() == 'AB12:PENDING:ORIGINAL:3'
    }

    void "RequisitionItem.createFromStockMovementItem() should copy the requested line"() {
        given:
        Product product = productWithId('p1')
        InventoryItem inventoryItem = new InventoryItem(lotNumber: 'L1')
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee')
        StockMovementItem stockMovementItem = new StockMovementItem(
                id: 'smi-1',
                product: product,
                inventoryItem: inventoryItem,
                quantityRequested: 12,
                recipient: recipient,
                sortOrder: 300,
        )

        when:
        RequisitionItem item = RequisitionItem.createFromStockMovementItem(stockMovementItem)

        then:
        item.product.is(product)
        item.inventoryItem.is(inventoryItem)
        item.quantity == 12
        item.recipient.is(recipient)
        item.orderIndex == 300
        item.requisitionItemType == RequisitionItemType.ORIGINAL
    }

    void "RequisitionItem.toJson() should describe the item with its package as #productName"() {
        given:
        Product product = productWithId('p1')
        product.productCode = 'AB12'
        product.name = 'Gauze'
        product.unitOfMeasure = unitOfMeasure
        domain.product = product
        domain.productPackage = packageQuantity ? new ProductPackage(quantity: packageQuantity, uom: new UnitOfMeasure(code: 'BX')) : null
        domain.quantity = 4
        domain.quantityCanceled = 1
        domain.cancelReasonCode = 'STOCKOUT'
        domain.orderIndex = 2

        when:
        Map json = domain.toJson()

        then:
        json.productId == 'p1'
        json.productName == productName
        json.productPackageName == packageName
        json.productPackageQuantity == (packageQuantity ?: 1)
        json.unitOfMeasure == expectedUnitOfMeasure
        json.quantity == 4
        json.totalQuantity == 4 * (packageQuantity ?: 1)
        json.quantityCanceled == 1
        json.totalQuantityCanceled == (packageQuantity ?: 1)
        json.requisitionItemType == RequisitionItemType.ORIGINAL
        json.status == RequisitionItemStatus.PENDING
        json.isPending == true
        json.isCanceled == false
        json.isApproved == false
        json.isChanged == false
        json.isSubstituted == false
        json.isSubstitution == false
        json.orderIndex == 2

        where:
        packageQuantity | unitOfMeasure || productName          | packageName | expectedUnitOfMeasure
        null            | null          || 'AB12 Gauze (EA/1)'  | null        | 'EA'
        10              | 'EA'          || 'AB12 Gauze (BX/10)' | 'BX/10'     | 'EA'
        10              | 'ROLL'        || 'AB12 Gauze (BX/10)' | 'BX/10'     | 'ROLL'
    }

    void "RequisitionItem.toStockListDetailsJson() should describe the stock list line and its product"() {
        given:
        ProductPackage box = new ProductPackage(quantity: 10, uom: new UnitOfMeasure(code: 'BX', name: 'Box'))
        box.id = 'pp-1'
        Product product = Stub(Product) {
            getId() >> 'p1'
            getActive() >> true
            getProductCode() >> 'AB12'
            getName() >> 'Gauze'
            getDisplayName() >> 'Gauze roll'
            getCategory() >> new Category(name: 'Supplies')
            getUnitOfMeasure() >> 'EA'
            getPricePerUnit() >> pricePerUnit
            getPackages() >> ([box] as Set)
            getColor() >> '#ffffff'
        }
        domain.id = 'ri-1'
        domain.product = product
        domain.productPackage = box
        domain.quantity = 3
        domain.requisition.replenishmentPeriod = 30

        when:
        Map json = domain.toStockListDetailsJson()

        then:
        json.id == 'ri-1'
        json.quantity == 3
        json.monthlyDemand == 3
        json.productPackageId == 'pp-1'
        json.totalCost == totalCost
        json.product.id == 'p1'
        json.product.active == true
        json.product.productCode == 'AB12'
        json.product.name == 'Gauze'
        json.product.displayName == 'Gauze roll'
        json.product.category == 'Supplies'
        json.product.unitOfMeasure == 'EA'
        json.product.pricePerUnit == expectedPricePerUnit
        json.product.packages == [[id: 'pp-1', quantity: 10, uom: [code: 'BX', name: 'Box']]]
        json.product.color == '#ffffff'

        where:
        pricePerUnit || totalCost | expectedPricePerUnit
        2.0          || 6.0       | 2.0
        null         || 0         | 0
    }

    private static Product productWithId(String id) {
        Product product = new Product(name: "Product ${id}")
        product.id = id
        return product
    }

    private static RequisitionItem itemWith(String id, Integer orderIndex, RequisitionItemType type) {
        RequisitionItem item = new RequisitionItem(orderIndex: orderIndex, requisitionItemType: type)
        item.id = id
        return item
    }

    private PicklistItem addPick(RequisitionItem requisitionItem, Integer quantity, String binName = null) {
        Picklist picklist = new Picklist()
        picklist.save(validate: false, flush: true)
        Location bin = binName ? new Location(name: binName) : null
        // OBAF: calculateQuantityPicked() sums PicklistItem.quantityPicked instead of the allocated quantity, so a
        // pick in these features is allocated and picked in full
        PicklistItem picklistItem = new PicklistItem(requisitionItem: requisitionItem, quantity: quantity,
                quantityPicked: quantity, binLocation: bin)
        picklist.addToPicklistItems(picklistItem)
        picklistItem.save(validate: false, flush: true)
        return picklistItem
    }

    private ShipmentItem shipmentItem(RequisitionItem requisitionItem, Integer quantity) {
        ShipmentItem shipmentItem = Stub(ShipmentItem)
        shipmentItem.requisitionItem >> requisitionItem
        shipmentItem.quantity >> quantity
        return shipmentItem
    }

    private Shipment shipmentWith(List<ShipmentItem> shipmentItems) {
        Shipment shipment = Stub(Shipment)
        shipment.shipmentItems >> (shipmentItems as Set)
        return shipment
    }
}
