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
            <g:render template="/order/summary" model="[orderInstance:order,currentState:'addItems']"/>

            <div class="box">
                <h2><warehouse:message code="order.wizard.addItems.label"/></h2>
                <table style="">
                    <thead>
                        <tr class="odd">
                            <th><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                            <th><warehouse:message code="product.code.label"/></th>
                            <th><warehouse:message code="product.name.label"/></th>
                            <th class="center"><warehouse:message code="product.sourceCode.label"/></th>
                            <th class="center"><warehouse:message code="product.supplierCode.label"/></th>
                            <th class="center"><warehouse:message code="product.manufacturer.label"/></th>
                            <th class="center"><warehouse:message code="product.manufacturerCode.label"/></th>
                            <th class="center"><warehouse:message code="default.quantity.label"/></th>
                            <th class="center"><warehouse:message code="default.uom.label"/></th>
                            <th class="center"><warehouse:message code="order.unitPrice.label"/></th>
                            <th class="right"><warehouse:message code="orderItem.totalCost.label"/></th>
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
                                <td class="center middle">
                                    ${orderItem?.productSupplier?.code}
                                </td>
                                <td class="center middle">
                                    ${orderItem?.productSupplier?.supplierCode}
                                </td>
                                <td class="center middle">
                                    ${orderItem?.productSupplier?.manufacturer}
                                </td>
                                <td class="center middle">
                                    ${orderItem?.productSupplier?.manufacturerCode}
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
                                    ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.totalPrice() }" />
                                    ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                                <td class="actionButtons center">
                                    <g:if test="${orderItem?.id }">
                                        <a href="javascript:void(-1);" id="edit-item-dialog-${orderItem?.id}"
                                           class="button edit-item-button" data-order-item-id="${orderItem?.id}">
                                            <warehouse:message code="default.button.edit.label"/>
                                        </a>
                                        <g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem"
                                                class="button" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                            <warehouse:message code="default.button.delete.label"/>
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                        <g:form action="purchaseOrder" method="post">
                            <g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
                            <g:hiddenField name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                            <tr>
                                <td>
                                    ${i+1}
                                </td>
                                <td>

                                </td>
                                <td>
                                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true"
                                                   width="400" valueId="" valueName="" styleClass="text"/>
                                </td>
                                <td class="middle center">
                                    <g:select id="productSupplier" name="productSupplier.id"/>
                                </td>
                                <td class="middle center">
                                    <input type="text" id="supplierCode" name='supplierCode' disabled value="" size="10" class="text" />
                                </td>
                                <td class="middle center">
                                    <g:selectOrganization name="manufacturer"
                                                          id="manufacturer"
                                                          noSelection="['':'']"
                                                          disabled="true"
                                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                                          value="" />
                                </td>
                                <td class="middle center">
                                    <input type="text" id="manufacturerCode" name='manufacturerCode' disabled value="" size="10" class="text" />
                                </td>
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
                                <td></td>
                                <td class="center">
                                    <g:submitButton name="addItem" value="${warehouse.message(code:'order.button.save.label', default: 'Save')}" class="button icon add"/>
                                </td>
                            </tr>

                        </g:form>
                    </tbody>
                    <tfoot>
                        <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                            <th colspan="12" class="right">
                                <warehouse:message code="default.total.label"/>
                                <g:formatNumber number="${order?.totalPrice()?:0.0 }"/>
                                ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                            </th>
                        </tr>
                    </tfoot>

                </table>
            </div>

            <div class="buttons">
                <div class="left">
                    <g:link controller="purchaseOrderWorkflow"
                            action="purchaseOrder"
                            id="${order?.id}"
                            event="enterOrderDetails"
                            params="[skipTo:'details']"
                            class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" />&nbsp;
                        <warehouse:message code="default.back.label" default="Back"/>
                    </g:link>
                </div>
                <div class="right">
                    <g:if test="${!order?.isPlaced()}">
                        <g:link controller="order" action="placeOrder" id="${order?.id}" class="button" >
                            <img src="${resource(dir: 'images/icons/silk', file: 'creditcards.png')}" />&nbsp;
                            ${warehouse.message(code: 'order.wizard.placeOrder.label')}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="order" action="placeOrder" id="${order?.id}" class="button" disabled="disabled" >
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
                            ${warehouse.message(code: 'order.wizard.placeOrder.label')}
                        </g:link>
                    </g:else>
                </div>
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

        // When chosen product has changed, trigger function that updates source code column
        $("#product-id").change(function() {
          productChanged(this.value);
        });

        // When chosen source code has changed, trigger function that updates supplier code, manufacturer and manufacturer code columns
        $("#productSupplier").change(function() {
          sourceCodeChanged($("#productSupplier option:selected").val());
        });

        // Update source code column with product supplier source codes based on product chosen by user
        function productChanged(productId) {
          $.ajax({
            type: 'POST',
            data: 'productId=' + productId,
            url: '${request.contextPath}/json/productChanged',
            success: function (data, textStatus) {
              $('#productSupplier').html(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
            }
          });
        }

        // Update supplier code, manufacturer and manufacturer code columns based on source code chosen by user
        function sourceCodeChanged(productSupplierId) {
          $.ajax({
            type: 'POST',
            data: 'productSupplierId=' + productSupplierId,
            url: '${request.contextPath}/json/productSupplierChanged',
            success: function (data, textStatus) {
              $('#supplierCode').val(data.supplierCode);
              $('#manufacturerCode').val(data.manufacturerCode);
              $('#manufacturer').html(data.manufacturer);
              console.log(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
            }
          });
        }

        $(document).ready(function(){

            // Place focus on product search box
            $("#product-suggest").focus();

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
