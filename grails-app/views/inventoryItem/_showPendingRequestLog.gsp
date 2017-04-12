<div class="box">
    <h2><warehouse:message code="stockCard.pendingRequisitions.label" default="Pending requisitions"/></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${commandInstance?.product?.id }"/>
        <table>
            <thead>
                <tr class="odd">
                    <th style="width: 10%;">
                        ${warehouse.message(code: 'default.date.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'requisition.requestNumber.label')}
                    </th>
                    <th style="width: 20%;">
                        ${warehouse.message(code: 'default.name.label')}
                    </th>
                    <th style="width: 15%;">
                        ${warehouse.message(code: 'requisition.origin.label')}
                    </th>
                    <th style="width: 15%;">
                        ${warehouse.message(code: 'requisition.destination.label')}
                    </th>
                    <th style="width: 10%; text-align: center;">
                        ${warehouse.message(code: 'requisition.quantity.label')}
                    </th>
                    <th style="width: 15%; text-align: center;">
                        ${warehouse.message(code: 'requisition.status.label')}
                    </th>
                </tr>

            </thead>
            <tbody>
                <g:each var="entry" in="${commandInstance?.requisitionMap}" status="status">
                    <g:set var="requestInstance" value="${entry.key }"/>


                    <tr class="${(status%2==0)?'even':'odd' } prop">
                        <td style="width: 10%;" nowrap="nowrap">
                            <g:if test="${requestInstance?.dateRequested }">
                                <g:formatDate date="${requestInstance.dateRequested }" format="dd/MMM/yyyy"/>
                            </g:if>
                        </td>
                        <td>
                            ${requestInstance?.requestNumber}
                        </td>
                        <td>
                            <g:link controller="requisition" action="show" id="${requestInstance?.id }">
                                ${requestInstance?.name }
                            </g:link>
                        </td>
                        <td>
                            ${requestInstance?.origin?.name }
                        </td>
                        <td>
                            ${requestInstance?.destination?.name }
                        </td>
                        <td class="center">
                            ${entry.value} ${commandInstance?.product?.unitOfMeasure}
                        </td>
                        <td class="center">
                            ${requestInstance.status }
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!commandInstance?.requisitionMap}">
                    <tr>
                        <td colspan="8" class="even center">
                            <div class="fade empty">
                                <warehouse:message code="requisition.empty.label" default="No pending requisitions"/>
                            </div>
                        </td>
                    </tr>
                </g:if>
            </tbody>
        </table>
    </g:form>
</div>
