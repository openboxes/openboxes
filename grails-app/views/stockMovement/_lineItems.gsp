
<div id="lineItems" class="box dialog">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
        ${warehouse.message(code:'stockMovement.lineItems.label')}
    </h2>
    <table>
        <tr>
            <th><g:message code="product.productCode.label"/></th>
            <th><g:message code="product.label"/></th>
            <th><g:message code="requisitionItem.quantityRequested.label"/></th>
            <th><g:message code="requisitionItem.comment.label"/></th>
        </tr>
        <g:if test="${stockMovement.lineItems}">
            <g:each var="lineItem" in="${stockMovement?.lineItems}" status="i">
                <tr class="prop ${i % 2 == 0?'odd':'even'}">
                    <td>
                        ${lineItem?.product?.productCode}
                    </td>
                    <td class="product">
                        <g:link controller="inventoryItem" action="showStockCard" id="${lineItem?.product?.id}">
                            <format:product product="${lineItem?.product}"/>
                        </g:link>
                    </td>
                    <td>
                        ${lineItem?.quantityRequested}
                    </td>
                    <td>
                        ${lineItem?.comments}
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>
