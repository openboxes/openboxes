package org.pih.warehouse.order

import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.IdentifierTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.identification.IdentifierGeneratorContext
import org.pih.warehouse.shipping.Shipment

class PurchaseOrderIdentifierService extends IdentifierService<Order> {

    @Override
    String getIdentifierName() {
        return "purchaseOrder"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        Integer count = Order.countByOrderNumber(id)

        // TODO: Refactor how ids are generated for shipments so that we don't need to do this check. Uniqueness
        //       of ids on one service shouldn't depend on another. The easiest solution is to override the format
        //       for generating shipment ids to be "${custom.orderNumber}${random}" so that it's like the order
        //       number but still unique.

        // We use order.orderNumber as shipment.shipmentNumber when creating a shipment from an order so we need to
        // check that the id is unique for shipments as well. See ShipmentService.createOrUpdateShipment for details.
        // Only bother checking shipment if order doesn't already have a duplicate though.
        return count > 0 ? count : Shipment.countByShipmentNumber(id)
    }

    @Override
    String generate(Order order, IdentifierGeneratorContext context=null) {

        // TODO: Move this sequential logic into the identifier service to support sequential id generation more
        //       generally. We'll need an abstract method there for fetching the next sequence number to use.
        String sequenceNumber = formatHasSequenceNumber(context) ?
                getNextSequenceNumber(order.destinationParty.id) :
                null

        return super.generate(order, IdentifierGeneratorContext.builder()
                .customProperties(["sequenceNumber": sequenceNumber])
                .build())
    }

    private boolean formatHasSequenceNumber(IdentifierGeneratorContext context) {
        String format = context?.formatOverride
        if (!format) {
            format = super.getIdentifierPropertyWithDefault('format')
        }
        return format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER)
    }

    private String getNextSequenceNumber(String partyId) {
        Organization organization = Organization.get(partyId)

        Integer sequenceNumber = Integer.valueOf(
                organization.sequences.getOrDefault(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString(), "0"))
        Integer nextSequenceNumber = sequenceNumber + 1

        // We need to actually update the sequence number so that it doesn't get re-used.
        organization.sequences.put(IdentifierTypeCode.PURCHASE_ORDER_NUMBER.toString(), nextSequenceNumber.toString())
        organization.save()

        Integer sequenceNumberMinSize = configService.getProperty(
                "openboxes.identifier.purchaseOrder.sequenceNumber.minSize", Integer)

        return StringUtils.leftPad(
                nextSequenceNumber.toString(), sequenceNumberMinSize, Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT_CHAR)
    }
}
