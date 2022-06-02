<%@ page import="org.pih.warehouse.api.AvailableItemStatus; org.pih.warehouse.inventory.LotStatusCode" %>
<div class="box">
    <h2>
        <g:message code="inventory.currentStock.label" default="Current Stock"/>
        <small>${session.warehouse.name}</small>
    </h2>
    <table >
        <thead>
            <tr class="odd">
                <th class="left" style="">
                    <warehouse:message code="default.actions.label"/>
                </th>
                <th>
                    <warehouse:message code="inventoryItem.lotStatus.label" default="Status"/>
                </th>
                <th>
                    <warehouse:message code="inventory.binLocation.label" default="Bin Location"/>
                </th>
                <th>
                    <warehouse:message code="default.lotSerialNo.label"/>
                </th>
                <th>
                    <warehouse:message code="default.expires.label"/>
                </th>
                <th>
                    <warehouse:message code="stockCard.qtyOnHand.label"/>
                </th>
                <th>
                    <warehouse:message code="stockCard.qtyAllocated.label" default="Allocated"/>
                </th>
                <th>
                    <warehouse:message code="stockCard.qtyAvailable.label"/>
                </th>
                <th>
                    <warehouse:message code="stockMovements.label" default="Stock Movements"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <%-- FIXME The g:isSuperuser tag becomes expensive when executed within a for loop, so we should find a better way to implement it without this hack --%>
            <g:isSuperuser>
                <g:set var="isSuperuser" value="${true}"/>
            </g:isSuperuser>
            <g:each var="entry" in="${commandInstance.availableItems}" status="status">
                <g:set var="statusClass" value="${entry?.recalled ? 'recalled' : entry?.onHold ? 'restricted' : entry?.unavailable ? 'unavailable' : ''}"/>
                <g:set var="styleClass" value="${(status%2==0)?'even':'odd' } "/>
                <g:set var="title" value="${entry?.inventoryItem?.lotStatus == LotStatusCode.RECALLED ? warehouse.message(code: 'inventoryItem.recalledLot.label') : (entry?.onHold ?  warehouse.message(code: 'inventoryItem.restrictedBin.label') : '')}"/>
                <tr class="prop ${styleClass} ${statusClass}" title="${title}">
                    <td class="middle" style="text-align: left; width: 10%" nowrap="nowrap">
                        <g:render template="actionsCurrentStock"
                                  model="[commandInstance:commandInstance,binLocation:entry.binLocation,itemInstance:entry.inventoryItem,itemQuantity:entry.quantityOnHand,isSuperuser:isSuperuser]" />
                    </td>
                    <td>
                        <g:if test="${entry?.status in [AvailableItemStatus.PICKED, org.pih.warehouse.api.AvailableItemStatus.ALLOCATED]}">
                            <a href="javascript:void(0);" onclick="$('#showPendingOutboundTabLink').click();">
                                <warehouse:message code="stockCard.enum.AvailableItemStatus.${entry?.status}"/>
                            </a>
                        </g:if>
                        <g:else>
                            <warehouse:message code="stockCard.enum.AvailableItemStatus.${entry?.status}"/>
                        </g:else>
                        <div class="small">
                            ${entry?.inventoryItem?.lotStatus}
                        </div>
                    </td>
                    <td>
                        <div class="line">
                            <g:if test="${entry?.binLocation}">
                                <g:if test="${entry?.binLocation?.zone}">
                                    <span class="line-base" title="${entry?.binLocation?.zone?.name}">
                                        <g:link controller="location" action="edit" id="${entry.binLocation?.zone?.id}">
                                            ${entry?.binLocation?.zone?.name}
                                        </g:link>
                                    </span>&nbsp;&rsaquo;&nbsp;
                                </g:if>
                                <span class="line-extension" title="${entry?.binLocation?.name}">
                                    <g:link controller="location" action="edit" id="${entry.binLocation?.id}">${entry?.binLocation?.name}</g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <warehouse:message code="default.label" default="Default"/>
                            </g:else>
                        </div>
                    </td>
                    <td>
                        ${entry?.inventoryItem?.lotNumber?:"Default"}
                    </td>
                    <td>
                        <g:expirationDate date="${entry?.inventoryItem?.expirationDate}"/>
                    </td>
                    <td>
                        ${g.formatNumber(number: entry?.quantityOnHand, format: '###,###,###') }
                        ${entry?.inventoryItem?.product?.unitOfMeasure}
                    </td>
                    <td>
                        <g:if test="${entry?.quantityAllocated}">
                            ${g.formatNumber(number: entry?.quantityAllocated, format: '###,###,###') }
                            ${entry?.inventoryItem?.product?.unitOfMeasure}
                        </g:if>
                    </td>
                    <td>
                        ${g.formatNumber(number: entry?.quantityAvailable, format: '###,###,###') }
                        ${entry?.inventoryItem?.product?.unitOfMeasure}
                    </td>
                    <td>
                        <g:if test="${entry?.stockMovements}">
                            <g:each var="stockMovement" in="${entry?.stockMovements}">
