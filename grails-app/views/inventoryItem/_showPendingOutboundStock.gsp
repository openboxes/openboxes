<div class="box dialog">
    <h2><warehouse:message code="stockCard.pendingOutbound.label" default="Pending Outbound"/></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${product?.id }"/>
        <table>
            <thead>
            <tr class="odd">
                <th/>
                <th>
                    ${warehouse.message(code: 'requisition.date.label')}
                </th>
                <th class="center">
                    ${warehouse.message(code: 'requisition.status.label')}
                </th>
                <th class="center">
                    ${warehouse.message(code: 'default.code.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.name.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'requisition.destination.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'requisition.quantityRequested.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'requisition.quantityRequired.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'requisition.quantityAllocated.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'requisition.quantityPicked.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.lotSerialNo.label')}
                </th>
            </tr>

            </thead>
            <tbody>
            <g:each var="entry" in="${itemsMap}" status="status">
                <g:set var="item" value="${entry.key }"/>
                <g:set var="shipmentType" value="${item?.shipment?.shipmentType}"
                />

                <tr class="${(status%2==0)?'even':'odd' } prop">
                    <td><g:getShipmentTypeIcon shipmentType="${shipmentType}" /></td>
                    <td style="width: 10%;" nowrap="nowrap">
                        <g:if test="${item?.dateRequested }">
                            <g:formatDate date="${item.dateRequested }" format="dd/MMM/yyyy"/>
                        </g:if>
                    </td>
                    <td class="center">
                        <format:metadata obj="${item?.status}" />
                    </td>
                    <td class="center">
                        <g:link controller="stockMovement" action="show" id="${item?.id}">
                            ${item?.requestNumber}
                        </g:link>
                    </td>
                    <td style="word-break: break-word;">
                        <g:link controller="stockMovement" action="show" id="${item?.id}">
                            ${item?.name}
                        </g:link>
                    </td>
                    <td>
                        ${item?.destination?.name }
                    </td>
                    <td>
                        ${entry.value["quantityRequested"]} ${product?.unitOfMeasure}
                    </td>
                    <td>
                        ${entry.value["quantityRequired"]} ${product?.unitOfMeasure}
                    </td>
                    <td>
                        ${entry.value["quantityAllocated"] ?: 0} ${product?.unitOfMeasure}
                    </td>
                    <g:if test="${entry.value.picklistItemsByLot}">
                        <g:set var="lotEntries" value="${entry.value.picklistItemsByLot.entrySet().toList()}"/>
                        <g:set var="firstLot" value="${lotEntries[0]}"/>
                        <td>
                            ${firstLot.value.quantity.sum()} ${product?.unitOfMeasure}
                        </td>
                        <td>
                            ${firstLot.key}
                        </td>
                    </g:if>
                    <g:else>
                        <td>
                            0 ${product?.unitOfMeasure}
                        </td>
                        <td></td>
                    </g:else>
                </tr>
                <g:if test="${entry.value.picklistItemsByLot && entry.value.picklistItemsByLot.size() > 1}">
                    <g:set var="lotEntries" value="${entry.value.picklistItemsByLot.entrySet().toList()}"/>
                    <g:each var="piEntry" in="${lotEntries[1..-1]}" status="index">
                        <tr class="${(status%2==0)?'even':'odd' } prop">
                            <td colspan="9"></td>
                            <td>
                                ${piEntry.value.quantity.sum()} ${product?.unitOfMeasure}
                            </td>
                            <td>
                                ${piEntry.key}
                            </td>
                        </tr>
                    </g:each>
                </g:if>
            </g:each>
            <g:if test="${!itemsMap}">
                <tr>
                    <g:set var="colspan" value="11"/>
                    <td colspan="${colspan}" class="even center">
                        <div class="fade empty">
                            <warehouse:message code="stockMovements.empty.label" default="No pending stock movements"/>
                        </div>
                    </td>
                </tr>
            </g:if>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="6"></td>
                <td>
                    ${itemsMap.values()["quantityRequested"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityRequired"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityAllocated"].sum() ?: 0} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["picklistItemsByLot"]*.values()["quantity"]?.flatten()?.sum() ?: 0} ${product?.unitOfMeasure}
                </td>
                <td></td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
