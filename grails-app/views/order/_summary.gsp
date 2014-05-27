<div style="padding: 10px; border-bottom: 1px solid lightgrey;">
	<g:if test="${orderInstance?.id}">
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
		<table>
			<tbody>			
				<tr>
                    <td>
                        <g:render template="/order/actions" model="[orderInstance:orderInstance]"/>
                    </td>
					<td>
                        <div class="order-title" style="font-size: 1.5em; font-weight: bold; line-height: 1em;">
                            ${orderInstance?.orderNumber}
							${orderInstance?.description}
						</div> 
						<div>
							<span class="order-number">
								<warehouse:message code="order.orderNumber.label"/>: <b>${orderInstance?.orderNumber}</b>  
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="ordered-date">
								<warehouse:message code="order.dateOrdered.label"/>: <b><format:date obj="${orderInstance?.dateOrdered}"/></b>
							</span>
                            <g:if test="${orderInstance?.orderedBy }">
                                <span class="fade">&nbsp;|&nbsp;</span>
                                <span class="ordered-by"><warehouse:message code="order.orderedBy.label"/>:</span>
								<b>${orderInstance?.orderedBy?.name }</b>
                            </g:if>
                            <g:if test="${orderInstance?.destination }">
                                <span class="fade">&nbsp;|&nbsp;</span>
                                <span class="destination"><warehouse:message code="order.destination.label"/>:</span>
                                <b>${orderInstance?.destination?.name }</b>
                            </g:if>

                            <g:if test="${orderInstance?.origin }">
                                <span class="fade">&nbsp;|&nbsp;</span>
                                <span class="origin"><warehouse:message code="order.origin.label"/>:</span>
                                <b>${orderInstance?.origin?.name }</b>
                            </g:if>
                            <span class="fade">&nbsp;|&nbsp;</span>
                            <span class="total-price"><warehouse:message code="order.totalPrice.label"/>:</span>
                            <b><g:formatNumber number="${orderInstance?.totalPrice()?:0 }" type="currency" currencyCode="USD"/></b>



						</div>
					</td>										
					<td style="text-align: right;" class="middle">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							<p name="status"><format:metadata obj="${orderInstance?.status}"/></p>
						</div>
						<g:if test="${!params.execution && !isAddingComment && !isAddingDocument}">
                            <br/>
							<g:if test="${!orderInstance?.isPlaced()}">
								<g:form action="placeOrder">
									<g:hiddenField name="id" value="${orderInstance?.id }"/>
									<button name="placeOrder" class="button">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}" />&nbsp;
                                        ${warehouse.message(code: 'order.placeOrder.label')}</button>
								</g:form>
							</g:if>
							<g:elseif test="${!orderInstance?.isReceived() && orderInstance?.isPlaced() }">
								<g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}" class="button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                                    ${warehouse.message(code: 'order.receiveOrder.label')}
								</g:link>
							</g:elseif>
						</g:if>
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
						<div>
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">
								<g:if test="${orderInstance?.description}">
									${orderInstance?.description }
								</g:if>
								<g:else>
									<warehouse:message code="order.untitled.label"/>
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						<div class="tag">
							<warehouse:message code="default.new.label"/>
						</div>
					</td>
				</tr>
			</tbody>
		</table>

	</g:else>
</div>
<g:if test="${orderInstance?.id}">
    <div class="wizard-box">
        <div class="wizard-steps">
            <div class="${currentState?.contains('createOrder')||currentState?.equals('showOrder')?'active-step':''}">
                <g:link controller="order" action="show" id="${orderInstance?.id}">
                    <span class="step">1</span>
                    <g:if test="${orderInstance?.id}">
                        <warehouse:message code="order.wizard.showOrder.label" default="View purchase order"/>
                    </g:if>
                    <g:else>
                        <warehouse:message code="order.wizard.createOrder.label" default="Create purchase order"/>
                    </g:else>
                </g:link>
            </div>

            <div class="${currentState?.equals('editOrder')?'active-step':''}">
                <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" event="enterOrderDetails" params="[skipTo:'details']">
                    <span class="step">2</span>
                    <warehouse:message code="order.wizard.editOrder.label" default="Edit purchase order"/>
                </g:link>
            </div>
            <div class="${currentState?.equals('addItems')?'active-step':''}">
                <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" event="showOrderItems" params="[skipTo:'items']">
                    <span class="step">3</span>
                    <warehouse:message code="order.wizard.addItems.label" default="Add items"/>
                </g:link>
            </div>
            <div class="center ${currentState?.equals('placeOrder')?'active-step':''}" >
                <g:link action="purchaseOrderWorkflow" event="placeOrder">
                    <span class="step">4</span>
                    <warehouse:message code="order.placeOrder.label" default="Place order"/>
                </g:link>
            </div>
            <div class="center ${currentState?.equals('enterShipmentDetails')?'active-step':''}" >
                <g:link controller="receiveOrderWorkflow" action="receiveOrder" event="enterShipmentDetails" id="${orderInstance?.id}"  params="[skipTo:'shipment']">
                    <span class="step">5</span>
                    <warehouse:message code="order.enterShipmentDetails.label"/>
                </g:link>
            </div>
            <div class="center ${currentState?.equals('processOrderItems')?'active-step':''}">
                <g:link controller="receiveOrderWorkflow" action="receiveOrder" event="processOrderItems" id="${orderInstance?.id}"  params="[skipTo:'process']">
                    <span class="step">6</span>
                    <warehouse:message code="order.selectItemsToReceive.label"/>
                </g:link>
            </div>
            <div class="center ${currentState?.equals('confirmOrderReceipt')?'active-step':''}">
                <g:link controller="receiveOrderWorkflow" action="receiveOrder" event="confirmOrderReceipt" id="${orderInstance?.id}" params="[skipTo:'confirm']">
                    <span class="step">7</span>
                    <warehouse:message code="order.markOrderAsReceived.label"/>
                </g:link>
            </div>
        </div>

        <div class="right button-group">
            <g:link controller="order" action="print" id="${orderInstance?.id}" target="_blank" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                ${warehouse.message(code: 'order.button.print.label', default: 'Print purchase order')}
            </g:link>
        </div>



    </div>
</g:if>