${warehouse.message(code: 'email.requisitionStatusChanged.message', args: [requisition.requestNumber, requisition?.status])}

<g:link controller="mobile" action="outboundDetails" id="${requisition?.id}" absolute="${true}">Click for more details</g:link>
