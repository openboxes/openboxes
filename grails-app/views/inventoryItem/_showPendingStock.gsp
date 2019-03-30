<div class="box dialog">

    <g:set var="stockMovementTypeCode" value="enum.StockMovementType.${params.type}"/>
    <h2><warehouse:message code="stockCard.pending.label" args="[g.message(code: stockMovementTypeCode)]" /></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${product?.id }"/>
        <table>
            <thead>
                <tr class="odd">
                    <th>
                        ${warehouse.message(code: 'default.date.label')}
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
                <g:each var="entry" in="${requisitionMap}" status="status">
                    <g:set var="requisition" value="${entry.key }"/>


                    <tr class="${(status%2==0)?'even':'odd' } prop">
                        <td style="width: 10%;" nowrap="nowrap">
                            <g:if test="${requisition?.dateRequested }">
                                <g:formatDate date="${requisition.dateRequested }" format="dd/MMM/yyyy"/>
                            </g:if>
                        </td>
                        <td class="center">
                            ${requisition.status }
                        </td>
                        <td class="center">
                            ${requisition?.requestNumber}
                        </td>
                        <td>
                            <g:link controller="requisition" action="show" id="${requisition?.id }">
                                ${requisition?.name }
                            </g:link>
                        </td>
                        <td>
                            ${requisition?.origin?.name }
                        </td>
                        <td>
                            ${requisition?.destination?.name }
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
                <g:if test="${!requisitionMap}">
                    <tr>
                        <td colspan="7" class="even center">
                            <div class="fade empty">
                                <warehouse:message code="requisition.empty.label" default="No pending requisitions"/>
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
                    ${requisitionMap.values()["quantityRequested"].sum()} ${product?.unitOfMeasure}
                </td>
                <g:if test="${params.type=='OUTBOUND'}">
                <td>
                    ${requisitionMap.values()["quantityRequired"].sum()} ${product?.unitOfMeasure}
                </td>
                <td>
                    ${requisitionMap.values()["quantityPicked"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
                <g:if test="${params.type=='INBOUND'}">
                <td>
                    ${requisitionMap.values()["quantityReceived"].sum()} ${product?.unitOfMeasure}
                </td>
                </g:if>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
