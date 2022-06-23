<div class="box">
    <h2><warehouse:message code="product.associations.label"/></h2>
    <table >
        <thead>
            <tr class="odd">
                <th class="center middle" >
                    <warehouse:message code="default.type.label"/>
                </th>
                <th>
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th>
                    <warehouse:message code="product.name.label"/>
                </th>
                <th class="center middle" >
                    <warehouse:message code="product.quantityAvailableToPromise.label"/>
                </th>
                <th class="center middle" >
                    <warehouse:message code="productAssociation.comments.label"/>
                </th>
            </tr>
        </thead>

        <tbody>
            <g:if test="${associations}">
                <g:each var="association" in="${associations}" status="status">
                    <g:set var="associatedProduct" value="${association.associatedProduct}"/>
                    <g:set var="quantityAvailable" value="${quantityAvailableMap[associatedProduct?.id] ?: 0}"/>
                    <tr class="${(status%2)?'odd':'even'}">
                        <td class="center">
                            ${association.code}
                        </td>
                        <td>${associatedProduct?.productCode}</td>
                        <td>
                            <g:link controller="inventoryItem" action="showStockCard" id="${associatedProduct.id}" fragment="ui-tabs-1">
                                ${associatedProduct?.name}
                            </g:link>
                        </td>
                        <td class="center">
                            ${g.formatNumber(number: quantityAvailable, format: '###,###,###') }
                            ${associatedProduct.unitOfMeasure}
                        </td>
                        <td class="border-right middle center">
                            <g:if test="${association.comments}">
                                <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${association.comments}"/>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:unless test="${quantityAvailableMap}">
                <tr>
                    <td colspan="8">
                        <div class="fade empty center">
                            <div>
                                <g:message code="default.empty.message" default="There are no {0}" args="[g.message(code:'product.associations.label')]"/>
                            </div>
                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr class="odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
                <td colspan="3" class="left">
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
                <td></td>
            </tr>
        </tfoot>
    </table>
</div>

