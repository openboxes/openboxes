<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>

<div id="order-summary" class="summary">
	<g:if test="${orderInstance?.id}">
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
		<table width="50%">
			<tbody>
				<tr class="odd">
                    <td width="1%">
                        <g:render template="/order/actions" model="[orderInstance:orderInstance]"/>
                    </td>
					<td>
                        <div class="title">
                            <small>${orderInstance?.orderNumber}</small>
                            <g:link controller="order" action="show" id="${orderInstance?.id}">
                                ${orderInstance?.name}</g:link>
						</div>
                    </td>
                    <td class="top right" width="1%">
                        <div class="tag tag-alert">
                            <format:metadata obj="${orderInstance?.status}"/>
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
    <div class="button-group">
        <g:link controller="order" action="show" id="${orderInstance?.id}" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
            <g:if test="${orderInstance?.id}">
                <warehouse:message code="order.wizard.showOrder.label" default="View purchase order"/>
            </g:if>
            <g:else>
                <warehouse:message code="order.wizard.createOrder.label" default="Create purchase order"/>
            </g:else>
        </g:link>

        <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" event="enterOrderDetails" params="[skipTo:'details']" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
            <warehouse:message code="order.wizard.editOrder.label" default="Edit"/>
        </g:link>

        <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" event="showOrderItems" params="[skipTo:'items']" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'cart_put.png')}" />&nbsp;
            <warehouse:message code="order.wizard.addItems.label" default="Add line items"/>
        </g:link>
        <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
            <warehouse:message code="order.wizard.addComment.label" default="Add comment"/>
        </g:link>

        <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
            <warehouse:message code="order.wizard.addDocument.label" default="Add document"/>
        </g:link>
    </div>

    <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}"
        <div class="button-group">

            <g:if test="${!orderInstance?.isPlaced()}">
                <g:link controller="order" action="placeOrder" id="${orderInstance?.id}" class="button" >
                    <img src="${resource(dir: 'images/icons/silk', file: 'creditcards.png')}" />&nbsp;
                    ${warehouse.message(code: 'order.wizard.placeOrder.label')}</g:link>
            </g:if>
            <g:else>
                <g:link controller="order" action="placeOrder" id="${orderInstance?.id}" class="button" disabled="disabled" >
                    <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
                    ${warehouse.message(code: 'order.wizard.placeOrder.label')}</g:link>

            </g:else>
            <g:if test="${!orderInstance?.isReceived() && orderInstance?.isPlaced() }">
                <g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                    ${warehouse.message(code: 'order.wizard.receiveOrder.label')}
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}" class="button" onClick="alert('You cannot perform this action at this time.'); return false;">
                    <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                    ${warehouse.message(code: 'order.wizard.receiveOrder.label')}
                </g:link>
            </g:else>
            <g:link controller="order" action="print" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                <warehouse:message code="order.wizard.printOrder.label" default="Print PO"/>
            </g:link>

            <g:link controller="order" action="download" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                <warehouse:message code="order.wizard.downloadOrder.label" default="Download Order"/>
            </g:link>
            <g:link controller="order" action="downloadOrderItems" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                <warehouse:message code="order.wizard.downloadOrderItems.label" default="Download Items Only"/>
            </g:link>

        </div>

    </g:if>
    <g:elseif test="${orderInstance?.orderTypeCode==OrderTypeCode.TRANSFER_ORDER}">
        <div class="button-group">
            <g:link controller="putAway" action="generatePdf" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" />&nbsp;
                <warehouse:message code="order.generatePdf.label" default="Generate PDF"/>
            </g:link>
        </div>
    </g:elseif>


</div>