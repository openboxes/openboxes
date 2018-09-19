<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
        <g:hasErrors bean="${orderItem}">
            <div class="errors">
                <g:renderErrors bean="${orderItem}" as="list" />
            </div>
        </g:hasErrors>


        <div class="dialog">

            <g:render template="/order/summary" model="[orderInstance:order,currentState:'placeOrder']"/>

            <div class="box">
                <h2><warehouse:message code="order.wizard.placeOrder.label"/></h2>
                <div class="empty center" style="margin:15px">
                    <div>
                        Are you sure you want to place this purchase order with the supplier?
                        <g:link controller="order" action="print" id="${order?.id}" class="button icon log" target="_blank">
                            ${warehouse.message(code: 'order.print.label', default:"Print order")}
                        </g:link>
                    </div>


                </div>
                <%--
                <table style="">
                    <thead>
                        <tr class="odd">
                            <th><warehouse:message code="default.actions.label"/></th>
                            <th><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                            <g:sortableColumn property="product.productCode" title="${warehouse.message(code:'product.productCode.label')}" />
                            <g:sortableColumn property="product.name" title="${warehouse.message(code:'product.name.label')}" />
                            <g:sortableColumn property="product.vendor" title="${warehouse.message(code:'vendor.name.label', default: 'Vendor')}" />
                            <g:sortableColumn property="product.manufacturer" title="${warehouse.message(code:'manufacturer.name.label', default: 'Manufacturer')}" />

                            <g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
                            <g:sortableColumn property="unitOfMeasure" title="${warehouse.message(code:'default.uom.label', default: 'UOM')}" class="center"/>
                            <g:sortableColumn class="right" property="unitPrice" title="${warehouse.message(code:'order.unitPrice.label')}" />
                            <g:sortableColumn class="right" property="totalPrice" title="${warehouse.message(code:'order.totalPrice.label')}" />
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="i" value="${0 }"/>
                        <g:each var="orderItem" in="${order?.orderItems}">
                            <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                                <g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>
                                <td class="actionButtons">
                                    <g:if test="${orderItem?.id }">
                                        <a href="javascript:void(-1);" id="edit-item-dialog-${orderItem?.id}" class="button icon edit edit-item-button" data-order-item-id="${orderItem?.id}">
                                            <warehouse:message code="default.button.edit.label"/>
                                        </a>
                                        <g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem" class="button icon trash" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                            <warehouse:message code="default.button.delete.label"/>
                                        </g:link>
                                    </g:if>
                                </td>

                                <td class="middle">
                                    ${i}
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.productCode}
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.name?:orderItem?.description?.encodeAsHTML()}
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.vendor?:"N/A"}
                                    <g:if test="${orderItem?.product?.vendorCode}">
                                        #${orderItem?.product?.vendorCode}
                                    </g:if>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.manufacturer?:"N/A"}
                                    <g:if test="${orderItem?.product?.manufacturerCode}">
                                       #${orderItem?.product?.manufacturerCode}
                                    </g:if>
                                </td>
                                <td class="middle">
                                    ${orderItem?.quantity }
                                    <g:hiddenField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"/>
                                </td>
                                <td class="middle center">
                                    ${orderItem?.product?.unitOfMeasure?:"EA"}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.unitPrice ?: 0.00 }" format="###,###,##0.00##"/>
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.totalPrice() }"  type="currency" currencyCode="USD" />
                                </td>
                            </tr>
                        </g:each>
                        <g:unless test="${order?.orderItems}">
                            <tr>
                                <td colspan="10">
                                    <div class="fade center empty">
                                        <p name="numItemInOrder">
                                            <warehouse:message code="order.itemsInOrder.message" args="[(order?.orderItems)?order?.orderItems?.size():0]"/>
                                            <button class="button icon add add-item-button">${warehouse.message(code:'order.button.addItem.label', default: 'Add item')}</button>
                                        </p>
                                    </div>
                                </td>
                            </tr>

                        </g:unless>
                    </tbody>
                    <tfoot>
                        <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                            <th>
                                <button class="button icon add add-item-button">${warehouse.message(code:'order.button.addItem.label', default: 'Add item')}</button>
                            </th>
                            <th colspan="9" class="right">
                                <warehouse:message code="default.total.label"/>
                                <g:formatNumber number="${order?.totalPrice()?:0.0 }" type="currency" currencyCode="USD"/>
                            </th>
                        </tr>
                    </tfoot>

                </table>
                --%>

            </div>
		</div>
	</div>
	<g:comboBox />
    <script type="text/javascript">
        $( function() {
            var cookieName, $tabs, stickyTab;

            cookieName = 'stickyTab';
            $tabs = $( '.tabs' );

            $tabs.tabs( {
                select: function( e, ui )
                {
                    $.cookies.set( cookieName, ui.index );
                }
            } );

            stickyTab = $.cookies.get( cookieName );
            if( ! isNaN( stickyTab )  )
            {
                $tabs.tabs( 'select', stickyTab );
            }
        } );


        $(document).ready(function(){

        });


    </script>
</body>
</html>