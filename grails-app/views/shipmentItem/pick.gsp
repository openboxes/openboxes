<g:set var="binLocationSelected" value="${binLocations.findAll{it?.binLocation == shipmentItem?.binLocation && it.inventoryItem==shipmentItem?.inventoryItem}}"/>
<div class="box">
    <h2>
        <g:message code="shipping.editPicklistItem.label" default="Edit Picklist Item"/>
    </h2>


    <div class="dialog">
        <g:form controller="createShipmentWorkflow" action="createShipment" params="[execution:params.execution]" autocomplete="off">
            <g:hiddenField name="id" value="${shipmentItem?.shipment?.id}" />
            <g:hiddenField name="shipmentItem.id" value="${shipmentItem?.id}" />
            <g:hiddenField name="version" value="${shipmentItem?.version}" />
            <g:hiddenField name="currentShipmentItemId" value="${shipmentItem?.id}"/>

            <table>
                <tr class="prop">
                    <td class="name">

                    </td>
                    <td class="value">
                        <div class="button-group">
                            <a href="#" class="previous-picklist-item button">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'previous-green.png')}" alt="Previous Item"/>&nbsp;
                                <g:message code="default.button.back.label" default="Back"/>
                            </a>
                            <a href="#" class="next-picklist-item button">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'next-green.png')}" alt="Next Item"/>&nbsp;
                                <g:message code="default.button.next.label" default="Next"/>
                            </a>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="default.status.label"/></label>
                    </td>
                    <td class="value">

                        <g:if test="${binLocations}">
                            <g:set var="totalQtyByProduct" value="${binLocations.sum { it.quantity }}"/>
                        </g:if>
                        <g:set var="totalQtyByBin" value="${binLocationSelected.sum { it.quantity }}"/>
                        <g:set var="availableInBin" value="${totalQtyByBin >= shipmentItem?.quantity}"/>
                        <g:set var="availableInProduct" value="${totalQtyByProduct >= shipmentItem?.quantity}"/>
                        <g:if test="${availableInBin}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
                                 title="${g.message(code:'picklist.picked.label')}">
                            ${g.message(code:'picklist.picked.message', default: 'Item has been picked')}
                        </g:if>
                        <g:elseif test="${availableInProduct}">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"
                                 title="${g.message(code:'picklist.notPicked.label', default: 'Not Picked')}">
                            ${g.message(code:'picklist.notAvailable.message', default: 'Item has not been picked')}
                        </g:elseif>
                        <g:else>
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}"
                                 title="${g.message(code:'picklist.notAvailable.label', default: 'Not Available')}">
                            ${g.message(code:'picklist.notAvailable.message', default: 'Insufficient quantity available for item')}
                        </g:else>

                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="product.label"/></label>
                    </td>
                    <td class="value">
                        ${shipmentItem?.inventoryItem?.product?.name}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="location.binLocation.label"/></label>
                    </td>
                    <td class="value" style="padding:0;margin:0;">

                        <g:if test="${binLocations}">

                            <div style="overflow-y: auto; max-height: 400px;">
                            <table id="tableBinLocations" >
                                <thead>
                                <tr>
                                    <th></th>
                                    <th><g:message code="default.bin.label" default="Bin"/></th>
                                    <th><g:message code="default.lot.label" default="Lot"/></th>
                                    <th><g:message code="default.exp.label" default="Exp"/></th>
                                    <th><g:message code="default.qty.label" default="Qty"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each var="entry" in="${binLocations}" status="status">
                                    <g:set var="statusClass" value="${entry.quantity>=shipmentItem?.quantity?'':''}"/>
                                    <g:set var="selected" value="${entry?.binLocation?.id == shipmentItem?.binLocation?.id &&
                                            entry?.inventoryItem?.id == shipmentItem?.inventoryItem?.id}"/>
                                    <g:set var="isSameLotNumber" value="${entry?.inventoryItem?.id==shipmentItem?.inventoryItem?.id}"/>

                                    <tr class="${selected?'active':''} ${statusClass}">
                                        <td class="top">
                                            <g:radio name="binLocationAndInventoryItem" value="${entry?.binLocation?.id}:${entry?.inventoryItem?.id}"
                                                     checked="${selected}"/>
                                        </td>
                                        <td class="middle">
                                            ${entry?.binLocation?.name?:g.message(code:'default.label')}
                                        </td>
                                        <td class="middle">
                                            <p class="${isSameLotNumber?'same-lot-number':'different-lot-number'}">
                                                ${entry?.inventoryItem?.lotNumber}
                                            </p>
                                        </td>
                                        <td class="middle">
                                            <g:formatDate date="${entry?.inventoryItem?.expirationDate}" format="MMM/yyyy"/>
                                        </td>
                                        <td class="middle">
                                            ${entry?.quantity}
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                                <tfoot>
                                <tr class="prop">
                                    <th colspan="4">
                                        <g:message code="default.total.label"/>
                                    </th>
                                    <th class="center">
                                        ${totalQtyByProduct?:0}
                                    </th>
                                </tr>
                                </tfoot>
                            </table>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="fade">
                                <g:message code="shipping.noBinLocation.label" default="There are no bin locations for item {0}"
                                           args="[shipmentItem?.inventoryItem?.lotNumber]"/>

                            </div>
                        </g:else>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><g:message code="default.quantity.label"/></label>
                    </td>
                    <td class="value">
                        <g:textField id="quantity" name="quantity" value="${shipmentItem?.quantity}" class="text"/>
                        <button name="_eventId_pickShipmentItem" class="button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" alt="Save Item"/>&nbsp;
                        <g:message code="default.button.save.label"/>
                        </button>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">

                    </td>
                    <td class="value">

                        <g:link controller="createShipmentWorkflow" action="createShipment" event="splitShipmentItem2" class="button"
                                id="${shipmentInstance?.id}" params="[execution:params.execution, 'shipmentItem.id': shipmentItem?.id]">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_divide.png')}" alt="Split Item"/>&nbsp;
                            <warehouse:message code="shipping.button.splitItem.label"/>
                        </g:link>

                        <g:link controller="createShipmentWorkflow" action="createShipment" event="deleteShipmentItem" class="button"
                                id="${shipmentInstance?.id}" params="[execution:params.execution, 'shipmentItem.id': shipmentItem?.id]"
                                onclick="return confirm('Are you sure you want to delete this item? NOTE: If this is a split item, quantity will not be returned to the original item.')">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="Delete Item"/>&nbsp;
                            <g:message code="shipping.button.deleteItem.label"/>
                        </g:link>
                        <%--
                        <g:link controller="createShipmentWorkflow" action="createShipment" event="nextShipmentItem"
                                id="${shipmentInstance?.id}" params="[execution:params.execution, 'shipmentItem.id': shipmentItem?.id]" class="button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'next_blue.png')}" alt="Next Item"/>&nbsp;
                            <g:message code="default.button.next.label" default="Next"/>
                        </g:link>
                        --%>
                    </td>
                </tr>
            </table>

        </g:form>
    </div>
</div>