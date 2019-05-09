<div class="dialog">

    <g:form name="saveBinLocation" controller="Item" action="update">
        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>

        <table>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="product.label" /></label></td>
                <td valign="top" class="value">
                    <format:product product="${inventoryItem?.product}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label" /></label></td>
                <td valign="top" class="value">
                    ${binLocation?.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="inventoryItem.lotNumber.label"/></label></td>
                <td valign="top" class="value">
                    ${inventoryItem?.lotNumber}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="inventoryItem.expirationDate.label"/></label></td>
                <td valign="top" class="">
                    <g:if test="${inventoryItem?.expirationDate}">
                        <g:formatDate date="${inventoryItem.expirationDate}"/>
                    </g:if>
                    <g:else>
                        <span class="fade">${g.message(code: 'default.never.label')}</span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="default.quantity.label"/></label></td>
                <td valign="top" class="">
                    <input name="quantity" type="number" class="text large" value="${quantity}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="default.reasonCode.label" default="Reason Code"/></label></td>
                <td valign="top" class="">
                    <g:selectInventoryAdjustmentReasonCode name="reasonCode" value="${reasonCode}" class="chzn-select-deselect" noSelection="['':'']"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><warehouse:message code="default.comments.label" default="Comments"/></label>
                </td>
                <td valign="top" class="">
                    <g:textArea name="comments" width="100%" rows="3"></g:textArea>
                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td></td>
                <td>
                    <button type="submit" name="addItem" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="default.button.save.label"/>
                    </button>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>

</div>

