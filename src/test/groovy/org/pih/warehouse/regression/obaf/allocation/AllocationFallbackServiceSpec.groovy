package org.pih.warehouse.regression.obaf.allocation

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AllocationFallbackResolution
import org.pih.warehouse.allocation.AllocationFallbackService
import org.pih.warehouse.allocation.AllocationSourceStrategy
import org.pih.warehouse.allocation.AllocationStep
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * AllocationFallbackService.resolve: where the quantity no bin can supply is recorded.
 * Step 2 (NEGATIVE_INVENTORY): a candidate bin (an InventoryLevel putaway location or a bin that
 * currently holds the product) that is active, allocable, pickable, not the fallback bin and
 * allowed to go negative (ALLOW_NEGATIVE_INVENTORY on the bin AND on its facility), in source
 * strategy order. Step 3 (FALLBACK_LOCATION): the facility's NEGATIVE_INVENTORY_FALLBACK location.
 */
class AllocationFallbackServiceSpec extends Specification implements ServiceUnitTest<AllocationFallbackService>, DataTest {

    LocationService locationService = Mock()
    ProductAvailabilityService productAvailabilityService = Mock()

    Location facility = facility('DC', [ActivityCode.MANAGE_INVENTORY, ActivityCode.ALLOW_NEGATIVE_INVENTORY])
    Product product = product('FB')
    Location shortfall = bin(facility, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK, ActivityCode.ALLOW_NEGATIVE_INVENTORY])

    void setup() {
        service.locationService = locationService
        service.productAvailabilityService = productAvailabilityService
        locationService.getNegativeInventoryFallbackLocation(facility) >> shortfall
        preferredBin(null)
    }

    void cleanup() {
        resetPreferredBin()
    }

    Location negBin(String name, List<ActivityCode> extra = []) {
        return bin(facility, name, [ActivityCode.ALLOW_NEGATIVE_INVENTORY] + extra)
    }

    void stocked(Location... bins) {
        productAvailabilityService.getAvailableItems(facility, [product.id], false, true) >>
                bins.collect { available(lot(product, 'L', null), it, 1) }
    }

    void 'a stocked bin allowed to go negative is chosen at step NEGATIVE_INVENTORY'() {
        given:
        Location neg = negBin('NEG')
        stocked(neg)

        when:
        AllocationFallbackResolution r = service.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST)

        then:
        r.binLocation == neg
        r.step == AllocationStep.NEGATIVE_INVENTORY
        r.step.stepNumber == 2
        r.toString().contains('NEG')
    }

    void 'a putaway location from the InventoryLevel is a candidate even with no stock in it'() {
        given:
        Location assigned = negBin('ASSIGNED')
        preferredBin(assigned)
        stocked()

        expect:
        service.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST).binLocation == assigned
    }

    @Unroll
    void 'a bin that is #why is never a negative-inventory candidate; the shortfall location is used'() {
        given:
        Location candidate = build.call(this)
        stocked(candidate)

        when:
        AllocationFallbackResolution r = service.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST)

        then:
        r.binLocation == shortfall
        r.step == AllocationStep.FALLBACK_LOCATION

        where:
        why                                   | build
        'inactive'                            | { s -> Location l = s.negBin('INACTIVE'); l.active = false; l }
        'not allocable (no PICK_STOCK)'       | { s -> bin(s.facility, 'NOPICK', [ActivityCode.ALLOW_NEGATIVE_INVENTORY], false) }
        'on hold'                             | { s -> s.negBin('HELD', [ActivityCode.HOLD_STOCK]) }
        'lost and found'                      | { s -> s.negBin('LOST', [ActivityCode.LOST_AND_FOUND]) }
        'the fallback location itself'        | { s -> s.shortfall }
        'not allowed to go negative'          | { s -> bin(s.facility, 'PLAIN') }
    }

    void 'a bin allowed to go negative in a facility that does not allow it is not a candidate'() {
        given:
        Location strictFacility = facility('STRICT')
        Location neg = bin(strictFacility, 'NEG', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        Location strictShortfall = bin(strictFacility, 'SF', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        locationService.getNegativeInventoryFallbackLocation(strictFacility) >> strictShortfall
        productAvailabilityService.getAvailableItems(strictFacility, [product.id], false, true) >> [available(lot(product, 'L', null), neg, 1)]

        expect:
        service.resolve(strictFacility, product, AllocationSourceStrategy.STORAGE_FIRST).step == AllocationStep.FALLBACK_LOCATION
    }

    @Unroll
    void 'candidates follow the source strategy: #strategy picks #expected'() {
        given:
        Location storageNeg = negBin('STORAGE-NEG')
        Location displayNeg = negBin('DISPLAY-NEG', [ActivityCode.DISPLAY_STOCK])
        stocked(storageNeg, displayNeg)

        when:
        AllocationFallbackResolution r = service.resolve(facility, product, strategy)

        then:
        r.binLocation.name == expected
        r.step == step

        where:
        strategy                               || expected      | step
        AllocationSourceStrategy.STORAGE_FIRST || 'STORAGE-NEG' | AllocationStep.NEGATIVE_INVENTORY
        AllocationSourceStrategy.DISPLAY_FIRST || 'DISPLAY-NEG' | AllocationStep.NEGATIVE_INVENTORY
        AllocationSourceStrategy.STORAGE_ONLY  || 'STORAGE-NEG' | AllocationStep.NEGATIVE_INVENTORY
        AllocationSourceStrategy.DISPLAY_ONLY  || 'DISPLAY-NEG' | AllocationStep.NEGATIVE_INVENTORY
    }

    void 'DISPLAY_ONLY with only storage candidates skips step 2 and records the shortfall at the fallback location'() {
        given:
        stocked(negBin('STORAGE-NEG'))

        expect:
        service.resolve(facility, product, AllocationSourceStrategy.DISPLAY_ONLY).step == AllocationStep.FALLBACK_LOCATION
    }

    void 'the preferred bin is chosen first among storage candidates'() {
        given:
        Location other = negBin('OTHER')
        Location preferred = negBin('PREFERRED')
        preferredBin(preferred)
        stocked(other, preferred)

        expect:
        service.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST).binLocation == preferred
    }

    void 'with no strategy the first candidate in natural order is taken'() {
        given:
        Location first = negBin('FIRST')
        stocked(first, negBin('SECOND'))

        expect:
        service.resolve(facility, product, null).binLocation == first
    }

    void 'no candidate and no shortfall location: null (allocation then fails)'() {
        given:
        Location bare = facility('BARE')
        locationService.getNegativeInventoryFallbackLocation(bare) >> null
        productAvailabilityService.getAvailableItems(bare, [product.id], false, true) >> []

        expect:
        service.resolve(bare, product, AllocationSourceStrategy.STORAGE_FIRST) == null
    }

    void 'a bin both assigned and stocked is considered once'() {
        given:
        Location neg = negBin('BOTH')
        preferredBin(neg)
        stocked(neg, neg)

        expect:
        service.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST).binLocation == neg
    }
}
