<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<g:form name="editOrderItemForm" action="purchaseOrder" method="post">
    <g:hiddenField id="dlgOrderId" name="order.id" value="${orderItem?.order?.id}" />
    <g:hiddenField id ="dlgOrderItemId" name="orderItem.id" value="${orderItem?.id}"/>
    <table>
        <tbody>

        <g:if test="${orderItem?.order.status <= org.pih.warehouse.order.OrderStatus.PENDING}">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProduct"><warehouse:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${orderItem?.product?.productCode}"/>
                    <format:product product="${orderItem.product}"/>
                    <g:hiddenField id="dlgProduct" name="product.id" value="${orderItem?.product?.id}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProductSupplier"><warehouse:message code="productSupplier.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectProductSupplier id="dlgProductSupplier"
                                             name="productSupplier.id"
                                             product="${orderItem.product}"
                                             supplier="${orderItem?.order?.originParty}"
                                             value="${orderItem?.productSupplier?.id}"
                                             class="chzn-select-deselect"
                                             noSelection="['':'']"></g:selectProductSupplier>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgQuantity"><warehouse:message code="orderItem.quantity.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text" id="dlgQuantity" name="quantity" value="${orderItem.quantity}" size="10" class="text" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgQuantityUom"><warehouse:message code="orderItem.quantityUom.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectUnitOfMeasure name="quantityUom.id" class="chzn-select-deselect" value="${orderItem?.quantityUom?.id}"
                    noSelection="['':'']"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgQuantityPerUom"><warehouse:message code="orderItem.quantityPerUom.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text"
                           id="dlgQuantityPerUom"
                           name="quantityPerUom"
                           value="${g.formatNumber(number: orderItem?.quantityPerUom, maxFractionDigits: 0)?:1}"
                           min="0"
                           size="10"
                           class="text" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgUnitPrice"><warehouse:message code="orderItem.unitPrice.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text" id="dlgUnitPrice" name="unitPrice" value="${orderItem.unitPrice}" size="10" class="text" />
                    <span class="fade"><warehouse:message code="order.unitPrice.hint"/></span>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgRecipient"><warehouse:message code="orderItem.recipient.label"/></label>
                </td>
                <td valign="top" class="value">
                   <g:selectPerson id="dlgRecipient" name="recipient" value="${orderItem?.recipient?.id}"
                                noSelection="['':'']" class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgEstimatedReadyDate"><warehouse:message code="orderItem.estimatedReadyDate.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input class="text large datepicker" id="dlgEstimatedReadyDate" name="estimatedReadyDate"
                           value="${orderItem?.estimatedReadyDate?.format("MM/dd/yyyy")}" />
                </td>
            </tr>
        </g:if>
        <g:else>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProduct"><warehouse:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${orderItem?.product?.productCode}"/>
                    <format:product product="${orderItem.product}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProductSupplier"><warehouse:message code="productSupplier.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${orderItem?.productSupplier?.code?:"NONE"}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgQuantity"><warehouse:message code="default.quantity.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${orderItem.quantity}
                    ${orderItem?.unitOfMeasure}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgUnitPrice"><warehouse:message code="orderItem.unitPrice.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:formatNumber number="${orderItem?.unitPrice?:0.0 }"/>
                    ${orderItem?.order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgUnitPrice"><warehouse:message code="orderItem.totalCost.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:formatNumber number="${orderItem?.totalPrice()?:0.0 }"/>
                    ${orderItem?.order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgRecipient"><warehouse:message code="orderItem.recipient.label"/></label>
                </td>
                <td valign="top" class="value">
                   <g:selectPerson id="dlgRecipient" name="recipient" value="${orderItem?.recipient?.id}"
                                noSelection="['':'']" class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgEstimatedReadyDate"><warehouse:message code="orderItem.estimatedReadyDate.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${orderItem?.estimatedReadyDate?.format("MM/dd/yyyy")}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgActualReadyDate"><warehouse:message code="orderItem.actualReadyDate.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input class="text large datepicker" id="dlgActualReadyDate" name="actualReadyDate" value="${orderItem?.actualReadyDate?.format("MM/dd/yyyy")}" />
                </td>
            </tr>
        </g:else>

        </tbody>
        <tfoot>
        <tr class="prop">
            <td></td>
            <td valign="top" class="value">
                <button class="button save-item-button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
                    <warehouse:message code="default.button.save.label"/>
                </button>
                <g:if test="${orderItem?.order?.status == OrderStatus.PENDING}">
                    <button class="button delete-item-button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                        <warehouse:message code="default.button.delete.label"/>
                    </button>
                </g:if>
                <div class="right">
                    <button class="button close-dialog-button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_cross.png')}" />&nbsp;
                        <warehouse:message code="default.button.close.label"/>
                    </button>
                </div>

            </td>
        </tr>
        </tfoot>

    </table>
</g:form>
<script>

    function saveOrderItemDialog() {
        var id = $("#dlgOrderItemId").val();
        var data = $("#editOrderItemForm").serialize();
        $.ajax({
          url: "${g.createLink(controller:'order', action:'saveOrderItem')}",
          data: data,
          success: function () {
            $.notify("Saved order item successfully", "success");
            $("#edit-item-dialog").dialog("close");
            loadOrderItems();
            applyFocus("#product-suggest");
          },
          error: function (jqXHR, textStatus, errorThrown) {
            $.notify("An error occurred", "errur");
          }
        });
        return false
    }

    $(document).ready(function() {

        $(".close-dialog-button").click(function(event){
          event.preventDefault();
          $("#edit-item-dialog").dialog("close");
        });

        $(".delete-item-button").click(function(event) {
          event.preventDefault();
          var id = $("#dlgOrderItemId").val();
          deleteOrderItem(id);
          $("#edit-item-dialog").dialog("close");
        });

        $(".save-item-button").click(function(event){
          event.preventDefault();
          saveOrderItemDialog();
        });

        $(".datepicker").livequery(function(){
            $(this).datepicker({dateFormat: "mm/dd/yy"})
        });

    });

</script>
