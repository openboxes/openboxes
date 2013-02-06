<g:if test="${requisition?.id }">		
	<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
		<div class="button-group">
			<g:link class="button" controller="requisition" action="list">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="Back to requisitions" style="vertical-align: middle" />
				&nbsp;${warehouse.message(code: 'requisition.back.label', default: 'Back to requisitions')}
			</g:link>
		</div>
	</g:if>
	<div class="button-group">
		<g:link class="button" controller="requisition" action="show" id="${requisition?.id}">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
			&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Preview requisition')}
		</g:link>		
		<g:link class="button" controller="requisition" action="edit" id="${requisition?.id}">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
			&nbsp;${warehouse.message(code: 'requisition.edit.label', default: 'Edit requisition')}
		</g:link>		
		<%-- 
		<g:link class="button" controller="requisition" action="printDraft" id="${requisition?.id}" target="_blank">
			<img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
			&nbsp;${warehouse.message(code: 'requisition.print.label', default: 'Print requisition')}
		</g:link>
		--%>		
		<g:if test="${session?.warehouse?.id == requisition?.destination?.id }">
			<g:link class="button" controller="picklist" action="print" id="${requisition?.id}" target="_blank">
				<img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
				&nbsp;${warehouse.message(code: 'picklist.print.label', default: 'Print picklist')}
			</g:link>				
			<g:link class="button" controller="requisition" name="processRequisition" action="pick" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.process.label', default: 'Process requisition')}
			</g:link>				
			<g:link class="button" controller="requisition" action="confirm" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.confirm.label', default: 'Confirm picklist')}
			</g:link>				
			<g:link class="button" controller="requisition" action="transfer" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.transfer.label', default: 'Transfer stock')}
			</g:link>
			<g:link class="button" controller="requisition" action="printDeliveryNote" id="${requisition?.id}" target="_blank">
				<img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.printDeliveryNote.label', default: 'Print delivery note')}
			</g:link>				
		</g:if>
	</div>
	
	<g:if test="${session?.warehouse?.id == requisition?.destination?.id && false }">
		<g:isUserAdmin>
			<div class="button-group">
				<g:link class="button" controller="requisition" action="cancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}" />
					&nbsp;${warehouse.message(code: 'requisition.cancel.label', default: 'Cancel requisition')}
				</g:link>				
		        <g:link class="button" controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
		            <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
		            &nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
		        </g:link>
	        </div>
        </g:isUserAdmin>
    </g:if>
    	
</g:if>

