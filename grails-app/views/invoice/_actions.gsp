<g:if test="${invoiceId}">
	<span class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions" style="min-width: 200px;">
			<g:hasRoleInvoice>
				<div class="action-menu-item">
					<g:link controller="invoice" action="show" id="${invoiceId}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />&nbsp;
						<warehouse:message code="invoice.viewDetails.label" default="View Invoice Details"/>
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="invoice" action="create" id="${invoiceId}">
						<img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
						<warehouse:message code="invoice.editInvoice.label" default="Edit Invoice"/>
					</g:link>
				</div>
			</g:hasRoleInvoice>
        </div>
	</span>
</g:if>
