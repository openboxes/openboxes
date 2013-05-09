<div class="page" style="page-break-after: always;">
    <table id="requisition-items" class="fs-repeat-header" border="0">
        <tr class="theader">
            <th></th>
            <th><warehouse:message code="report.number.label"/></th>
            <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
            <th>${warehouse.message(code: 'product.label')}</th>
            <th class="center border-right">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
            <th class="center">${warehouse.message(code: 'requisitionItem.quantityPicked.label')}</th>
            <th width="10%" class="center">${warehouse.message(code:'picklistItem.picked.label', default: 'Picked')}</th>
        </tr>
        <g:each in="${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() } }" status="i" var="requisitionItem">
            <g:if test="${picklist}">
                <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}"/>
                <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
            </g:if>
            <g:else>
                <%--
                <g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}"/>
                --%>
                <g:set var="numInventoryItem" value="${1}"/>
            </g:else>
            <g:set var="j" value="${0}"/>
            <g:while test="${j < numInventoryItem}">
                <tr class="prop">
                    <td class="middle center">
                        <g:if test="${requisitionItem?.product?.coldChain}">
                            <img src="${resource(dir: 'images/icons/', file: 'coldchain.gif')}"/>
                        </g:if>
                        <g:elseif test="${requisitionItem?.product?.controlledSubstance}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'error.png')}" title="Controlled substance"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.product?.hazardousMaterial}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'information.png')}" title="Hazardous material"/>
                        </g:elseif>
                    </td>
                    <td class=" middle center">${i + 1}</td>
                    <td class="middle center">
                        <span class="product-code">${requisitionItem?.product?.productCode}</span>
                    </td>
                    <%--
                    <td>
                        <g:if test="${requisitionItem?.product?.images }">
                            <div class="nailthumb-container">
                                <g:set var="image" value="${requisitionItem?.product?.images?.sort()?.first()}"/>
                                <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" />
                            </div>
                        </g:if>
                        <g:else>
                            <div class="nailthumb-container">
                                <img src="${resource(dir: 'images', file: 'default-product.png')}" />
                            </div>
                        </g:else>
                    </td>
                    --%>
                    <td class="middle">${requisitionItem?.product?.name}</td>
                    <td class="center middle">
                        ${requisitionItem?.quantity ?: 0}
                        ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                    </td>
                    <td class="center middle" style="width: 150px;">
                        <g:set var="binLocation" value="${requisitionItem?.product?.getInventoryLevel(session.warehouse.id)?.binLocation}"/>
                        <g:each in="${binLocation?.split(';')}" var="binLocationPart">
                            ${binLocationPart}<br/>
                        </g:each>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            <span class="lotNumber">${picklistItems[j]?.inventoryItem?.lotNumber}</span>
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            <g:formatDate date="${picklistItems[j]?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            ${picklistItems[j]?.quantity ?: 0}
                            ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </g:if>
                    </td>
                    <td>


                    </td>
                    <% j++ %>
                </tr>
            </g:while>
        </g:each>
    </table>
    <p><warehouse:message code="requisitionItem.comment.label"/>:</p>
    <div id="comment-box">
        <!--Empty comment box -->
    </div>
</div>