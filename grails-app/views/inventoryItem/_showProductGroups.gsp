<div class="box">
    <h2><warehouse:message code="product.alternativeProducts.label"/></h2>
    <table >
        <thead>
            <tr class="odd">
                <th>
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th>
                    <warehouse:message code="product.name.label"/>
                </th>
                <th>
                    <warehouse:message code="category.label"/>
                </th>
                <th>
                    <warehouse:message code="productGroup.label"/>
                </th>
                <th>
                    <warehouse:message code="product.manufacturer.label"/>
                </th>
                <th>
                    <warehouse:message code="product.vendor.label"/>
                </th>
                <th class="center middle" >
                    <warehouse:message code="default.qty.label"/>
                </th>
                <th>
                    <warehouse:message code="product.unitOfMeasure.label"/>
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
                            <g:link controller="inventoryItem" action="showStockCard" id="${product.id}">
                                ${product?.name}
                            </g:link>
                        </td>
                        <td>
                        <g:link controller="inventory" action="browse" params="[subcategoryId:product?.category?.id,showHiddenProducts:'on',showOutOfStockProducts:'on',searchPerformed:true]">
                                ${product?.category?.name}
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="productGroup" action="show" id="${product?.genericProduct?.id}">
                                ${product?.genericProduct?.description}
                            </g:link>
                        </td>
                        <td>
                            ${product.manufacturer}

                        </td>
                        <td>
                            ${product.vendor}
                        </td>
                        <td class="center">
                            ${quantity}
                        </td>
                        <td class="center">
                            ${product.unitOfMeasure}
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:unless test="${quantityMap}">
                <tr>
                    <td colspan="8">
                        <div class="fade empty center">
                            <warehouse:message code="product.emptyProductGroups.message" /> &rsaquo;
                            <g:link controller="product" action="edit" id="${product?.id}" fragment="tabs-productGroups">
                                <warehouse:message code="product.editProductGroups.label" default="Edit product groups"/>
                            </g:link>

                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr class="odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
                <td colspan="6" class="left">
                </td>
                <td class="center">
                    <span style="font-size: 1em;">
                        <g:set var="styleClass" value="color: black;"/>
                        <g:if test="${totalQuantity < 0}">
                            <g:set var="styleClass" value="color: red;"/>
                        </g:if>
                        <span style="${styleClass }" id="totalQuantity">${g.formatNumber(number: totalQuantity, format: '###,###,###') }</span>
                    </span>
                </td>
                <td class="center">
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

