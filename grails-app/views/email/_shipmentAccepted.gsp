${warehouse.message(code: 'email.shipmentAccepted.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}

<g:createLink controller="mobile" action="outboundDetails" id="${shipmentInstance?.requisition?.id}" absolute="${true}"/>
