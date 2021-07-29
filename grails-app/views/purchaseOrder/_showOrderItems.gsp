<%@ page import="org.pih.warehouse.shipping.ShipmentStatus; org.pih.warehouse.order.OrderStatus" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
<style>
    .dlg { display: none; }
    .non-editable { background-color: #e6e6e6; cursor: not-allowed }
    .non-editable.odd { background-color: #e1e1e1; }
    .canceled-item { background-color: #ffcccb; }
    .items-table { table-layout: fixed; }
    .import-template {
        width: 0.1px;
        height: 0.1px;
        opacity: 0;
        overflow: hidden;
        position: absolute;
        z-index: -1;
    }
    .import-template + label {
        display: inline-block;
    }
</style>

</head>
<body>
	<div class="body">
        <g:hasRoleApprover>
            <g:set var="isApprover" value="${true}"/>
        </g:hasRoleApprover>
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
            <div class="tabs">
                <ul>
                    <li>
                        <a href="#edit-items"><warehouse:message code="order.wizard.addItems.label"/></a>
                    </li>
                    <li>
                        <a href="#add-adjustment"><warehouse:message code="default.add.label" args="[g.message(code: 'orderAdjustment.label')]"/></a>
                    </li>
                </ul>
                <div id="edit-items" class="box">
                    <h2 style="display: flex; align-items: center; justify-content: space-between;">
                        <warehouse:message code="order.wizard.addItems.label"/>
                        <div class="button-group" style="margin-right: 5px;">
                            <input type="file" name="importTemplate" id="importTemplate" class="import-template" />
                            <label for="importTemplate" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'disk_upload.png')}" />&nbsp;
                                <warehouse:message code="default.importTemplate.label" default="Import template"/>
                            </label>
                        </div>
                    </h2>
                    <g:form name="orderItemForm" action="create" method="post">
                        <g:hiddenField id="orderId" name="order.id" value="${order?.id }"></g:hiddenField>
                        <g:hiddenField id="orderItemId" name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                        <g:hiddenField id="supplierId" name="supplier.id" value="${order?.originParty?.id }"></g:hiddenField>
                        <g:hiddenField id="destinationPartyId" name="destinationPartyId.id" value="${order?.destinationParty?.id }"></g:hiddenField>
                        <g:hiddenField id="validationCode" name="validationCode" value=""></g:hiddenField>
                        <g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${isAccountingRequired}">
                        </g:hiddenField>
                        <table id="orderItemsTable" class="items-table">
                            <thead>
                            <tr class="odd">
                                <th width="1%"><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                                <th width="15%"><warehouse:message code="product.label"/></th>
                                <th width="10%" class="center"><warehouse:message code="product.sourceCode.label"/></th>
                                <th class="center"><warehouse:message code="product.supplierCode.label"/></th>
                                <th class="center"><warehouse:message code="product.manufacturer.label"/></th>
                                <th class="center"><warehouse:message code="product.manufacturerCode.label"/></th>
                                <th class="center" width="8%"><warehouse:message code="default.quantity.label"/></th>
                                <th class="center" colspan="2"><warehouse:message code="default.unitOfMeasure.label"/></th>
                                <th class="center"><warehouse:message code="default.cost.label"/></th>
                                <th class="center"><warehouse:message code="orderItem.totalCost.label"/></th>
                                <th class="center"><warehouse:message code="order.recipient.label"/></th>
                                <th class="center"><warehouse:message code="orderItem.estimatedReadyDate.label"/></th>
                                <th class="center"><warehouse:message code="orderItem.budgetCode.label"/></th>
                                <th class="center"><warehouse:message code="default.actions.label"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <!-- data is dynamically loaded -->
                            </tbody>
                            <tfoot>
                            <g:if test="${order?.status < org.pih.warehouse.order.OrderStatus.PLACED || isApprover}">
                                <g:render template="/order/orderItemForm"/>
                            </g:if>
                            <tr class="">
                                <th colspan="15" class="right">
                                    <warehouse:message code="default.total.label"/>
                                    <span id="totalPrice">
                                        <g:formatNumber number="${order?.totalPrice()?:0.0 }"/>
                                    </span>
                                    ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </th>
                            </tr>
                            </tfoot>
                        </table>
                    </g:form>
                </div>
                <div id="add-adjustment" class="box">
                    <g:set var="canManageAdjustments" value="${order?.status >= OrderStatus.PLACED && isApprover
                            || order?.status == OrderStatus.PENDING}"/>
                    <h2 style="display: flex; align-items: center; justify-content: space-between;">
                        <warehouse:message code="default.add.label" args="[g.message(code: 'orderAdjustment.label')]"/>
                    </h2>
                    <g:form name="orderAdjustmentForm" controller="order" action="saveAdjustment">
                        <g:hiddenField name="order.id" value="${order?.id}" />
                        <input type="hidden" name="canManageAdjustments" id="canManageAdjustments" value="${canManageAdjustments}">
                        <table id="orderAdjustmentsTable" class="items-table">
                            <thead>
                            <tr class="odd">
                                <th><warehouse:message code="default.type.label"/></th>
                                <th><warehouse:message code="order.orderItem.label"/></th>
                                <th><warehouse:message code="default.description.label"/></th>
                                <th><warehouse:message code="orderAdjustment.percentage.label"/></th>
                                <th><warehouse:message code="orderAdjustment.amount.label"/></th>
                                <th><warehouse:message code="comments.label"/></th>
                                <th><warehouse:message code="orderAdjustment.budgetCode.label"/></th>
                                <th class="center"><g:message code="default.actions.label"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <!-- data is dynamically loaded -->
                            </tbody>
                            <tfoot>
                                <td>
                                    <g:selectOrderAdjustmentTypes name="orderAdjustmentType.id"
                                                                  id="orderAdjustmentType"
                                                                  class="select2"
                                                                  noSelection="['':'']"/>
                                </td>
                                <td>
                                    <g:selectOrderItems name="orderItem.id"
                                                        id="orderItems"
                                                        orderId="${order?.id}"
                                                        class="select2"
                                                        noSelection="['':'']"/>
                                </td>
                                <td>
                                    <g:textField name="description" id="description" class="large text"/>
                                </td>
                                <td>
                                    <g:textField name="percentage" id="percentage" class="large text"/>
                                </td>
                                <td>
                                    <g:textField name="amount" id="amount" class="large text"/>
                                </td>
                                <td>
                                    <g:textArea name="comments" id="comments"/>
                                </td>
                                <td>
                                    <g:selectBudgetCode name="budgetCode"
                                                        id="adjustmentBudgetCode"
                                                        class="select2"
                                                        noSelection="['':'']"/>
                                </td>
                                <td class="center middle">
                                    <button type="button" class="button" onclick="saveOrderAdjustment()">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp
                                        <warehouse:message code="default.button.save.label"/>
                                    </button>
                                </td>
                                <tr class="">
                                    <th colspan="8" class="right">
                                        <warehouse:message code="default.total.label"/>
                                        <span id="totalAdjustments">
                                            <g:formatNumber number="${order?.totalAdjustments}"/>
                                        </span>
                                        ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </th>
                                </tr>
                            </tfoot>
                        </table>
                    </g:form>
                </div>
            </div>
            <div class="buttons">
                <div class="left">
                    <g:link controller="purchaseOrder"
                            action="edit"
                            id="${order?.id}"
                            params="[id:order?.id]"
                            class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'resultset_previous.png')}" />&nbsp;
                        <warehouse:message code="default.back.label" default="Back"/>
                    </g:link>
                </div>
                <div class="right">
                    <g:if test="${!order?.isPlaced()}">
                        <g:link controller="order" action="placeOrder" id="${order?.id}" class="button validate" >
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />&nbsp;
                            ${warehouse.message(code: 'order.wizard.placeOrder.label')}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="order" action="show" id="${order?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'cart_magnify.png')}" />&nbsp;
                            <warehouse:message code="default.button.saveAndExit.label" default="Save and Exit"/>
                        </g:link>
                    </g:else>
                </div>
            </div>
		</div>
        <div id="edit-item-dialog" class="dlg box">
            <!-- contents will be lazy loaded -->
        </div>
        <div id="create-product-source-dialog" class="dlg box">
            <!-- contents will be lazy loaded -->
        </div>
    </div>
    <script type="text/javascript">
        const CREATE_NEW = "Create New";

        // Validate the create line item form in case someone forgot to
        $(".validate").click(function (event) {
          if (isFormDirty()) {
            $.notify("Please save item before proceeding");
            return false;
          } else {
            // This seems to be the best way to proceed after stopping propagation
            window.location = $(this).attr("href");
          }
        });

        // When chosen product has changed, trigger function that updates source code column
        $("#product-id").change(function() {
          var supplierId = $("#supplierId").val();
          var destinationPartyId = $("#destinationPartyId").val();
          if (!this.value) {
            $("#productSupplier").html("").attr("disabled", true);
          } else {
            clearSource();
            $('#productSupplier').html("");
            productChanged(this.value, supplierId, destinationPartyId);
          }
        });

        // When chosen source code has changed, trigger function that updates supplier code, manufacturer and manufacturer code columns
        $("#productSupplier").live('change', function(event) {
          var selectedSourceCode = $("#productSupplier option:selected").val();
          var destinationPartyId = $("#destinationPartyId").val();
          if (selectedSourceCode === CREATE_NEW) {
            createProductSource();
          } else if (selectedSourceCode) {
            sourceCodeChanged(selectedSourceCode, destinationPartyId);
          }
        });

        $("#quantityUom").live('change', function() {
          if($("#quantityUom option:selected").val() == 'EA') {
            $("#quantityPerUom").val("1");
            $("#quantityPerUom").attr("disabled", true);
          } else {
            $("#quantityPerUom").val("");
            $("#quantityPerUom").removeAttr("disabled");
          }
        });

        $("#percentage").live('change', function() {
          if($("#percentage").val()) {
            $("#amount").attr("disabled", true);
          } else {
            $("#amount").removeAttr("disabled");
          }
        });

        $("#amount").live('change', function() {
          if($("#amount").val()) {
            $("#percentage").attr("disabled", true);
          } else {
            $("#percentage").removeAttr("disabled");
          }
        });

        function deleteOrderItem(id) {
          $.ajax({
            url: '${g.createLink(controller:'order', action:'removeOrderItem')}',
            data: { id: id },
            success: function () {
              clearOrderItems();
              loadOrderItems();
              clearOrderAdjustments();
              loadOrderAdjustments();
              $('#orderItems').html('<option></option>').trigger('change');
              $.notify("Successfully deleted item " + id, "success")
            },
            error: function (jqXHR, textStatus, errorThrown) {
              if (jqXHR.responseText) {
                let data = JSON.parse(jqXHR.responseText);
                $.notify(data.errorMessage, "error");
              }
              else {
                $.notify("Error deleting item " + id, "error")
              }
            }
          });
          return false
        }

        function deleteOrderAdjustment(id) {
          var orderId = $("#orderId").val();
          $.ajax({
            url: '${g.createLink(controller:'order', action:'deleteAdjustment')}',
            data: { id: id, 'order.id': orderId },
            success: function () {
              clearOrderAdjustments();
              loadOrderAdjustments();
              $.notify("Successfully deleted adjustment", "success")
            },
            error: function (jqXHR, textStatus, errorThrown) {
              if (jqXHR.responseText) {
                let data = JSON.parse(jqXHR.responseText);
                $.notify(data.errorMessage, "error");
              }
              else {
                $.notify("Error deleting adjustment", "error")
              }
            }
          });
          return false
        }

        function changeOrderItemStatus(id, actionUrl) {
          $.ajax({
            url: actionUrl,
            data: { id: id },
            success: function () {
              clearOrderItems();
              loadOrderItems();
              $('#orderItems').html('<option></option>').trigger('change');
              getTotalPrice();
            },
            error: function (jqXHR, textStatus, errorThrown) {
              if (jqXHR.responseText) {
                let data = JSON.parse(jqXHR.responseText);
                $.notify(data.errorMessage, "error");
              }
              else {
                $.notify("Error changing order item status " + id, "error")
              }
            }
          });
          return false
        }

        function changeOrderAdjustmentStatus(id, actionUrl) {
          $.ajax({
            url: actionUrl,
            data: { id: id },
            success: function () {
              clearOrderAdjustments();
              loadOrderAdjustments();
              getTotalAdjustments();
            },
            error: function (jqXHR, textStatus, errorThrown) {
              if (jqXHR.responseText) {
                let data = JSON.parse(jqXHR.responseText);
                $.notify(data.errorMessage, "error");
              }
              else {
                $.notify("Error changing order adjustment status " + id, "error")
              }
            }
          });
          return false
        }

        function getTotalPrice() {
          var orderId = $("#orderId").val();
          $.ajax({
            url:'${g.createLink( controller:'order', action:'getTotalPrice')}',
            data: { id: orderId },
            success: function(data, textStatus){
                $("#totalPrice").html(parseFloat(data).toFixed(2));
            }
          });
        }

        function getTotalAdjustments() {
          var orderId = $("#orderId").val();
          $.ajax({
            url:'${g.createLink( controller:'order', action:'getTotalAdjustments')}',
            data: { id: orderId },
            success: function(data, textStatus){
              $("#totalAdjustments").html(parseFloat(data).toFixed(2));
            }
          });
        }

        /**
         * @FIXME Didn't have time to make this pretty - should use required class on
         * fields instead of hardcoding the IDs.
         */
        function validateItemsForm() {

          var product = $("#product-suggest").val();
          var quantity = $("#quantity").val();
          var unitPrice = $("#unitPrice").val();
          var quantityUom = $("#quantityUom").val();
          var quantityPerUom = $("#quantityPerUom").val();
          var budgetCode = $("#budgetCode").val();
          var isAccountingRequired = ($("#isAccountingRequired").val() === "true");

          if (!product) $("#product-suggest").notify("Required")
          if (!quantity) $("#quantity").notify("Required")
          if (!unitPrice) $("#unitPrice").notify("Required")
          if (!quantityUom) $("#quantityUom_chosen").notify("Required")
          if (!quantityPerUom) $("#quantityPerUom").notify("Required")
          if (!budgetCode && isAccountingRequired) {
            $("#budgetCode").notify("Required")
            return false
          }

          return product && quantity && unitPrice && quantityPerUom && quantityUom
        }

        function validateAdjustmentsForm() {
          var orderAdjustmentType = $("#orderAdjustmentType").val();
          var amount = $("#amount").val();
          var percentage = $("#percentage").val();
          var canManageAdjustments = ($("#canManageAdjustments").val() === "true");
          var budgetCode = $("#adjustmentBudgetCode").val();
          var description = $("#description").val();
          var isAccountingRequired = ($("#isAccountingRequired").val() === "true");

          if (!orderAdjustmentType) $("#orderAdjustmentType").notify("Required")
          if (!(percentage || amount)) $("#amount").notify("Amount or percentage required")
          if (!(percentage || amount)) $("#percentage").notify("Amount or percentage required")
          if (!description) $("#description").notify("Description required")
          if (!canManageAdjustments) $.notify("You do not have permissions to perform this action")
          if (!budgetCode && isAccountingRequired) {
            $("#adjustmentBudgetCode").notify("Required")
            return false
          }

          if (orderAdjustmentType && canManageAdjustments && (amount || percentage) && description) {
            return true
          } else {
            return false
          }
        }

        function isFormDirty() {
          var product = $("#product-suggest").val();
          var quantity = $("#quantity").val();
          var unitPrice = $("#unitPrice").val();
          var quantityUom = $("#quantityUom").val();
          var quantityPerUom = $("#quantityPerUom").val();
          return product || quantity || unitPrice || quantityPerUom || quantityUom
        }

        function saveOrderItem() {
            var data = $("#orderItemForm").serialize();
            data += '&orderIndex=' + $("#orderItemsTable tbody tr").length;
            if (validateItemsForm()) {
              if ($("#validationCode").val() == 'WARN' && !confirm('${warehouse.message(code: 'orderItem.warningSupplier.label').replaceAll( /(')/, '\\\\$1' )}')) {
                return false
              } else {
                $.ajax({
                  url:'${g.createLink(controller:'order', action:'saveOrderItem')}',
                  data: data,
                  success: function() {
                    clearOrderItemForm();
                    loadOrderItems();
                    applyFocus("#product-suggest");
                    $('#supplierCode').text('');
                    $('#manufacturerCode').text('');
                    $('#manufacturer').text('');
                    $.notify("Successfully saved new item", "success")
                  },
                  error: function(jqXHR, textStatus, errorThrown) {
                    if (jqXHR.responseText) {
                      $.notify(jqXHR.responseText, "error");
                    } else {
                      $.notify("Error saving your item");
                    }
                  }
                });
              }
            }
            else {
              $.notify("Please enter a value for all required fields")
            }
            return false
        }

        function saveOrderAdjustment() {
            var data = $("#orderAdjustmentForm").serialize();
            if (validateAdjustmentsForm()) {
                $.ajax({
                    url:'${g.createLink(controller:'order', action:'saveAdjustment')}',
                    data: data,
                    success: function() {
                        clearOrderAdjustments();
                        loadOrderAdjustments();
                        clearOrderAdjustmentForm();
                        $.notify("Successfully saved new adjustment", "success");
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        if (jqXHR.responseText) {
                            $.notify(jqXHR.responseText, "error");
                        } else {
                            $.notify("Error saving adjustment");
                        }
                    }
                });
            } else {
                $.notify("Please enter a value for all required fields");
            }
            return false;
        }

        function initializeTable() {
            loadOrderItems();
            loadOrderAdjustments();
        }

        function applyFocus(id) {
            // Place focus on product search box
            $(id).focus();
        }

        function clearOrderItemForm() {
          $("#orderItemForm")[0].reset();
          $("#product-id").val("");
          $("#product-value").val("");
          $("#productSupplier").html("");
          clearSource();
          $("#quantityUom").val(null).trigger('change');

          // Reset recipient to default recipient
          var defaultRecipient = $("#defaultRecipient").val();
          $("#recipient").val(defaultRecipient).trigger("change");

          // Reset estimated ready date
          $("#estimatedReadyDate-datepicker").datepicker('setDate', null);

          $("#budgetCode").val(null).trigger('change');
        }

        function clearOrderAdjustmentForm() {
            $("#orderAdjustmentForm")[0].reset();
            $("#orderAdjustmentType").val(null).trigger('change');
            $("#orderItems").val(null).trigger('change');
            $("#description").val("");
            $("#percentage").removeAttr("disabled").val("");
            $("#amount").removeAttr("disabled").val("");
            $("#comments").val("");
            $("#adjustmentBudgetCode").val(null).trigger('change');
        }

        function clearOrderItems() {
            $("#orderItemsTable.body tr").remove();
        }

        function clearOrderAdjustments() {
          $("#orderAdjustmentsTable.body tr").remove();
        }

        function loadOrderItems(table) {
            clearOrderItems();
            var orderId = $("#orderId").val();
            $.ajax({
                url:'${g.createLink( controller:'order', action:'getOrderItems')}',
                data: { id: orderId },
                success: function(data, textStatus, jqXHR){
                    // Remove all rows
                    $("#orderItemsTable > tbody").find("tr").remove();

                    // Generate HTML for each row in response data
                    $.each(data, function(index, row){
                      row["index"] = index + 1;
                        $(buildOrderItemRow(row)).appendTo("#orderItemsTable > tbody");
                    });

                    // Style table rows
                    $("#orderItemsTable > tbody tr").removeClass("odd").filter(":odd").addClass("odd");

                    // Update select for order items in add adjustments tab
                    $('#orderItems').select2({
                        data: data.filter(function(data) {
                          return data.orderItemStatusCode != 'CANCELED';
                        }),
                        placeholder: 'Select an option',
                        width: '100%',
                        allowClear: true,
                        tokenSeparators: [","],
                    });
                }
            });
        }

        function loadOrderAdjustments() {
          clearOrderAdjustments();
          var orderId = $("#orderId").val();
          $.ajax({
            url:'${g.createLink( controller:'order', action:'getOrderAdjustments')}',
            data: { id: orderId },
            success: function(data, textStatus, jqXHR){
              // Remove all rows
              $("#orderAdjustmentsTable > tbody").find("tr").remove();

              // Generate HTML for each row in response data
              $.each(data, function(index, row){
                row["index"] = index + 1;
                $(buildOrderAdjustmentRow(row)).appendTo("#orderAdjustmentsTable > tbody");
              });

              // Style table rows
              $("#orderAdjustmentsTable > tbody tr").removeClass("odd").filter(":odd").addClass("odd");
            }
          });
        }

        function buildOrderItemRow(data) {
          return $("#itemsRowTemplate").tmpl(data);
	    }

        function buildOrderAdjustmentRow(data) {
          return $("#adjustmentsRowTemplate").tmpl(data);
        }

        // Update source code column with product supplier source codes based on product chosen by user
        function productChanged(productId, supplierId, destinationPartyId, sourceId = null) {
          $.ajax({
            type: 'POST',
            data: {
              productId: productId,
              supplierId: supplierId,
              destinationPartyId: destinationPartyId,
            },
            url: '${request.contextPath}/json/productChanged',
            success: function (data, textStatus) {
              $('#productSupplier').select2({
                data: [{id: "", text: ""}].concat(data.productSupplierOptions),
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
              .append(new Option(CREATE_NEW, CREATE_NEW, false, false))
              .trigger('change')
              .removeAttr("disabled");
              if (sourceId) {
                $('#productSupplier').val(sourceId).trigger('change');
              }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {}
          });
        }

        // Update supplier code, manufacturer and manufacturer code columns based on source code chosen by user
        function sourceCodeChanged(productSupplierId, destinationPartyId) {
          $.ajax({
            type: 'POST',
            data: {
              productSupplierId: productSupplierId,
              destinationPartyId: destinationPartyId,
            },
            url: '${request.contextPath}/json/productSupplierChanged',
            success: function (data, textStatus) {
              $('#supplierCode').text(data.supplierCode);
              $('#manufacturerCode').text(data.manufacturerCode);
              $("#validationCode").val(data.validationCode ? data.validationCode.name : '');
              if (data.manufacturer) {
                $('#manufacturer').text(data.manufacturer.name);
              }
              $("#unitPrice").val(data.unitPrice);
              if (data.minOrderQuantity) {
                $("#quantity").val(data.minOrderQuantity);
                $("#quantity").attr("min", data.minOrderQuantity)
              }
              if (data.unitOfMeasure) {
                $("#quantityUom").val(data.unitOfMeasure.id).trigger("change");
              }
              if (data.quantityPerUom) {
                $("#quantityPerUom").val(data.quantityPerUom)
              }

            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
            }
          });
        }

        function getOrderItem(id) {
          $.ajax({
                dataType: "json",
                timeout: 2000,
                url: "${request.contextPath}/json/getOrderItem",
                data: { id: id },
                success: function (data) {
                    return data;
                },
                error: function(xhr, status, error) {
                    alert(error);
                }
            });
        }
        function onCompleteHandler(response, status, xhr ) {
          if (status == "error") {
            if (xhr.responseText) {
              let data = JSON.parse(xhr.responseText);
              $.notify(data.errorMessage, "error");
            } else {
              $.notify("Error deleting item " + id, "error")
            }
          } else {
            $(this).dialog("open");
          }
        }

        function editOrderItem(id) {
            var executionKey = $(this).data("execution");
            var url = "${request.contextPath}/order/orderItemFormDialog/" + id + "?execution=" + executionKey;
            $('.loading').show();
            $("#edit-item-dialog").html("Loading ...").load(url, onCompleteHandler);
        }

        function createProductSource() {
          var productId = $("#product-id").val();
          var supplierId = $("#supplierId").val();
          var url = "${request.contextPath}/order/productSourceFormDialog/?productId=" + productId + "&supplierId=" + supplierId;
          $('.loading').show();
          $("#create-product-source-dialog").html("Loading ...").load(url, onCompleteHandler);
        }

        $(document).ready(function() {
          initializeTable();
          applyFocus("#product-suggest");

          $("table").dblclick(function(event) {
            var id = event.target.closest("tr").id;
            if (id) {
              editOrderItem(id);
            }
            return false;
          });

          $("#edit-item-dialog").dialog({
            autoOpen: false,
            modal: true,
            width: 800,
            title: "Edit line item"
          });

          $("#create-product-source-dialog").dialog({
            autoOpen: false,
            modal: true,
            width: 800,
            title: "Create product source",
            close: function(event, ui) {
              $('#productSupplier').val(null).trigger("change");
            }
          });

          // Submit order item form when we blur on the last field (before button)
          $("#estimatedReadyDate-datepicker")
          .blur(function (event) {
            event.preventDefault();

            // Only save if the blur event is sending focus to save button
            // Note: we don't save on focus of save button because it creates duplicate requests
            if ($(event.relatedTarget).attr("id") === "save-item-button") {
              saveOrderItem();
            }
          });

          $("#product-suggest")
          .keydown(function (event) {
            if (event.keyCode == 13) {
              event.preventDefault();
              return false;
            }
          });

          $(".clear-item-form")
          .live("click", function (event) {
            event.preventDefault();
            clearOrderItemForm();
          });

          $(".delete-item")
          .live("click", function (event) {
            event.preventDefault();
            var id = $(this).data("order-item-id");
            deleteOrderItem(id);

          });

          $(".delete-adjustment")
          .live("click", function (event) {
            event.preventDefault();
            var id = $(this).data("order-adjustment-id");
            deleteOrderAdjustment(id);
          });

          $('.save-item')
          .live("click", function (event) {
            event.preventDefault();
            saveOrderItem();
          });

          $('.edit-item')
          .live("click", function (event) {
            event.preventDefault();
            var id = $(this)
            .data("order-item-id");
            editOrderItem(id);
          });

          $('.edit-adjustment')
            .live("click", function (event) {
              event.preventDefault();
              var id = $(this).data("order-adjustment-id");
              var orderId = $("#orderId").val();
              var canManageAdjustments = ($("#canManageAdjustments").val() === "true");

              if (canManageAdjustments) {
                window.location.href = '${request.contextPath}/order/editAdjustment/' + id + '?order.id=' + orderId;
              } else {
                $.notify("You do not have permissions to perform this action", "error");
              }
            });

          $(".cancel-order-item").live("click", function (event) {
              event.preventDefault();
              var id = $(this).data("order-item-id");
              changeOrderItemStatus(id, '${g.createLink(controller:'order', action:'cancelOrderItem')}');
          });

          $(".restore-order-item").live("click", function (event) {
              event.preventDefault();
              var id = $(this).data("order-item-id");
              changeOrderItemStatus(id, '${g.createLink(controller:'order', action:'restoreOrderItem')}');
          });

          $(".cancel-order-adjustment").live("click", function (event) {
            event.preventDefault();
            var id = $(this).data("order-adjustment-id");
            changeOrderAdjustmentStatus(id, '${g.createLink(controller:'order', action:'cancelOrderAdjustment')}');
          });

          $(".restore-order-adjustment").live("click", function (event) {
            event.preventDefault();
            var id = $(this).data("order-adjustment-id");
            changeOrderAdjustmentStatus(id, '${g.createLink(controller:'order', action:'restoreOrderAdjustment')}');
          });

          $("#btnImportItems")
          .click(function (event) {
            $("#dlgImportItems")
            .dialog('open');
          });

          $("#dlgImportItems")
          .dialog({
            autoOpen: false,
            modal: true,
            width: 600
          });

          $("#importTemplate").change(function(){
            var orderId = $("#orderId").val();
            var supplierId = $("#supplierId").val();
            var fileInput = document.getElementById('importTemplate');
            if (orderId && fileInput.files && fileInput.files.length > 0) {
              var file = fileInput.files[0];
              var importData = new FormData();
              importData.append('fileContents', file);
              importData.append('id', orderId);
              importData.append('supplierId', supplierId);
              $.ajax({
                type: 'POST',
                url: '${g.createLink(controller:'order', action:'importOrderItems')}',
                data: importData,
                enctype: "multipart/form-data",
                contentType: false,
                processData: false,
                success: function () {
                  $("#importTemplate").val('');
                  loadOrderItems();
                  $.notify("Successfully added items", "success")
                },
                error: function (jqXHR, textStatus, errorThrown) {
                  $("#importTemplate").val('');
                  if (jqXHR.responseText) {
                    $.notify(jqXHR.responseText, "error");
                  }
                  else {
                    $.notify("An error occurred", "error");
                  }
                }
              });
            }
          });
        });

        $(function() {
          $(".tabs").tabs(
            {
              cookie: {
                expires: 1
              },
              selected: ${params.skipTo ==  'adjustments' ? 1 : 0}
            }
          );
        });

    </script>

<script id="itemsRowTemplate" type="x-jquery-tmpl">
<tr id="{{= id}}" tabindex="{{= index}}" class="{{if orderItemStatusCode == "CANCELED" }} canceled-item {{else !canEdit }} non-editable {{/if}}">
	<td class="center middle">
    	{{= index }}
	</td>
	<td class="left middle" style="color: {{= product.color }}">
        {{= product.productCode }}
        {{= product.name }}
	</td>
	<td class="center middle">
    	{{if productSupplier }}
	    {{= productSupplier.code }}
	    {{/if}}
	</td>
	 {{if orderItemStatusCode == "PENDING"}}
	<td class="center middle">
    	{{if productSupplier }}
	    {{= productSupplier.supplierCode }}
	    {{/if}}
	</td>
	<td class="center middle">
        {{= manufacturerName || ""  }}
	</td>
	<td class="center middle">
    	{{if productSupplier && productSupplier.manufacturerCode }}
	    {{= productSupplier.manufacturerCode || "" }}
	    {{/if}}
	</td>
	<td class="center middle">
        {{= quantity }}
	</td>
	<td class="center middle" colspan="2">
    	{{= unitOfMeasure }}
	</td>
	<td class="center middle">
	    {{= unitPrice }} {{= currencyCode }}<br/>
	    <small>per {{= unitOfMeasure }}</small>
	</td>
	<td class="center middle">
	    {{= totalPrice }} {{= currencyCode }}
	</td>
	<td class="center middle">
    	{{if recipient }}
	    {{= recipient.name }}
	    {{/if}}
	</td>
	<td class="center middle">
	    {{= estimatedReadyDate }}
	</td>
    <td>
    	{{if budgetCode }}
	    {{= budgetCode.code || "" }}
	    {{/if}}
    </td>
	{{else}}
	<td colspan="11">
	</td>
	{{/if}}
	<td class="center middle">
        <div class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"/>
            </button>
            <div class="actions">
                {{if orderItemStatusCode == "PENDING"}}
                    <div class="action-menu-item">
                        <a href="javascript:void(-1);" class="edit-item" data-order-item-id="{{= id}}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
                            <warehouse:message code="default.button.edit.label"/>
                        </a>
                    </div>
                {{/if}}
                <div class="action-menu-item">
                    <a href="javascript:void(-1);" class="delete-item" data-order-item-id="{{= id}}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
                        <warehouse:message code="default.button.delete.label"/>
                    </a>
                </div>
                {{if !hasShipmentAssociated}}
                   {{if orderItemStatusCode == "PENDING"}}
                        <div class="action-menu-item">
                            <a href="javascript:void(-1);" class="cancel-order-item" data-order-item-id="{{= id}}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
                                <warehouse:message code="default.button.cancel.label"/>
                            </a>
                        </div>
                    {{else orderItemStatusCode == "CANCELED"}}
                        <div class="action-menu-item">
                            <a href="javascript:void(-1);" class="restore-order-item" data-order-item-id="{{= id}}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}"/>
                                <warehouse:message code="default.button.uncancel.label"/>
                            </a>
                        </div>
                    {{/if}}
                {{/if}}
            </div>
        </div>
	</td>
</tr>
</script>
<script id="adjustmentsRowTemplate" type="x-jquery-tmpl">
<tr id="{{= id}}" tabindex="{{= index}}" class="{{if isCanceled }} canceled-item {{/if}}">
	<td class="left middle">
        {{= type.name }}
	</td>
	<td>
    	{{if orderItem }}
	    {{= orderItem.product.name }}
	    {{else}}
        ALL
	    {{/if}}
	</td>
    {{if !isCanceled }}
	<td>
	    {{= description }}
	</td>
	<td>
        {{= percentage  }}
	</td>
	<td>
	    {{= amount }}
	</td>
	<td>
        {{= comments }}
	</td>
	<td>
    	{{if budgetCode }}
	    {{= budgetCode.code || "" }}
	    {{/if}}
    </td>
	{{else}}
	<td colspan="5">
	</td>
	{{/if}}
	<td class="center middle">
        <div class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"/>
            </button>
            <div class="actions">
                {{if !isCanceled }}
                    <div class="action-menu-item">
                        <a href="javascript:void(-1);" class="edit-adjustment" data-order-adjustment-id="{{= id}}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
                            <warehouse:message code="default.button.edit.label"/>
                        </a>
                    </div>
                {{/if}}
                <div class="action-menu-item">
                    <a href="javascript:void(-1);" class="delete-adjustment" data-order-adjustment-id="{{= id}}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
                        <warehouse:message code="default.button.delete.label"/>
                    </a>
                </div>
                {{if !isCanceled}}
                    <div class="action-menu-item">
                        <a href="javascript:void(-1);" class="cancel-order-adjustment" data-order-adjustment-id="{{= id}}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}"/>
                            <warehouse:message code="default.button.cancel.label"/>
                        </a>
                    </div>
                {{else}}
                    <div class="action-menu-item">
                        <a href="javascript:void(-1);" class="restore-order-adjustment" data-order-adjustment-id="{{= id}}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}"/>
                            <warehouse:message code="default.button.uncancel.label"/>
                        </a>
                    </div>
                {{/if}}
           </div>
        </div>
	</td>
</tr>
</script>
</body>
</html>
