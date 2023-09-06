/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.hibernate.ObjectNotFoundException
import org.hibernate.sql.JoinType
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.ShipmentType

@Transactional
class OutboundStockMovementService {

    def getStockMovement(String id) {
        OutboundStockMovement outboundStockMovement = OutboundStockMovement.get(id)

        // Temporary returning outboundStockMovement even if not found.
        // This is a workaround, because we don't have INBOUND SM covered for the PO shipment case.
        return outboundStockMovement
        // if (outboundStockMovement) {
        //     return outboundStockMovement
        // } else {
        //     throw new ObjectNotFoundException(id, OutboundStockMovement.class.toString())
        // }
    }

    def getStockMovements(StockMovement stockMovement, Map params) {
        params.includeStockMovementItems = false
        def max = params.max ? params.int("max") : null
        def offset = params.offset ? params.int("offset") : null
        Date createdAfter = params.createdAfter ? Date.parse("MM/dd/yyyy", params.createdAfter) : null
        Date createdBefore = params.createdBefore ? Date.parse("MM/dd/yyyy", params.createdBefore) : null
        List<ShipmentType> shipmentTypes = params.list("shipmentType") ? params.list("shipmentType").collect{ ShipmentType.read(it) } : null

        def stockMovements = OutboundStockMovementListItem.createCriteria().list(max: max, offset: offset) {

            if (stockMovement?.receiptStatusCodes) {
                'in'("shipmentStatus", stockMovement.receiptStatusCodes)
            }

            if (stockMovement?.identifier || stockMovement.name || stockMovement?.description) {
                or {
                    if (stockMovement?.identifier) {
                        ilike("identifier", stockMovement.identifier)
                    }
                    if (stockMovement?.name) {
                        ilike("name", stockMovement.name)
                    }
                    if (stockMovement?.description) {
                        ilike("description", stockMovement.description)
                    }
                }
            }

            if (stockMovement.destination == stockMovement?.origin) {
                or {
                    if (stockMovement?.destination) {
                        eq("destination", stockMovement.destination)
                    }
                    if (stockMovement?.origin) {
                        eq("origin", stockMovement.origin)
                    }
                }
            } else {
                if (stockMovement?.destination) {
                    eq("destination", stockMovement.destination)
                }
                if (stockMovement?.origin) {
                    eq("origin", stockMovement.origin)
                }
            }
            if (stockMovement.requisitionStatusCodes) {
                'in'("status", stockMovement.requisitionStatusCodes)
            }
            if (stockMovement.requestedBy) {
                eq("requestedBy", stockMovement.requestedBy)
            }
            if (stockMovement.createdBy) {
                eq("createdBy", stockMovement.createdBy)
            }
            if (stockMovement.updatedBy) {
                eq("updatedBy", stockMovement.updatedBy)
            }
            if (stockMovement.requestType) {
                eq("requestType", stockMovement.requestType)
            }
            if (stockMovement.sourceType) {
                eq ('sourceType', stockMovement.sourceType)
                if (stockMovement.sourceType == RequisitionSourceType.ELECTRONIC) {
                    not {
                        'in'("status", [RequisitionStatus.CREATED, RequisitionStatus.ISSUED, RequisitionStatus.CANCELED])
                    }
                }
            }
            if(createdAfter) {
                ge("dateCreated", createdAfter)
            }
            if(createdBefore) {
                le("dateCreated", createdBefore)
            }
            if (shipmentTypes) {
                shipment {
                    'in'("shipmentType", shipmentTypes)
                }
            }

            if (params.sort) {
                if (params.sort == "destination.name") {
                    destination {
                        order("name", params.order ?: "desc")
                    }
                } else if (params.sort == "origin.name") {
                    origin {
                        order("name", params.order ?: "desc")
                    }
                } else if (params.sort == "requestedBy.name") {
                    requestedBy {
                        order("firstName", params.order ?: "desc")
                        order("lastName", params.order ?: "desc")
                    }
                } else if (params.sort == "dateRequested") {
                    order("dateRequested", params.order ?: "desc")
                } else if (params.sort == "stocklist.name") {
                    requisition(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        requisitionTemplate(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                            order("name", params.order ?: "desc")
                        }
                    }
                } else {
                    order(params.sort, params.order ?: "desc")
                }
            } else {
                order("statusSortOrder", "asc")
                order("dateCreated", "desc")
            }
        }

        return stockMovements
    }
}
