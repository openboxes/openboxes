<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<g:form name="editOrderItemForm" action="purchaseOrder" method="post">
    <g:hiddenField id="dlgOrderId" name="order.id" value="${orderItem?.order?.id}" />
    <g:hiddenField id ="dlgOrderItemId" name="orderItem.id" value="${orderItem?.id}"/>
    <g:hiddenField id ="dlgQuantityInShipments" name="quantityInShipments" value="${orderItem?.quantityInShipments}"/>
    <table>
        <tbody>

        <g:if test="${canEdit && !orderItem.hasShipmentAssociated()}">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProduct"><warehouse:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${orderItem?.product?.productCode}"/>
                    <format:product product="${orderItem.product}"/>
                    <g:hiddenField id="dlgProduct" name="product.id" value="${orderItem?.product?.id}"/>
                    <g:hiddenField id="dlgSupplierId" name="supplier.id" value="${orderItem?.order?.originParty?.id }"></g:hiddenField>
                    <g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${isAccountingRequired}">
                    </g:hiddenField>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProductSupplier"><warehouse:message code="productSupplier.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectProductSupplier id="dlgProductSupplier"
                                             name="productSupplier"
                                             product="${orderItem.product}"
                                             supplier="${orderItem?.order?.originParty}"
                                             value="${orderItem?.productSupplier?.id}"
                                             class="select2"
                                             noSelection="['':'']" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSourceCodeRow">
                <td valign="top" class="name">
                    <label for="dlgSourceCode"><warehouse:message code="productSupplier.sourceCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSourceCode" name="sourceCode" class="text" size="24" placeholder="Source Code" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSourceNameRow">
                <td valign="top" class="name">
                    <label for="dlgSourceName"><warehouse:message code="productSupplier.sourceName.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSourceName" name="sourceName" class="text" size="24" placeholder="Source Name" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSupplierCodeRow">
                <td valign="top" class="name">
                    <label for="dlgSupplierCode"><warehouse:message code="product.supplierCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSupplierCode" name="supplierCode" class="text" placeholder="Supplier code" style="width: 100px" disabled />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgManufacturerRow">
                <td valign="top" class="name">
                    <label for="dlgManufacturer"><warehouse:message code="product.manufacturer.label"/></label>
                </td>
                <td>
                    <g:selectOrganization name="manufacturer"
                                          id="dlgManufacturer"
                                          value="${manufacturer?.id}"
                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                          noSelection="['':'']"
                                          class="select2"
                                          disabled="${true}" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgManufacturerCodeRow">
                <td valign="top" class="name">
                    <label for="dlgManufacturerCode"><warehouse:message code="product.manufacturerCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgManufacturerCode" name="manufacturerCode" class="text" placeholder="Manufacturer code" style="width: 100px" disabled />
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
                    <g:selectUnitOfMeasure id="dlgQuantityUom" name="quantityUom.id" class="select2" value="${orderItem?.quantityUom?.id}"
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
                                noSelection="['':'']" class="select2"/>
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
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgBudgetCode"><warehouse:message code="orderItem.budgetCode.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectBudgetCode name="budgetCode"
                                        id="dlgBudgetCode"
                                        value="${orderItem.budgetCode?.id}"
                                        class="select2"
                                        noSelection="['':'']"/>
                </td>
            </tr>
            <g:if test="${orderItem?.order?.status >= OrderStatus.PLACED}">
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="orderItem.actualReadyDate.label"/></label>
                    </td>
                    <td valign="top" class="value">
                        <input class="text large datepicker" name="actualReadyDate"
                               value="${orderItem?.actualReadyDate?.format("MM/dd/yyyy")}" />
                    </td>
                </tr>
            </g:if>
        </g:if>
        <g:elseif test="${canEdit && orderItem.hasShipmentAssociated()}">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProduct"><warehouse:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${orderItem?.product?.productCode}"/>
                    <format:product product="${orderItem.product}"/>
                    <g:hiddenField id="dlgProduct" name="product.id" value="${orderItem?.product?.id}"/>
                    <g:hiddenField id="dlgSupplierId" name="supplier.id" value="${orderItem?.order?.originParty?.id }"></g:hiddenField>
                    <g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${isAccountingRequired}">
                    </g:hiddenField>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgProductSupplier"><warehouse:message code="productSupplier.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectProductSupplier id="dlgProductSupplier"
                                             name="productSupplier"
                                             product="${orderItem.product}"
                                             supplier="${orderItem?.order?.originParty}"
                                             value="${orderItem?.productSupplier?.id}"
                                             class="select2"
                                             noSelection="['':'']" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSourceCodeRow">
                <td valign="top" class="name">
                    <label for="dlgSourceCode"><warehouse:message code="productSupplier.sourceCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSourceCode" name="sourceCode" class="text" size="24" placeholder="Source Code" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSourceNameRow">
                <td valign="top" class="name">
                    <label for="dlgSourceName"><warehouse:message code="productSupplier.sourceName.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSourceName" name="sourceName" class="text" size="24" placeholder="Source Name" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgSupplierCodeRow">
                <td valign="top" class="name">
                    <label for="dlgSupplierCode"><warehouse:message code="product.supplierCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgSupplierCode" name="supplierCode" class="text" placeholder="Supplier code" style="width: 100px" disabled />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgManufacturerRow">
                <td valign="top" class="name">
                    <label for="dlgManufacturer"><warehouse:message code="product.manufacturer.label"/></label>
                </td>
                <td>
                    <g:selectOrganization name="manufacturer"
                                          id="dlgManufacturer"
                                          value="${manufacturer?.id}"
                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                          noSelection="['':'']"
                                          class="select2"
                                          disabled="${true}" />
                </td>
            </tr>
            <tr class="prop hidden" id="dlgManufacturerCodeRow">
                <td valign="top" class="name">
                    <label for="dlgManufacturerCode"><warehouse:message code="product.manufacturerCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="dlgManufacturerCode" name="manufacturerCode" class="text" placeholder="Manufacturer code" style="width: 100px" disabled />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgQuantity"><warehouse:message code="default.quantity.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text" id="dlgQuantity" name="quantity" value="${orderItem.quantity}" size="10" class="text" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><warehouse:message code="orderItem.quantityUom.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${orderItem?.unitOfMeasure}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgUnitPrice"><warehouse:message code="orderItem.unitPrice.label"/></label>
                </td>
                <g:if test="${orderItem.hasRegularInvoice}">
                    <td valign="top" class="value">
                        <g:formatNumber number="${orderItem?.unitPrice?:0.0 }"/>
                        ${orderItem?.order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                    </td>
                </g:if>
                <g:else>
                    <td valign="top" class="value">
                        <input type="text" id="dlgUnitPrice" name="unitPrice" value="${orderItem.unitPrice}" size="10" class="text" />
                        <span class="fade"><warehouse:message code="order.unitPrice.hint"/></span>
                    </td>
                </g:else>
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
                                noSelection="['':'']" class="select2"/>
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
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgActualReadyDate"><warehouse:message code="orderItem.actualReadyDate.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input class="text large datepicker" id="dlgActualReadyDate" name="actualReadyDate" value="${orderItem?.actualReadyDate?.format("MM/dd/yyyy")}" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="dlgBudgetCode"><warehouse:message code="orderItem.budgetCode.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:selectBudgetCode name="budgetCode"
                                        id="dlgBudgetCode"
                                        value="${orderItem.budgetCode?.id}"
                                        class="select2"
                                        noSelection="['':'']"
                                        disabled="${orderItem.hasRegularInvoice}"/>
                </td>
            </tr>
        </g:elseif>

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
    const CREATE_NEW = "Create New";

  function validateForm() {
    var budgetCode = $("#dlgBudgetCode").val();
    var quantity = parseInt($("#dlgQuantity").val());
    var quantityInShipments = parseInt($("#dlgQuantityInShipments").val())
    if (quantity < quantityInShipments) {
        $("#dlgQuantity").notify("Must enter a quantity greater than or equal to the quantity in shipments(" + quantityInShipments + ")")
        return false
    }
    var isAccountingRequired = ($("#isAccountingRequired").val() === "true");
    if (!budgetCode && isAccountingRequired) {
      $("#dlgBudgetCode").notify("Required")
      return false
    } else {
      return true
    }
  }

    function enableEditing() {
        $("#dlgSupplierCode").removeAttr("disabled");
        $("#dlgSupplierCodeRow").removeClass("hidden");
        $("#dlgSourceCode").removeAttr("disabled");
        $("#dlgSourceCodeRow").removeClass("hidden");
        $("#dlgSourceName").removeAttr("disabled");
        $("#dlgSourceNameRow").removeClass("hidden");
        $("#dlgManufacturer").removeAttr("disabled");
        $("#dlgManufacturerRow").removeClass("hidden");
        $("#dlgManufacturerCode").removeAttr("disabled");
        $("#dlgManufacturerCodeRow").removeClass("hidden");
    }

    function disableEditing() {
        $("#dlgSupplierCode").attr("disabled", true);
        $("#dlgSupplierCodeRow").addClass("hidden");
        $("#dlgSourceCode").attr("disabled", true);
        $("#dlgSourceCodeRow").addClass("hidden");
        $("#dlgSourceName").attr("disabled", true);
        $("#dlgSourceNameRow").addClass("hidden");
        $("#dlgManufacturer").attr("disabled", true);
        $("#dlgManufacturerRow").addClass("hidden");
        $("#dlgManufacturerCode").attr("disabled", true);
        $("#dlgManufacturerCodeRow").addClass("hidden");
    }

    function clearSource() {
        $("#dlgSupplierCode").val("");
        $("#dlgManufacturer").val(null).trigger('change');
        $("#dlgManufacturerCode").val("");
    }

    $('#dlgProductSupplier').on('select2:select', function (e) {
        if (e.params.data.id === CREATE_NEW) {
            enableEditing();
        } else {
            clearSource();
            disableEditing();
            $("#dlgSupplierCode").val(e.params.data.code);
            $("#dlgManufacturer").val(e.params.data.manufacturerCode);
            $("#dlgManufacturerCode").val(e.params.data.manufacturer);
        }
    });

    $('#dlgProductSupplier').on('select2:unselect', function (e) {
        clearSource();
        disableEditing();
    });

    $('#dlgProductSupplier').on('select2:clear', function (e) {
        clearSource();
        disableEditing();
    });

    $("#dlgQuantityUom").live('change', function() {
      if($("#dlgQuantityUom option:selected").val() == 'EA') {
        $("#dlgQuantityPerUom").val("1");
        $("#dlgQuantityPerUom").attr("readonly", true);
      } else {
        $("#dlgQuantityPerUom").val("");
        $("#dlgQuantityPerUom").removeAttr("readonly");
      }
    });

    function saveOrderItemDialog() {
        var id = $("#dlgOrderItemId").val();
        var data = $("#editOrderItemForm").serialize();
        if(validateForm()) {
          $.ajax({
            url: "${g.createLink(controller:'order', action:'saveOrderItem')}",
            data: data,
            success: function () {
              $.notify("Saved order item successfully", "success");
              $("#edit-item-dialog")
                .dialog("close");
              loadOrderItems();
              applyFocus("#product-suggest");
            },
            error: function (jqXHR, textStatus, errorThrown) {
              if (jqXHR.responseText) {
                try {
                  let data = JSON.parse(jqXHR.responseText);
                  $.notify(data.errorMessage, "error");
                } catch (e) {
                  $.notify(jqXHR.responseText, "error");
                }
              } else {
                $.notify("An error occurred", "error");
              }
            }
          });
        } else {
          $.notify("Please enter a proper value for all required fields")
        }
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

        if($("#dlgQuantityUom option:selected").val() == 'EA') {
            $("#dlgQuantityPerUom").val("1");
            $("#dlgQuantityPerUom").attr("readonly", true);
        }

        $("#dlgProductSupplier")
            .select2({
                placeholder: 'Select an option',
                width: '100%',
                allowClear: true,
              matcher: function (params, data) {
                if ($.trim(params.term) === '') {
                  return data;
                }
                if (typeof data.text === 'undefined') {
                  return null;
                }
                if (data.text.toUpperCase().indexOf(params.term.toUpperCase()) > -1 || data.text === CREATE_NEW) {
                  return data;
                }
                return null;
              }
            })
            .append(new Option(CREATE_NEW, CREATE_NEW, false, false)).trigger('change');
    });

</script>
