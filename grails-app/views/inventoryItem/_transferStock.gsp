<div class="dialog">
    <g:form controller="inventoryItem" action="transferStock">
        <g:hiddenField name="id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="location.id" value="${location?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="transferOut" value="true"/>

        <table>
            <thead>
            <tr>
                <th width="20%"></th>
                <th width="40%">${warehouse.message(code: 'default.origin.label')}</th>
                <th width="40%">${warehouse.message(code: 'default.destination.label')}</th>
            </tr>
            </thead>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name" width="20%">
                    <label><g:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.product?.productCode}
                    ${inventoryItem?.product?.name}
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.product?.productCode}
                    ${inventoryItem?.product?.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="inventoryItem.lotNumber.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.lotNumber?:warehouse.message(code:'default.label')}
                </td>
                <td valign="top" class="value">
                    ${inventoryItem?.lotNumber?:warehouse.message(code:'default.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="inventoryItem.expirationDate.label"/></label>
                </td>
                <td valign="top" class="value">
                    <g:if test="${inventoryItem?.expirationDate }">
                        <g:formatDate date="${inventoryItem?.expirationDate }" format="d MMM yyyy"/>
                    </g:if>
                    <g:else>
                        <span class="fade">${warehouse.message(code: 'default.never.label')}</span>
                    </g:else>
                </td>
                <td valign="top" class="value">
                    <g:if test="${inventoryItem?.expirationDate }">
                        <g:formatDate date="${inventoryItem?.expirationDate }" format="d MMM yyyy"/>
                    </g:if>
                    <g:else>
                        <span class="fade">${warehouse.message(code: 'default.never.label')}</span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="location.label"/></label>
                </td>
                <td valign="top" class="value">
                    ${location.name}
                </td>
                <td>
                    <g:selectLocation id="transferTo-${inventoryItem?.id}" name="otherLocation.id" class="chzn-select-deselect trigger-change"
                                      noSelection="['null':'']"
                                      data-placeholder="Choose where stock is being transferred to ..."
                                      data-url="${request.contextPath}/inventoryItem/refreshBinLocation"
                                      data-target="#transferTo-binLocation"
                                      value=""/>
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
                <td>
                    <div id="transferTo-binLocation">
                        <g:select from="[]" name="otherBinLocation.id" class="chzn-select-deselect"
                                  noSelection="['':g.message(code: 'default.label')]"></g:select>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="default.quantity.label" /></label>
                </td>
                <td valign="top" class="value">
                    ${quantityAvailable}
                    ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                </td>
                <td>
                    <input type="number" name="quantity" size="10" value="${quantityAvailable }" class="text"/>
                    ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                </td>
            </tr>

            </tbody>
            <tfoot>
            <tr>
                <td colspan="3" class="center">
                    <button class="button icon approve">
                        <warehouse:message code="inventory.transferStock.label"/>
                    </button>
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
