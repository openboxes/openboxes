<%@ page import="org.pih.warehouse.core.Constants" %>

<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="order.itemStatus.label" default="Item Status"/>
    </h2>
    <g:if test="${!isPutawayOrder}">
        <input type="text" id="orderItemsStatusFilter" class="text large" placeholder="${g.message(code: 'order.filterByProductOrSupplier.label', default: 'Filter by product name, code, or supplier code')}"/>
    </g:if>
    <g:if test="${orderItems}">
        <table data-testid="item-status-table" class="table table-bordered" id="order-items-status">
            <thead>
            <tr class="odd">
                <th class="bottom">
                    <warehouse:message code="default.status.label"/>
                </th>
                <th><warehouse:message code="product.productCode.label" /></th>
                <th><warehouse:message code="product.label" /></th>
                <th class="center">${warehouse.message(code: 'product.supplierCode.label')}</th>
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
                        <td data-testid="order-item-status-code">
                            ${orderItem?.orderItemStatusCode}
                        </td>
                    </g:if>
                    <td data-testid="product-code">
                        ${orderItem?.product?.productCode?:""}
                    </td>

                <td data-testid="product-name" class="order-item-product">
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
                <td data-testid="unit-of-measure" class="center">
                    ${orderItem?.productSupplier?.supplierCode}
                </td>
                <td class="center">
                    ${orderItem?.unitOfMeasure}
                </td>
                <td data-testid="quantity" class="order-item-quantity right">
                    ${orderItem?.quantity}
                </td>
                <g:if test="${isPurchaseOrder}">
                    <td data-testid="quantity-shipped" class="order-item-fullfilled right">
                        ${orderItem?.quantityShipped}
                    </td>
                    <td data-testid="quantity-received" class="order-item-received right">
                        ${orderItem?.quantityReceived}
                    </td>
                    <td data-testid="posted-quantity-invoiced" class="right">
                        ${orderItem?.postedQuantityInvoiced}
                    </td>
                    <td data-testid="unit-price" class="">
                        <g:formatNumber number="${orderItem?.unitPrice?:0}" />
                        ${currencyCode}
                    </td>
                    <td data-testid="total-price" class="">
                        <g:formatNumber number="${orderItem?.totalPrice()?:0}" />
                        ${currencyCode}
                    </td>
                </g:if>
                <g:elseif test="${isPutawayOrder}">
                    <td data-testid="lot-number">
                        ${orderItem?.inventoryItem?.lotNumber}
                    </td>
                    <td data-testid="expiration-date">
                        <g:formatDate
                                date="${orderItem?.inventoryItem?.expirationDate}"
                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                        />
                    </td>
                    <td data-testid="origin-bin-location">
                        ${orderItem?.originBinLocation}
                    </td>
                    <td data-testid="destination-bin-location">
                        ${orderItem?.destinationBinLocation}
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
      const filterCells = [1, 2, 3]; // filter by product code, product name, or supplier code
      const filterValue = $("#orderItemsStatusFilter")
        .val()
        .toUpperCase();
      const tableRows = $("#order-items-status tr.dataRowItemStatus");
      filterTableItems(filterCells, filterValue, tableRows)
    });

  });
</script>
