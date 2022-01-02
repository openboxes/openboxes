${warehouse.message(code: 'email.shipmentStatusChanged.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.shipmentNumber, shipmentInstance?.mostRecentEvent?.eventType?.name])}

<g:link controller="mobile" action="outboundDetails" id="${shipmentInstance?.requisition?.id}" absolute="${true}">Click for more details</g:link>
