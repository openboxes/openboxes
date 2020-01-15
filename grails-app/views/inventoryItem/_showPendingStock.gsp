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
                            ${warehouse.message(code: 'default.type.label')}
                        </g:else>
                    </th>
                    <th class="center">
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.status.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'stockCard.number.label')}
                        </g:else>
                    </th>
                    <th class="center">
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'default.code.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'default.description.label')}
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'default.name.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'requisition.origin.label')}
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.origin.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'default.status.label')}
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.destination.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'order.orderDate.label')}
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${params.type=='OUTBOUND'}">
                            ${warehouse.message(code: 'requisition.quantityRequested.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code: 'shipping.shipDate.label')} or
                            <div style="color: darkgrey">
                                ${warehouse.message(code: 'shipping.expectedShippingDate.label')}
                            </div>
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
                            ${warehouse.message(code: 'stockCard.purchasedNotShipped.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'stockCard.shippedNotReceived.label')}
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
                                ${entry.value["type"]}
                            </g:else>
                        </td>
                        <td class="center">
                            <g:if test="${params.type=='OUTBOUND'}">
                                ${item?.status}
                            </g:if>
                            <g:else>
                                <g:if test="${entry.value['type']=='Stock Movement'}">
                                    <g:link controller="stockMovement" action="show" id="${item?.requisition.id}">
                                        ${item?.shipmentNumber}
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="order" action="show" id="${item?.id}">
                                        ${item?.orderNumber}
                                    </g:link>
                                </g:else>
                            </g:else>
                        </td>
                        <td class="center">
                            <g:if test="${params.type=='OUTBOUND'}">
                                <g:link controller="stockMovement" action="show" id="${item?.id}">
                                    ${item?.requestNumber}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:if test="${entry.value['type']=='Stock Movement'}">
                                    <g:link controller="stockMovement" action="show" id="${item?.requisition.id}">
                                        ${item?.name }
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="order" action="show" id="${item?.id}">
                                        ${item?.name}
                                    </g:link>
                                </g:else>
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${params.type=='OUTBOUND'}">
                                <g:link controller="stockMovement" action="show" id="${item?.id}">
                                    ${item?.name}
                                </g:link>
                            </g:if>
                            <g:else>
                                ${item?.origin?.name}
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${params.type=='OUTBOUND'}">
                                ${item?.origin?.name}
                            </g:if>
                            <g:else>
                                ${entry.value['type']=='Stock Movement' ? item?.currentStatus : item?.status}
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${params.type=='OUTBOUND'}">
                                ${item?.destination?.name }
                            </g:if>
                            <g:else>
                                <g:if test="${entry.value['type']=='Purchase Order'}">
                                    <format:date obj="${item?.dateOrdered}"/>
                                </g:if>
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${params.type=='OUTBOUND'}">
                                ${entry.value["quantityRequested"]} ${product?.unitOfMeasure}
                            </g:if>
                            <g:else>
                                <g:if test="${entry.value['type']=='Stock Movement'}">
                                    <g:formatDate date="${item.expectedShippingDate }" format="dd/MMM/yyyy"/>
                                </g:if>
                            </g:else>
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
                                <g:if test="${entry.value['quantityPurchased']}">
                                    ${entry.value['quantityPurchased']} ${product?.unitOfMeasure}
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${entry.value['quantityRemaining']}">
                                    ${entry.value['quantityRemaining']} ${product?.unitOfMeasure}
                                </g:if>
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
                <g:if test="${params.type=='OUTBOUND'}">
                <td>
                    ${itemsMap.values()["quantityRequested"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityRequired"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityPicked"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
                <g:if test="${params.type=='INBOUND'}">
                <td>
                </td>
                <td>
                    ${itemsMap.values()["quantityPurchased"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${itemsMap.values()["quantityRemaining"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
