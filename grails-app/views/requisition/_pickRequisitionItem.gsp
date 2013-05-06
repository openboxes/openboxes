<g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
<g:set var="noneSelected" value="${!selectedRequisitionItem }"/>
<g:if test="${selected}">
    <tr class="odd selected-top" id="selected-requisition-item">
        <th>
            <a name="${requisitionItem?.id}"></a>
        </th>
        <th>
            ${warehouse.message(code: 'product.label')}
        </th>
        <th>
            ${warehouse.message(code: 'product.unitOfMeasure.label')}
        </th>
        <th class="center">
            ${warehouse.message(code: 'requisitionItem.quantityRequested.label')}
        </th>
        <th class="center">
            ${warehouse.message(code: 'requisitionItem.quantityPicked.label')}
        </th>
        <th class="center">
            ${warehouse.message(code: 'requisitionItem.quantityCanceled.label')}
        </th>
        <th class="center">
            ${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}
        </th>
        <th>
            ${warehouse.message(code: 'requisitionItem.status.label')}
        </th>
        <th>
            %
        </th>
        <th>
            ${warehouse.message(code: 'default.actions.label')}
        </th>

    </tr>
</g:if>


<tr class="${selected ?'selected-middle':'unselected'} ${i%2?'odd':'even'} prop">
    <%--
    <td>
        <g:render template="../requisitionItem/actions" model="[requisitionItem:requisitionItem]"/>
    </td>
    --%>
    <td>
        <g:if test="${requisitionItem.parentRequisitionItem}">
            <img src="${createLinkTo(dir: 'images/icons', file: 'indent.gif')}"/>
        </g:if>
        <g:else>
            <div class="count ${selected?'active':''}">${i+1 }</div>
        </g:else>

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
    <td>
        <g:set var="value" value="${((requisitionItem?.calculateQuantityPicked()?:0)+(requisitionItem?.quantityCanceled?:0))/(requisitionItem?.quantity?:1) * 100 }" />
        <div id="progress-bar-${requisitionItem?.id }" class="progress-bar" style="width: 100px;"></div>
        <script type="text/javascript">
            $(function() {
                $( "#progress-bar-${requisitionItem?.id }" ).progressbar({value: ${value}});
            });
        </script>
    </td>
    <td>
        ${value }%
    </td>
    <td>
        <%--
        <a href="javascript:void(-1);" data-id="${requisitionItem?.id}" class="button open-dialog">
            ${warehouse.message(code:'requisitionItem.process.label', default: 'Process') }
        </a>
        --%>
        <g:if test="${requisitionItem?.isCanceled()||requisitionItem?.isChanged()}">
        </g:if>
        <g:else>
            <g:link class="button" data-id="${requisitionItem?.id}" controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]" fragment="${requisitionItem.id}">
                ${warehouse.message(code:'requisitionItem.process.label', default: 'Process') }
            </g:link>
        </g:else>




    </td>
</tr>
<g:if test="${selectedRequisitionItem == requisitionItem}">
<tr class="selected-bottom">

<td colspan="10" >

<g:if test="${selectedRequisitionItem}">
<div>

<%--
    <h2>
        <label class="bottom">
            <format:product
                    product="${selectedRequisitionItem?.product}"/>
        </label>
        <div class="button-group right top">

            <g:set var="prevRequisitionItem" value="${selectedRequisitionItem?.previousRequisitionItem}"/>
            <g:set var="nextRequisitionItem" value="${selectedRequisitionItem?.nextRequisitionItem}"/>

        </div>
        <div class="clear"></div>
        <table border="0" class="requisition-details">
            <tr>
                <td>
                    <label>Requested</label>

                    <p>${selectedRequisitionItem.quantity ?: 0}
                        ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}</p>
                </td>
                <td>
                    <label>Picked</label>

                    <p>${selectedRequisitionItem.calculateQuantityPicked() ?: 0}
                        ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}</p>
                </td>
                <td>
                    <label>Canceled</label>

                    <p>${selectedRequisitionItem.quantityCanceled ?: 0}
                        ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}</p>
                </td>
                <td>
                    <label>Remaining</label>

                    <p>${selectedRequisitionItem.calculateQuantityRemaining() ?: 0}
                        ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}</p>
                </td>
            </tr>
        </table>


    </h2>
    --%>


