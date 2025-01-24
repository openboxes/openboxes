<%@ page import="org.pih.warehouse.order.OrderItemStatusCode" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<%@ page import="org.pih.warehouse.core.Constants;" %>


<script>
  $(document).ready(function() {
    $("#orderItemsDetailsFilter").keyup(function(event){
      const filterCells = [0, 1]; // filter by product code or name
      const filterValue = $("#orderItemsDetailsFilter")
        .val()
        .toUpperCase();
      const tableRows = $("#order-items-details tr.dataRowItemDetails");
      filterTableItems(filterCells, filterValue, tableRows)
    });
  });

</script>


<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="order.itemDetails.label" default="Item Details"/>
    </h2>
    <g:if test="${orderInstance.orderType != OrderType.findByCode(Constants.PUTAWAY_ORDER)}">
        <input type="text" id="orderItemsDetailsFilter" class="text large" placeholder="${g.message(code: 'order.filterByProduct.label', default: 'Filter by product name or code')}"/>
    </g:if>
    <g:if test="${orderInstance?.orderItems }">
        <table class="table table-bordered" id="order-items-details">
            <thead>
            <tr class="odd">
                <th><warehouse:message code="product.productCode.label" /></th>
                <th><warehouse:message code="product.label" /></th>
                <th class="center"><warehouse:message code="product.supplierCode.label"/></th>
                <th class="center"><warehouse:message code="product.manufacturer.label"/></th>
                <th class="center"><warehouse:message code="product.manufacturerCode.label"/></th>
                <th class="center"><warehouse:message code="orderItem.quantity.label"/></th>
                <th class="center"><warehouse:message code="product.uom.label"/></th>
                <th class="center"><warehouse:message code="orderItem.recipient.label"/></th>
                <th class="center"><warehouse:message code="orderItem.estimatedReadyDate.label"/></th>
                <th class="center"><warehouse:message code="orderItem.actualReadyDate.label"/></th>
                <th class="center"><warehouse:message code="orderItem.budgetCode.label"/></th>
                %{-- When adding/removing a column, make sure to check the filterCell in function for filtering above --}%
            </tr>
            </thead>
            <tbody>
            <g:each var="orderItem" in="${orderInstance?.orderItems?.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }}" status="i">
                <g:set var="isItemCanceled" value="${orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED}"/>
                <g:if test="${!isItemCanceled || orderInstance?.orderType==OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())}">
                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'} dataRowItemDetails" style="${isItemCanceled ? 'background-color: #ffcccb;' : ''}">
                        <td>
                            ${orderItem?.product?.productCode?:""}
                        </td>
                        <td class="order-item-product">
                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':orderItem?.product?.id]">
                                <format:displayName product="${orderItem?.product}" productSupplier="${orderItem?.productSupplier}" showTooltip="${true}" />
                                <g:renderHandlingIcons product="${orderItem?.product}" />
                            </g:link>
                        </td>
                        <g:if test="${!isItemCanceled}">
                            <td class="center">
                                ${orderItem?.productSupplier?.supplierCode}
                            </td>
                            <td class="center">
                                ${orderItem?.productSupplier?.manufacturer?.name}
                            </td>
                            <td class="center">
                                ${orderItem?.productSupplier?.manufacturerCode}
                            </td>
                            <td class="center">
                                ${orderItem?.quantity }
                            </td>
                            <td class="center">
                                ${orderItem?.unitOfMeasure}
                            </td>
                            <td class="center">
                                ${orderItem?.recipient}
                            </td>
                            <td class="center">
                                <g:formatDate date="${orderItem?.estimatedReadyDate}" format="dd/MMM/yyyy"/>
                            </td>
                            <td class="center">
                                <g:formatDate date="${orderItem?.actualReadyDate}" format="dd/MMM/yyyy"/>
                            </td>
                            <td class="center">
                                ${orderItem?.budgetCode?.code}
                            </td>
                        </g:if>
                        <g:else>
                            <td colspan="9"></td>
                        </g:else>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>

