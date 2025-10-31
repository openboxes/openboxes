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
        CycleCountItem countItem = new CycleCountItem(
                countIndex: 0,
                location: new Location(name: 'bin-1'),
                status: CycleCountItemStatus.COUNTED,
        )
        countItem.id = '0'
        CycleCountItem recountItem = new CycleCountItem(
                countIndex: 1,
                location: new Location(name: 'bin-1'),
                status: CycleCountItemStatus.INVESTIGATING,
        )
        recountItem.id = '1'
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [countItem, recountItem],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.INVESTIGATING
    }

    void 'recomputeStatus should return INVESTIGATING when some items of the recount are INVESTIGATING and others APPROVED'() {
        given:
        Location bin1 = new Location(name: 'bin-1')
        Location bin2 = new Location(name: 'bin-2')
        CycleCountItem countItem1 = new CycleCountItem(
                countIndex: 0,
                location: bin1,
                status: CycleCountItemStatus.COUNTED,
        )
        countItem1.id = '1'
        CycleCountItem countItem2 = new CycleCountItem(
                countIndex: 0,
                location: bin2,
                status: CycleCountItemStatus.COUNTED,
        )
        countItem2.id = '2'
        CycleCountItem recountItem1 = new CycleCountItem(
                countIndex: 1,
                location: bin1,
                status: CycleCountItemStatus.INVESTIGATING,
        )
        recountItem1.id = '3'
        CycleCountItem recountItem2 = new CycleCountItem(
                countIndex: 1,
                location: bin2,
                status: CycleCountItemStatus.APPROVED,
        )
        recountItem1.id = '4'
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [countItem1, countItem2, recountItem1, recountItem2],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.INVESTIGATING
    }

    void 'recomputeStatus should return INVESTIGATING when all items of the recount are INVESTIGATING and some items of the count are APPROVED'() {
        given:
        Location bin1 = new Location(name: 'bin-1')
        CycleCountItem countItem1 = new CycleCountItem(
                countIndex: 0,
                location: bin1,
                status: CycleCountItemStatus.COUNTED,
        )
        countItem1.id = '1'
        CycleCountItem countItem2 = new CycleCountItem(
                countIndex: 0,
                location: new Location(name: 'bin-2'),
                status: CycleCountItemStatus.APPROVED,
        )
        countItem2.id = '2'
        CycleCountItem recountItem1 = new CycleCountItem(
                countIndex: 1,
                location: bin1,
                status: CycleCountItemStatus.INVESTIGATING,
        )
        recountItem1.id = '3'
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [countItem1, countItem2, recountItem1],
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
        CycleCountItem countItem1 = new CycleCountItem(
                countIndex: 0,
                location: bin1,
                status: CycleCountItemStatus.COUNTED,
        )
        countItem1.id = '1'
        CycleCountItem countItem2 = new CycleCountItem(
                countIndex: 0,
                location: new Location(name: 'bin-2'),
                status: CycleCountItemStatus.APPROVED,
        )
        countItem2.id = '2'
        CycleCountItem recountItem1 = new CycleCountItem(
                countIndex: 1,
                location: bin1,
                status: CycleCountItemStatus.APPROVED,
        )
        recountItem1.id = '3'
        CycleCount cycleCount = new CycleCount(
                cycleCountItems: [countItem1, countItem2, recountItem1],
        )

        expect:
        assert cycleCount.recomputeStatus() == CycleCountStatus.COMPLETED
    }

    void 'getMaxCountIndex should return #maxCountIndex when there are items with #countIndexes count indexes'() {
        given:
        Set<CycleCountItem> cycleCountItems = new TreeSet<>()
        for (int countIndex : countIndexes) {
            CycleCountItem item = new CycleCountItem(
                    countIndex: countIndex,
                    location: new Location(),
                    status: CycleCountItemStatus.APPROVED
            )
            item.id = 1
            cycleCountItems.add(item)
        }
        CycleCount cycleCount = new CycleCount(cycleCountItems: cycleCountItems)

        expect:
        cycleCount.maxCountIndex == maxCountIndex

        where:
        countIndexes || maxCountIndex
        [0, 1]       || 1
        [0, 1, 0]    || 1
        [1, 11, 49]  || 49
        null         || null
    }

    void 'getItemsOfMostRecountCount should return #numberOfItems items with the highest count index: #highestCountIndex when there #countIndexes count indexes'() {
        given:
        Set<CycleCountItem> cycleCountItems = new TreeSet<>()
        for (int countIndex : countIndexes) {
            CycleCountItem cycleCountItem = Spy(CycleCountItem)
            cycleCountItem.countIndex = countIndex
            cycleCountItems.add(cycleCountItem)
        }
        CycleCount cycleCount = Spy(CycleCount)
        cycleCount.cycleCountItems = cycleCountItems

        expect:
        cycleCount.itemsOfMostRecentCount?.size() == numberOfItems
        cycleCount.itemsOfMostRecentCount?.getAt(0)?.countIndex == highestCountIndex

        where:
        countIndexes  || highestCountIndex | numberOfItems
        [0, 1, 0, 0]  || 1                 | 1
        [1, 1, 1, 1]  || 1                 | 4
        [49, 11, 49]  || 49                | 2
        null          || null              | 0
    }
}
