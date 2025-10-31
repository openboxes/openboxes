<div class="box">
    <h2>
        <warehouse:message code="default.summary.label"/>
    </h2>
    <g:if test="${!isPutawayOrder}">
        <input type="text" id="orderItemsFilter" class="text large" placeholder="${g.message(code: 'order.filterByProductOrSupplier.label', default: 'Filter by product name, code, or supplier code')}"/>
    </g:if>
    <g:if test="${orderItems}">
        <g:set var="status" value="${0}"/>
        <g:set var="columnsNumber" value="5"/>
        <table class="order-items" id="order-items">
            <thead>
            <tr>
                <g:if test="${isPurchaseOrder}">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}" />
                    <th class="bottom">
                        <warehouse:message code="default.status.label"/>
                    </th>
                </g:if>
                <th class="bottom">
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="product.name.label"/>
                </th>
                <g:if test="${hasSupplierCode}">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                    <th class="center">
                        <warehouse:message code="product.supplierCode.label"/>
                    </th>
                </g:if>
                <g:if test="${hasManufacturerName}">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                    <th class="center">
                        <warehouse:message code="product.manufacturer.label"/>
                    </th>
                </g:if>
                <g:if test="${hasManufacturerCode}">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                    <th class="center">
                        <warehouse:message code="product.manufacturerCode.label"/>
                    </th>
                </g:if>
                <th class="center bottom">
                    <warehouse:message code="orderItem.quantity.label" default="Quantity"/>
                </th>
                <th class="center bottom">
                    <warehouse:message code="product.uom.label" default="UOM"/>
                </th>
                <th class="right bottom">
                    <warehouse:message code="orderItem.unitPrice.label" default="Unit price"/>
                </th>
                <th class="right bottom">
                    <warehouse:message code="orderItem.totalPrice.label" default="Total amount"/>
                </th>
                %{-- When adding/removing a column, make sure to check the filterCell in function for filtering above --}%

            </tr>
            </thead>

            <tbody data-testid="order-summary-table">

            <g:each var="orderItem" in="${orderItems?.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }}" status="i">
                <g:if test="${!orderItem?.canceled || isPurchaseOrder}">
                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'} dataRow" style="${orderItem?.canceled ? 'background-color: #ffcccb;' : ''}">
                        <g:if test="${isPurchaseOrder}">
                            <td>
                                <div class="tag ${orderItem?.canceled ? 'tag-danger' : ''}">
                                    <span class="${orderItem?.id}">${g.message(code: 'default.loading.label')}</span>
                                </div>
                            </td>
                        </g:if>
                        <td data-testid="product-code" style="color: ${orderItem?.product?.color}">
                            ${orderItem?.product?.productCode}
                        </td>
                        <td data-testid="product-name">
                            <g:link controller="inventoryItem" action="showStockCard"
                                    style="color: ${orderItem?.product?.color}"  params="['product.id':orderItem?.product?.id]">
                                <format:displayNameWithColor product="${orderItem?.product}" productSupplier="${orderItem?.productSupplier}"  showTooltip="${true}" />
                                <g:renderHandlingIcons product="${orderItem?.product}" />
                            </g:link>
                        </td>
                        <g:if test="${!orderItem?.canceled}">
                            <g:if test="${hasSupplierCode}">
                                <td data-testid="supplier-code" class="center">
                                    ${orderItem?.productSupplier?.supplierCode}
                                </td>
                            </g:if>
                            <g:if test="${hasManufacturerName}">
                                <td data-testid="manufacturer-name" class="center">
                                    ${orderItem?.productSupplier?.manufacturerName}
                                </td>
                            </g:if>
                            <g:if test="${hasManufacturerCode}">
                                <td data-testid="manufacturer-code" class="center">
                                    ${orderItem?.productSupplier?.manufacturerCode}
                                </td>
                            </g:if>
                            <td data-testid="quantity" class="center">
                                ${orderItem?.quantity}
                            </td>
                            <td data-testid="uom" class="center">
                                ${orderItem?.unitOfMeasure}
                            </td>
                            <td data-testid="unit-price" class="right">
                                <g:formatNumber number="${orderItem?.unitPrice}" />
                                ${currencyCode}
                            </td>
                            <td data-testid="total-price" class="right">
                                <g:formatNumber number="${orderItem?.totalPrice()}"/>
                                ${currencyCode}
                            </td>
                        </g:if>
                        <g:else>
                            <td colspan="${columnsNumber}"></td>
                        </g:else>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="${columnsNumber}" class="right">
                    <warehouse:message code="default.subtotal.label" default="Subtotal"/>
                </th>
                <th class="right">
                    <g:formatNumber number="${subtotal}"/>
                    ${currencyCode}
                </th>
            </tr>
            <tr>
                <th colspan="${columnsNumber}" class="right">
                    <warehouse:message code="default.adjustments.label" default="Adjustments"/>
                </th>
                <th class="right">
                    <g:formatNumber number="${totalAdjustments}"/>
                    ${currencyCode}
                </th>
            </tr>
            <tr>
                <th colspan="${columnsNumber}" class="right">
                    <warehouse:message code="default.total.label"/>
                </th>
                <th class="right">
                    <g:formatNumber number="${total}"/>
                    ${currencyCode}
                </th>
            </tr>
            </tfoot>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>

<script>
  $(document).ready(function() {
    setTimeout(fetchOrderItemsDerivedStatus, ${grailsApplication.config.openboxes.purchaseOrder.derivedStatusFetch.delay});

    $("#orderItemsFilter").keyup(function(event){
      const filterCells = [1, 2, 3]; // filter by product code, product name, or supplier code
      const filterValue = $("#orderItemsFilter")
        .val()
        .toUpperCase();
      const tableRows = $("#order-items tr.dataRow");
      filterTableItems(filterCells, filterValue, tableRows)
    });
  });
</script>
