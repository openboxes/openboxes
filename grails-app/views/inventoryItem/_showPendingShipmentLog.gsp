<div class="box">
    <h2><warehouse:message code="stockCard.pendingShipments.label" default="Pending shipments"/></h2>

    <g:form method="GET" action="showStockCard">
        <g:hiddenField name="product.id" value="${commandInstance?.product?.id }"/>
        <table>
            <thead>
                <tr class="odd">
                    <th>
                        ${warehouse.message(code: 'default.date.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'shipping.shipmentNumber.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'default.name.label')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'shipping.origin.label', default: 'Origin')}
                    </th>
                    <th>
                        ${warehouse.message(code: 'shipping.destination.label', default: 'Destination')}
                    </th>
                    <th class="center">
                        ${warehouse.message(code: 'shipping.quantity.label', default: 'Quantity')}
                    </th>
                    <th class="center">
                        ${warehouse.message(code: 'shipping.status.label', default: 'Status')}
                    </th>
                </tr>

            </thead>
            <!--  Shipment Log -->
            <tbody>
                <g:set var="anyPendingShipments" value="${false }"/>
                <g:each var="entry" in="${commandInstance?.shipmentMap}" status="status">
                    <g:set var="shipment" value="${entry.key }"/>

                    <g:set var="anyPendingShipments" value="${true }"/>
                    <tr class="${(status%2==0)?'even':'odd' } prop">
                        <td nowrap="nowrap">
                            <g:if test="${shipment?.expectedShippingDate }">
                                <g:formatDate date="${shipment.expectedShippingDate }" format="dd/MMM/yyyy"/>
                            </g:if>
                        </td>
                        <td>
                            <g:link controller="shipment" action="showDetails" id="${shipment?.id }">
                                ${shipment.shipmentNumber}
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="shipment" action="showDetails" id="${shipment?.id }">
                                ${shipment?.name }
                            </g:link>
                        </td>
                        <td>
                            ${shipment?.origin?.name }
                        </td>
                        <td>
                            ${shipment?.destination?.name }
                        </td>
                        <td class="center">
                            ${entry.value} ${commandInstance?.product?.unitOfMeasure}
                        </td>
                        <td class="center">
                            ${shipment.status }
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!anyPendingShipments }">
                    <tr>
                        <td colspan="7" class="even center">
                            <div class="fade empty">
                                <warehouse:message code="shipment.noPendingShipments.label"/>
                            </div>
                        </td>
                    </tr>
                </g:if>
            </tbody>
        </table>
    </g:form>

</div>
