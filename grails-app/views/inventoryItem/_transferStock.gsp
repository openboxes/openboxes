<div class="dialog">
    <g:form controller="inventoryItem" action="transferStock">
        <g:hiddenField name="id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="location.id" value="${location?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="transferOut" value="true"/>

        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">
                    <label><label><g:message code="product.label"/></label></label>
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.product?.productCode}
                    ${inventoryItem?.product?.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><label><g:message code="inventoryItem.lotNumber.label"/></label></label>
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.lotNumber}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><label><g:message code="transaction.source.label"/></label></label>
                </td>
                <td valign="top" class="value">
                    ${location.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="location.binLocation.label" /></label>
                </td>
                <td valign="middle" class="value">
                    <g:if test="${binLocation}">
                        ${binLocation?.name}
                    </g:if>
                    <g:else>
                        <g:message code="default.label"/>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="inventory.quantity.label" /></label>
                </td>
                <td valign="top" class="value">
                    ${quantityAvailable}
                    ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="transaction.destination.label"/></label>
                </td>
                <td valign="top" class="value">
                    <div>
                        <g:selectLocation id="transferTo-${inventoryItem?.id}" name="otherLocation.id" class="chzn-select-deselect trigger-change"
                                            noSelection="['null':'']"
                                            data-placeholder="Choose where stock is being transferred to ..."
                                            data-url="${request.contextPath}/inventoryItem/refreshBinLocation"
                                            data-target="#transferTo-binLocation"
                                            value=""/>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="location.binLocation.label" /></label>
                </td>
                <td valign="top" class="value">
                    <div id="transferTo-binLocation">
                        <g:select from="[]" name="otherBinLocation.id" class="chzn-select-deselect"
                                  noSelection="['':g.message(code: 'default.label')]"></g:select>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="inventory.quantityToTransfer.label" /></label>
                </td>
                <td valign="middle" class="value">
                    <input type="number" name="quantity" size="10" value="${quantityAvailable }" class="text" style="font-size:15px"/>
                    ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2" class="center">
                    <button class="button icon approve">
                        <warehouse:message code="inventory.transferStock.label"/>
                    </button>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
