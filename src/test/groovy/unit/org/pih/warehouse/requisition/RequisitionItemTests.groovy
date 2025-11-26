package org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest

import grails.validation.ValidationException
import spock.lang.Ignore

import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.springframework.context.ApplicationEvent
import spock.lang.Specification
import static org.junit.Assert.*;

@Ignore('Fix these tests and move them to RequisitionItemSpec')
class RequisitionItemTests extends Specification implements DomainUnitTest<Requisition> {

    Requisition requisition
    Picklist picklist
    Product ibuprofen200mg
    Product ibuprofen800mg
    ProductPackage bottle200
    ProductPackage bottle1000
    InventoryItem abc123

    protected void setup() {
        UnitOfMeasureClass quantityClass = new UnitOfMeasureClass(name: "Quantity", code: "QTY")
        UnitOfMeasure bottle = new UnitOfMeasure(name: "Bottle", code: "BTL", uomClass: quantityClass)

        ibuprofen200mg = new Product(name: "Ibuprofen 200mg tablet");
        ibuprofen800mg = new Product(name: "Ibuprofen 800mg tablet")
        bottle200 = new ProductPackage(name: "200 count bottle", quantity: 200, uom: bottle)
        bottle1000 = new ProductPackage(name: "1000 count bottle", quantity: 1000, uom: bottle)
        abc123 = new InventoryItem(lotNumber: "abc123")
        picklist = new Picklist()
        requisition = new Requisition()
        //picklist.requisition = requisition

        mockDomain(ProductPackage, [bottle200,bottle1000])
        mockDomain(Product, [ibuprofen200mg,ibuprofen800mg])
        mockDomain(InventoryItem, [abc123])
        mockDomain(Picklist, [picklist])
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem)
    }

    void constructor() {
        when:
        def requisitionItem = new RequisitionItem()
        then:
        assertEquals RequisitionItemType.ORIGINAL, requisitionItem.requisitionItemType
    }

    void validate_shouldSucceedWhenCanceledRequisitionItemHasCancelReasonCode() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50, cancelReasonCode: "Because.", requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        requisitionItem.save(failOnError: true)
        then:
        assertTrue requisitionItem.validate()
        assertEquals 0, requisitionItem.errors.errorCount
    }

    void validate_shouldFailWhenCanceledRequisitionItemDoesNotHaveCancelReasonCode() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        then:
        assertFalse requisitionItem.validate()
        println requisitionItem.errors.getFieldError("quantityCanceled")
        assertNotNull requisitionItem.errors.getFieldError("quantityCanceled")
    }

    void cancelQuantity() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        then:
        assertEquals "Because I said so", requisitionItem.cancelComments
        assertEquals "Not needed", requisitionItem.cancelReasonCode
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertTrue requisitionItem.isCanceled()
        assertTrue requisitionItem.isCompleted()
        assertFalse requisitionItem.isChanged()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()
    }

    void cancelQuantity_shouldRemovePicklistItems() {
        when:
        def requisition = new Requisition()
        def picklist = new Picklist()
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: requisition)
        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem, picklist: picklist)
        mockDomain(Requisition, [requisition])
        mockDomain(Picklist, [picklist])
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem, [picklistItem])
        PicklistItem.metaClass.publishEvent = { ApplicationEvent event -> }

        requisition.picklist = picklist
        requisition.picklist.addToPicklistItems(picklistItem)
        requisitionItem.addToPicklistItems(picklistItem)
        requisition.addToRequisitionItems(requisitionItem)

        then:
        assertEquals 1, requisitionItem.getPicklistItems().size()
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        assertEquals 0, requisitionItem.getPicklistItems().size()

    }

    void cancelQuantity_shouldFailCannotCancelTwice() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        then:
        shouldFail(ValidationException) {
            requisitionItem.cancelQuantity("Not needed", "Because I'm impatient")
        }
    }

    void getStatus() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        assertEquals RequisitionItemStatus.PENDING, requisitionItem.getStatus()

        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        assertEquals RequisitionItemStatus.CANCELED, requisitionItem.getStatus()

        requisitionItem.undoChanges()
        requisitionItem.changeQuantity(90, "Package size", "Because I said so")
        assertEquals RequisitionItemStatus.CHANGED, requisitionItem.getStatus()

        requisitionItem.undoChanges()
        requisitionItem.approveQuantity()
        assertEquals RequisitionItemStatus.APPROVED, requisitionItem.getStatus()

    }

    void approveQuantity() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.approveQuantity()
        then:
        assertEquals 100, requisitionItem.quantityApproved
        assertTrue requisitionItem.isApproved()
    }

    void approveQuantity_shouldFailWhenAlreadyCancelled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("STOCKOUT", "Because.")

        shouldFail(ValidationException) {
            requisitionItem.approveQuantity()
        }

        then:
        assertEquals null, requisitionItem.quantityApproved
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.isCanceled()
    }

    void getChange_shouldReturnNullWhenChangesNotExist() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        then:
        assertNull requisitionItem.change
    }

    @Ignore
    void getChange_shouldReturnFirstChangeWhenOneChangeExists() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem2 = new RequisitionItem(product: ibuprofen800mg, productPackage: null, quantity: 25, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem, requisitionItem2])
        requisitionItem.addToRequisitionItems(requisitionItem2)
        then:
        assertEquals requisitionItem2, requisitionItem.change
    }

    @Ignore
    void getChange_shouldReturnFirstChangeWhenMultipleChangesExist() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem2 = new RequisitionItem(product: ibuprofen800mg, productPackage: null, quantity: 25, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem3 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem, requisitionItem2, requisitionItem3])
        requisitionItem.addToRequisitionItems(requisitionItem3)
        then:
        assertEquals requisitionItem3, requisitionItem.change
    }

    void changeQuantity() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisitionItem.changeQuantity(10, "Change", "Because I said so")
        then:
        assertNotNull requisitionItem.change
        assertEquals 10, requisitionItem.change.quantity
        assertEquals "Because I said so", requisitionItem.cancelComments
        assertEquals "Change", requisitionItem.cancelReasonCode
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertTrue requisitionItem.isChanged()
        assertFalse requisitionItem.isCanceled()
        assertTrue requisitionItem.isCompleted()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()
    }

    void changePackageSize() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisitionItem.changeQuantity(1, bottle200, "Package size", "Because I said so")
        then:
        assertNotNull requisitionItem.change
        assertEquals 1, requisitionItem.change.quantity
        assertEquals "Because I said so", requisitionItem.cancelComments
        assertEquals "Package size", requisitionItem.cancelReasonCode
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertEquals 200, requisitionItem.change.totalQuantity()
        assertTrue requisitionItem.isChanged()
        assertFalse requisitionItem.isCanceled()
        assertTrue requisitionItem.isCompleted()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()
    }

    void changeQuantity_shouldFailOnEmptyOrNullReasonCode() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(10, "", "Because I said so")
        }

        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(10, null, null)
        }

    }

    void changeQuantity_shouldFailOnNegativeQuantity() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(-1, "Change", "Because I said so")
        }

        assertEquals requisitionItem.quantity, 100
        assertEquals requisitionItem.quantityCanceled, 0
        assertNull requisitionItem.change
        assertFalse requisitionItem.isChanged()
    }

    void changeQuantity_shouldFailOnSameQuantity() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(100, "Change", "Because I said so")
        }

        assertEquals requisitionItem.quantity, 100
        assertEquals requisitionItem.quantityCanceled, 0
        assertNull requisitionItem.change
        assertFalse requisitionItem.isChanged()
        assertTrue requisitionItem.validate()
    }

    void changeQuantity_shouldCancelOnQuantityEqualsZero() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        requisitionItem.changeQuantity(0, "Not needed", "Because I said so")
        then:
        assertEquals "Because I said so", requisitionItem.cancelComments
        assertEquals "Not needed", requisitionItem.cancelReasonCode
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertFalse requisitionItem.isChanged()
        assertTrue requisitionItem.isCanceled()
        assertTrue requisitionItem.isCompleted()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()
    }

    void undoChanges() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        requisitionItem.changeQuantity(10, "Change", "Because I said so")

        then:
        assertNotNull requisitionItem.change
        assertEquals 10, requisitionItem.change.quantity
        assertEquals RequisitionItemType.QUANTITY_CHANGE, requisitionItem.change.requisitionItemType
        assertEquals "Because I said so", requisitionItem.cancelComments
        assertEquals "Change", requisitionItem.cancelReasonCode
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertTrue requisitionItem.isChanged()
        assertFalse requisitionItem.isCanceled()
        assertTrue requisitionItem.isCompleted()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()

        when:
        requisitionItem.undoChanges()
        //assertEquals 0, requisitionItem?.requisitionItems?.size()
        then:
        assertNull requisitionItem.change
        assertNull requisitionItem.cancelComments
        assertNull requisitionItem.cancelReasonCode
        assertEquals 0, requisitionItem.quantityCanceled
        assertEquals 100, requisitionItem.quantity
        assertFalse requisitionItem.isChanged()
        assertFalse requisitionItem.isCanceled()
        assertFalse requisitionItem.isCompleted()
        assertFalse requisitionItem.isFulfilled()
        assertFalse requisitionItem.isSubstitution()
        assertFalse requisitionItem.isPartiallyFulfilled()
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.validate()
    }

    void chooseSubstitute() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.chooseSubstitute(ibuprofen800mg, null, 25, "Availability", "Only dosage remaining")

        then:
        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals "Availability", requisitionItem.cancelReasonCode
        assertEquals "Only dosage remaining", requisitionItem.cancelComments
        assertFalse requisitionItem.isCanceled()
        assertTrue requisitionItem.hasSubstitution()

        when:
        def substitution = requisitionItem.getSubstitution()
        then:
        assertNotNull substitution
        assertEquals RequisitionItemType.SUBSTITUTION, substitution.requisitionItemType
        assertEquals 25, substitution.quantity
    }


    /*
    @Ignore
    void changeProductPackage() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 200)
        //requisitionItem.changeProductPackage(bottle200, )
    }
    */

    void totalQuantityNotCanceled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50)
        then:
        assertEquals 50, requisitionItem.totalQuantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        then:
        assertEquals 200, requisitionItem.totalQuantityNotCanceled()
    }

    void quantityNotCanceled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50)
        then:
        assertEquals 50, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        then:
        assertEquals 1, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 10)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 0)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 0)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: null)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: null, quantityCanceled: null)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

        when:
        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: null, quantityCanceled: 0)
        then:
        assertEquals 0, requisitionItem.quantityNotCanceled()

    }

    void totalQuantity() {
        when:
        def requisitionItem1 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100)
        then:
        assertEquals 100, requisitionItem1.totalQuantity()

        when:
        def requisitionItem2 = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10)
        then:
        assertEquals 2000, requisitionItem2.totalQuantity()
    }

    void totalQuantityCanceled() {
        when:
        def requisitionItem1 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 100)
        requisition.addToRequisitionItems(requisitionItem1)

        then:
        assertEquals 100, requisitionItem1.totalQuantityCanceled()

        when:
        def requisitionItem2 = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)

        then:
        assertEquals 2000, requisitionItem2.totalQuantity()
        assertEquals 1800, requisitionItem2.totalQuantityCanceled()
    }

    void totalQuantityApproved() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0)
        requisition.addToRequisitionItems(requisitionItem)
        requisitionItem.quantityApproved = 50
        requisitionItem.quantityCanceled = 50
        then:
        assertEquals 50, requisitionItem.totalQuantityApproved()
    }

    void totalQuantityApproved_usingPackage() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 3, quantityCanceled: 0)
        requisition.addToRequisitionItems(requisitionItem)
        requisitionItem.quantityApproved = 2
        requisitionItem.quantityCanceled = 1
        then:
        assertEquals 400, requisitionItem.totalQuantityApproved()
    }

    void calculatePercentagesBeforeItemPicked() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        assertEquals 2000, requisitionItem.totalQuantity()
        assertEquals 1800, requisitionItem.totalQuantityCanceled()
        assertEquals 90, requisitionItem.calculatePercentageCompleted()
        assertEquals 10, requisitionItem.calculatePercentageRemaining()
        assertEquals 90, requisitionItem.calculatePercentageCanceled()
        assertEquals 0, requisitionItem.calculatePercentagePicked()
    }

    void calculatePercentageAfterItemPicked() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])

        then:
        assertEquals 0, requisitionItem.calculateQuantityRemaining()
        assertEquals 90, requisitionItem.calculatePercentageCompleted()
        assertEquals 90, requisitionItem.calculatePercentageCanceled()
        assertEquals 10, requisitionItem.calculatePercentagePicked()
    }

    void calculateQuantityPickedBeforeItemPicked() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        then:
        assertEquals 0, requisitionItem.totalQuantityPicked()
        assertEquals 200, requisitionItem.totalQuantityRemaining()
    }

    void calculateQuantityPickedAfterItemPicked() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])

        println requisitionItem.retrievePicklistItems()

        then:
        assertEquals 200, requisitionItem.calculateQuantityPicked()
        assertEquals 200, requisitionItem.totalQuantityPicked()
        assertEquals 0, requisitionItem.totalQuantityRemaining()
    }

    void isCanceled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        then:
        assertFalse requisitionItem.isCanceled()

        when:
        requisitionItem.quantityCanceled = 9
        then:
        assertFalse requisitionItem.isCanceled()

        when:
        requisitionItem.quantityCanceled = 10
        then:
        assertTrue requisitionItem.isCanceled()
    }

    void isCompleted() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        then:
        assertFalse requisitionItem.isCompleted()

        when:
        requisitionItem.quantityCanceled = 10
        then:
        assertTrue requisitionItem.isCompleted()


        // FIXME this test case is no longer valid
        //requisitionItem.quantityCanceled = 5
        //def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 1000, requisitionItem: requisitionItem)
        //mockDomain(PicklistItem, [picklistItem])
        //assertTrue requisitionItem.isCompleted()
    }

    void isFulfilled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 10)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        then:
        assertFalse requisitionItem.isFulfilled()

        when:
        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 2000, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])
        requisitionItem.quantityCanceled = 0
        then:
        assertTrue requisitionItem.isFulfilled()
    }

    void isPartiallyFulfilled() {
        when:
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        then:
        assertFalse requisitionItem.isPartiallyFulfilled()

        // Pick all 2000
        when:
        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 2000, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])
        requisitionItem.quantityCanceled = 0
        then:
        assertEquals 0, requisitionItem.totalQuantityRemaining()
        assertEquals 2000, requisitionItem.totalQuantityPicked()
        assertFalse requisitionItem.isPartiallyFulfilled()

        // Pick just 1000
        when:
        picklistItem.quantity = 1000
        requisitionItem.quantityCanceled = 0
        then:
        assertTrue requisitionItem.isPartiallyFulfilled()

        // Cancel the other 1000 (5 x 200)
        when:
        requisitionItem.quantityCanceled = 5
        then:
        assertEquals 2000, requisitionItem.totalQuantity()
        assertEquals 1000, requisitionItem.totalQuantityPicked()
        assertEquals 1000, requisitionItem.totalQuantityCanceled()
        assertEquals 0, requisitionItem.totalQuantityRemaining()
        assertEquals 1000, requisitionItem.totalQuantityPicked()
        assertFalse requisitionItem.isPartiallyFulfilled()
    }

    void testNotNullableConstraints() {
        when:
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: null)
        then:
        assertFalse requisitionItem.validate()
        assertEquals "nullable", requisitionItem.errors["product"]
        assertEquals "nullable", requisitionItem.errors["quantity"]
    }

    void testQuantityConstraint() {
        when:
//        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: 0)
        then:
        assertFalse requisitionItem.validate()
//        println requisitionItem.errors["quantity"]
    }

    void testToJsonData(){
        when:
        def product = new Product(id: "prod1", productCode: "ASP", name:"aspin")
      def requisitionItem = new RequisitionItem(
        id: "1234",
        product: product,
        quantity: 3000,
        comment: "good",
        recipient: "peter",
        substitutable: true,
        orderIndex: 3
      )

      mockDomain(Product, [product])
      mockDomain(RequisitionItem, [requisitionItem])

      Map json = requisitionItem.toJson()

      println json
        then:
      assert json.id == requisitionItem.id
      assert json.productId == requisitionItem.product.id
      assert json.productName == "ASP " + requisitionItem.product.name + " (EA/1)"
      assert json.quantity == requisitionItem.quantity
      assert json.comment == requisitionItem.comment
      assert json.recipient == requisitionItem.recipient
      assert json.substitutable
      assert json.orderIndex == requisitionItem.orderIndex
    }

    void testcalculateQuantityPicked() {

        when:
        def requisitionItem = new RequisitionItem(id: "reqItem1")
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem1 = new PicklistItem(id: "pickItem1", quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", quantity: 100)
        def picklistItem3 = new PicklistItem(id: "pickItem3", quantity: 205)
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        picklistItem1.requisitionItem = requisitionItem
        picklistItem2.requisitionItem = requisitionItem
        picklistItem3.requisitionItem = requisitionItem

        then:
        assert requisitionItem.calculateQuantityPicked() == (30 + 100 + 205)

    }

    void testcalcuateNumInventoryItem()
    {
        when:
        def inventoryItem1 = new InventoryItem(id: "invent1")
        def inventoryItem2 = new InventoryItem(id: "invent2")
        def inventoryItem3 = new InventoryItem(id: "invent3")
        mockDomain(InventoryItem, [inventoryItem1, inventoryItem2, inventoryItem3])

        def product = new Product(id: "prod1")
        mockDomain(Product, [product])

        inventoryItem1.product = product
        inventoryItem2.product = product
        inventoryItem3.product = product

        def requisitionItem = new RequisitionItem(id: "reqItem1")
        mockDomain(RequisitionItem, [requisitionItem])

        requisitionItem.product = product

        then:
        assert requisitionItem.calculateNumInventoryItem() == 3
    }

    void testRetrievePicklistItems() {
        when:
        def requisitionItem = new RequisitionItem(id: "reqItem1")
        def picklistItem1 = new PicklistItem(id: "pickItem1", requisitionItem: requisitionItem, quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", requisitionItem: requisitionItem, quantity: 50)
        def picklistItem3 = new PicklistItem(id: "pickItem3", requisitionItem: requisitionItem, quantity: 60)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        def list = requisitionItem.retrievePicklistItems()
        then:
        assert list.contains(picklistItem1)
        assert list.contains(picklistItem2)
        assert list.contains(picklistItem3)
    }
}
