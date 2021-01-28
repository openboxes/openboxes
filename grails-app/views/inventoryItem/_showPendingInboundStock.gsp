<div class="box dialog">
    <h2><warehouse:message code="stockCard.pending.label" args="[g.message(code: 'default.inbound.label')]" /></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${product?.id }"/>
        <table>
            <thead>
                <tr class="odd">
                    <th/>
                    <th>
                        ${warehouse.message(code: 'default.type.label')}
                    </th>
                    <th class="center">
                        ${warehouse.message(code: 'stockCard.number.label')}
                    </th>
                    <th class="center">
                        ${warehouse.message(code: 'default.description.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'requisition.origin.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'default.status.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'order.orderDate.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'shipping.shipDate.label')} or
                        <div style="color: darkgrey">
                            ${warehouse.message(code: 'shipping.expectedShippingDate.label')}
                        </div>
                    </th>
                    <th>
                        ${warehouse.message(code: 'stockCard.purchasedNotShipped.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'stockCard.shippedNotReceived.label')}
                    </th>
                </tr>

            </thead>
            <tbody>
                <g:each var="entry" in="${itemsMap}" status="status">
                    <g:set var="item" value="${entry.key }"/>
                    <g:set var="shipmentType" value="${entry.value.type=='Purchase Order' ? '' : item?.shipmentType}"
                    />

                    <tr class="${(status%2==0)?'even':'odd' } prop">
                        <td><g:getShipmentTypeIcon shipmentType="${shipmentType}" /></td>
                        <td style="width: 10%;" nowrap="nowrap">
                            ${entry.value["type"]}
                        </td>
                        <td class="center">
                            <g:if test="${entry.value['type']=='Stock Movement'}">
                                <g:link controller="stockMovement" action="show" id="${item?.id}">
                                    ${item?.shipmentNumber}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="order" action="show" id="${item?.order?.id}">
                                    ${item?.order?.orderNumber}
                                </g:link>
                            </g:else>
                        </td>
                        <td class="center" style="word-break: break-word;">
                            <g:if test="${entry.value['type']=='Stock Movement'}">
                                <g:link controller="stockMovement" action="show" id="${item?.id}">
                                    ${item?.name }
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="order" action="show" id="${item?.order?.id}">
                                    ${item?.order?.name}
                                </g:link>
                            </g:else>
                        </td>
                        <td>
                            ${entry.value['type']=='Stock Movement' ? item?.origin?.name : item?.order?.origin?.name}
                        </td>
                        <td>
                            ${entry.value['type']=='Stock Movement' ? item?.currentStatus : item?.order?.status}
                        </td>
                        <td>
                            <g:if test="${entry.value['type']=='Purchase Order'}">
                                <format:date obj="${item?.order?.dateOrdered}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:formatDate date="${entry.value['shipDate']}" format="dd/MMM/yyyy"/>
                        </td>
                        <td>
                            <g:if test="${entry.value['quantityPurchased']}">
                                ${entry.value['quantityPurchased']} <warehouse:message code="default.ea.label" default="EACH"/>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${entry.value['quantityRemaining']}">
                                ${entry.value['quantityRemaining']} ${product?.unitOfMeasure}
                            </g:if>
                        </td>
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
                <td colspan="8"></td>
                <td>
                    ${itemsMap.values()["quantityPurchased"].sum()} <warehouse:message code="default.ea.label" default="EACH"/>
                </td>
                <td>
                    ${itemsMap.values()["quantityRemaining"].sum()} ${product?.unitOfMeasure}
                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
