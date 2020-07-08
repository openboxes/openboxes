<div class="box">
    <h2>
        <warehouse:message code="default.summary.label"/>
    </h2>
    <g:if test="${orderInstance?.orderItems}">
        <g:set var="status" value="${0}"/>
        <g:set var="columnsNumber" value="5"/>
        <table class="order-items">
            <thead>
            <tr>
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

            </tr>
            </thead>

            <tbody>

            <g:each var="orderItem" in="${orderInstance?.listOrderItems() }" status="i">
                <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}">
                    <td>
                        ${orderItem?.product?.productCode}
                    </td>
                    <td>
                        <format:product product="${orderItem?.product}"/>
                    </td>
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
                    </td>
                    <td class="right">
                        <g:formatNumber number="${orderItem?.totalPrice()}"/>
                    </td>
                </tr>

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
