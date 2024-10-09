package org.pih.warehouse.order

import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.IdentifierTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.identification.IdentifierGeneratorParams
import org.pih.warehouse.shipping.Shipment

// TODO: We should be able to move this sequential logic into the identifier service. That way we can support sequential
//       id generation more generally. We'd need an abstract method there for fetching the next sequence number to use,
//       and a new ${sequence} config option to know where to put the sequence.
class PurchaseOrderIdentifierService extends IdentifierService {

    @Override
    String getPropertyKey() {
        return "purchaseOrder"
    }

    @Override
    protected Integer countDuplicates(String orderNumber) {
        // We use order.orderNumber as shipment.shipmentNumber when creating a shipment from an order so we need to
        // check that the id is unique for shipments as well. See ShipmentService.createOrUpdateShipment for details.
        Integer count = Order.countByOrderNumber(orderNumber)

        // Only bother checking shipment if order doesn't already have a duplicate.
        return count > 0 ? count : Shipment.countByShipmentNumber(orderNumber)
    }

    String generateForEntity(Order order) {
        IdentifierGeneratorTypeCode identifierGeneratorTypeCode = configService.getProperty(
                "openboxes.identifier.purchaseOrder.generatorType", IdentifierGeneratorTypeCode)

        switch (identifierGeneratorTypeCode) {
            case IdentifierGeneratorTypeCode.SEQUENCE:
                return generateSequential(order)
            case IdentifierGeneratorTypeCode.RANDOM:
                return generateForEntity(order)
        }
    }

    private String generateSequential(Order order) {
        Integer sequenceNumber = getNextSequenceNumber(order.destinationParty.id)
        String sequenceNumberFormat = configService.getProperty("openboxes.identifier.purchaseOrder.sequenceNumber.format")
        String sequenceNumberStr = StringUtils.leftPad(
                sequenceNumber.toString(), sequenceNumberFormat.length(), sequenceNumberFormat.substring(0, 1))

        return generate(
                IdentifierGeneratorParams.builder()
                        .templateEntity(order)
                        .templateCustomValues(["sequenceNumber": sequenceNumberStr])
                        .build())
    }

    private int getNextSequenceNumber(String partyId) {
        Organization organization = Organization.get(partyId)

        Integer sequenceNumber = Integer.valueOf(
                organization.sequences.getOrDefault(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString(), "0"))
        Integer nextSequenceNumber = sequenceNumber + 1

        // We need to actually update the sequence number so that it doesn't get re-used.
        organization.sequences.put(IdentifierTypeCode.PURCHASE_ORDER_NUMBER, nextSequenceNumber.toString())
        organization.save()

        return nextSequenceNumber
    }
}
