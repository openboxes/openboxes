<%@page import="org.pih.warehouse.inventory.LotStatusCode" %>
<g:set var="shipmentInstance" value="${stockMovement?.shipment}"/>
<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
<div id="picklist" class="box dialog">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'text_list_numbers.png')}" alt="contents" style="vertical-align: middle"/>
        ${warehouse.message(code:'picklist.label')}
        <g:if test="${picklist?.picker}">
            <small>Assigned to ${picklist?.picker?.name}</small>
        </g:if>
    </h2>
    <table>
        <tr>
            <th></th>
            <th><warehouse:message code="picklistItem.status.label" default="Status"/></th>
            <th><warehouse:message code="product.productCode.label"/></th>
            <th><warehouse:message code="product.label"/></th>
            <th><warehouse:message code="location.label"/></th>
            <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
            <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
            <th><warehouse:message code="picklistItem.picker.label" default="Picker"/></th>
            <th class="center"><warehouse:message code="picklistItem.quantity.label" default="Required"/></th>
            <th class="center"><warehouse:message code="picklistItem.quantityPicked.label" default="Picked"/></th>
            <th class="center"><warehouse:message code="picklistItem.quantityCanceled.label" default="Canceled"/></th>
            <th class="center"><warehouse:message code="picklistItem.quantityRemaining.label" default="Remaining"/></th>
            <th class="center"><warehouse:message code="picklistItem.shortage.label" default="Shortage"/></th>
            <th><warehouse:message code="picklistItem.reasonCode.label" default="Shortage Reason"/></th>
        </tr>

        <g:set var="picklistItems" value="${stockMovement?.requisition?.picklist?.picklistItems?.groupBy {it.requisitionItem }}"/>


        <g:each var="requisitionItem" in="${picklistItems?.keySet()}" status="i">
            <g:each var="picklistItem" in="${picklistItems[requisitionItem]}" status="j">
                <tr class="${(i % 2)?'odd':'even'}">
                    <td>
                        <span class="action-menu">
                            <button class="action-btn">
                                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"/>
                            </button>
                            <div class="actions">
                                <div class="action-menu-item">
                                    <g:link controller="picklistItem" action="delete" id="${picklistItem?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"/>
                                        &nbsp;<warehouse:message code="default.delete.label" default="Delete {0}" args="[g.message(code: 'picklist.picklistItem.label')]"/>
                                    </g:link>
                                </div>
                                <div class="action-menu-item">
                                    <g:link controller="picklistItem" action="undo" id="${picklistItem?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_undo.png')}"/>
                                        &nbsp;<warehouse:message code="default.undo.label" default="Undo {0}" args="[g.message(code: 'picklist.picklistItem.label')]"/>
                                    </g:link>
                                </div>
                            </div>
                        </span>

                    </td>
                    <td>
                        <g:if test="${picklistItem?.shortage && picklistItem?.quantityPicked > 0}"><div class="tag tag-warning">Shorted</div></g:if>
                        <g:elseif test="${picklistItem?.shortage && picklistItem?.quantityPicked == 0}"><div class="tag tag-warning">Canceled</div></g:elseif>
                        <g:elseif test="${picklistItem?.quantityPicked > 0 && picklistItem?.quantityRemaining > 0}"><div class="tag tag-warning">Partially Picked</div></g:elseif>
                        <g:elseif test="${picklistItem?.quantityPicked > 0 && picklistItem?.quantityRemaining == 0}"><div class="tag tag-success">Picked</div></g:elseif>
                        <g:elseif test="${picklistItem?.quantityPicked == 0 && picklistItem?.quantityRemaining > 0}"><div class="tag tag-info">Ready</div></g:elseif>
                    </td>
                    <td>
                        <g:link controller="inventoryItem" action="showStockCard" id="${picklistItem?.inventoryItem?.product?.id}">
                            ${picklistItem?.inventoryItem?.product?.productCode}
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="inventoryItem" action="showStockCard" id="${picklistItem?.inventoryItem?.product?.id}">
                            ${picklistItem?.inventoryItem?.product?.name}
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${picklistItem?.binLocation}">
                            <g:link controller="location" action="edit" id="${picklistItem?.binLocation?.id}">
                                ${picklistItem?.binLocation?.name}
                            </g:link>
                        </g:if>
                        <g:else>
                            ${g.message(code:'default.label')}
                        </g:else>

                    </td>
                    <td>
                        ${picklistItem?.inventoryItem?.lotNumber?:g.message(code:'default.label')}
                    </td>
                    <td>
                        ${picklistItem?.inventoryItem?.expirationDate}
                    </td>
                    <td>
                        ${picklistItem?.picker?.name}
                    </td>
                    <td width="5%" class="center">
                        ${picklistItem?.quantity}
                    </td>
                    <td width="5%" class="center">
                        ${picklistItem?.quantityPicked}
                    </td>
                    <td width="5%" class="center">
                        ${picklistItem?.quantityCanceled}
                    </td>
                    <td width="5%" class="center">
                        ${picklistItem?.quantityRemaining}
                    </td>
                    <td width="5%" class="center">
                        ${picklistItem?.shortage}
                    </td>
                    <td>
                        <g:if test="${picklistItem?.reasonCode}">
                            <g:message code="enum.ReasonCode.${picklistItem?.reasonCode}"/>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </table>
    <g:unless test="${stockMovement?.requisition.picklist?.picklistItems}">
        <div class="empty fade center">
            <g:message code="default.noItems.label"/>
        </div>
    </g:unless>
</div>
