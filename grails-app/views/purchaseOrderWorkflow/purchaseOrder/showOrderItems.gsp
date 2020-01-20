<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
<style>
    .dlg { display: none; }
</style>

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
            <g:render template="/order/addItemsSummary" model="[orderInstance:order,currentState:'addItems']"/>

            <div class="box">
                <h2><warehouse:message code="order.wizard.addItems.label"/></h2>
                <table style="">
                    <thead>
                        <tr class="odd">
                            <th><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                            <g:sortableColumn property="product.productCode" title="${warehouse.message(code:'product.productCode.label')}" />
                            <g:sortableColumn property="product.name" title="${warehouse.message(code:'product.name.label')}" />
                            <g:sortableColumn property="product.vendor" title="${warehouse.message(code:'vendor.name.label', default: 'Vendor')}" />
                            <g:sortableColumn property="product.manufacturer" title="${warehouse.message(code:'manufacturer.name.label', default: 'Manufacturer')}" />
                            <g:sortableColumn property="product.manufacturerCode" title="${warehouse.message(code:'product.manufacturerNumber.label', default: 'Manufacturer number')}" />

                            <g:sortableColumn class="center" property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
                            <g:sortableColumn class="center" property="unitOfMeasure" title="${warehouse.message(code:'default.uom.label', default: 'UOM')}" class="center"/>
                            <g:sortableColumn class="center" property="unitPrice" title="${warehouse.message(code:'order.unitPrice.label')}" />
                            <g:sortableColumn class="right" property="totalPrice" title="${warehouse.message(code:'orderItem.totalCost.label')}" />
                            <th class="center" ><warehouse:message code="default.actions.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="i" value="${0 }"/>
                        <g:each var="orderItem" in="${order?.listOrderItems()}">
                            <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                                <g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>
                                <td class="middle">
                                    ${i}
                                </td>
                                <td class="middle">
                                    <g:link controller="inventoryItem" action="showStockCard" id="${orderItem?.product?.id}">
                                        ${orderItem?.product?.productCode}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    <g:link controller="inventoryItem" action="showStockCard" id="${orderItem?.product?.id}">
                                        ${orderItem?.product?.name?:orderItem?.description?.encodeAsHTML()}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.vendor?:"N/A"}
                                    <g:if test="${orderItem?.product?.vendorCode}">
                                        #${orderItem?.product?.vendorCode}
                                    </g:if>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.manufacturer?:"N/A"}
                                </td>
                                <td class="middle">
                                    <g:if test="${orderItem?.product?.manufacturerCode}">
                                        #${orderItem?.product?.manufacturerCode}
                                    </g:if>
                                </td>
                                <td class="middle center">
                                    ${orderItem?.quantity }
                                    <g:hiddenField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"/>
                                </td>
                                <td class="middle center">
                                    ${orderItem?.product?.unitOfMeasure?:"EA"}
                                </td>
                                <td class="center middle">
                                    <g:formatNumber number="${orderItem?.unitPrice ?: 0.00 }" format="###,###,##0.00##"/>
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.totalPrice() }" />
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                                <td class="actionButtons center">
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
                                <td colspan="11">
                                    <div class="fade center empty">
                                        <p name="numItemInOrder">
                                            <warehouse:message code="order.itemsInOrder.message" args="[(order?.orderItems)?order?.orderItems?.size():0]"/>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        </g:unless>
                        <g:form action="purchaseOrder" method="post">
                            <g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
                            <g:hiddenField name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                            <tr>
                                <td colspan="2"></td>
                                <td>
                                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                   width="400" valueId="" valueName="" styleClass="text"/>
                                </td>
                                <td colspan="3"></td>
                                <td class="middle center">
                                    <input type="text" id="quantity" name='quantity' value="" size="10" class="text" />
                                </td>
                                <td class="center middle">
                                    <g:hiddenField name="unitOfMeasure" value="each"/>
                                    each
                                </td>
                                <td class="center middle">
                                    <input type="text" id="unitPrice" name='unitPrice' value="" size="10" class="text" />
                                </td>
                                <td colspan="2"></td>
                            </tr>
                            <tr class="prop">
                                <td colspan="11" class="center">
                                    <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${order?.id}" event="enterOrderDetails" params="[skipTo:'details']" class="button">
                                        <warehouse:message code="default.back.label" default="Back"/>
                                    </g:link>
                                    <g:submitButton name="addItem" value="${warehouse.message(code:'order.button.addItem.label', default: 'Add line item')}" class="button icon add"/>
                                </td>
                            </tr>
                        </g:form>
                    </tbody>
                    <tfoot>
                        <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                            <th>
                            </th>
                            <th colspan="10" class="right">
                                <warehouse:message code="default.total.label"/>
                                <g:formatNumber number="${order?.totalPrice()?:0.0 }"/>
                                ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                            </th>
                        </tr>
                    </tfoot>

                </table>
            </div>
		</div>
        <div id="edit-item-dialog" class="dlg box">
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
                            <label for='unitOfMeasure'><warehouse:message code="product.unitOfMeasure.label"/>:</label>
                        </td>
                        <td valign='top' class='value'>
                            <div id="edit-uom"></div>
                        </td>
                    </tr>
                    <tr class='prop'>
                        <td valign='top' class='name'>
                            <label for='unitPrice'><warehouse:message code="order.unitPrice.label"/>:</label>
                        </td>
                        <td valign='top' class='value'>
                            <input type="text" id="edit-unitPrice" name='unitPrice' value="" size="10" class="text" />
                            <div class="fade"><warehouse:message code="order.unitPrice.hint"/></div>
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
            $("#edit-item-dialog").dialog({autoOpen:false, modal: true, width: 800, title: "Edit line item"});
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
                        $("#edit-uom").text(data.product.unitOfMeasure);
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

            $("#btnImportItems").click(function(event){
                $("#dlgImportItems").dialog('open');
            });
            $("#dlgImportItems").dialog({
                autoOpen: false,
                modal: true,
                width: 600
            });



        });


    </script>

</body>
</html>
