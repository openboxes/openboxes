package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.apache.commons.text.StringSubstitutor
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.identification.BlankIdentifierResolver

@Transactional
class ShipmentIdentifierService extends IdentifierService<Shipment> implements BlankIdentifierResolver<Shipment> {

    @Override
    String getIdentifierName() {
        return "shipment"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        Integer count = Shipment.countByShipmentNumber(id)

        // TODO: Refactor how ids are generated for locations so that we don't need to do this check. Uniqueness
        //       of ids on one service shouldn't depend on another. The easiest solution is to override the format
        //       for generating location ids to be "${custom.shipmentNumber}${random}" so that it's like the
        //       shipment number but still unique.

        // We use shipment.shipmentNumber as location.locationNumber when creating internal locations from shipments
        // (see LocationService.findOrCreateInternalLocation for details) so we need to check that the id is unique
        // for locations as well. Only bother checking location if shipment doesn't already have a duplicate though.
        return count > 0 ? count : Location.countByLocationNumber(id)
    }

    @Override
    List<Shipment> getAllUnassignedEntities() {
        return Shipment.findAll("from Shipment as s where shipmentNumber is null or shipmentNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String id, Shipment entity) {
        entity.shipmentNumber = id
    }

    def generateSequenceNumber(String sequenceNumber) {
        String sequenceNumberFormat = Holders.grailsApplication.config.openboxes.identifier.sequenceNumber.format
        return generateSequenceNumber(sequenceNumber, sequenceNumberFormat)
    }

    def generateSequenceNumber(String sequenceNumber, String sequenceNumberFormat) {
        return StringUtils.leftPad(sequenceNumber, sequenceNumberFormat.length(), sequenceNumberFormat.substring(0, 1))
    }

    def renderTemplate(String template, Map model) {
        return StringSubstitutor.replace(template, model)
    }
}
