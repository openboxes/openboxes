<g:if test="${session?.showHiddenProducts }">
	<div class="action-menu-item">				
		<g:link controller="inventory" action="showHiddenProducts">
			<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>
			&nbsp;Hide unsupported products
		</g:link>
	</div>	
</g:if> 
<g:else>
	<div class="action-menu-item">				
		<g:link controller="inventory" action="showHiddenProducts">
			<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>
			&nbsp;Show unsupported products
		</g:link>
	</div>	
</g:else>

