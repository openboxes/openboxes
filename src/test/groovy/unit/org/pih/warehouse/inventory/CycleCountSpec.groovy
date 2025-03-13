package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountItemStatus
import org.pih.warehouse.inventory.CycleCountStatus

@Unroll
class CycleCountSpec extends Specification implements DomainUnitTest<CycleCount>, DataTest {

    void 'recomputeStatus should return REQUESTED when all items are READY_TO_COUNT'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                status: CycleCountItemStatus.READY_TO_COUNT
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.REQUESTED
    }

    void 'recomputeStatus should return COUNTING when some items are READY_TO_COUNT and others COUNTING'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.READY_TO_COUNT
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.COUNTING
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COUNTING
    }

    void 'recomputeStatus should return COUNTING when all items are COUNTING'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                status: CycleCountItemStatus.COUNTING
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COUNTING
    }

    void 'recomputeStatus should return COUNTING when some items are COUNTED and others COUNTING'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.COUNTING
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COUNTING
    }

    void 'recomputeStatus should return COUNTED when all items are COUNTED'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COUNTED
    }

    void 'recomputeStatus should return COUNTED when some items are COUNTED and others APPROVED'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.APPROVED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COUNTED
    }

    void 'recomputeStatus should return COMPLETED when all items are APPROVED'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                status: CycleCountItemStatus.APPROVED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COMPLETED
    }

    void 'recomputeStatus should return INVESTIGATING when all items of the recount are INVESTIGATING'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.INVESTIGATING
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.INVESTIGATING
    }

    void 'recomputeStatus should return INVESTIGATING when some items of the recount are INVESTIGATING and others APPROVED'() {
        given:
        Location bin1 = new Location(name: 'bin-1')
        Location bin2 = new Location(name: 'bin-2')
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: bin1,
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: bin2,
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: bin1,
                                status: CycleCountItemStatus.INVESTIGATING
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: bin2,
                                status: CycleCountItemStatus.APPROVED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.INVESTIGATING
    }

    void 'recomputeStatus should return INVESTIGATING when all items of the recount are INVESTIGATING and some items of the count are APPROVED'() {
        given:
        Location bin1 = new Location(name: 'bin-1')
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: bin1,
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.APPROVED
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: bin1,
                                status: CycleCountItemStatus.INVESTIGATING
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.INVESTIGATING
    }

    void 'recomputeStatus should return COMPLETED when all items of the recount are APPROVED'() {
        given:
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-1'),
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.APPROVED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COMPLETED
    }

    void 'recomputeStatus should return COMPLETED when all items of the recount are APPROVED and some items of the count are APPROVED'() {
        given:
        Location bin1 = new Location(name: 'bin-1')
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [
                        new CycleCountItem(
                                countIndex: 0,
                                location: bin1,
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                countIndex: 0,
                                location: new Location(name: 'bin-2'),
                                status: CycleCountItemStatus.APPROVED
                        ),
                        new CycleCountItem(
                                countIndex: 1,
                                location: bin1,
                                status: CycleCountItemStatus.APPROVED
                        ),
                ],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COMPLETED
    }
}
