<%@ page import="org.pih.warehouse.core.Constants" %>

<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="order.itemStatus.label" default="Item Status"/>
    </h2>
    <g:if test="${!isPutawayOrder}">
        <input type="text" id="orderItemsStatusFilter" class="text large" placeholder="${g.message(code: 'order.filterByProduct.label', default: 'Filter by product name or code')}"/>
    </g:if>
    <g:if test="${orderItems}">
        <table class="table table-bordered" id="order-items-status">
            <thead>
            <tr class="odd">
                <th class="bottom">
                    <warehouse:message code="default.status.label"/>
                </th>
                <th><warehouse:message code="product.productCode.label" /></th>
                <th><warehouse:message code="product.label" /></th>
                <th class="center">${warehouse.message(code: 'product.unitOfMeasure.label')}</th>
                <th class="right">${warehouse.message(code: 'orderItem.quantity.label')}</th>
                <g:if test="${isPurchaseOrder}">
                    <th class="right">${warehouse.message(code: 'order.shipped.label')}</th>
                    <th class="right">${warehouse.message(code: 'order.received.label')}</th>
                    <th class="right">${warehouse.message(code: 'invoice.invoiced.label')}</th>
                    <th><warehouse:message code="order.unitPrice.label" /></th>
                    <th><warehouse:message code="order.totalPrice.label" /></th>
                </g:if>
                <g:elseif test="${isPutawayOrder}">
                    <th><warehouse:message code="inventoryItem.lotNumber.label" /></th>
                    <th><warehouse:message code="inventoryItem.expirationDate.label" /></th>
                    <th><warehouse:message code="orderItem.originBinLocation.label" /></th>
                    <th><warehouse:message code="orderItem.destinationBinLocation.label" /></th>
                    <th><warehouse:message code="requisitionItem.cancelReasonCode.label" /></th>
                </g:elseif>
                %{-- When adding/removing a column, make sure to check the filterCell in function for filtering above --}%
            </tr>
            </thead>
            <tbody>
            <g:each var="orderItem" in="${orderItems}" status="i">
                <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'} dataRowItemStatus">
                    <g:if test="${isPurchaseOrder}">
                        <td>
                            <div class="tag ${orderItem?.canceled ? 'tag-danger' : ''}">
                                <span class="${orderItem?.id}">${g.message(code: 'default.loading.label')}</span>
                            </div>
                        </td>
                    </g:if>
                    <g:if test="${isPutawayOrder}">
                        <td>
                            ${orderItem?.orderItemStatusCode}
                        </td>
                    </g:if>
                    <td>
                        ${orderItem?.product?.productCode?:""}
                    </td>

                <td class="order-item-product">
                    <g:if test="${orderItem?.product }">
                        <g:link controller="inventoryItem" action="showStockCard" params="['product.id':orderItem?.product?.id]">
                            <format:displayName product="${orderItem?.product}" productSupplier="${orderItem?.productSupplier}" showTooltip="${true}" />
                            <g:renderHandlingIcons product="${orderItem?.product}" />
                        </g:link>
                    </g:if>
                    <g:else>
                        ${orderItem?.description }
                    </g:else>
                </td>
                <td class="center">
                    ${orderItem?.unitOfMeasure}
                </td>
                <td class="order-item-quantity right">
                    ${orderItem?.quantity}
                </td>
                <g:if test="${isPurchaseOrder}">
                    <td class="order-item-fullfilled right">
                        ${orderItem?.quantityShipped}
                    </td>
                    <td class="order-item-received right">
                        ${orderItem?.quantityReceived}
                    </td>
                    <td class="right">
                        ${orderItem?.postedQuantityInvoiced}
                    </td>
                    <td class="">
                        <g:formatNumber number="${orderItem?.unitPrice?:0}" />
                        ${currencyCode}
                    </td>
                    <td class="">
                        <g:formatNumber number="${orderItem?.totalPrice()?:0}" />
                        ${currencyCode}
                    </td>
                </g:if>
                <g:elseif test="${isPutawayOrder}">
                    <td>
                        ${orderItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        <g:formatDate
                                date="${orderItem?.inventoryItem?.expirationDate}"
                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                        />
                    </td>
                    <td>
                        ${orderItem?.originBinLocation}
                    </td>
                    <td>
                        ${orderItem?.destinationBinLocation}
                    </td>
                    <td>
                        ${orderItem?.reasonCode}
                    </td>
                </g:elseif>
            </tr>
        </g:each>
        </tbody>
        <g:if test="${isPurchaseOrder}">
            <tfoot>
            <tr class="">
                <th colspan="9" class="right">
                </th>
                <th colspan="1" class="left">
                    <g:formatNumber number="${total}"/>
                    ${currencyCode}
                </th>
            </tr>
            </tfoot>
        </g:if>

        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>

<script>
  $(document).ready(function() {
    setTimeout(fetchOrderItemsDerivedStatus, ${grailsApplication.config.openboxes.purchaseOrder.derivedStatusFetch.delay});

    $("#orderItemsStatusFilter").keyup(function(event){
      const filterCells = [1, 2]; // filter by product code or name
      const filterValue = $("#orderItemsStatusFilter")
        .val()
        .toUpperCase();
      const tableRows = $("#order-items-status tr.dataRowItemStatus");
      filterTableItems(filterCells, filterValue, tableRows)
    });

  });
</script>