<div class="tabs">
<ul>
    <li><a href="#tabs-picked"><warehouse:message
            code="requisitionItem.picked.label"
            default="Picked"/></a></li>
    <li><a href="#tabs-available"><warehouse:message
            code="requisitionItem.available.label"
            default="Available"/></a></li>
    <li><a href="#tabs-substitute"><warehouse:message
            code="requisitionItem.substitute.label"
            default="Substitute"/></a></li>
    <li><a href="#tabs-cancel"><warehouse:message
            code="requisitionItem.cancel.label"
            default="Cancel"/></a></li>
</ul>

<div id="tabs-picked" class="box" style="border: 1px solid lightgrey;">

    <h2>${warehouse.message(code:'requisition.pickedItems.label', default:'Picked items')}</h2>
    <table>
        <tr>
            <th>${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
            <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
            <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
            <th>${warehouse.message(code: 'picklistItem.quantity.label')}</th>
            <th></th>
        </tr>
        <g:each var="picklistItem"
                in="${selectedRequisitionItem?.retrievePicklistItems()}"
                status="status">
            <tr class="${status % 2 ? 'odd' : 'even'}">
                <td>
                    <span class="fade">
                        ${picklistItem?.inventoryItem?.product?.getInventoryLevel(session?.warehouse?.id)?.binLocation ?: "N/A"}
                    </span>
                </td>
                <td>
                    <span class="lotNumber">${picklistItem?.inventoryItem?.lotNumber}</span>
                </td>
                <td>
                    <g:formatDate
                            date="${picklistItem?.inventoryItem?.expirationDate}"
                            format="MMM yyyy"/>
                </td>
                <td class="right">
                    ${picklistItem?.quantity}
                    ${picklistItem?.requisitionItem?.product?.unitOfMeasure ?: "EA"}
                </td>
                <td>
                    <g:link controller="picklistItem"
                            action="delete"
                            id="${picklistItem?.id}"
                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${createLinkTo(dir: 'images/icons', file: 'trash.png')}"
                             alt="Delete"/>
                    </g:link>
                </td>
            </tr>
        </g:each>
        <g:unless
                test="${selectedRequisitionItem?.retrievePicklistItems()}">
            <tr style="height: 60px;">
                <td colspan="5" class='center middle'>
                    <span class="fade">${warehouse.message(code: 'requisitionItem.noPicklistItems.label')}</span>
                </td>
            </tr>
        </g:unless>
        <g:if test="${selectedRequisitionItem?.retrievePicklistItems()}">
            <tfoot>
            <tr>
                <th>
                    <warehouse:message code="default.total.label"/>
                </th>
                <th colspan="2">
                </th>
                <th colspan="2" class="right">
                    ${selectedRequisitionItem.calculateQuantityPicked()}
                    ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                </th>
            </tr>
            </tfoot>
        </g:if>
    </table>
</div>

<div id="tabs-available" class="box" style="border: 1px solid lightgrey;">

    <h2>${warehouse.message(code:'requisition.availableItems.label', default:'Available items')}</h2>

    <g:set var="inventoryItemMap"
           value="${selectedRequisitionItem?.retrievePicklistItems().groupBy { it.inventoryItem }}"/>
    <g:form action="addToPicklistItems">
        <g:hiddenField name="id" value="${requisition?.id}"/>
        <g:hiddenField name="requisitionItem.id"
                       value="${selectedRequisitionItem?.id}"/>

        <table >
            <thead>
            <tr>

                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.quantityOnHand.label')}</th>
                <th>${warehouse.message(code: 'requisition.quantityPicked.label')}</th>
                <th>${warehouse.message(code: 'picklistItem.quantity.label')}</th>
                <th>${warehouse.message(code: 'product.uom.label')}</th>
                <th></th>

            </tr>
            </thead>
            <tbody>
            <g:set var="inventoryItems"
                   value="${productInventoryItemsMap[selectedRequisitionItem?.product?.id]}"/>
            <g:unless test="${inventoryItems}">
                <tr style="height: 60px;">
                    <td colspan="7" class="center middle">
                        <span class="fade">${warehouse.message(code: 'requisitionItem.noInventoryItems.label', default: 'No available items')}</span>
                    </td>
                </tr>
            </g:unless>
            <g:each var="inventoryItem" in="${inventoryItems}"
                    status="status">
                <g:set var="picklistItem"
                       value="${inventoryItemMap[inventoryItem]?.first()}"/>
                <g:set var="quantity"
                       value="${inventoryItemMap[inventoryItem]?.first()?.quantity ?: 0}"/>

                <tr class="prop ${status % 2 ? 'odd' : 'even'}">
                    <td class="middle">
                        <span class="lotNumber">${inventoryItem?.lotNumber?:warehouse.message(code:'default.none.label')}</span>
                    </td>
                    <td class="middle">
                        <g:if test="${inventoryItem?.expirationDate}">
                            <g:formatDate
                                    date="${inventoryItem?.expirationDate}"
                                    format="MMM yyyy"/>
                        </g:if>
                        <g:else>
                            <warehouse:message code="default.never.label"/>

                        </g:else>
                    </td>
                    <td class="middle">
                        ${inventoryItem?.quantity ?: 0}
                    </td>
                    <td class="middle">
                        ${quantity ?: 0}
                    </td>
                    <td class="middle">
                        <g:if test="${picklistItem}">
                            <g:hiddenField
                                    name="picklistItems[${status}].id"
                                    value="${picklistItem.id}"/>
                        </g:if>
                        <g:hiddenField
                                name="picklistItems[${status}].requisitionItem.id"
                                value="${selectedRequisitionItem.id}"/>
                        <g:hiddenField
                                name="picklistItems[${status}].inventoryItem.id"
                                value="${inventoryItem.id}"/>
                        <input name="picklistItems[${status}].quantity"
                               value="${quantity}" size="5"
                               type="text" class="text"/>



                    </td>
                    <td>
                        ${inventoryItem?.product?.unitOfMeasure ?: "EA"}
                    </td>
                    <td>
                        <g:if test="${inventoryItem?.quantity > 0}">
                            <g:link controller="requisition" action="addToPicklistItems" id="${requisition?.id}"
                                    params="['requisitionItem.id':selectedRequisitionItem?.id, quantity: selectedRequisitionItem?.quantity]" class="button">

                                <warehouse:message code="requisition.addToPicklistItems.label"
                                                   default="Pick {0}" args="[selectedRequisitionItem?.quantity]"  />
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
                        <button class="button">
                            ${warehouse.message(code: 'default.button.save.label')}
                        </button>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </g:form>
</div>

<div id="tabs-substitute" class="box" style="border: 1px solid lightgrey;">

    <h2>${warehouse.message(code:'requisition.subsituteItems.label', default:'Substitute items')}</h2>
    <g:form controller="requisition" action="substitute">
        <g:autoSuggestSearchable id="searchable"
                                 name="searchable" width="100%"
                                 jsonUrl="${request.contextPath}/json/findInventoryItems"
                                 styleClass="text"/>
        <h4 id="productName"></h4>
        <g:hiddenField id="productId" name="product.id"
                       value=""/>
        <g:hiddenField id="inventoryItemId"
                       name="inventoryItem.id" value=""/>
        <g:hiddenField id="lotNumber" name="lotNumber"
                       value=""/>
        <g:hiddenField name="id" value="${requisition?.id}"/>
        <g:hiddenField name="requisitionItem.id"
                       value="${selectedRequisitionItem?.id}"/>

        <div id="substitutionItemDetails" style="display:none;">
            <table>
                <tr class="prop">
                    <td class="name">
                        <label>Lot Number</label>
                    </td>
                    <td>
                        <span id="lotNumberText"
                              class="lotNumber"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label>Expiration date</label>
                    </td>
                    <td>
                        <span id="expirationDateText"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label>Quantity on hand</label>
                    </td>
                    <td>
                        <span id="maxQuantityText"></span>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label>Quantity</label>
                    </td>
                    <td>
                        <g:textField id="quantity"
                                     name="quantity" value=""
                                     class="text" size="6"/>

                    </td>
                </tr>
                <tr>
                    <td>
                    </td>
                    <td>
                        <button class="button">
                            <warehouse:message
                                    code="default.button.add.label"/>
                        </button>
                    </td>
                </tr>
            </table>
        </div>
    </g:form>
</div>

<div id="tabs-cancel" class="box" style="border: 1px solid lightgrey;">
    <h2>${warehouse.message(code:'requisition.cancelItems.label', default: 'Cancel items')}</h2>

    <g:if test="${!selectedRequisitionItem.isCompleted()}">
        <g:form controller="requisitionItem" action="cancel">
            <g:hiddenField name="id"
                           value="${selectedRequisitionItem?.id}"/>
            <g:hiddenField name="requisition.id"
                           value="${requisition?.id}"/>

            <table>
                <tbody>
                <tr>
                    <td>
                        <label><warehouse:message code="requisitionItem.quantityCanceled.label" default="Amount to cancel"/></label>
                    </td>
                    <td class="middle">
                        <g:if test="${selectedRequisitionItem?.quantityCanceled}">
                            <g:textField name="quantityCanceled"
                                         value="${selectedRequisitionItem.quantityCanceled}"
                                         class="text right"
                                         size="6"/>
                            ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </g:if>
                        <g:else>
                            <g:textField name="quantityCanceled"
                                         value="${selectedRequisitionItem.calculateQuantityRemaining()}"
                                         class="text right"
                                         size="6"/>
                            ${selectedRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </g:else>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td>

                        <g:selectReasonCode name="cancelReasonCode"
                                            noSelection="['':'']" value='${selectedRequisitionItem?.cancelReasonCode}'/>


                    </td>
                </tr>
                <tr>
                    <td>
                        <label><warehouse:message code="requisitionItem.cancelComments.label" default="Additional comments"/></label>
                    </td>
                    <td>
                        <g:textArea name="cancelComments" value=""
                                    style="width: 100%"
                                    rows="3">${selectedRequisitionItem?.cancelComments}</g:textArea>

                    </td>
                </tr>
                <tr>
                    <td class="right">

                    </td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td>


                    </td>
                    <td class="left">

                        <button class="button">
                            <warehouse:message code="requisition.cancelQuantity.label" default="Cancel quantity"/>
                        </button>
                    </td>
                </tr>
                </tfoot>
            </table>
        </g:form>
    </g:if>
    <g:else>
        <div class="center">
            ${warehouse.message(code:'requisitionItem.fulfilled.message', default:'This item has been fulfilled/cancelled')}
        </div>
        <div class="center middle">
            <g:if test="${selectedRequisitionItem.quantityCanceled}">
                <g:link controller="requisitionItem"
                        action="uncancel" class="button"
                        id="${selectedRequisitionItem.id}">
                    Undo cancel of ${selectedRequisitionItem.quantityCanceled ?: 0}
                    ${selectedRequisition?.product?.unitOfMeasure ?: "EA"}
                </g:link>
            </g:if>
        </div>

    </g:else>
</div>
</div>

</div>
</g:if>

</td>
</tr>
</g:if>