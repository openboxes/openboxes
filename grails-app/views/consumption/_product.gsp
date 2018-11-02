<div class="box dialog">
    <table>
        <tbody>

            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="product.productCode.label"/></label>
                </td>
                <td>
                    ${product?.productCode}
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="product.label"/></label>
                </td>
                <td>
                    ${product?.name}
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="category.label"/></label>
                </td>
                <td>
                    ${product?.category?.name?:""}
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label><warehouse:message code="product.unitOfMeasure.label"/></label>
                </td>
                <td>
                    ${product?.unitOfMeasure?:""}
                </td>
            </tr>
        </tbody>

    </table>
</div>

<div class="buttons">
    <div class="button-group">
        <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}" class="button btn-close-dialog" target="_blank">
            <img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}"/>
            <warehouse:message code="inventory.showStockCard.label"/>
        </g:link>
    </div>

</div>
