<div class="dialog">
    <g:form controller="inventoryItem" action="transferStock">
        <g:hiddenField name="id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="product.id" value="${inventoryItem?.product?.id}"/>
        <g:hiddenField name="inventoryItem.id" value="${inventoryItem?.id}"/>
        <g:hiddenField name="location.id" value="${location?.id}"/>
        <g:hiddenField name="binLocation.id" value="${binLocation?.id}"/>
        <g:hiddenField name="transferOut" value="false"/>

        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><label><g:message code="transaction.source.label" default="Source"/></label></td>
                <td valign="top" class="value">
                    <g:selectLocation id="transferFrom-${itemInstance?.id}" name="otherLocation.id" noSelection="['null':'']"
                                      class="chzn-select-deselect trigger-change"
                                      data-placeholder="Choose where stock is being returned from ..."
                                      data-url="${request.contextPath}/inventoryItem/refreshBinLocation"
                                      data-target="#transferFrom-binLocation"

                                      value=""/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label><g:message code="location.binLocation.label" /></label>
                </td>
                <td valign="top" class="value">
                    <div id="transferFrom-binLocation">
                        <g:select from="[]" name="otherBinLocation.id" class="chzn-select-deselect"
                                  noSelection="['':g.message(code: 'default.label')]"></g:select>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><g:message code="transaction.destination.label" default="Destination" /></label></td>
                <td valign="top" class="value">
                    ${session.warehouse.name}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><g:message code="location.binLocation.label" /></label></td>
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
                <td valign="top" class="name">
                    <label><g:message code="inventory.quantityOnHand.label" default="Quantity on Hand" /></label>
                </td>
                <td valign="top" class="value">
                    ${quantityAvailable}
                    ${inventoryItem?.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><label><g:message code="inventory.quantityToReturn.label" /></label></td>
                <td valign="top" class="value">
                    <input type="number" name="quantity" size="6" value="${quantityAvailable }" class="text"/>
                    ${inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                </td>
            </tr>

            </tbody>
            <tfoot>
                <tr>
                    <td colspan="2" class="center">
                        <button class="button icon approve">
                            <warehouse:message code="inventory.returnStock.label" default="Return stock"/>
                        </button>
                    </td>
                </tr>
            </tfoot>
        </table>
    </g:form>
</div>

