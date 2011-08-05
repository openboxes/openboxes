<g:if test="${session?.showHiddenProducts }">
	<div class="action-menu-item">				
		<g:link controller="inventory" action="showHiddenProducts">
			<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>
			&nbsp;<warehouse:message code="inventory.hideUnsupportedProducts.label"/>
		</g:link>
	</div>	
</g:if> 
<g:else>
	<div class="action-menu-item">				
		<g:link controller="inventory" action="showHiddenProducts">
			<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>
			&nbsp;<warehouse:message code="inventory.showUnsupportedProducts.label"/>
		</g:link>
	</div>	
</g:else>
<div class="action-menu-item">			
	<a class="toggle-outofstock" href="javascript:void();">
		<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>
		<warehouse:message code="inventory.toggleOutOfStockItems.label"/>
	</a>
</div>

