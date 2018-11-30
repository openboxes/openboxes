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
                        ${warehouse.message(code: 'requisition.quantity.label')}
                    </th>
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
                        <td class="center">
                            ${entry.value} ${product?.unitOfMeasure}
                        </td>
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
                <td class="center">
                    ${requisitionMap.values().sum()} ${product?.unitOfMeasure}

                </td>
            </tr>
            </tfoot>
        </table>
    </g:form>
</div>
