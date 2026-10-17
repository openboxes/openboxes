package org.pih.warehouse.regression.obaf.putaway

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.PendingFeature
import spock.lang.Specification

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.putaway.PutawayController
import org.pih.warehouse.putaway.PutawayService

/**
 * OBAF deletes putaway/PutAwayController (index, list, create, generatePdf; URLs /putAway/*) and
 * adds putaway/PutawayController (URLs /putaway/*) with the putaway order page, the putaway task
 * actions and the web rollback / rollback-and-delete. This spec pins the rename and the two
 * rollback actions' delegation and redirects; the services are in PutawayOrdersIntegrationSpec.
 */
class PutawayControllerRenameSpec extends Specification implements ControllerUnitTest<PutawayController>, DataTest {

    PutawayService putawayService = Mock()

    void setup() {
        controller.putawayService = putawayService
    }

    void 'the controller is putaway/PutawayController; the old PutAwayController is gone'() {
        when: 'on a case-insensitive file system the lookup finds PutawayController.class under the old name: NoClassDefFoundError (wrong name)'
        Throwable lookup = null
        try {
            Class.forName('org.pih.warehouse.putaway.PutAwayController', false, getClass().classLoader)
        } catch (Throwable t) {
            lookup = t
        }

        then:
        lookup instanceof ClassNotFoundException || lookup instanceof NoClassDefFoundError
        PutawayController.declaredMethods*.name.toSet().containsAll(['index', 'list', 'create', 'generatePdf', 'show', 'putawayTasks',
                                                                    'rollback', 'rollbackAndDelete', 'putawayTaskFormDialog',
                                                                    'savePutawayTask', 'putawayTaskTicket', 'downloadPutawayTaskTicket'])
    }

    void 'index and list send the user to the (React) create page, as before the rename'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/putaway/create'

        when:
        response.reset()
        controller.list()

        then:
        response.redirectedUrl == '/putaway/create'
    }

    void 'rollback rolls the putaway back and returns to its page'() {
        given:
        Putaway putaway = new Putaway(id: 'po-1', putawayNumber: 'P-1')

        when:
        controller.rollback('po-1')

        then:
        1 * putawayService.getPutaway('po-1') >> putaway
        1 * putawayService.rollbackPutaway(putaway)
        0 * putawayService.rollbackAndDelete(_)
        response.redirectedUrl == '/putaway/show/po-1'
        flash.message == 'Rollback was completed successfully for putaway P-1'
    }

    void 'rollbackAndDelete deletes it and returns to the pending putaway order list'() {
        given:
        Putaway putaway = new Putaway(id: 'po-2', putawayNumber: 'P-2')

        when:
        controller.rollbackAndDelete('po-2')

        then:
        1 * putawayService.getPutaway('po-2') >> putaway
        1 * putawayService.rollbackAndDelete(putaway)
        response.redirectedUrl.startsWith('/order/list')
        response.redirectedUrl.contains('orderType=PUTAWAY_ORDER')
        response.redirectedUrl.contains('status=PENDING')
        flash.message == 'Putaway has been deleted successfully for putaway P-2'
    }

    @PendingFeature(reason = 'grails-app/views/order/_actions.gsp:106 and order/_summary.gsp:105 still link controller="putAway" action="generatePdf"; the rename to PutawayController leaves /putAway/generatePdf with no controller, so the "Generate PDF" button of a putaway order is dead (the React page uses /putaway/generatePdf)')
    void 'no GSP still links to the removed putAway controller'() {
        given:
        File views = new File('grails-app/views')
        assert views.directory: "run from the OBAF checkout root (cwd ${new File('.').absolutePath})"

        when:
        List<String> stale = []
        views.eachFileRecurse { File f ->
            if (f.name.endsWith('.gsp')) {
                f.readLines().eachWithIndex { String line, int i ->
                    if (line =~ /controller\s*[:=]\s*["']putAway["']/) {
                        stale << "${views.toPath().relativize(f.toPath())}:${i + 1}".toString()
                    }
                }
            }
        }

        then:
        stale == []
    }
}
