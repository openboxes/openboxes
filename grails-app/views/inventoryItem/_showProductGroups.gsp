<div class="box">
    <h2><warehouse:message code="product.substitutions.label"/></h2>
    <table >
        <thead>
            <tr class="odd">
                <th>
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th>
                    <warehouse:message code="product.name.label"/>
                </th>
                <th class="center middle" >
                    <warehouse:message code="default.qty.label"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <g:if test="${quantityMap}">
                <g:each var="entrySet" in="${quantityMap}" status="status">
                    <g:set var="product" value="${entrySet.key}"/>
                    <g:set var="quantity" value="${entrySet.value}"/>
                    <tr class="${(status%2)?'odd':'even'}">
                        <td>${product?.productCode}</td>
                        <td>
                            <g:link controller="inventoryItem" action="showStockCard" id="${product.id}" fragment="ui-tabs-1">
                                ${product?.name}
                            </g:link>
                        </td>
                        <td class="center">
                            ${quantity}
                            ${product.unitOfMeasure}
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:unless test="${quantityMap}">
                <tr>
                    <td colspan="8">
                        <div class="fade empty center">
                            <div>
                                <g:message code="default.empty.message" default="There are no {0}" args="[g.message(code:'product.substitutions.label')]"/>
                            </div>
                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr class="odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
                <td colspan="2" class="left">
                </td>
                <td class="center">
                    <span style="font-size: 1em;">
                        <g:set var="styleClass" value="color: black;"/>
                        <g:if test="${totalQuantity < 0}">
                            <g:set var="styleClass" value="color: red;"/>
                        </g:if>
                        <span style="${styleClass }" id="totalQuantity">${g.formatNumber(number: totalQuantity, format: '###,###,###') }</span>
                    </span>
                    <g:if test="${product?.unitOfMeasure }">
                        <format:metadata obj="${product?.unitOfMeasure}"/>
                    </g:if>
                    <g:else>
                        ${warehouse.message(code:'default.each.label') }
                    </g:else>
                </td>
            </tr>
        </tfoot>
    </table>
</div>

