<%@ page import="org.pih.warehouse.api.StockMovementDirection" %>
<div class="box">
    <h2>
        ${entityName} &rsaquo;
        <g:if test="${params.sourceType}">
            <warehouse:message code="requests.label"/>
        </g:if>
        <g:elseif test="${params.direction}">
            <warehouse:message code="enum.StockMovementDirection.${params.direction}"/>
        </g:elseif>
        (${totalCount?:0})
    </h2>
    <table>
        <thead>
        <tr>
            <th>
                <warehouse:message code="default.actions.label"/>
            </th>
            <th>
                <warehouse:message code="default.numItems.label"/>
            </th>
            <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
                <g:sortableColumn property="status" params="${pageParams}"
                                  title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
            </g:if>
            <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
                <th>
                    <warehouse:message code="receiving.status.label"/>
                </th>
            </g:if>
            <g:sortableColumn property="requestNumber" params="${pageParams}"
                              title="${warehouse.message(code: 'stockMovement.identifier.label', default: 'Stock movement number')}" />

            <th><g:message code="default.name.label"/></th>
            <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
            <th><g:message code="stockMovement.origin.label"/></th>
            </g:if>
            <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
            <th><g:message code="stockMovement.destination.label"/></th>
            </g:if>
            <th><g:message code="stockMovement.stocklist.label"/></th>


            <g:sortableColumn property="requestedBy" params="${pageParams}"
                              title="${warehouse.message(code: 'stockMovement.requestedBy.label', default: 'Requested by')}" />

            <g:if test="${params.direction && params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
                <g:sortableColumn property="dateRequested" params="${pageParams}"
                                  title="${warehouse.message(code: 'stockMovement.dateRequested.label', default: 'Date requested')}" />
            </g:if>

            <th><g:message code="default.dateCreated.label"/></th>

            <g:if test="${params.direction && params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
                <th><g:message code="stockMovement.expectedReceiptDate.message"/></th>
            </g:if>

        </tr>
        </thead>
        <tbody>
        <g:unless test="${stockMovements}">
            <tr class="prop odd">
                <td colspan="11" class="center">
                    <div class="empty">
                        <warehouse:message code="default.noItems.label"/>
                    </div>
                </td>
            </tr>
        </g:unless>
        <g:each in="${stockMovements}" status="i" var="stockMovement">
            <g:set var="requisition" value="${stockMovement.requisition}"/>
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td class="middle center">
                    <g:render template="/stockMovement/actions" model="[stockMovement:stockMovement]"/>
                </td>
                <td>
                    <div class="count">${stockMovement?.lineItemCount}</div>
                </td>
                <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
                    <td>
                        <label class="status"><format:metadata obj="${stockMovement?.status}"/></label>
                    </td>
                </g:if>
                <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
                    <td>
                        <label class="status"><format:metadata obj="${stockMovement?.shipment?.status}"/></label>
                    </td>
                </g:if>
                <td>
                    <g:link
                        controller="${stockMovement?.order ? "stockTransfer" : "stockMovement" }"
                        action="show"
                        id="${stockMovement?.order?.id ?: stockMovement?.id}"
                    >
                        <strong>${stockMovement.identifier }</strong>
                    </g:link>
                </td>
                <td>
                    <g:link
                        controller="${stockMovement?.order ? "stockTransfer" : "stockMovement" }"
                        action="show"
                        id="${stockMovement?.order?.id ?: stockMovement?.id}"
                    >
                        <g:if test="${stockMovement?.hasProperty('stockMovementType')}">
                            <format:metadata obj="${stockMovement?.stockMovementType}"/> &rsaquo;
                        </g:if>
                        <span title="${stockMovement.name}">${stockMovement.description?:stockMovement?.name}</span>
                    </g:link>
                </td>
                <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
                <td>
                    ${stockMovement?.origin?.name}
                </td>
                </g:if>
                <g:if test="${!params.direction || params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
                <td>
                    ${stockMovement?.destination?.name}
                </td>
                </g:if>
                <td>
                    ${stockMovement?.stocklist?.name?:"None"}
                </td>
                <td>
                    ${stockMovement.requestedBy?:warehouse.message(code:'default.noone.label')}
                </td>
                <g:if test="${params.direction && params.direction as StockMovementDirection == StockMovementDirection.OUTBOUND}">
                    <td>
                        <g:formatDate format="MMM dd, yyyy" date="${stockMovement?.dateRequested}"/>
                    </td>
                </g:if>
                <td>
                    <g:formatDate format="MMM dd, yyyy" date="${stockMovement?.dateCreated}"/>
                </td>
                <g:if test="${params.direction && params.direction as StockMovementDirection == StockMovementDirection.INBOUND}">
                    <td>
                        <g:formatDate format="MMM dd, yyyy" date="${stockMovement?.expectedDeliveryDate}"/>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="paginateButtons">
        <g:paginate total="${totalCount}" controller="stockMovement" action="list" max="${params.max}"
                    params="${pageParams.findAll {it.value}}"/>

    </div>
</div>
