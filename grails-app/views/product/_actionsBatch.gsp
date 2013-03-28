<span class="action-menu">
	<button name="actionButtonDropDown" class="action-btn" id="product-action">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link controller="product" action="batchEdit2">
				<img src="${resource(dir: 'images/icons/silk', file: 'database_table.png')}"/>&nbsp;
				<warehouse:message code="product.batchEdit.label" default="Batch edit"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
	</div>
</span>
