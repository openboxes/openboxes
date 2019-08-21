
<span class="action-menu">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
	</button>
	<div class="actions left">
		<g:if test="${params?.product?.id }">
			<div class="action-menu-item">
				<g:link controller="inventoryItem" action="showStockCard" params="['product.id':params?.product?.id]">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'book.png')}"/>
					&nbsp;${warehouse.message(code: 'transaction.backToStockCard.label')}
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="inventory" action="browse">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_list.png')}"/>
				&nbsp;${warehouse.message(code: 'transaction.backToInventory.label')}
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<g:if test="${params.action != 'showTransaction' }">
			<div class="action-menu-item">
				<g:link controller="inventory" action="showTransaction" id="${transactionInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Show" />
				    &nbsp;${warehouse.message(code: 'default.button.show.label')}
				</g:link>
			</div>
		</g:if>
		<g:if test="${params.action != 'editTransaction' }">
			<div class="action-menu-item">
				<g:link controller="inventory" action="editTransaction" id="${transactionInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
				    &nbsp;${warehouse.message(code: 'default.button.edit.label')}
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="inventory" action="deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
  					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
				&nbsp;${warehouse.message(code: 'default.button.delete.label')}
			</g:link>				
		</div>			
	</div>
</span>			
