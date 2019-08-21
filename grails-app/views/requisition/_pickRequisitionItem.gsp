<g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
<g:set var="noneSelected" value="${!selectedRequisitionItem }"/>
<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled())?'canceled':''} ${selected ?'selected-middle':'unselected'}">

    <td>
        <div class="action-menu">
            <span class="action-btn">
                <g:if test="${requisitionItem?.isCanceled()}">
                    <img src="${resource(dir:'images/icons/silk', file: 'decline.png')}"/>
                </g:if>
                <g:elseif test="${requisitionItem?.isApproved()}">
                    <img src="${resource(dir:'images/icons/silk', file: 'accept.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isChanged()}">
                    <img src="${resource(dir:'images/icons/silk', file: 'accept.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isSubstituted()}">
                    <img src="${resource(dir:'images/icons/silk', file: 'arrow_switch.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isPending()}">
                    <img src="${resource(dir:'images/icons/silk', file: 'hourglass.png')}"/>
                </g:elseif>
                <g:else>
                    <img src="${resource(dir:'images/icons/silk', file: 'information.png')}"/>
                </g:else>
            </span>
            <div class="actions">
                <div class="box" style="width:450px;">
                    <div>
                        <label>Requisition item:</label>
                        ${requisitionItem.toJson()}
                        ${requisitionItem.calculatePercentageCompleted()}
                    </div>
                    <div>
                    </div>
                    <div>
                        <label>Substitution:</label>
                        ${requisitionItem?.substitutionItem?.toJson()}
                    </div>
                    <div>
                        <label>Modification:</label>
                        ${requisitionItem?.modificationItem?.toJson()}
                    </div>
                </div>
            </div>
        </div>

    </td>

    <td class="left">
        <a name="${selectedRequisitionItem?.id}"></a>
        <g:if test="${!isChild }">
            <div class="action-menu">
                <button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                </button>
                <div class="actions">


                    <div class="box" style="max-width: 600px;">
                        <g:set var="inventoryItemMap" value="${requisitionItem?.retrievePicklistItems()?.groupBy { it?.inventoryItem }}"/>
                        <g:form controller="requisition" action="addToPicklistItems">
                            <g:hiddenField name="requisition.id" value="${requisition?.id}"/>
                            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id}"/>
                            <h2>
                                ${warehouse.message(code:'picklist.pickingItemsFor.label', default:'Picking items for')}
                                ${requisitionItem?.product?.productCode}
                                ${requisitionItem?.product?.name}

                                <g:if test="${requisitionItem?.productPackage}">
                                    (${requisitionItem?.quantity} ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity})
                                </g:if>
                                <g:else>
                                    (${requisitionItem?.quantity} EA)
                                </g:else>
                                <span class="fade">
                                    ${picklistItem?.inventoryItem?.product?.getInventoryLevel(session?.warehouse?.id)?.binLocation}
                                </span>


                            </h2>
                            <table>

                                <thead>
                                <tr>
                                    <th colspan="3" class="center no-border-bottom border-right">
                                        ${warehouse.message(code: 'inventory.availableItems.label', default: 'Available items')}
                                    </th>
                                    <th colspan="4" class="center no-border-bottom">
                                        ${warehouse.message(code: 'picklist.picklistItems.label')}
                                    </th>
                                </tr>
                                <tr>
                                    <th>
                                        ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
                                    </th>
                                    <th>
                                        ${warehouse.message(code: 'inventoryItem.expirationDate.label')}
                                    </th>
                                    <th class="center border-right">
                                        ${warehouse.message(code: 'requisitionItem.quantityAvailable.label')}
                                    </th>
                                    <th class="center">
                                        ${warehouse.message(code: 'picklistItem.quantity.label')}
                                    </th>
                                    <th class="center">
                                        ${warehouse.message(code: 'product.uom.label')}
                                    </th>
                                    <th>
                                        ${warehouse.message(code: 'default.actions.label')}
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:set var="inventoryItems" value="${productInventoryItemsMap[requisitionItem?.product?.id]?.findAll { it.quantity > 0 }}"/>
                                <g:unless test="${inventoryItems}">
                                    <tr style="height: 60px;">
                                        <td colspan="7" class="center middle">
                                            <span class="fade">${warehouse.message(code: 'requisitionItem.noInventoryItems.label', default: 'No available items')}</span>
                                        </td>
                                    </tr>
                                </g:unless>
                                <g:each var="inventoryItem" in="${inventoryItems}" status="status">
                                    <g:set var="picklistItem" value="${inventoryItemMap[inventoryItem]?.first()}"/>
                                    <g:set var="quantityPicked" value="${inventoryItemMap[inventoryItem]?.first()?.quantity ?: 0}"/>
                                    <g:set var="quantityRemaining" value="${requisitionItem?.calculateQuantityRemaining()?: 0}"/>
                                    <tr class="prop ${status % 2 ? 'odd' : 'even'}">
                                        <td class="middle">
                                            <span class="lotNumber">${inventoryItem?.lotNumber?:warehouse.message(code:'default.none.label')}</span>
                                        </td>
                                        <td class="middle">
                                            <g:if test="${inventoryItem?.expirationDate}">
                                                <g:formatDate
                                                        date="${inventoryItem?.expirationDate}"
                                                        format="d MMM yyyy"/>
                                            </g:if>
                                            <g:else>
                                                <span class="fade"><warehouse:message code="default.never.label"/></span>
                                            </g:else>
                                        </td>
                                        <td class="middle center border-right">
                                            ${inventoryItem?.quantity ?: 0}
                                            ${inventoryItem?.product?.unitOfMeasure?:"EA"}
                                        </td>
                                        <td class="middle center">
                                            <g:hiddenField name="picklistItems[${status}].id" value="${picklistItem?.id}"/>
                                            <g:hiddenField name="picklistItems[${status}].requisitionItem.id" value="${picklistItem?.requisitionItem?.id?:requisitionItem?.id}"/>
                                            <g:hiddenField name="picklistItems[${status}].inventoryItem.id" value="${picklistItem?.inventoryItem?.id?:inventoryItem?.id}"/>
                                            <input name="picklistItems[${status}].quantity" value="${quantityPicked}" size="5" type="text" class="text"/>
                                        </td>
                                        <td class="middle center">
                                            ${inventoryItem?.product?.unitOfMeasure ?: "EA"}
                                        </td>
                                        <td>
                                            <g:if test="${picklistItem}">
                                                <g:link controller="picklistItem"
                                                        action="delete"
                                                        id="${picklistItem?.id}"
                                                        class="button icon remove"
                                                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                    <warehouse:message code="picklist.removeFromPicklistItems.label" default="Remove from picklist"/>
                                                </g:link>
                                            </g:if>
                                        </td>
                                    </tr>

                                </g:each>
                                </tbody>
                                <g:if test="${inventoryItems}">
                                    <tfoot>
                                    <tr>
                                        <td colspan="7" class="center">
                                            <g:if test="${requisitionItem?.retrievePicklistItems()}">
                                                <button class="button">
                                                    ${warehouse.message(code: 'picklist.updatePicklistItems.label', default:'Update picklist items')}
                                                </button>
                                            </g:if>
                                            <g:else>
                                                <button class="button">
                                                    ${warehouse.message(code: 'picklist.addToPicklistItems.label', default:'Add to picklist items')}
                                                </button>

                                            </g:else>
                                        </td>
                                    </tr>
                                    </tfoot>
                                </g:if>
                            </table>
                        </g:form>

                    </div>

                </div>
            </div>



        </g:if>
    </td>
    <td>
        <div class="${isCanceled?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
            <format:metadata obj="${requisitionItem.status}"/>
        </div>
    </td>
    <td>
        ${requisitionItem?.product?.productCode}
    </td>
    <td>
        <g:link controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]">
            <format:product product="${requisitionItem?.product}"/>
        </g:link>
    </td>
    <td>
        <g:if test="${requisitionItem?.productPackage}">
            ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
        </g:if>
        <g:else>
            ${requisitionItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
        </g:else>
    </td>
    <td class="center">
        ${requisitionItem?.quantity?:0 }
    </td>
    <td class="center">
        ${requisitionItem?.calculateQuantityPicked()?:0 }
    </td>
    <td class="center">
        ${requisitionItem?.quantityCanceled?:0}
    </td>
    <td class="center">
        ${requisitionItem?.calculateQuantityRemaining()?:0 }
    </td>
    <td class="center">
        ${requisitionItem.orderIndex}
    </td>
</tr>

