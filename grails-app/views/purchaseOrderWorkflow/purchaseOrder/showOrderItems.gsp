<%@ page import="org.pih.warehouse.order.OrderStatus" %>
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
                <g:form name="orderItemForm" action="purchaseOrder" method="post">
                    <g:hiddenField id="orderId" name="order.id" value="${order?.id }"></g:hiddenField>
                    <g:hiddenField id="orderItemId" name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                    <g:hiddenField id="supplierId" name="supplier.id" value="${order?.originParty?.id }"></g:hiddenField>
                    <table id="orderItemsTable">
                        <thead>
                            <tr class="odd">
                                <th><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th class="center"><warehouse:message code="product.sourceCode.label"/></th>
                                <th class="center"><warehouse:message code="product.supplierCode.label"/></th>
                                <th class="center"><warehouse:message code="product.manufacturer.label"/></th>
                                <th class="center"><warehouse:message code="product.manufacturerCode.label"/></th>
                                <th class="center"><warehouse:message code="default.quantity.label"/></th>
                                <th class="center"><warehouse:message code="default.uom.label"/></th>
                                <th class="center"><warehouse:message code="order.unitPrice.label"/></th>
                                <th class="right"><warehouse:message code="orderItem.totalCost.label"/></th>
                                <th class="center"><warehouse:message code="order.recipient.label"/></th>
                                <th class="center"><warehouse:message code="orderItem.estimatedReadyDate.label"/></th>
                                <th class="center"><warehouse:message code="orderItem.actualReadyDate.label"/></th>
                                <th class="center" width="1%"><warehouse:message code="default.actions.label"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- data is dynamically loaded -->
                        </tbody>
                        <tfoot>
                            <g:if test="${order?.status < org.pih.warehouse.order.OrderStatus.PLACED}">
                                <g:render template="/order/orderItemForm"/>
                            </g:if>
                            <tr class="">
                                <th colspan="15" class="right">
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
                        <g:link controller="order" action="shipOrder" id="${order?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'lorry.png')}" />&nbsp;
                            ${warehouse.message(code: 'order.wizard.shipOrder.label', default: 'Ship Order')}
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
          var productId = $("#product-suggest").val();
          var quantity = $("#quantity").val();
          var unitPrice = $("#unitPrice").val();
          if (productId || quantity || unitPrice) {
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
          productChanged(this.value, supplierId);
        });

        // When chosen source code has changed, trigger function that updates supplier code, manufacturer and manufacturer code columns
        $("#productSupplier").live('change', function(event) {
          sourceCodeChanged($("#productSupplier option:selected")
          .val());
        });

        function deleteOrderItem(id) {
          $.ajax({
            url: '${g.createLink(controller:'order', action:'deleteOrderItem')}',
            data: { id: id },
            success: function () {
              clearOrderItems();
              loadOrderItems();
              $.notify("Successfully deleted item " + id, "success")
            },
            error: function (jqXHR, textStatus, errorThrown) {
              $.notify("Error deleting item " + id, "error")
            }
          });
          return false
        }

        function saveOrderItem() {
            var data = $("#orderItemForm").serialize();
            data += "&productSupplier.id=" + $("#productSupplier").val();
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
                },
                complete: function(jqXHR, textStatus) {

                }
            });
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
          $("#productSupplier").html("");
          $("#supplierCode").html("");
          $("#manufacturer").html("");
          $("#manufacturerCode").html("");
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
              $('#productSupplier').replaceWith(data);
              $("#productSupplier").show().focus();
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
              console.log("data: ", data);
              $('#supplierCode').html(data.supplierCode);
              $('#manufacturerCode').html(data.manufacturerCode);
              $('#manufacturer').html(data.manufacturer);
              $("#unitPrice").val(data.unitPrice);
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
                    console.log(data);
                    return data;
                },
                error: function(xhr, status, error) {
                    alert(error);
                }
            });
        }
        function onCompleteHandler(response, status, xhr ) {
          if (status == "error") {
            var errorMessage = "Sorry, there was an error: " + xhr.status + " " + xhr.statusText;
            var errorHtml = $("<div/>").addClass("fade empty center").append(errorMessage);
            $(this).html(errorHtml);
          }
        }

        function editOrderItem(id) {
            var executionKey = $(this).data("execution");
            var url = "${request.contextPath}/order/orderItemFormDialog/" + id + "?execution=" + executionKey;
            $('.loading').show();
            $("#edit-item-dialog").html("Loading ...").load(url, onCompleteHandler).dialog("open");
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
            saveOrderItem();
          });

          $("#product-suggest")
          .keydown(function (event) {
            console.log(event);
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
            var id = $(this)
            .data("order-item-id");
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
        });

    </script>

<script id="rowTemplate" type="x-jquery-tmpl">
<tr id="{{= id}}" tabindex="{{= index}}">
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
    	{{if productSupplier && productSupplier.manufacturer }}
	    {{= productSupplier.manufacturer.name || "None"  }}
	    {{/if}}
	</td>
	<td class="center middle">
    	{{if productSupplier && productSupplier.manufacturer }}
	    {{= productSupplier.manufacturer.manufacturerCode || "None" }}
	    {{/if}}
	</td>
	<td class="center middle">
	    {{= quantity }}
	</td>
	<td class="center middle">
	    {{= unitOfMeasure }}
	</td>
	<td class="center middle">
	    {{= unitPrice }}
	</td>
	<td class="center middle">
	    {{= totalPrice }}
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
	    {{= actualReadyDate }}
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
