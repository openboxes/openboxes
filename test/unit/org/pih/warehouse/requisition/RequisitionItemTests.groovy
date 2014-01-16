package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase
import grails.validation.ValidationException
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem
// import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.picklist.Picklist;
import org.pih.warehouse.picklist.PicklistItem
// import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage

class RequisitionItemTests extends GrailsUnitTestCase {

    Requisition requisition
    Picklist picklist
    Product ibuprofen200mg
    Product ibuprofen800mg
    ProductPackage bottle200
    ProductPackage bottle1000
    InventoryItem abc123

    protected void setUp() {
        super.setUp();
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

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void constructor() {
        def requisitionItem = new RequisitionItem()
        assertEquals RequisitionItemType.ORIGINAL, requisitionItem.requisitionItemType
    }

    @Test
    void validate_shouldSucceedWhenCanceledRequisitionItemHasCancelReasonCode() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50, cancelReasonCode: "Because.", requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        requisitionItem.save(failOnError: true)
        assertTrue requisitionItem.validate()
        assertEquals 0, requisitionItem.errors.errorCount
    }

    @Test
    void validate_shouldFailWhenCanceledRequisitionItemDoesNotHaveCancelReasonCode() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        assertFalse requisitionItem.validate()
        println requisitionItem.errors.getFieldError("quantityCanceled")
        assertNotNull requisitionItem.errors.getFieldError("quantityCanceled")
    }

    @Test
    void cancelQuantity() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
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

    @Test
    void cancelQuantity_shouldRemovePicklistItems() {
        def requisition = new Requisition()
        def picklist = new Picklist()
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: requisition)
        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem, picklist: picklist)
        mockDomain(Requisition, [requisition])
        mockDomain(Picklist, [picklist])
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem, [picklistItem])

        requisition.picklist = picklist
        requisition.picklist.addToPicklistItems(picklistItem)
        requisitionItem.addToPicklistItems(picklistItem)
        requisition.addToRequisitionItems(requisitionItem)

        assertEquals 1, requisitionItem.getPicklistItems().size()
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        assertEquals 0, requisitionItem.getPicklistItems().size()

    }

    @Test
    void cancelQuantity_shouldFailCannotCancelTwice() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("Not needed", "Because I said so")
        shouldFail(ValidationException) {
            requisitionItem.cancelQuantity("Not needed", "Because I'm impatient")
        }
    }

    @Test
    void getStatus() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

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

    @Test
    void approveQuantity() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.approveQuantity()
        assertEquals 100, requisitionItem.quantityApproved
        assertTrue requisitionItem.isApproved()
    }

    @Test
    void approveQuantity_shouldFailWhenAlreadyCancelled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.cancelQuantity("STOCKOUT", "Because.")

        shouldFail(ValidationException) {
            requisitionItem.approveQuantity()
        }

        assertEquals null, requisitionItem.quantityApproved
        assertFalse requisitionItem.isApproved()
        assertTrue requisitionItem.isCanceled()
    }


    @Test
    void getChange_shouldReturnNullWhenChangesNotExist() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        assertNull requisitionItem.change
    }

    @Ignore
    void getChange_shouldReturnFirstChangeWhenOneChangeExists() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem2 = new RequisitionItem(product: ibuprofen800mg, productPackage: null, quantity: 25, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem, requisitionItem2])
        requisitionItem.addToRequisitionItems(requisitionItem2)
        assertEquals requisitionItem2, requisitionItem.change
    }

    @Ignore
    void getChange_shouldReturnFirstChangeWhenMultipleChangesExist() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem2 = new RequisitionItem(product: ibuprofen800mg, productPackage: null, quantity: 25, quantityCanceled: 0, requisition: new Requisition())
        def requisitionItem3 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem, requisitionItem2, requisitionItem3])
        requisitionItem.addToRequisitionItems(requisitionItem3)
        assertEquals requisitionItem3, requisitionItem.change
    }


    @Test
    void changeQuantity() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisitionItem.changeQuantity(10, "Change", "Because I said so")
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


    @Test
    void changePackageSize() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem)
        mockDomain(PicklistItem)
        requisitionItem.changeQuantity(1, bottle200, "Package size", "Because I said so")
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

    @Test
    void changeQuantity_shouldFailOnEmptyOrNullReasonCode() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(10, "", "Because I said so")
        }

        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(10, null, null)
        }

    }

    @Test
    void changeQuantity_shouldFailOnNegativeQuantity() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(-1, "Change", "Because I said so")
        }

        assertEquals requisitionItem.quantity, 100
        assertEquals requisitionItem.quantityCanceled, 0
        assertNull requisitionItem.change
        assertFalse requisitionItem.isChanged()
    }

    @Test
    void changeQuantity_shouldFailOnSameQuantity() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        shouldFail(ValidationException) {
            requisitionItem.changeQuantity(100, "Change", "Because I said so")
        }

        assertEquals requisitionItem.quantity, 100
        assertEquals requisitionItem.quantityCanceled, 0
        assertNull requisitionItem.change
        assertFalse requisitionItem.isChanged()
        assertTrue requisitionItem.validate()
    }

    @Test
    void changeQuantity_shouldCancelOnQuantityEqualsZero() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        requisitionItem.changeQuantity(0, "Not needed", "Because I said so")
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

    @Test
    void undoChanges() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        requisitionItem.changeQuantity(10, "Change", "Because I said so")

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

        requisitionItem.undoChanges()
        //assertEquals 0, requisitionItem?.requisitionItems?.size()
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

    @Test
    void chooseSubstitute() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0, requisition: new Requisition())
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        requisitionItem.chooseSubstitute(ibuprofen800mg, null, 25, "Availability", "Only dosage remaining")

        assertEquals 100, requisitionItem.quantityCanceled
        assertEquals "Availability", requisitionItem.cancelReasonCode
        assertEquals "Only dosage remaining", requisitionItem.cancelComments
        assertFalse requisitionItem.isCanceled()
        assertTrue requisitionItem.hasSubstitution()

        def substitution = requisitionItem.getSubstitution()
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

    @Test
    void totalQuantityNotCanceled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50)
        assertEquals 50, requisitionItem.totalQuantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        assertEquals 200, requisitionItem.totalQuantityNotCanceled()
    }

    @Test
    void quantityNotCanceled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 50)
        assertEquals 50, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        assertEquals 1, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 10)
        assertEquals 0, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 0)
        assertEquals 0, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: 0)
        assertEquals 0, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 0, quantityCanceled: null)
        assertEquals 0, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: null, quantityCanceled: null)
        assertEquals 0, requisitionItem.quantityNotCanceled()

        requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: null, quantityCanceled: 0)
        assertEquals 0, requisitionItem.quantityNotCanceled()

    }

    @Test
    void totalQuantity() {
        def requisitionItem1 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100)
        assertEquals 100, requisitionItem1.totalQuantity()

        def requisitionItem2 = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10)
        assertEquals 2000, requisitionItem2.totalQuantity()
    }

    @Test
    void totalQuantityCanceled() {
        def requisitionItem1 = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 100)
        requisition.addToRequisitionItems(requisitionItem1)

        assertEquals 100, requisitionItem1.totalQuantityCanceled()

        def requisitionItem2 = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)

        assertEquals 2000, requisitionItem2.totalQuantity()
        assertEquals 1800, requisitionItem2.totalQuantityCanceled()
    }

    @Test
    void totalQuantityApproved() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: null, quantity: 100, quantityCanceled: 0)
        requisition.addToRequisitionItems(requisitionItem)
        requisitionItem.quantityApproved = 50
        requisitionItem.quantityCanceled = 50
        assertEquals 50, requisitionItem.totalQuantityApproved()
    }

    @Test
    void totalQuantityApproved_usingPackage() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 3, quantityCanceled: 0)
        requisition.addToRequisitionItems(requisitionItem)
        requisitionItem.quantityApproved = 2
        requisitionItem.quantityCanceled = 1
        assertEquals 400, requisitionItem.totalQuantityApproved()
    }

    @Test
    void calculatePercentagesBeforeItemPicked() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        assertEquals 2000, requisitionItem.totalQuantity()
        assertEquals 1800, requisitionItem.totalQuantityCanceled()
        assertEquals 90, requisitionItem.calculatePercentageCompleted()
        assertEquals 10, requisitionItem.calculatePercentageRemaining()
        assertEquals 90, requisitionItem.calculatePercentageCanceled()
        assertEquals 0, requisitionItem.calculatePercentagePicked()
    }

    @Test
    void calculatePercentageAfterItemPicked() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])

        assertEquals 0, requisitionItem.calculateQuantityRemaining()
        assertEquals 90, requisitionItem.calculatePercentageCompleted()
        assertEquals 90, requisitionItem.calculatePercentageCanceled()
        assertEquals 10, requisitionItem.calculatePercentagePicked()

    }

    @Test
    void calculateQuantityPickedBeforeItemPicked() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)

        assertEquals 0, requisitionItem.totalQuantityPicked()
        assertEquals 200, requisitionItem.totalQuantityRemaining()
    }

    @Test
    void calculateQuantityPickedAfterItemPicked() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 9)
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 200, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])

        println requisitionItem.retrievePicklistItems()

        assertEquals 200, requisitionItem.calculateQuantityPicked()
        assertEquals 200, requisitionItem.totalQuantityPicked()
        assertEquals 0, requisitionItem.totalQuantityRemaining()
    }


    @Test
    void isCanceled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        assertFalse requisitionItem.isCanceled()

        requisitionItem.quantityCanceled = 9
        assertFalse requisitionItem.isCanceled()

        requisitionItem.quantityCanceled = 10
        assertTrue requisitionItem.isCanceled()

    }

    @Test
    void isCompleted() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        assertFalse requisitionItem.isCompleted()

        requisitionItem.quantityCanceled = 10
        assertTrue requisitionItem.isCompleted()


        // FIXME this test case is no longer valid
        //requisitionItem.quantityCanceled = 5
        //def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 1000, requisitionItem: requisitionItem)
        //mockDomain(PicklistItem, [picklistItem])
        //assertTrue requisitionItem.isCompleted()

    }

    @Test
    void isFulfilled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 10)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        assertFalse requisitionItem.isFulfilled()

        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 2000, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])
        requisitionItem.quantityCanceled = 0
        assertTrue requisitionItem.isFulfilled()

    }

    @Test
    void isPartiallyFulfilled() {
        def requisitionItem = new RequisitionItem(product: ibuprofen200mg, productPackage: bottle200, quantity: 10, quantityCanceled: 0)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem)
        assertFalse requisitionItem.isPartiallyFulfilled()

        // Pick all 2000
        def picklistItem = new PicklistItem(inventoryItem: abc123, quantity: 2000, requisitionItem: requisitionItem)
        mockDomain(PicklistItem, [picklistItem])
        requisitionItem.quantityCanceled = 0
        assertEquals 0, requisitionItem.totalQuantityRemaining()
        assertEquals 2000, requisitionItem.totalQuantityPicked()
        assertFalse requisitionItem.isPartiallyFulfilled()

        // Pick just 1000
        picklistItem.quantity = 1000
        requisitionItem.quantityCanceled = 0
        assertTrue requisitionItem.isPartiallyFulfilled()

        // Cancel the other 1000 (5 x 200)
        requisitionItem.quantityCanceled = 5
        assertEquals 2000, requisitionItem.totalQuantity()
        assertEquals 1000, requisitionItem.totalQuantityPicked()
        assertEquals 1000, requisitionItem.totalQuantityCanceled()
        assertEquals 0, requisitionItem.totalQuantityRemaining()
        assertEquals 1000, requisitionItem.totalQuantityPicked()
        assertFalse requisitionItem.isPartiallyFulfilled()


    }

    /*
    @Test
    void hasSubstitution() {

    }




    @Test
    void calculateQuantityRemaining() {
    }

    @Test
    void calculateNumInventoryItem(Inventory inventory) {
    }

    @Test
    void retrievePicklistItems() {
    }

    @Test
    void availableInventoryItems() {
    }

    @Test
    void calculatePercentagePicked() {
    }


    @Test
    void calculatePercentageRemaining() {
    }
    */


    @Test
    void testNotNullableConstraints() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: null)
        assertFalse requisitionItem.validate()
        assertEquals "nullable", requisitionItem.errors["product"]
        assertEquals "nullable", requisitionItem.errors["quantity"]
    }

    @Test
    void testQuantityConstraint() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: 0)
        assertFalse requisitionItem.validate()
        println requisitionItem.errors["quantity"]
    }

    @Test
    void testToJsonData(){
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
      assert json.id == requisitionItem.id
      assert json.productId == requisitionItem.product.id
      assert json.productName == "ASP " + requisitionItem.product.name + " (EA/1)"
      assert json.quantity == requisitionItem.quantity
      assert json.comment == requisitionItem.comment
      assert json.recipient == requisitionItem.recipient
      assert json.substitutable
      assert json.orderIndex == requisitionItem.orderIndex
    }

    @Test
    void testcalculateQuantityPicked() {

        def requisitionItem = new RequisitionItem(id: "reqItem1")
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem1 = new PicklistItem(id: "pickItem1", quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", quantity: 100)
        def picklistItem3 = new PicklistItem(id: "pickItem3", quantity: 205)
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        picklistItem1.requisitionItem = requisitionItem
        picklistItem2.requisitionItem = requisitionItem
        picklistItem3.requisitionItem = requisitionItem

        assert requisitionItem.calculateQuantityPicked() == (30 + 100 + 205)

    }

    @Test
    void testcalcuateNumInventoryItem()
    {
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

        assert requisitionItem.calculateNumInventoryItem() == 3
    }

    @Test
    void testRetrievePicklistItems() {
        def requisitionItem = new RequisitionItem(id: "reqItem1")
        def picklistItem1 = new PicklistItem(id: "pickItem1", requisitionItem: requisitionItem, quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", requisitionItem: requisitionItem, quantity: 50)
        def picklistItem3 = new PicklistItem(id: "pickItem3", requisitionItem: requisitionItem, quantity: 60)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        def list = requisitionItem.retrievePicklistItems()
        assert list.contains(picklistItem1)
        assert list.contains(picklistItem2)
        assert list.contains(picklistItem3)

    }
}
