<g:if test="${orderInstance?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('order/list')}">
			<div class="action-menu-item">
				<g:link controller="order" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View orders" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'order.list.label')} 
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="show" id="${orderInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Edit" style="vertical-align: middle" />
				&nbsp;${warehouse.message(code: 'order.viewDetails.label')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" style="vertical-align: middle" />
				&nbsp;${warehouse.message(code: 'order.editDetails.label')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" params="['skipTo': 'items']">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: middle" />
				&nbsp;${warehouse.message(code: 'order.editItems.label')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="addComment" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
				&nbsp;${warehouse.message(code: 'order.addComment.label')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<g:link controller="order" action="addDocument" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
				&nbsp;${warehouse.message(code: 'order.addDocument.label')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="placeOrder" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}" />
				&nbsp;${warehouse.message(code: 'order.placeOrder.label')} 
			</g:link>				
		</div>		
		<g:if test="${!hideDelete}">					
			<div class="action-menu-item">
				<g:link controller="order" action="delete" id="${orderInstance?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${warehouse.message(code: 'order.deleteOrder.label')} 
				</g:link>				
			</div>
		</g:if>		
	</div>
</g:if>