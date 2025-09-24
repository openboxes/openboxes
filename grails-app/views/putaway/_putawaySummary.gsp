<%@ page import="org.pih.warehouse.core.ActivityCode" %>
<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.DocumentCode" %>
<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>


<div id="order-summary" class="summary">
	<g:if test="${orderInstance?.id}">
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
        %{-- For fetching derived statuses (preparing the list of order ids to be sent with request) --}%
		<table width="50%">
			<tbody>
				<tr class="odd">
                    <td width="1%">
                        <g:render template="/order/actions" model="[orderInstance:orderInstance]"/>
                    </td>
					<td>
                        <div class="title">
                            <small class="font-weight-bold">${orderInstance?.orderNumber}</small>
                            <g:link controller="order" action="show" id="${orderInstance?.id}">
                                ${orderInstance?.name}</g:link>
						</div>
                    </td>
                    <td class="top right" width="1%">
                        <div class="tag tag-alert">
                            <span class="${orderInstance?.id}">${g.message(code: 'default.loading.label')}</span>
                        </div>
                    </td>
                </tr>
			</tbody>
		</table>
	</g:if>
	<g:else>
		<table>
			<tbody>
				<tr>
					<td>
						<div class="title">
                            <g:if test="${orderInstance?.name}">
                                ${orderInstance?.name }
                            </g:if>
                            <g:else>
                                <warehouse:message code="order.untitled.label"/>
                            </g:else>
						</div>
					</td>
					<td width="1%" class="right">
                        <div class="tag tag-alert">
                            <warehouse:message code="default.new.label"/>
                        </div>
					</td>
				</tr>
			</tbody>
		</table>

	</g:else>
</div>
<div class="buttonBar">
    <div class="button-container">
        <g:if test="${!orderInstance?.id}">
            <g:link controller="order" action="create" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.create.label" args="[g.message(code: 'order.label')]" default="Create purchase order" />
            </g:link>
        </g:if>
        <g:if test="${orderInstance?.id}">
            <g:hasRoleApprover>
                <g:set var="isApprover" value="${true}"/>
            </g:hasRoleApprover>
            <g:if test="${!isApprover}">
                <g:set var="disabledMessage" value="${g.message(code:'errors.noPermissions.label')}"/>
            </g:if>
            <g:elseif test="${orderInstance?.shipments}">
                <g:set var="disabledMessage" value="${g.message(code:'order.errors.rollback.message')}"/>
            </g:elseif>
            <g:hasRoleInvoice>
                <g:set var="hasRoleInvoice" value="${true}"/>
            </g:hasRoleInvoice>
            <g:if test="${!hasRoleInvoice}">
                <g:set var="disabledInvoiceMessage" value="${g.message(code:'errors.noPermissions.label')}"/>
            </g:if>
            <g:if test="${orderInstance?.orderType == OrderType.findByCode(Constants.PUTAWAY_ORDER)}">

                <g:link controller="order" action="list" class="button" params="[orderType: Constants.PUTAWAY_ORDER]">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[g.message(code: 'orders.label')]" default="List orders"/>
                </g:link>
                <div class="button-group right">
                    <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
                        <warehouse:message code="order.addComment.label" default="Add comment"/>
                    </g:link>
                    <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                        <warehouse:message code="order.addDocument.label" default="Add document"/>
                    </g:link>
                    <g:link controller="putaway" action="generatePdf" id="${orderInstance?.id}" class="button" target="_blank">
                        <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />&nbsp;
                        <warehouse:message code="putaway.generatePutawayList.label" default="Generate Putaway List"/>
                    </g:link>
                </div>
                <div class="button-group right">
                    <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                        <warehouse:message code="order.wizard.showOrder.label" default="Show Putaway"/>
                    </g:link>
                    <g:set var="disabled" value="${orderInstance?.status in [OrderStatus.COMPLETED, OrderStatus.CANCELED]}"/>
                    <g:link controller="putaway" action="create" id="${orderInstance?.id}" class="button" disabled="${disabled}" disabledMessage="This feature is not available for completed and canceled putaways">
                        <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                        <warehouse:message code="default.edit.label" args="[warehouse.message(code:'putawayOrder.label')]"/>
                    </g:link>
                    <g:link controller="putaway" action="rollback" id="${orderInstance.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />&nbsp;
                        <g:message code="default.rollback.label" args="[g.message(code: 'order.label')]" default="Rollback Putaway"/>
                    </g:link>
                </div>
            </g:if>
        </g:if>
    </div>
</div>
