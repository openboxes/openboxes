package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.hibernate.FetchMode
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class DemandService {
    Map<DemandTypeCode, List<OutboundDemand>> getDemandMap() {
        def statuses = RequisitionStatus.values().findAll {
            it.sortOrder < RequisitionStatus.PICKING.sortOrder
        }

        def relevantItems = RequisitionItem.createCriteria().list {
            requisition {
                'in'('status', statuses)
            }
            fetchMode 'product', FetchMode.JOIN
            fetchMode 'requisition', FetchMode.JOIN
        }

        if (!relevantItems) {
            return [:]
        }

        def groupedByProductAndType = relevantItems.groupBy { item ->
            def demandType = item.requisition.getDemandTypeCode()
            [product: item.product, demandTypeCode: demandType]
        }.findAll { key, value ->
            key.demandTypeCode != null
        }

        List<OutboundDemand> demands = groupedByProductAndType.collect { key, items ->
            new OutboundDemand(
                    product: key.product,
                    demandTypeCode: key.demandTypeCode,
                    quantity: items.sum { it.quantity ?: 0 } as Integer
            )
        }

        return demands.groupBy { it.demandTypeCode }
    }
}