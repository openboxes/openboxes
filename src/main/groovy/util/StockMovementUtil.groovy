package util

import grails.util.Holders
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition

class StockMovementUtil {
    static String generateStockMovementName(Requisition req, String trackingNumber) {
        final String separator =
                Holders.getConfig().getProperty("openboxes.generateName.separator") ?: Constants.DEFAULT_NAME_SEPARATOR

        Location origin = req?.origin
        Location destination = req?.destination
        Date dateRequested = req?.dateRequested
        Requisition stocklist = req?.requisitionTemplate
        String description = req?.destination

        String originIdentifier = origin?.locationNumber ?: origin?.name
        String destinationIdentifier = destination?.locationNumber ?: destination?.name
        String name = "${originIdentifier}${separator}${destinationIdentifier}"
        if (dateRequested) name += "${separator}${dateRequested?.format("ddMMMyyyy")}"
        if (stocklist?.name) name += "${separator}${stocklist.name}"
        if (trackingNumber) name += "${separator}${trackingNumber}"
        if (description) name += "${separator}${description}"
        name = name.replace(" ", "")
        return name
    }
}
