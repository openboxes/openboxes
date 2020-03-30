<%@ page import="org.pih.warehouse.api.StockMovementType" %>
<div class="box">
    <h2>
        ${entityName} &rsaquo;
        <warehouse:message code="enum.StockMovementType.${params.direction}"/>
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
            <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                <g:sortableColumn property="status" params="${pageParams}"
                                  title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
            </g:if>
            <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.INBOUND}">
                <th>
                    <warehouse:message code="receiving.status.label"/>
                </th>
            </g:if>
            <g:sortableColumn property="requestNumber" params="${pageParams}"
                              title="${warehouse.message(code: 'stockMovement.identifier.label', default: 'Stock movement number')}" />

            <th><g:message code="default.name.label"/></th>
            <th><g:message code="stockMovement.origin.label"/></th>
            <th><g:message code="stockMovement.destination.label"/></th>
            <th><g:message code="stockMovement.stocklist.label"/></th>


            <g:sortableColumn property="requestedBy" params="${pageParams}"
                              title="${warehouse.message(code: 'stockMovement.requestedBy.label', default: 'Requested by')}" />

            <g:sortableColumn property="dateRequested" params="${pageParams}"
                              title="${warehouse.message(code: 'stockMovement.dateRequested.label', default: 'Date requested')}" />

            <th><g:message code="default.dateCreated.label"/></th>

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
                    <div class="count">${stockMovement?.lineItems?.size()?:0}</div>
                </td>
                <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                    <td>
                        <label class="status"><format:metadata obj="${stockMovement?.status}"/></label>
                    </td>
                </g:if>
                <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.INBOUND}">
                    <td>
                        <label class="status"><format:metadata obj="${stockMovement?.shipment?.status}"/></label>
                    </td>
                </g:if>
                <td>
                    <g:link controller="stockMovement" action="show" id="${stockMovement.id}">
                        <strong>${stockMovement.identifier }</strong>
                    </g:link>
                </td>
                <td>
                    <g:link controller="stockMovement" action="show" id="${stockMovement.id}">
                        <div title="${stockMovement.name}">${stockMovement.description}</div>
                    </g:link>
                </td>
                <td>
                    ${stockMovement?.origin?.name}
                </td>
                <td>
                    ${stockMovement?.destination?.name}
                </td>
                <td>
                    ${stockMovement?.stocklist?.name?:"N/A"}
                </td>
                <td>
                    ${stockMovement.requestedBy?:warehouse.message(code:'default.noone.label')}
                </td>
                <td>
                    <g:formatDate format="MMM dd, yyyy" date="${stockMovement?.dateRequested}"/>
                </td>
                <td>
                    <g:formatDate format="MMM dd, yyyy" date="${stockMovement?.dateCreated}"/>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="paginateButtons">
        <g:paginate total="${totalCount}" controller="stockMovement" action="list" max="${params.max}"
                    params="${pageParams.findAll {it.value}}"/>

    </div>
</div>
