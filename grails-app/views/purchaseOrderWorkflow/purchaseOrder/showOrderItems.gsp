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

            <div class="box">
                <h2 style="display: flex; align-items: center; justify-content: space-between;">
                    <warehouse:message code="order.wizard.addItems.label"/>
                    <div class="button-group" style="margin-right: 5px;">
                        <g:if test="${order?.status < OrderStatus.PLACED}">
                            <input type="file" name="importTemplate" id="importTemplate" class="import-template" />
                            <label for="importTemplate" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'disk_upload.png')}" />&nbsp;
                                <warehouse:message code="default.importTemplate.label" default="Import template"/>
                            </label>
                        </g:if>
                    </div>
                </h2>
                <g:form name="orderItemForm" action="purchaseOrder" method="post">
                    <g:hiddenField id="orderId" name="order.id" value="${order?.id }"></g:hiddenField>
                    <g:hiddenField id="orderItemId" name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                    <g:hiddenField id="supplierId" name="supplier.id" value="${order?.originParty?.id }"></g:hiddenField>
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
                                <th colspan="14" class="right">
                                    <warehouse:message code="default.total.label"/>
                                    <g:formatNumber number="${order?.totalPrice()?:0.0 }"/>
                                    ${order?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </th>
                            </tr>
                        </tfoot>

                    </table>
                </g:form>
            </div>

            <div class="buttons">
                <div class="left">
                    <g:link controller="purchaseOrderWorkflow"
                            action="purchaseOrder"
                            id="${order?.id}"
                            event="enterOrderDetails"
                            params="[skipTo:'details']"
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
    </div>
    <script type="text/javascript">

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
          if (!this.value) {
            $("#productSupplier").html("");
          } else {
            clearSource();
            disableEditing();
            $('#productSupplier').html("");
            productChanged(this.value, supplierId);
          }
        });

        // When chosen source code has changed, trigger function that updates supplier code, manufacturer and manufacturer code columns
        $("#productSupplier").live('change', function(event) {
          sourceCodeChanged($("#productSupplier option:selected")
          .val());
        });

        function deleteOrderItem(id) {
          $.ajax({
            url: '${g.createLink(controller:'order', action:'removeOrderItem')}',
            data: { id: id },
            success: function () {
              clearOrderItems();
              loadOrderItems();
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

        /**
         * @FIXME Didn't have time to make this pretty - should use required class on
         * fields instead of hardcoding the IDs.
         */
        function validateForm() {

          var product = $("#product-suggest").val();
          var quantity = $("#quantity").val();
          var unitPrice = $("#unitPrice").val();
          var quantityUom = $("#quantityUom").val();
          var quantityPerUom = $("#quantityPerUom").val();

          if (!product) $("#product-suggest").notify("Required")
          if (!quantity) $("#quantity").notify("Required")
          if (!unitPrice) $("#unitPrice").notify("Required")
          if (!quantityUom) $("#quantityUom_chosen").notify("Required")
          if (!quantityPerUom) $("#quantityPerUom").notify("Required")

          return product && quantity && unitPrice && quantityPerUom && quantityUom
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
            if (validateForm()) {
                $.ajax({
                    url:'${g.createLink(controller:'order', action:'saveOrderItem')}',
                    data: data,
                    success: function() {
                        clearOrderItemForm();
                        loadOrderItems();
                        applyFocus("#product-suggest");
                        $.notify("Successfully saved new item", "success")
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                      $.notify("Error saving your item");
                    }
                });
            }
            else {
              $.notify("Please enter a value for all required fields")
            }
            return false
        }

        function initializeTable() {
            loadOrderItems();
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
          var defaultRecipient = $("#defaultRecipient").val();
          $("#recipient").val(defaultRecipient).trigger("chosen:updated");
          $("#estimatedReadyDate-datepicker").datepicker('setDate', null);
          disableEditing();
        }

        function clearOrderItems() {
            $("#orderItemsTable.body tr").remove();
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
                }
            });
        }

        function buildOrderItemRow(data) {
          return $("#rowTemplate").tmpl(data);
	    }

        // Update source code column with product supplier source codes based on product chosen by user
        function productChanged(productId, supplierId) {
          $.ajax({
            type: 'POST',
            data: {
              productId: productId,
              supplierId: supplierId,
            },
            url: '${request.contextPath}/json/productChanged',
            success: function (data, textStatus) {
              $('#productSupplier').select2({
                data: [{id: "", text: ""}].concat(data.productSupplierOptions),
                placeholder: 'Select an option',
                width: '100%',
                allowClear: true,
                tags: true,
                tokenSeparators: [","],
                createTag: function (tag) {
                  return {
                    id: tag.term,
                    text: tag.term + " (create new)",
                    isNew : true
                  };
                }
              });
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
              $('#supplierCode').html(data.supplierCode);
              $('#manufacturerCode').html(data.manufacturerCode);
              if (data.manufacturer.id) {
                $('#manufacturer').val(data.manufacturer.id).trigger("change");
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

    </script>

<script id="rowTemplate" type="x-jquery-tmpl">
<tr id="{{= id}}" tabindex="{{= index}}" {{if !canEdit }} class="non-editable" {{/if}}>
	<td class="center middle">
    	{{= index }}
	</td>
	<td class="left middle">
        {{= product.productCode }}
        {{= product.name }}
	</td>
	<td class="center middle">
    	{{if productSupplier }}
	    {{= productSupplier.code }}
	    {{/if}}
	</td>
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
	<td class="center middle">
        <div class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <a href="javascript:void(-1);" class="edit-item" data-order-item-id="{{= id}}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
                        <warehouse:message code="default.button.edit.label"/>
                    </a>
                </div>
                <div class="action-menu-item">
                    <a href="javascript:void(-1);" class="delete-item" data-order-item-id="{{= id}}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>
                        <warehouse:message code="default.button.delete.label"/>
                    </a>
                </div>
            </div>
        </div>
	</td>
</tr>
</script>
</body>
</html>
