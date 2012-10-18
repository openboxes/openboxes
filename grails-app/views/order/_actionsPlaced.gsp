<g:if test="${orderInstance?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('order/list')}">
			<div class="action-menu-item">
				<g:link controller="order" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View orders" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'order.list.label', default: 'List orders')} 
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="show" id="${orderInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
				&nbsp;${warehouse.message(code: 'order.viewDetails.label')} 
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
			<g:link name="receiveOrderLink" controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />
				&nbsp;${warehouse.message(code: 'order.receiveOrder.label')} 
			</g:link>				
		</div>						
		<div class="action-menu-item">
			<g:link controller="order" action="withdraw" id="${orderInstance?.id}" onclick="alert('${warehouse.message(code: 'default.button.notSupported.message', default: 'This feature is not currently supported.')}'); return false;">
				<img src="${resource(dir: 'images/icons/silk', file: 'cart_delete.png')}" />
				&nbsp;${warehouse.message(code: 'order.cancelOrder.label')} 
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