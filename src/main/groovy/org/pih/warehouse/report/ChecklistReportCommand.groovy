/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.report

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment

class ChecklistReportCommand {

    Shipment shipment
    Location location
    Date date
    Date startDate
    Date endDate
    Category category
    Category rootCategory

    List<Shipment> shipments
    List<ChecklistReportEntryCommand> checklistReportEntryList = []
    Map<Product, ChecklistReportEntryCommand> checklistReportEntryMap = [:]

    static constraints = {
        shipment(nullable: false)
        location(nullable: true)
        startDate(nullable: true)
        endDate(nullable: true)
        category(nullable: true)
    }

}