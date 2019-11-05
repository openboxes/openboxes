<div class="box dialog">

    <g:set var="stockMovementTypeCode" value="enum.StockMovementType.${params.type}"/>
    <h2><warehouse:message code="stockCard.pending.label" args="[g.message(code: stockMovementTypeCode)]" /></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${product?.id }"/>
        <table>
            <thead>
                <tr class="odd">
                    <th>
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.date.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'shipping.dateShipped.label')}
                        </g:else>
                    </th>
                    <th class="center">
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.status.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'shipping.shipmentStatus.label')}
                        </g:else>
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
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.quantityRequested.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'shipping.shipped.label')}
                        </g:else>
                    </th>
                    <g:if test="${params.type=='OUTBOUND'}">
                        <th>
                            ${warehouse.message(code: 'requisition.quantityRequired.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'requisition.quantityPicked.label')}
                        </th>
                    </g:if>
                    <g:if test="${params.type=='INBOUND'}">
                        <th>
                            ${warehouse.message(code: 'requisition.quantityReceived.label')}
                        </th>
                    </g:if>
                </tr>

            </thead>
            <tbody>
                <g:each var="entry" in="${itemsMap}" status="status">
                    <g:set var="item" value="${entry.key }"/>


                    <tr class="${(status%2==0)?'even':'odd' } prop">
                        <td style="width: 10%;" nowrap="nowrap">
                            <g:if test="${params.type=='OUTBOUND'}">
                                <g:if test="${item?.dateRequested }">
                                    <g:formatDate date="${item.dateRequested }" format="dd/MMM/yyyy"/>
                                </g:if>
                            </g:if>
                            <g:else>
                                <g:if test="${item?.expectedShippingDate }">
                                    <g:formatDate date="${item.expectedShippingDate }" format="dd/MMM/yyyy"/>
                                </g:if>
                            </g:else>
                        </td>
                        <td class="center">
                            ${params.type=='OUTBOUND' ? item?.status : item?.currentStatus}
                        </td>
                        <td class="center">
                            <g:link controller="stockMovement" action="show" id="${params.type=='OUTBOUND' ? item?.id : item?.requisition.id }">
                                ${params.type=='OUTBOUND' ? item?.requestNumber : item?.shipmentNumber }
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="stockMovement" action="show" id="${params.type=='OUTBOUND' ? item?.id : item?.requisition.id }">
                                ${item?.name }
                            </g:link>
                        </td>
                        <td>
                            ${item?.origin?.name }
                        </td>
                        <td>
                            ${item?.destination?.name }
                        </td>
                        <td>
                            ${entry.value["quantityRequested"]} ${product?.unitOfMeasure}
                        </td>
                        <g:if test="${params.type=='OUTBOUND'}">
                            <td>
                                ${entry.value["quantityRequired"]} ${product?.unitOfMeasure}
                            </td>
                            <td>
                                ${entry.value["quantityPicked"]} ${product?.unitOfMeasure}
                            </td>
                        </g:if>
                        <g:if test="${params.type=='INBOUND'}">
                            <td>
                                ${entry.value["quantityReceived"]} ${product?.unitOfMeasure}
                            </td>
                        </g:if>
                    </tr>
                </g:each>
                <g:if test="${!itemsMap}">
                    <tr>
                        <g:set var="colspan" value="${params.type=='INBOUND'?8:9}"/>
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
                <td colspan="6">

                </td>
                <td>
                    ${itemsMap.values()["quantityRequested"].sum()} ${product?.unitOfMeasure}
                </td>
                <g:if test="${params.type=='OUTBOUND'}">
                <td>
                    ${itemsMap.values()["quantityRequired"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityPicked"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
                <g:if test="${params.type=='INBOUND'}">
                <td>
                    ${itemsMap.values()["quantityReceived"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
