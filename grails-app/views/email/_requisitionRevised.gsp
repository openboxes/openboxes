${warehouse.message(code: 'email.requisitionRevised.message', args: [requisition?.requestNumber])}

<g:link controller="mobile" action="outboundDetails" id="${requisition?.id}" absolute="${true}">Click for more details</g:link>
