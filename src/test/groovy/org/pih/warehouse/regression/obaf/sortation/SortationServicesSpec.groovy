package org.pih.warehouse.regression.obaf.sortation

import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UserService
import org.pih.warehouse.inboundSortation.DemandService
import org.pih.warehouse.inboundSortation.InboundSortationService
import org.pih.warehouse.inboundSortation.PutawayStrategyService
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask

import static org.pih.warehouse.core.DeliveryTypeCode.*

/**
 * DemandService.calculateUnmetDemand arithmetic (demand minus open putaway "allocations" per
 * delivery type, never negative, ordered by delivery priority) and the guards of
 * InboundSortationService.rerunPutawayStrategy. The queries behind both are in
 * InboundSortationIntegrationSpec.
 */
class SortationServicesSpec extends Specification implements DataTest {

    Location facility = new Location(name: 'F')
    Product product = new Product(productCode: 'P')

    Map unmet(Map allocations, Map demands) {
        DemandService demand = Spy(DemandService)
        demand.getAllocations(facility, product) >> allocations
        demand.getDemands(facility, product) >> demands
        return demand.calculateUnmetDemand(facility, product)
    }

    void 'unmet demand: nothing either side is no entry'() {
        expect:
        unmet([:], [:]) == [:]
    }

    void 'unmet demand: demand minus what open putaways already bring, per delivery type'() {
        expect:
        unmet([(LOCAL_DELIVERY): 4], [(LOCAL_DELIVERY): 10]) == [(LOCAL_DELIVERY): 6]
    }

    void 'unmet demand: never negative; a type with only putaways is listed at 0'() {
        expect:
        unmet([(LOCAL_DELIVERY): 12, (SHIP_TO): 3], [(LOCAL_DELIVERY): 10]) == [(LOCAL_DELIVERY): 0, (SHIP_TO): 0]
    }

    void 'unmet demand: several delivery types come back in delivery priority order'() {
        when:
        Map result = unmet([(PICK_UP): 1], [(DEFAULT): 9, (SHIP_TO): 2, (WILL_CALL): 3, (SERVICE): 4, (LOCAL_DELIVERY): 5, (PICK_UP): 6])

        then:
        result.keySet() as Set == [PICK_UP, SERVICE, LOCAL_DELIVERY, WILL_CALL, SHIP_TO, DEFAULT] as Set
        result.keySet().toList().collect { it.priority } == [1, 2, 2, 3, 4, 5]
        result[PICK_UP] == 5
        result[DEFAULT] == 9
    }

    // ------------------------------------------------------------------ rerunPutawayStrategy guards

    void 'rerunning the strategy is refused for a task that is neither PENDING nor IN_PROGRESS'() {
        given:
        PutawayStrategyService strategies = Mock()
        InboundSortationService service = new InboundSortationService(putawayStrategyService: strategies, userService: Mock(UserService))

        when:
        service.rerunPutawayStrategy(new PutawayTask(status: status))

        then:
        thrown(IllegalStateException)
        0 * strategies.execute(_)

        where:
        status << [PutawayTaskStatus.STARTED, PutawayTaskStatus.COMPLETED, PutawayTaskStatus.CANCELED]
    }

    void 'rerunning the strategy on an IN_PROGRESS task needs a superuser'() {
        given:
        PutawayStrategyService strategies = Mock()
        UserService users = Mock()
        InboundSortationService service = new InboundSortationService(putawayStrategyService: strategies, userService: users)

        when:
        service.rerunPutawayStrategy(new PutawayTask(status: PutawayTaskStatus.IN_PROGRESS))

        then:
        1 * users.isSuperuser(_) >> false
        IllegalStateException e = thrown()
        e.message.contains('superuser')
        0 * strategies.execute(_)
    }
}
