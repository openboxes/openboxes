<g:if test="${requisition?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
			<div class="action-menu-item">
				<g:link controller="requisition" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'request.view.label', default: 'View requisitions')}
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="requisition" action="show" id="${requisition?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Show requisition details')}
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="requisition" action="edit" id="${requisition?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.edit.label', default: 'Edit requisition details')}
			</g:link>		
		</div>		
		<div class="action-menu-item">
			<g:link controller="requisition" action="addComment" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
				&nbsp;${warehouse.message(code: 'request.addComment.label', default: 'Add comment')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<g:link controller="requisition" action="addDocument" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
				&nbsp;${warehouse.message(code: 'request.addDocument.label', default: 'Add document')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<hr/>
		</div>
		<g:if test="${session?.warehouse?.id == requisition?.origin?.id }">
			<div class="action-menu-item">
				<g:link controller="fulfillRequestWorkflow" action="fulfillRequest" id="${requisition?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />
					&nbsp;${warehouse.message(code: 'request.fulfill.label', default: 'Fulfill request')} 
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="requisition" action="showPicklist" id="${requisition?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />
					&nbsp;${warehouse.message(code: 'request.showPickList.label', default: 'Show pick list')} 
				</g:link>				
			</div>		
		</g:if>
		<g:if test="${session?.warehouse?.id == requisition?.destination?.id }">
			<g:if test="${requisition?.status == org.pih.warehouse.requisition.RequisitionStatus.FULFILLED }">
				<div class="action-menu-item">
					<g:link controller="receiveRequestWorkflow" action="receiveRequest" id="${requisition?.id}">
						<img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />
						&nbsp;${warehouse.message(code: 'request.receive.label', default: 'Receive requisition')}
					</g:link>				
				</div>						
			</g:if>
			<div class="action-menu-item">
				<g:link controller="requisition" action="withdraw" id="${requisition?.id}" onclick="alert('${warehouse.message(code: 'default.button.notSupported.message', default: 'This feature is not currently supported.')}'); return false;">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart_delete.png')}" />
					&nbsp;${warehouse.message(code: 'request.cancel.label', default: 'Cancel requisition')}
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
				</g:link>				
			</div>		
		</g:if>
	</div>
</g:if>