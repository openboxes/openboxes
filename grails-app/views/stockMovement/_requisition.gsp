<div id="details-tab">
    <div class="box">
        <h2><warehouse:message code="requisition.label"/></h2>

        <div>
            <table>

                <thead>
                <tr class="odd">
                    <th></th>
                    <th></th>
                    <th><warehouse:message code="requisition.status.label"/></th>
                    <th><warehouse:message code="product.label" /></th>
                    <th class="center"><warehouse:message code="product.uom.label" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityRequested.label" default="Requested" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityApproved.label" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityPicked.label" default="Picked"/></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityRemaining.label" /></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="requisitionItem" in="${stockMovement?.requisition?.originalRequisitionItems.sort()}" status="i">
                    <g:render template="../requisition/showRequisitionItem" model="[i:i,requisitionItem:requisitionItem]"/>

                </g:each>
                </tbody>


                <%--
                <g:each var="stockMovementItem" in="${stockMovement.lineItems}" status="i">
                <g:set var="requisitionItem" value="${stockMovementItem?.requisitionItem}"/>
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td class="center">
                        ${i+1}
                    </td>
                    <td class="">
                        <div class="tag tag-success">
                            <g:if test="${requisitionItem?.isSubstituted()}">
                                <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                            </g:if>
                            <g:elseif test="${requisitionItem?.isSubstitution()}">
                                <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                            </g:elseif>
                            <g:elseif test="${requisitionItem?.isChanged()}">
                                <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                            </g:elseif>
                            <g:elseif test="${requisitionItem?.isCanceled()}">
                                <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                            </g:elseif>
                            <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                                <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                            </g:elseif>
                            <g:elseif test="${requisitionItem?.isPending()}">
                                <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                            </g:elseif>
                            <format:metadata obj="${requisitionItem?.status}"/>
                        </div>
                    </td>
                    <td>
                        <g:link controller="inventoryItem" action="showStockCard" id=${stockMovementItem?.product?.id}">
                            ${stockMovementItem?.product?.productCode}
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="inventoryItem" action="showStockCard" id="${stockMovementItem?.product?.id}">
                            ${stockMovementItem?.product?.name}
                        </g:link>
                    </td>
                    <td class="center">
                        ${stockMovementItem?.quantityRequested?:0}
                        ${stockMovementItem.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                    </td>

                </tr>
                </g:each>
                --%>


            </table>

        </div>
    </div>
</div>