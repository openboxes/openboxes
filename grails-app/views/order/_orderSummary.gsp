<%@ page import="org.pih.warehouse.order.OrderItemStatusCode;" %>
<%@ page import="org.pih.warehouse.order.OrderType;" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode;" %>
<%@ page import="org.pih.warehouse.core.Constants;" %>


<script>
  $(document).ready(function() {
    $("#orderItemsFilter").keyup(function(event){
      const filterCells = [1, 2]; // filter by product code or name
      const filterValue = $("#orderItemsFilter")
        .val()
        .toUpperCase();
      const tableRows = $("#order-items tr.dataRow");
      filterTableItems(filterCells, filterValue, tableRows)
    });
  });
</script>


<div class="box">
    <h2>
        <warehouse:message code="default.summary.label"/>
    </h2>
    <g:if test="${orderInstance.orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
        <input type="text" id="orderItemsFilter" class="text large" placeholder="${g.message(code: 'order.filterByProduct.label', default: 'Filter by product name or code')}"/>
    </g:if>
    <g:if test="${orderInstance?.orderItems}">
        <g:set var="status" value="${0}"/>
        <g:set var="columnsNumber" value="5"/>
        <table class="order-items" id="order-items">
            <thead>
            <tr>
                <g:if test="${orderInstance?.orderType == OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())}">
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
                <g:if test="${orderInstance.orderItems.any { it.productSupplier?.supplierCode } }">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                    <th class="center">
                        <warehouse:message code="product.supplierCode.label"/>
                    </th>
                </g:if>
                <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerName } }">
                    <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                    <th class="center">
                        <warehouse:message code="product.manufacturer.label"/>
                    </th>
                </g:if>
                <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerCode } }">
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

            <tbody>

            <g:set var="orderItemsDerivedStatus" value="${orderInstance?.getOrderItemsDerivedStatus()}"/>
            <g:each var="orderItem" in="${orderInstance?.orderItems?.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }}" status="i">
                <g:if test="${!orderItem?.canceled || orderInstance?.isPurchaseOrder}">
                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'} dataRow" style="${isItemCanceled ? 'background-color: #ffcccb;' : ''}">
                        <g:if test="${orderInstance?.isPurchaseOrder}">
                            <td>
                                <div class="tag ${orderItem?.canceled ? 'tag-danger' : ''}">
                                    <format:metadata obj="${!orderItem?.canceled && orderItemsDerivedStatus[orderItem?.id] ? orderItemsDerivedStatus[orderItem?.id] : orderItem?.orderItemStatusCode?.name()}"/>
                                </div>
                            </td>
                        </g:if>
                        <td style="color: ${orderItem?.product?.color}">
                            ${orderItem?.product?.productCode}
                        </td>
                        <td>
                            <g:link controller="inventoryItem" action="showStockCard"
                                    style="color: ${orderItem?.product?.color}"  params="['product.id':orderItem?.product?.id]">
                                <format:product product="${orderItem?.product}"/>
                                <g:renderHandlingIcons product="${orderItem?.product}" />
                            </g:link>
                        </td>
                        <g:if test="${!orderItem?.canceled}">
                            <g:if test="${orderInstance.orderItems.any { it.productSupplier?.supplierCode } }">
                                <td class="center">
                                    ${orderItem?.productSupplier?.supplierCode}
                                </td>
                            </g:if>
                            <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerName } }">
                                <td class="center">
                                    ${orderItem?.productSupplier?.manufacturerName}
                                </td>
                            </g:if>
                            <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerCode } }">
                                <td class="center">
                                    ${orderItem?.productSupplier?.manufacturerCode}
                                </td>
                            </g:if>
                            <td class="center">
                                ${orderItem?.quantity }
                            </td>
                            <td class="center">
                                ${orderItem?.unitOfMeasure}
                            </td>
                            <td class="right">
                                <g:formatNumber number="${orderItem?.unitPrice}" />
                                ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                            </td>
                            <td class="right">
                                <g:formatNumber number="${orderItem?.totalPrice()}"/>
                                ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
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
                    <g:formatNumber number="${orderInstance?.subtotal}"/>
                    ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                </th>
            </tr>
            <tr>
                <th colspan="${columnsNumber}" class="right">
                    <warehouse:message code="default.adjustments.label" default="Adjustments"/>
                </th>
                <th class="right">
                    <g:formatNumber number="${orderInstance?.totalAdjustments}"/>
                    ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                </th>
            </tr>
            <tr>
                <th colspan="${columnsNumber}" class="right">
                    <warehouse:message code="default.total.label"/>
                </th>
                <th class="right">
                    <g:formatNumber number="${orderInstance?.total}"/>
                    ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                </th>
            </tr>
            </tfoot>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>

