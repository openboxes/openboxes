package unit.org.pih.warehouse.core

import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import spock.lang.Specification
import spock.lang.Unroll
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.api.StockMovement

@Unroll
class StockMovementSpec extends Specification {

    void 'StockMovement.getDisplayStatus() should return: #expected for requisition status #status'() {
        when:
        StockMovement stockMovement = new StockMovement() {
            @Override
            String getTranslatedDisplayStatus(def status) {
                return status.name()
            }
        }
        stockMovement.requisition = new Requisition()
        stockMovement.requisition.status = status

        then:
        stockMovement.getDisplayStatus() == expected.name()

        where:
        status                                 || expected
        RequisitionStatus.APPROVED             || StockMovementStatusCode.APPROVED
        RequisitionStatus.REJECTED             || StockMovementStatusCode.REJECTED
        RequisitionStatus.PENDING_APPROVAL     || StockMovementStatusCode.PENDING_APPROVAL
        RequisitionStatus.VERIFYING            || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKING              || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKED               || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.CHECKING             || StockMovementStatusCode.IN_PROGRESS
    }

    void 'StockMovement.getDisplayStatus() should return: #expected for shipment status #status'() {
        when:
        StockMovement stockMovement = new StockMovement() {
            @Override
            String getTranslatedDisplayStatus(def status) {
                return status.name()
            }
        }
        stockMovement.shipment = new Shipment()
        stockMovement.shipment.status.code = status

        then:
        stockMovement.getDisplayStatus() == expected.name()

        where:
        status                                 || expected
        ShipmentStatusCode.CREATED             || ShipmentStatusCode.PENDING
        ShipmentStatusCode.PARTIALLY_RECEIVED  || ShipmentStatusCode.PENDING
        ShipmentStatusCode.RECEIVED            || ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED             || ShipmentStatusCode.PENDING
    }
}
