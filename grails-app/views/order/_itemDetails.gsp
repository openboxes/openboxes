<%@ page import="org.pih.warehouse.order.OrderItemStatusCode" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<div id="tab-content" class="box">
    <h2>
        <warehouse:message code="order.itemDetails.label" default="Item Details"/>
    </h2>
    <g:if test="${orderInstance?.orderItems }">
        <table class="table table-bordered">
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
            </tr>
            </thead>
            <tbody>
            <g:each var="orderItem" in="${orderInstance?.orderItems?.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }}" status="i">
                <g:set var="isItemCanceled" value="${orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED}"/>
                <g:if test="${!isItemCanceled || orderInstance?.orderType==OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())}">
                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}" style="${isItemCanceled ? 'background-color: #ffcccb;' : ''}">
                        <td>
                            ${orderItem?.product?.productCode?:""}
                        </td>
                        <td class="order-item-product">
                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':orderItem?.product?.id]">
                                <format:product product="${orderItem?.product}"/>
                            </g:link>
                        </td>
                        <g:if test="${!isItemCanceled}">
                            <td class="center">
                                ${orderItem?.productSupplier?.supplierCode}
                            </td>
                            <td class="center">
                                ${orderItem?.productSupplier?.manufacturerName}
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
