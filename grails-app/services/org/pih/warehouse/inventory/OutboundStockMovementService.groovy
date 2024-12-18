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
import org.hibernate.criterion.CriteriaSpecification
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
        Boolean isApprovalRequired = stockMovement?.origin?.isApprovalRequired()

        // OBPIH-5814
        // This query returns a list of OutboundStockMovementListItem ids with applied filters
        // which later will be hydrated by another OutboundStockMovementListItem.list()
        // this is a workaround to prevent missing approvers data when filtering by approvers
        def stockMovementsIds = OutboundStockMovementListItem.createCriteria().list() {
            projections {
                property "id"
            }
            setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)

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
                    // If we want to get stock movements with electronic source type when approval is not required
                    // we don't want to show the stock movements with approval statuses
                    if (!isApprovalRequired) {
                        not {
                            'in'("status", RequisitionStatus.listApproval())
                        }
                    } else if (!params.isRequestApprover) {
                        not {
                            'in'("status", [RequisitionStatus.PENDING_APPROVAL, RequisitionStatus.REJECTED])
                        }
                    }
                }
            } else {
                // When we are on the outbound list, we don't want to see requests that are not submitted by the requestor
                or {
                    isNull("sourceType")
                    and {
                        eq("sourceType", RequisitionSourceType.ELECTRONIC)
                        ne("status", RequisitionStatus.CREATED)
                    }
                }
                // If we are getting stock movements with default source type when approval is required
                // we want to show stock movements just with APPROVED status
                not {
                    'in'("status", [RequisitionStatus.PENDING_APPROVAL, RequisitionStatus.REJECTED])
                }
                // When approval is not required, we want to hide stock movements with all of the
                // approval statuses
                if (!isApprovalRequired) {
                    not {
                        'in'("status", [RequisitionStatus.APPROVED])
                    }
                }
            }
            if (params.list("approver")) {
                requisition {
                    or {
                        if (params.list("approver").contains("null")) {
                            isEmpty("approvers")
                        }
                        if (stockMovement.approvers) {
                            approvers {
                                'in'("id", stockMovement.approvers.collect { it?.id })
                            }
                        }
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
        }

        // without this guard criteria wil throw a SQL syntax exception
        // because it can't resolve condition 'in'("id", stockMovementsIds) with empty list
        if(!stockMovementsIds) {
            return []
        }

        // Hydrate previously fetched OutboundStockMovementListItem ids, also paginate and sort the data
        def stockMovements = OutboundStockMovementListItem.createCriteria().list(max: max, offset: offset) {
            'in'("id", stockMovementsIds)

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
                } else if (params.sort == "expectedShippingDate") {
                    shipment {
                        order("expectedShippingDate", params.order ?: "desc")
                    }
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
