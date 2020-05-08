<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<g:if test="${orderInstance?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
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
			<g:if test="${orderInstance?.isPending() && orderInstance?.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">
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
					<g:link controller="order" action="placeOrder" id="${orderInstance?.id}">
						<img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}" />
						&nbsp;${warehouse.message(code: 'order.placeOrder.label')}
					</g:link>
				</div>
			</g:if>
			<div class="action-menu-item">
				<g:link target="_blank" controller="order" action="print" id="${orderInstance?.id}"
						disabled="${orderInstance?.status < org.pih.warehouse.order.OrderStatus.PLACED}"
                        disabledMessage="Order must be placed in order to print.">
					<img src="${createLinkTo(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>&nbsp;
					<warehouse:message code="order.print.label" default="Print order"/>
				</g:link>
			</div>
			<g:if test="${orderInstance?.isPlaced() && orderInstance?.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">
				<div class="action-menu-item">
					<g:link controller="order" action="withdraw" id="${orderInstance?.id}" onclick="alert('${warehouse.message(code: 'default.button.notSupported.message', default: 'This feature is not currently supported.')}'); return false;">
						<img src="${resource(dir: 'images/icons/silk', file: 'cart_delete.png')}" />
						&nbsp;${warehouse.message(code: 'order.cancelOrder.label')}
					</g:link>
				</div>
			</g:if>
			<g:isSuperuser>
				<div class="action-menu-item">
					<hr/>
				</div>

				<div class="action-menu-item">
					<g:hasRoleApprover>
						<g:set var="isApprover" value="${true}"/>
					</g:hasRoleApprover>
					<g:if test="${!isApprover}">
						<g:set var="disabledMessage" value="${g.message(code:'errors.noPermissions.label')}"/>
					</g:if>
					<g:elseif test="${orderInstance?.shipments}">
						<g:set var="disabledMessage" value="${g.message(code:'order.errors.rollback.message')}"/>
					</g:elseif>
					<g:link controller="order" action="rollbackOrderStatus" id="${orderInstance?.id}"
							onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
							disabled="${orderInstance?.shipments || !isApprover}"
							disabledMessage="${disabledMessage}">
						<img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />
						&nbsp;${warehouse.message(code: 'order.rollbackOrderStatus.label', default: "Rollback order status" )}
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="order" action="remove" id="${orderInstance?.id}"
							disabled="${orderInstance?.status != org.pih.warehouse.order.OrderStatus.PENDING}"
							disabledMessage="${g.message(code:'order.errors.delete.message')}"
							onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
						<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
						&nbsp;${warehouse.message(code: 'order.deleteOrder.label')}
					</g:link>
				</div>
			</g:isSuperuser>
        </div>
	</span>
</g:if>
