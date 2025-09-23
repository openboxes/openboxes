<%@ page import="org.pih.warehouse.core.ActivityCode" %>
<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<g:if test="${orderInstance?.id }">
	<g:set var="PURCHASE_ORDER" value="${OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())}"/>
	<g:set var="PUTAWAY_ORDER" value="${OrderType.findByCode(Constants.PUTAWAY_ORDER)}"/>
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions" style="min-width: 200px;">
			<g:if test="${!request.request.requestURL.toString().contains('order/list') && orderInstance?.orderType != PUTAWAY_ORDER}">
				<div class="action-menu-item">
					<g:if test="${orderInstance?.orderType == PURCHASE_ORDER}">
						<g:link controller="purchaseOrder" action="list">
							<img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View orders" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'order.list.label')}
						</g:link>
					</g:if>
					<g:else>
						<g:link controller="order" action="list">
							<img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View orders" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'order.list.label')}
						</g:link>
					</g:else>
				</div>
			</g:if>
			<div class="action-menu-item">
				<hr/>
			</div>

            <div class="action-menu-item">
                <g:if test="${orderInstance?.orderType == PUTAWAY_ORDER}">
                    <g:link controller="putaway" action="show" id="${orderInstance?.id}">
                        <img src="${resource(dir:'images/icons/silk',file:'zoom.png')}" />
                        &nbsp;${warehouse.message(code: 'putaway.viewDetails.label', default: 'View Putaway')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="order" action="show" id="${orderInstance?.id}">
                        <img src="${resource(dir:'images/icons/silk',file:'zoom.png')}" />
                        &nbsp;${warehouse.message(code: 'order.viewDetails.label')}
                    </g:link>
                </g:else>
            </div>
			<g:if test="${orderInstance?.orderType != PURCHASE_ORDER || Location.load(session?.warehouse?.id).supports(ActivityCode.PLACE_ORDER)}">
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
			</g:if>
			<g:if test="${orderInstance?.orderType == PURCHASE_ORDER}">
				<g:supports activityCode="${ActivityCode.PLACE_ORDER}">
					<g:if test="${orderInstance?.isPending()}">
						<div class="action-menu-item">
							<g:link controller="purchaseOrder" action="edit" id="${orderInstance?.id}" params="[id:orderInstance?.id]">
								<img src="${resource(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" style="vertical-align: middle" />
								&nbsp;${warehouse.message(code: 'order.editDetails.label')}
							</g:link>
						</div>
						<div class="action-menu-item">
							<g:link controller="purchaseOrder" action="addItems" id="${orderInstance?.id}">
								<img src="${resource(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: middle" />
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
								disabled="${orderInstance?.status < OrderStatus.PLACED}"
								disabledMessage="Order must be placed in order to print.">
							<img src="${resource(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>&nbsp;
							<g:message code="order.print.label" default="Print order"/>
						</g:link>
					</div>
					<g:if test="${orderInstance?.isPlaced()}">
						<div class="action-menu-item">
							<g:link controller="order" action="withdraw" id="${orderInstance?.id}" onclick="alert('${warehouse.message(code: 'default.button.notSupported.message', default: 'This feature is not currently supported.')}'); return false;">
								<img src="${resource(dir: 'images/icons/silk', file: 'cart_delete.png')}" />
								&nbsp;${warehouse.message(code: 'order.cancelOrder.label')}
							</g:link>
						</div>
					</g:if>
				</g:supports>
			</g:if>
			<g:elseif test="${orderInstance?.orderType == PUTAWAY_ORDER}">
				<div class="action-menu-item">
					<g:link controller="putAway" action="generatePdf" id="${orderInstance?.id}" target="_blank">
						<img src="${resource(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>
						<warehouse:message code="putaway.generatePutawayList.label" default="Generate Putaway List"/>
					</g:link>
				</div>
			</g:elseif>
			<g:if test="${orderInstance?.orderType == PURCHASE_ORDER}">
				<g:supports activityCode="${ActivityCode.PLACE_ORDER}">
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
								<g:set var="disabledMessage" value="${g.message(code: 'order.errors.rollback.message')}"/>
							</g:elseif>
							<g:if test="${orderInstance?.isPlaced()}">
								<g:link controller="order" action="rollbackOrderStatus" id="${orderInstance?.id}"
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
										disabled="${orderInstance?.shipments || !isApprover}"
										disabledMessage="${disabledMessage}">
									<img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />
									&nbsp;${warehouse.message(code: 'order.rollbackOrderStatus.label', default: "Rollback order status" )}
								</g:link>
							</g:if>
						</div>
					</g:isSuperuser>
					<g:isUserInRole roles="[RoleType.ROLE_ASSISTANT]">
						<div class="action-menu-item">
							<g:link controller="order" action="remove" id="${orderInstance?.id}" params="${params}"
									disabled="${orderInstance?.status != OrderStatus.PENDING}"
									disabledMessage="${g.message(code:'order.errors.delete.message')}"
									onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
								&nbsp;${warehouse.message(code: 'order.deleteOrder.label')}
							</g:link>
						</div>
					</g:isUserInRole>
				</g:supports>
			</g:if>
			<g:elseif test="${orderInstance?.orderType == PUTAWAY_ORDER && orderInstance?.status != OrderStatus.COMPLETED}">
				<g:isUserInRole roles="[RoleType.ROLE_ASSISTANT]">
					<div class="action-menu-item">
						<g:link controller="order" action="remove" id="${orderInstance?.id}" params="${params}"
								disabled="${orderInstance?.status != OrderStatus.PENDING}"
								disabledMessage="${g.message(code: 'order.errors.delete.message')}"
								onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
							<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
							&nbsp;${warehouse.message(code: 'order.deleteOrder.label')}
						</g:link>
					</div>
				</g:isUserInRole>
			</g:elseif>
		</div>
	</span>
</g:if>
