<div class="dialog">

    <jqvalui:renderValidationScript for="org.pih.warehouse.inventory.AdjustStockCommand" form="adjustStockForm"/>
    <g:form id="adjustStockForm" name="adjustStockForm" controller="inventoryItem" action="adjustStock">
        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="location.id" value="${location?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="redirectUri" value="/inventory/manage"/>

        <div class="summary">

            <div class="title center">${inventoryItem?.product?.productCode}
                <g:link controller="inventoryItem" action="showStockCard" id="${inventoryItem?.product?.id}">
                    <format:product product="${inventoryItem?.product}"/></div>
                </g:link>

            <div class="center">
                <label><g:message code="inventoryItem.lotNumber.label"/></label>
                <g:if test="${inventoryItem?.lotNumber}">
                    ${inventoryItem?.lotNumber}
                </g:if>
                <g:else>
                    ${g.message(code: 'default.label')}
                </g:else>
                &nbsp;|&nbsp;

                <label><g:message code="inventoryItem.expires.label"/></label>
                <g:if test="${inventoryItem?.expirationDate}">
                    <g:formatDate date="${inventoryItem.expirationDate}" format="MMM dd, yyyy"/>
                </g:if>
                <g:else>
                    ${g.message(code: 'default.never.label')}
                </g:else>
            </div>


        </div>

        <table>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="location.label" /></label></td>
                <td valign="top" class="value">
                    ${session?.warehouse?.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="location.binLocation.label"/></label></td>
                <td valign="top" class="">
                    <g:if test="${binLocation?.name}">
                        ${binLocation?.name}
                    </g:if>
                    <g:else>
                        <span class="fade">${g.message(code: 'default.label')}</span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="default.quantity.label"/></label></td>
                <td valign="top" class="">
                    <input name="currentQuantity" type="number" class="text medium"
                           readonly="readonly"
                           value="${quantity}"/> ${inventoryItem?.product?.unitOfMeasure}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="inventory.newQuantity.label"/></label></td>
                <td valign="top" class="">
                    <input id="newQuantity" name="newQuantity" type="number" class="text medium"
                           autofocus="autofocus"
                           value="${quantity}"/> ${inventoryItem?.product?.unitOfMeasure}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><warehouse:message code="default.reasonCode.label" default="Reason Code"/></label></td>
                <td valign="top" class="">
                    <g:selectInventoryAdjustmentReasonCode name="reasonCode" value="${reasonCode}"  noSelection="['':'']"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><warehouse:message code="default.comments.label" default="Comments"/></label>
                </td>
                <td valign="top" class="">
                    <g:textArea name="comment" width="100%" rows="3"></g:textArea>
                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td></td>
                <td>
                    <button type="submit" name="addItem" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/>
                        <warehouse:message code="default.button.save.label"/>
                    </button>

                    <button class="btn-close-dialog button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}"/>
                        <warehouse:message code="default.button.close.label"/>
                    </button>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>

</div>

