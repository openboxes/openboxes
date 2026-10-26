package org.pih.warehouse.regression.obaf.allocation

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AllocationSourceGroup
import org.pih.warehouse.allocation.AllocationSourceStrategy
import org.pih.warehouse.allocation.AllocationSourceStrategyHandler
import org.pih.warehouse.allocation.AllocationSourceStrategyHandlerResolver
import org.pih.warehouse.allocation.AvailableItemGroups
import org.pih.warehouse.allocation.CandidateLocationGroups
import org.pih.warehouse.allocation.DisplayFirstHandler
import org.pih.warehouse.allocation.DisplayOnlyHandler
import org.pih.warehouse.allocation.StorageFirstHandler
import org.pih.warehouse.allocation.StorageOnlyHandler
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * The four allocation source strategies (STORAGE_FIRST, DISPLAY_FIRST, STORAGE_ONLY, DISPLAY_ONLY):
 * each handler's group order, how it orders available items and candidate locations, and the
 * resolver. Groups: DISPLAY (bins with DISPLAY_STOCK), PREFERRED_STORAGE (the facility-level
 * InventoryLevel's preferred bin), REMAINING_STORAGE (every other non-display bin).
 */
class SourceStrategyHandlersSpec extends Specification {

    Location facility = facility()
    Product product = product('SRC')
    Location preferred = bin(facility, 'PREF')
    Location storage = bin(facility, 'STOR')
    Location display = displayBin(facility, 'DISP')
    InventoryItem lotA = lot(product, 'A', days(30))

    AvailableItem inPreferred = available(lotA, preferred, 5)
    AvailableItem inStorage = available(lotA, storage, 5)
    AvailableItem inDisplay = available(lotA, display, 5)

    void cleanup() {
        resetPreferredBin()
    }

    @Unroll
    void '#handlerClass.simpleName declares #strategy with group order #groups'() {
        given:
        AllocationSourceStrategyHandler handler = handlerClass.newInstance()

        expect:
        handler.strategy == strategy
        handler.groupOrder == groups

        where:
        handlerClass        | strategy                                 || groups
        StorageFirstHandler | AllocationSourceStrategy.STORAGE_FIRST   || [AllocationSourceGroup.PREFERRED_STORAGE, AllocationSourceGroup.REMAINING_STORAGE, AllocationSourceGroup.DISPLAY]
        DisplayFirstHandler | AllocationSourceStrategy.DISPLAY_FIRST   || [AllocationSourceGroup.DISPLAY, AllocationSourceGroup.PREFERRED_STORAGE, AllocationSourceGroup.REMAINING_STORAGE]
        StorageOnlyHandler  | AllocationSourceStrategy.STORAGE_ONLY    || [AllocationSourceGroup.PREFERRED_STORAGE, AllocationSourceGroup.REMAINING_STORAGE]
        DisplayOnlyHandler  | AllocationSourceStrategy.DISPLAY_ONLY    || [AllocationSourceGroup.DISPLAY]
    }

    @Unroll
    void '#strategy orders available items #expectedNames when a preferred bin is set'() {
        given:
        preferredBin(preferred)
        AllocationSourceStrategyHandler handler = new AllocationSourceStrategyHandlerResolver().handlerFor(strategy)

        when: 'the input order is deliberately the reverse of every expected order'
        List<AvailableItem> ordered = handler.orderAvailableItems(facility, product, [inDisplay, inStorage, inPreferred])

        then:
        ordered*.binLocation*.name == expectedNames

        where:
        strategy                               || expectedNames
        AllocationSourceStrategy.STORAGE_FIRST || ['PREF', 'STOR', 'DISP']
        AllocationSourceStrategy.DISPLAY_FIRST || ['DISP', 'PREF', 'STOR']
        AllocationSourceStrategy.STORAGE_ONLY  || ['PREF', 'STOR']
        AllocationSourceStrategy.DISPLAY_ONLY  || ['DISP']
    }

    @Unroll
    void '#strategy without a preferred bin treats every storage bin as REMAINING_STORAGE, keeping their input order'() {
        given:
        preferredBin(null)
        AllocationSourceStrategyHandler handler = new AllocationSourceStrategyHandlerResolver().handlerFor(strategy)

        expect:
        handler.orderAvailableItems(facility, product, [inStorage, inDisplay, inPreferred])*.binLocation*.name == expectedNames

        where:
        strategy                               || expectedNames
        AllocationSourceStrategy.STORAGE_FIRST || ['STOR', 'PREF', 'DISP']
        AllocationSourceStrategy.DISPLAY_FIRST || ['DISP', 'STOR', 'PREF']
        AllocationSourceStrategy.STORAGE_ONLY  || ['STOR', 'PREF']
        AllocationSourceStrategy.DISPLAY_ONLY  || ['DISP']
    }

    void 'a preferred bin that is a display bin is not promoted into PREFERRED_STORAGE'() {
        given:
        preferredBin(display)

        when:
        AvailableItemGroups groups = new AvailableItemGroups(facility, product, [inStorage, inDisplay])

        then:
        groups.itemsFor(AllocationSourceGroup.PREFERRED_STORAGE).isEmpty()
        groups.itemsFor(AllocationSourceGroup.DISPLAY) == [inDisplay]
        groups.itemsFor(AllocationSourceGroup.REMAINING_STORAGE) == [inStorage]
    }

    void 'several lots in the preferred bin all land in PREFERRED_STORAGE in input order'() {
        given:
        preferredBin(preferred)
        AvailableItem otherLot = available(lot(product, 'B', days(10)), preferred, 2)

        expect:
        new StorageFirstHandler().orderAvailableItems(facility, product, [inStorage, inPreferred, otherLot]) ==
                [inPreferred, otherLot, inStorage]
    }

    void 'items without a bin (default location) count as storage, not display'() {
        given:
        preferredBin(null)
        AvailableItem noBin = available(lotA, null, 3)

        expect:
        new DisplayOnlyHandler().orderAvailableItems(facility, product, [noBin, inDisplay]) == [inDisplay]
        new StorageOnlyHandler().orderAvailableItems(facility, product, [noBin, inDisplay]) == [noBin]
    }

    @Unroll
    void '#strategy with null or empty input returns an empty list'() {
        given:
        preferredBin(preferred)
        AllocationSourceStrategyHandler handler = new AllocationSourceStrategyHandlerResolver().handlerFor(strategy)

        expect:
        handler.orderAvailableItems(facility, product, input) == []
        handler.orderLocations(facility, product, input) == []

        where:
        [strategy, input] << [AllocationSourceStrategy.values().toList(), [null, []]].combinations()
    }

    @Unroll
    void '#strategy orders candidate locations #expectedNames'() {
        given:
        preferredBin(preferred)
        AllocationSourceStrategyHandler handler = new AllocationSourceStrategyHandlerResolver().handlerFor(strategy)

        expect:
        handler.orderLocations(facility, product, [display, storage, preferred])*.name == expectedNames

        where:
        strategy                               || expectedNames
        AllocationSourceStrategy.STORAGE_FIRST || ['PREF', 'STOR', 'DISP']
        AllocationSourceStrategy.DISPLAY_FIRST || ['DISP', 'PREF', 'STOR']
        AllocationSourceStrategy.STORAGE_ONLY  || ['PREF', 'STOR']
        AllocationSourceStrategy.DISPLAY_ONLY  || ['DISP']
    }

    void 'CandidateLocationGroups splits locations like AvailableItemGroups splits items'() {
        given:
        preferredBin(preferred)

        when:
        CandidateLocationGroups groups = new CandidateLocationGroups(facility, product, [storage, display, preferred])

        then:
        groups.locationsFor(AllocationSourceGroup.DISPLAY) == [display]
        groups.locationsFor(AllocationSourceGroup.PREFERRED_STORAGE) == [preferred]
        groups.locationsFor(AllocationSourceGroup.REMAINING_STORAGE) == [storage]
    }

    void 'the default resolver knows every AllocationSourceStrategy'() {
        given:
        AllocationSourceStrategyHandlerResolver resolver = new AllocationSourceStrategyHandlerResolver()

        expect:
        AllocationSourceStrategy.values().every { resolver.handlerFor(it)?.strategy == it }
        resolver.handlerFor(null) == null
    }

    void 'a resolver built from a partial handler list answers null for the missing strategies'() {
        given:
        AllocationSourceStrategyHandlerResolver resolver = new AllocationSourceStrategyHandlerResolver([new DisplayOnlyHandler()])

        expect:
        resolver.handlerFor(AllocationSourceStrategy.DISPLAY_ONLY) instanceof DisplayOnlyHandler
        resolver.handlerFor(AllocationSourceStrategy.STORAGE_FIRST) == null
    }
}
