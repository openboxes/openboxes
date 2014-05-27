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
            <g:render template="/order/summary" model="[orderInstance:order,currentState:'addItems']"/>
            <div class="box">
                <h2><warehouse:message code="order.wizard.addItems.label"/></h2>
                <table style="">
                    <thead>
                        <tr class="odd">
                            <g:sortableColumn property="type" title="${warehouse.message(code:'default.type.label')}" />
                            <g:sortableColumn property="name" title="${warehouse.message(code:'default.name.label')}" />
                            <g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
                            <g:sortableColumn property="unitOfMeasure" title="${warehouse.message(code:'product.unitOfMeasure.label')}" />
                            <g:sortableColumn class="right" property="unitPrice" title="${warehouse.message(code:'order.unitPrice.label')}" />
                            <g:sortableColumn class="right" property="totalPrice" title="${warehouse.message(code:'order.totalPrice.label')}" />
                            <th><warehouse:message code="default.actions.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="i" value="${0 }"/>
                        <g:each var="orderItem" in="${order?.orderItems}">
                            <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                                <g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>
                                <td class="middle">
                                    ${orderItem?.orderItemType }
                                </td>
                                <td class="middle">
                                    ${orderItem?.description?.encodeAsHTML()}
                                </td>
                                <td class="middle">
                                    ${orderItem?.quantity }
                                    <g:hiddenField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"/>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.unitOfMeasure?:"EA"}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.unitPrice ?: 0.00 }" type="currency" currencyCode="USD"/>
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.totalPrice() }"  type="currency" currencyCode="USD" />
                                </td>
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
                            </tr>
                        </g:each>
                        <g:unless test="${order?.orderItems}">
                            <tr>
                                <td colspan="7">
                                    <div style="margin: 10px" class="fade center empty">
                                        <p name="numItemInOrder"><warehouse:message code="order.itemsInOrder.message" args="[(order?.orderItems)?order?.orderItems?.size():0]"/></p>
                                    </div>
                                </td>
                            </tr>

                        </g:unless>
                        <tr>
                            <td class="middle left" colspan="6">
                            </td>
                            <td class="middle left">
                                <button id="add-item-button" class="button icon add">${warehouse.message(code:'order.button.addItem.label', default: 'Add item')}</button>
                            </td>

                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                            <td colspan="5">
                                <warehouse:message code="default.total.label"/>
                            </td>
                            <td class="right">
                                <g:formatNumber number="${order?.totalPrice()?:0.0 }" type="currency" currencyCode="USD"/>
                            </td>
                            <td>

                            </td>
                        </tr>
                    </tfoot>

                </table>
            </div>

            <div id="add-item-dialog" class="dialog box">
                <g:form action="purchaseOrder" method="post">
                    <g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
                    <g:hiddenField name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                    <table>
                        <tbody>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='product'><warehouse:message code="product.label"/>:</label></td>
                                <td valign='top' class='value' nowrap="nowrap">
                                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                        width="400" valueId="" valueName="" styleClass="text"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
                                <td valign='top' class='value'>
                                    <input type="text" id="quantity" name='quantity' value="" size="10" class="text" />
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='unitPrice'><warehouse:message code="order.unitPrice.label"/>:</label></td>
                                <td valign='top' class='value'>
                                    <input type="text" id="unitPrice" name='unitPrice' value="" size="10" class="text" />
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td valign="top" class="value" colspan="2">

                                    <div class="buttons">
                                        <span class="formButton">
                                            <g:submitButton name="addItem" value="${warehouse.message(code:'order.saveItem.label', default: 'Save item')}" class="button icon approve"></g:submitButton>
                                        </span>
                                    </div>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </g:form>
            </div>
            <div id="edit-item-dialog" class="dialog box">
                <g:form action="purchaseOrder" method="post">
                    <g:hiddenField id="edit-orderId" name="order.id" value="" />
                    <g:hiddenField id ="edit-orderItemId" name="orderItem.id" value=""/>
                    <table>
                        <tbody>
                        <tr class='prop'>
                            <td valign='top' class='name'>
                                <label for='product'><warehouse:message code="product.label"/>:</label>
                            </td>
                            <td valign='top' class='value' nowrap="nowrap">
                                <div id="edit-product-name"></div>
                                <input type="hidden" id="edit-product-id"/>
                            </td>
                        </tr>
                        <tr class='prop'>
                            <td valign='top' class='name'>
                                <label for='quantity'><warehouse:message code="default.quantity.label"/>:</label>
                            </td>
                            <td valign='top' class='value'>
                                <input type="text" id="edit-quantity" name='quantity' value="" size="10" class="text" />
                            </td>
                        </tr>
                        <tr class='prop'>
                            <td valign='top' class='name'>
                                <label for='unitPrice'><warehouse:message code="order.unitPrice.label"/>:</label>
                            </td>
                            <td valign='top' class='value'>
                                <input type="text" id="edit-unitPrice" name='unitPrice' value="" size="10" class="text" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="value" colspan="2">
                                <div class="buttons">
                                    <span class="formButton">
                                        <g:submitButton name="addItem" value="${warehouse.message(code:'order.saveItem.label', default: 'Save item')}" class="button"></g:submitButton>
                                    </span>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </g:form>
            </div>

            <g:form action="purchaseOrder" autocomplete="off">
                <div class="buttons" style="border-top: 1px solid lightgrey;">
                    <g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}" class="button"></g:submitButton>
                    <g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}" class="button"></g:submitButton>
<%--
                    <g:link action="purchaseOrder" event="cancel" class="button"><warehouse:message code="default.button.cancel.label"/></g:link>
--%>
                </div>
            </g:form>
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
            $("#add-item-dialog").dialog({autoOpen:false, modal: true, width: 600, height: 300, title: "Add item to purchase order"});
            $("#edit-item-dialog").dialog({autoOpen:false, modal: true, width: 600, height: 300, title: "Add item to purchase order"});
            $("#add-item-button").click(function(event){
                event.preventDefault();
                $("#add-item-dialog").dialog("open");
            });
            $(".edit-item-button").click(function(event){
                var id = $(this).attr("data-order-item-id");
                $.ajax({
                    dataType: "json",
                    timeout: 2000,
                    url: "${request.contextPath}/json/getOrderItem",
                    data: { id: id },
                    success: function (data) {
                        console.log(data);
                        $("#edit-product-id").val(data.product.id);
                        $("#edit-product-name").html(data.product.productCode + " " + data.product.name);
                        $("#edit-orderId").val(data.order.id);
                        $("#edit-orderItemId").val(data.id);
                        $("#edit-quantity").val(data.quantity);
                        $("#edit-unitPrice").val(data.unitPrice);

                    },
                    error: function(xhr, status, error) {
                        alert(error);
                    }
                });

                $("#edit-item-dialog").dialog("open");
            });


        });


    </script>
</body>
</html>