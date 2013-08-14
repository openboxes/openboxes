<div class="page-content">
    <table id="requisition-items" class="fs-repeat-header">
        <thead>
        <tr>
            <th colspan="9">${pageTitle}</th>
        </tr>
        <tr>
            <th><warehouse:message code="report.number.label"/></th>
            <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
            <th>${warehouse.message(code: 'product.label')}</th>
            <th class="center border-right">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
            <th class="center">${warehouse.message(code: 'requisitionItem.quantityPicked.label')}</th>
            <th class="center">${warehouse.message(code:'requisition.checkedBy.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:unless test="${requisitionItems}">
            <tr>
                <td colspan="10" class="middle center">
                    <span class="fade">
                        <warehouse:message code="default.none.label"/>
                    </span>
                </td>
            </tr>

        </g:unless>
        <g:each in="${requisitionItems?.sort()}" status="i" var="requisitionItem">
            <g:if test="${picklist}">
                <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}"/>
                <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
            </g:if>
            <g:else>
            <%--<g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}"/>--%>
                <g:set var="numInventoryItem" value="${1}"/>
            </g:else>
            <g:set var="j" value="${0}"/>
            <g:while test="${j < numInventoryItem}">
                <tr class="prop">
                    <td class=" middle center">
                        <g:if test="${j==0}">
                            ${i + 1}
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${j==0}">
                            <span class="product-code">
                                ${requisitionItem?.product?.productCode}
                            </span>
                        <%--
                        <g:if test="${requisitionItem?.parentRequisitionItem}">
                            ${requisitionItem?.parentRequisitionItem?.product?.productCode}
                        </g:if>
                        --%>
                        </g:if>
                    </td>
                    <td class="middle">
                        <g:if test="${j==0}">
                            ${requisitionItem?.product?.name}
                        </g:if>
                    </td>
                    <td class="center middle">
                        <g:if test="${j==0}">
                            ${requisitionItem?.quantity ?: 0}
                            ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </g:if>
                    </td>
                    <td class="center middle">
                        <g:set var="binLocation" value="${requisitionItem?.product?.getInventoryLevel(location?.id)?.binLocation}"/>
                        <g:each in="${binLocation?.split(';')}" var="binLocationPart">
                            ${binLocationPart}<br/>
                        </g:each>
                    </td>
                    <td class="middle center" width="25%">
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
                    <td class="center middle">
                    </td>
                    <% j++ %>
                </tr>
            </g:while>
        </g:each>
        </tbody>
    </table>
    <%--
    <p><warehouse:message code="requisitionItem.comment.label"/>:</p>
    <div id="comment-box">
        <!--Empty comment box -->
    </div>
    --%>

</div>