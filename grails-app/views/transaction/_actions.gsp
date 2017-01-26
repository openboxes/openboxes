
<span class="action-menu">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
	</button>
	<div class="actions left">
		<div class="action-menu-item">
			<g:link controller="inventory" action="listTransactions">
				<img src="${resource(dir:'images/icons/silk',file:'text_list_numbers.png')}" alt="List" />
				&nbsp;${warehouse.message(code: 'transaction.list.label')}&nbsp;
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<g:if test="${params.action != 'showTransaction' }">
			<div class="action-menu-item">
				<g:link controller="inventory" action="showTransaction" id="${transactionInstance?.id }">
					<img src="${resource(dir:'images/icons/silk',file:'zoom.png')}" alt="Show" />
				    &nbsp;${warehouse.message(code: 'transaction.show.label')}&nbsp;        						
				</g:link>
			</div>
		</g:if>
		<g:if test="${params.action != 'editTransaction' }">
			<div class="action-menu-item">
				<g:link controller="inventory" action="editTransaction" id="${transactionInstance?.id }">
					<img src="${resource(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
				    &nbsp;${warehouse.message(code: 'transaction.edit.label')}&nbsp;        						
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="inventory" action="deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
  					<img src="${resource(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
				&nbsp;${warehouse.message(code: 'transaction.delete.label')}&nbsp;
			</g:link>				
		</div>
	</div>
</span>			