<g:set var="tooltipMessage">
Identifier: ${stockMovement.identifier}
Destination: ${stockMovement.destination?.name}
Expected: ${g.formatDate(date: stockMovement?.expectedShippingDate)}
Status: ${stockMovement?.stockMovementStatusCode?.name}
Packing: ${stockMovement?.packingLocation?:'Unassigned'}
Loading: ${stockMovement?.loadingLocation?:'Unassigned'}
</g:set>
                                <g:link controller="stockMovement" action="show" id="${stockMovement?.identifier}" title="${tooltipMessage}">
                                    ${stockMovement?.identifier}
                                </g:link>
                                <br/>
                            </g:each>
                        </g:if>
                    </td>

                </tr>
            </g:each>
            <g:unless test="${commandInstance.availableItems}">
                <tr>
                    <td colspan="8">
                        <div class="fade empty center">
                            <warehouse:message code="inventory.noItemsCurrentlyInStock.message"
                                               args="[format.product(product:commandInstance?.product)]"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr class="odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
                <td colspan="5" class="right">
                    <!-- This space intentionally left blank -->
                </td>
                <td>
                    <div class="large">
                        <g:set var="styleClass" value="color: black;"/>
                        <g:if test="${commandInstance.totalQuantity < 0}">
                            <g:set var="styleClass" value="color: red;"/>
                        </g:if>
                        <span style="${styleClass }" id="totalQuantity">
                            ${g.formatNumber(number: commandInstance.totalQuantity, format: '###,###,###') }
                        </span>
                        <g:if test="${commandInstance?.product?.unitOfMeasure }">
                            <format:metadata obj="${commandInstance?.product?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.each.label') }
                        </g:else>
                    </div>
                </td>
                <td>
                    <div class="large">
                        <g:set var="totalQuantityAllocated" value="${commandInstance?.availableItems?.sum { it?.quantityAllocated?:0 }}"/>
                        ${g.formatNumber(number: totalQuantityAllocated, format: '###,###,###') }
                        <g:if test="${commandInstance?.product?.unitOfMeasure }">
                            <format:metadata obj="${commandInstance?.product?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.each.label') }
                        </g:else>
                    </div>
                </td>
                <td>
                    <div class="large">
                        <g:set var="styleClass" value="color: black;"/>
                        <g:if test="${commandInstance.totalQuantityAvailableToPromise < 0}">
                            <g:set var="styleClass" value="color: red;"/>
                        </g:if>
                        <span style="${styleClass }" id="totalQuantityAvailableToPromise">
                            ${g.formatNumber(number: commandInstance.totalQuantityAvailableToPromise, format: '###,###,###') }
                        </span>
                        <g:if test="${commandInstance?.product?.unitOfMeasure }">
                            <format:metadata obj="${commandInstance?.product?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.each.label') }
                        </g:else>
                    </div>
                </td>
                <td>
                    <!-- this space intentionally left blank -->
                </td>
                <g:hasErrors bean="${flash.itemInstance}">
                    <td style="border: 0px;">
                        &nbsp;
                    </td>
                </g:hasErrors>
            </tr>
        </tfoot>
    </table>
</div>
<g:javascript>
    $(document).ready(function() {
        $(".trigger-change").live('change', function(event) {
            var url = $(this).data("url");
            var target = $(this).data("target");
            $.ajax({
                url: url,
                data: { "id":  $(this).val(), "name": "otherBinLocation.id", value: $(this).val()},
                cache: false,
                success: function(html) {
                    $(target).html(html)
                },
                error: function(error) {
                    $(target).html(error)
                }
            });
        });

    });
</g:javascript>

