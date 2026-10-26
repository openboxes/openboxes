package org.pih.warehouse.regression.platform.report

import org.pih.warehouse.report.*
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemCommand
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductDetailsCommand

class InventoryReportEntryCommandSpec extends Specification {

    void 'adjusted and ending quantities are derived from the running totals'() {
        given:
        InventoryReportEntryCommand entry = new InventoryReportEntryCommand(
                quantityTotalIn: 50, quantityTotalOut: 20, quantityFound: 5, quantityLost: 8)

        expect:
        entry.quantityTotalAdjusted == -3
        entry.quantityEnding == 27
    }

    void 'totals add up every entry, including the per-location transfers'() {
        given:
        Location clinic = new Location(name: 'Clinic')
        Location ward = new Location(name: 'Ward')
        InventoryReportEntryCommand first = new InventoryReportEntryCommand(
                quantityAdjusted: 1, quantityTransferredIn: 10, quantityTransferredOut: 4, quantityConsumed: 2,
                quantityDamaged: 1, quantityExpired: 3, quantityFinal: 20, quantityFound: 2, quantityInitial: 15,
                quantityLost: 1, quantityRunning: 18, quantityTotalIn: 12, quantityTotalOut: 9,
                quantityTransferredInByLocation: [(clinic): 10],
                quantityTransferredOutByLocation: [(ward): 4])
        InventoryReportEntryCommand second = new InventoryReportEntryCommand(
                quantityAdjusted: 2, quantityTransferredIn: 5, quantityTransferredOut: 6, quantityConsumed: 1,
                quantityDamaged: 0, quantityExpired: 1, quantityFinal: 7, quantityFound: 0, quantityInitial: 9,
                quantityLost: 2, quantityRunning: 8, quantityTotalIn: 5, quantityTotalOut: 7,
                quantityTransferredInByLocation: [(clinic): 3, (ward): 2],
                quantityTransferredOutByLocation: [(ward): 6])
        InventoryReportEntryCommand report = new InventoryReportEntryCommand(
                entries: [(new InventoryItem(lotNumber: 'L1')): first, (new InventoryItem(lotNumber: 'L2')): second])

        when:
        InventoryReportEntryCommand totals = report.totals

        then:
        totals.quantityAdjusted == 3
        totals.quantityTransferredIn == 15
        totals.quantityTransferredOut == 10
        totals.quantityConsumed == 3
        totals.quantityDamaged == 1
        totals.quantityExpired == 4
        totals.quantityFinal == 27
        totals.quantityFound == 2
        totals.quantityInitial == 24
        totals.quantityLost == 3
        totals.quantityRunning == 26
        totals.quantityTotalIn == 17
        totals.quantityTotalOut == 16
        totals.quantityTransferredInByLocation == [(clinic): 13, (ward): 2]
        totals.quantityTransferredOutByLocation == [(ward): 10]
    }

    void 'an empty report has zero totals'() {
        expect:
        new InventoryReportEntryCommand().totals.quantityEnding == 0
    }

    void 'inventory item commands are supported unless the level says otherwise'() {
        expect:
        new InventoryItemCommand(inventoryLevel: status == null ? null : new InventoryLevel(status: status))
                .with { [it.supported, it.notSupported] } == [supported, notSupported]

        where:
        status                                  || supported | notSupported
        null                                    || true      | false
        InventoryStatus.SUPPORTED               || true      | false
        InventoryStatus.NOT_SUPPORTED           || false     | true
        InventoryStatus.SUPPORTED_NON_INVENTORY || false     | true
        InventoryStatus.INACTIVE                || false     | false
    }

    void 'inventory item commands are equal when they are for the same product'() {
        given:
        Product product = new Product(name: 'Gloves')
        product.id = 'p1'
        Product other = new Product(name: 'Masks')
        other.id = 'p2'
        InventoryItemCommand a = new InventoryItemCommand(product: product, quantityOnHand: 1)
        InventoryItemCommand b = new InventoryItemCommand(product: product, quantityOnHand: 5)
        InventoryItemCommand c = new InventoryItemCommand(product: other)
        InventoryItemCommand noProduct = new InventoryItemCommand()

        expect:
        a == b
        a.hashCode() == b.hashCode()
        a.hashCode() == 'p1'.hashCode()
        a != c
        !a.equals('p1')
        noProduct.hashCode() == System.identityHashCode(noProduct)
        ([a, b, c] as Set).size() == 2
    }

    void 'product details describe strength and drug name'() {
        given:
        ProductDetailsCommand details = new ProductDetailsCommand(strengthNumber: '500', strengthUnit: 'mg',
                nonProprietaryName: 'Paracetamol', route: 'oral', dosageForm: 'tablet')

        expect:
        details.strength == '500 mg'
        details.drugName == 'Paracetamol, oral, tablet'
        details.packageSize == 1
        details.links == [:]
    }
}
