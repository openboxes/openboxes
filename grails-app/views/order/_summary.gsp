<%--
<g:if test="${orderInstance?.id}">
    <div class="wizard-box center" >
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
            <!--
            <div class="center ${currentState?.equals('placeOrder')?'active-step':''}" >
                <g:link action="purchaseOrderWorkflow" event="placeOrder">
                    <span class="step">4</span>
                    <warehouse:message code="order.wizard.placeOrder.label" default="Place order"/>
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
            -->
        </div>

    </div>
</g:if>
--%>
<div>
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
                        <g:link controller="order" action="show" id="${orderInstance?.id}"><warehouse:message code="order.label"/> ${orderInstance?.orderNumber}</g:link>
                        <div class="order-title" style="font-size: 1.5em; font-weight: bold; line-height: 1em;">
							${orderInstance?.description}
						</div>
                    </td>
                    <td class="top right">
                        <div>
                            <label class="fade">${warehouse.message(code: 'order.status.label')}</label>
                            <b><format:metadata obj="${orderInstance?.status}"/></b>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">

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
                            <warehouse:message code="order.wizard.editOrder.label" default="Edit purchase order"/>
                        </g:link>

                        <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" event="showOrderItems" params="[skipTo:'items']" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_put.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.addItems.label" default="Add items"/>
                        </g:link>

                        <g:link controller="order" action="addComment" id="${orderInstance?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'comment.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.addComment.label" default="Add comment"/>
                        </g:link>

                        <g:link controller="order" action="addDocument" id="${orderInstance?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.addDocument.label" default="Add document"/>
                        </g:link>

                        <g:link controller="order" action="print" id="${orderInstance?.id}" class="button" target="_blank">
                            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                            <warehouse:message code="order.wizard.printOrder.label" default="Print PO"/>
                        </g:link>

                        <g:if test="${!orderInstance?.isPlaced()}">
                            <g:link controller="order" action="placeOrder" id="${orderInstance?.id}" class="button" >
                                <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
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
                            <g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}" class="button" disabled="disabled">
                                <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                                ${warehouse.message(code: 'order.wizard.receiveOrder.label')}
                            </g:link>
                        </g:else>

                    </td>

                </tr>
                <tr class="prop">

                    <td colspan="3">
						<div>


                            <table>
                                <tr>
                                    <td>
                                        <div class="order-number">
                                            <label class="fade"><warehouse:message code="order.orderNumber.label"/></label>
                                            <b>${orderInstance?.orderNumber}</b>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="ordered-date">
                                            <label class="fade"><warehouse:message code="order.dateOrdered.label"/></label>
                                            <b><format:date obj="${orderInstance?.dateOrdered}"/></b>
                                        </div>
                                    </td>
                                    <td>
                                        <g:if test="${orderInstance?.orderedBy }">
                                            <div class="ordered-by">
                                                <label class="fade"><warehouse:message code="order.orderedBy.label"/></label>
                                                <b>${orderInstance?.orderedBy?.name }</b>
                                            </div>
                                        </g:if>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="total-price">
                                            <label class="fade"><warehouse:message code="order.totalPrice.label"/></label>
                                            <b><g:formatNumber number="${orderInstance?.totalPrice()?:0 }" type="currency" currencyCode="USD"/></b>
                                        </div>


                                    </td>
                                    <td>
                                        <g:if test="${orderInstance?.destination }">
                                            <div class="destination">
                                                <label class="fade"><warehouse:message code="order.destination.label"/></label>
                                                <b>${orderInstance?.destination?.name }</b>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <div class="origin">
                                            <label class="fade"><warehouse:message code="order.origin.label"/></label>
                                            <b>${orderInstance?.origin?.name }</b>
                                        </div>

                                    </td>
                                </tr>
                            </table>
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
                        <div>
                            <label class="fade">${warehouse.message(code: 'order.status.label')}</label>
                            <b><warehouse:message code="default.new.label"/></b>
                        </div>
					</td>
				</tr>
			</tbody>
		</table>

	</g:else>
</div>
