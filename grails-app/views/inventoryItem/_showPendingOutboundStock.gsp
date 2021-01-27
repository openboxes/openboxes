<div class="box dialog">
    <h2><warehouse:message code="stockCard.pending.label" args="[g.message(code: 'default.outbound.label')]" /></h2>

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
                    ${warehouse.message(code: 'requisition.origin.label')}
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
                    ${warehouse.message(code: 'requisition.quantityPicked.label')}
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
                        ${item?.status}
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
                        ${item?.origin?.name}
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
                        ${entry.value["quantityPicked"]} ${product?.unitOfMeasure}
                    </td>
                </tr>
            </g:each>
            <g:if test="${!itemsMap}">
                <tr>
                    <g:set var="colspan" value="9"/>
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
                <td colspan="7"></td>
                <td>
                    ${itemsMap.values()["quantityRequested"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityRequired"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityPicked"].sum()} ${product?.unitOfMeasure}
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
