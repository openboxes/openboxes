<g:if test="${orderInstance?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions" style="min-width: 200px;">
			<div class="action-menu-item">
				<g:link controller="order" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" alt="View orders" style="vertical-align: middle" />
					&nbsp;${message(code: 'orders.view.label', default: 'View orders')} 
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
			<div class="action-menu-item">
				<g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_edit.png')}" alt="Edit" style="vertical-align: middle" />
					&nbsp;${message(code: 'order.edit.label', default: 'Edit order details')} 
				</g:link>		
			</div>
			<div class="action-menu-item">
				<g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" params="['skipTo': 'items']">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: middle" />
					&nbsp;${message(code: 'order.add.label', default: 'Add order items')} 
				</g:link>		
			</div>
			<div class="action-menu-item">
				<g:link controller="order" action="addComment" id="${orderInstance?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
					&nbsp;${message(code: 'order.addComment.label', default: 'Add comment')} 
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="order" action="addDocument" id="${orderInstance?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
					&nbsp;${message(code: 'order.addDocument.label', default: 'Add document')} 
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<hr/>			
			</div>
			<div class="action-menu-item">
				<g:link controller="order" action="fulfill" id="${orderInstance?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}" />
					&nbsp;${message(code: 'order.fulfill.label', default: 'Fulfill order')} 
				</g:link>				
			</div>		
			
			
			<div class="action-menu-item">
				<hr/>			
			</div>
			<div class="action-menu-item">
				<g:link controller="order" action="delete" id="${orderInstance?.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${message(code: 'order.delete.label', default: 'Delete order')} 
				</g:link>				
			</div>		
			
		</div>
	</span>
</g:if>