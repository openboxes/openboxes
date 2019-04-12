<div class="dialog">

    <g:form controller="inventoryItem" action="adjustStock" autocomplete="off">

        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="location.id" value="${location?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>

        <table>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="product.label"/></label></td>
                <td valign="top" class="value">
                    <format:product product="${inventoryItem?.product}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
                <td valign="top" class="value">
                    <g:if test="${binLocation}">
                        ${binLocation?.name}
                    </g:if>
                    <g:else>
                        <g:message code="default.label"/>
                    </g:else>

                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="product.lotNumber.label"/></label></td>
                <td valign="top" class="value">
                    <g:if test="${inventoryItem?.lotNumber }">
                        <span class="lotNumber">${inventoryItem?.lotNumber }</span>
                    </g:if>
                    <g:else><span class="fade"><warehouse:message code="default.none.label"/></span></g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="product.expirationDate.label"/></label></td>
                <td valign="top" class="value">
                    <g:if test="${inventoryItem?.expirationDate }">
                        <format:expirationDate obj="${inventoryItem?.expirationDate}"/>
                    </g:if>
                    <g:else>
                        <span class="fade"><warehouse:message code="default.never.label"/></span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="inventory.previousQuantity.label" /></label></td>
                <td valign="top" class="value">
                    <g:hiddenField id="quantityAvailable" name="quantityAvailable" value="${quantityAvailable }"/>
                    ${quantityAvailable }
                    ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="inventory.quantity.label" /></label></td>
                <td valign="top" class="value">
                    <input type="number" name="quantity" size="6" value="${quantityAvailable }" class="text"/>
                    ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="default.comments.label" /></label></td>
                <td valign="top" class="value">
                    <g:textArea name="comment" rows="5" value="${params.comment }" class="text large"/>
                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2" class="center">
                    <button class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="inventory.adjustStock.label"/>
                    </button>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
