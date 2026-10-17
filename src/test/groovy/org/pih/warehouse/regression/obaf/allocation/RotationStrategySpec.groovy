package org.pih.warehouse.regression.obaf.allocation

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AvailableItemComparators
import org.pih.warehouse.allocation.FefoRotationStrategy
import org.pih.warehouse.allocation.NoneRotationStrategy
import org.pih.warehouse.allocation.RotationRule
import org.pih.warehouse.allocation.RotationStrategy
import org.pih.warehouse.allocation.RotationStrategyResolver
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * Rotation (`openboxes.order.allocation.rotation`, default FEFO): FEFO sorts earliest expiry
 * first with undated lots last; NONE keeps the source order. FIFO and LIFO exist in RotationRule
 * but have no strategy: the resolver falls back to NONE.
 */
class RotationStrategySpec extends Specification {

    Location facility = facility()
    Location binA = bin(facility, 'A')
    Product product = product('ROT')

    AvailableItem late = available(lot(product, 'LATE', days(300)), binA, 1)
    AvailableItem early = available(lot(product, 'EARLY', days(10)), binA, 1)
    AvailableItem middle = available(lot(product, 'MID', days(100)), binA, 1)
    AvailableItem undated = available(lot(product, 'NODATE', null), binA, 1)
    AvailableItem expired = available(lot(product, 'EXPIRED', days(-5)), binA, 1)

    void 'FEFO sorts earliest expiration first and undated lots last'() {
        expect:
        new FefoRotationStrategy().sort([undated, late, early, middle])*.inventoryItem*.lotNumber ==
                ['EARLY', 'MID', 'LATE', 'NODATE']
    }

    void 'FEFO keeps already-expired lots first (it does not filter them out)'() {
        expect:
        new FefoRotationStrategy().sort([late, expired, early])*.inventoryItem*.lotNumber == ['EXPIRED', 'EARLY', 'LATE']
    }

    void 'FEFO does not mutate the list it is given'() {
        given:
        List<AvailableItem> input = [late, early]

        when:
        List<AvailableItem> sorted = new FefoRotationStrategy().sort(input)

        then:
        sorted == [early, late]
        input == [late, early]
    }

    void 'FEFO is stable for equal expiry dates and for several undated lots (source order preserved)'() {
        given:
        Date same = days(50)
        AvailableItem first = available(lot(product, 'S1', same), binA, 1)
        AvailableItem second = available(lot(product, 'S2', same), binA, 1)
        AvailableItem undated2 = available(lot(product, 'NODATE2', null), binA, 1)

        expect:
        new FefoRotationStrategy().sort([undated, first, undated2, second])*.inventoryItem*.lotNumber ==
                ['S1', 'S2', 'NODATE', 'NODATE2']
    }

    void 'FEFO tolerates an item without an inventory item (sorted with the undated)'() {
        given:
        AvailableItem noLot = new AvailableItem(binLocation: binA, quantityAvailable: 1, quantityOnHand: 1)

        expect:
        new FefoRotationStrategy().sort([noLot, early]) == [early, noLot]
    }

    @Unroll
    void '#strategy.class.simpleName.sort(#input) answers an empty list'() {
        expect:
        strategy.sort(input) == []

        where:
        [strategy, input] << [[new FefoRotationStrategy(), new NoneRotationStrategy()], [null, []]].combinations()
    }

    void 'NONE preserves the source order exactly'() {
        expect:
        new NoneRotationStrategy().sort([undated, late, early]) == [undated, late, early]
    }

    void 'the comparator orders null-expiry pairs as equal'() {
        expect:
        AvailableItemComparators.BY_EXPIRATION_NULLS_LAST.compare(undated, undated) == 0
        AvailableItemComparators.BY_EXPIRATION_NULLS_LAST.compare(undated, early) > 0
        AvailableItemComparators.BY_EXPIRATION_NULLS_LAST.compare(early, undated) < 0
    }

    @Unroll
    void 'resolver: #rule resolves to #expected'() {
        expect:
        new RotationStrategyResolver().forRule(rule).rule == expected

        where:
        rule              || expected
        RotationRule.FEFO || RotationRule.FEFO
        RotationRule.NONE || RotationRule.NONE
        RotationRule.FIFO || RotationRule.NONE   // not implemented: silently no rotation
        RotationRule.LIFO || RotationRule.NONE   // not implemented: silently no rotation
        null              || RotationRule.NONE
    }

    void 'a resolver built without NONE still falls back to no rotation'() {
        given:
        RotationStrategy fallback = new RotationStrategyResolver([new FefoRotationStrategy()]).forRule(RotationRule.LIFO)

        expect:
        fallback.rule == RotationRule.NONE
        fallback.sort([late, early]) == [late, early]
    }
}
