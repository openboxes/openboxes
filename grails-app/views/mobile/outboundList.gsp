<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="stockMovements.outbound.label" default="Stock Movements (Outbound)"/></title>
</head>

<body>

    <div class="row g-0">
        %{-- table-stack turns each row into a label/value card: five columns
             of text cannot be a grid on a phone. The labels come from
             data-label on each cell, so the header row is hidden but the
             column names still travel with the data. table-bordered stays so
             the un-themed layout keeps its borders; the themed stylesheet
             neutralizes it when stacking. --}%
        <table class="table table-bordered table-stack">
            <thead>
                <tr>
                    <th><g:message code="stockMovement.status.label"/></th>
                    <th><g:message code="stockMovement.identifier.label"/></th>
                    <th><g:message code="stockMovement.destination.label"/></th>
                    <th><g:message code="stockMovement.requestedDeliveryDate.label" default="Requested Delivery Date"/></th>
                    <th><g:message code="default.actions.label"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each var="stockMovement" in="${stockMovements}">
                <tr>
                    <td data-label="${g.message(code: 'stockMovement.status.label')}">
                        <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                            ${stockMovement?.status}
                        </a>
                    </td>
                    <td data-label="${g.message(code: 'stockMovement.identifier.label')}">
                        <a href="${createLink(controller: 'mobile', action: 'stockMovementDetails', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                            ${stockMovement.identifier}
                        </a>
                    </td>
                    <td data-label="${g.message(code: 'stockMovement.destination.label')}">
                        ${stockMovement?.destination?.name} ${stockMovement?.destination?.locationNumber}
                    </td>
                    <td data-label="${g.message(code: 'stockMovement.requestedDeliveryDate.label', default: 'Requested Delivery Date')}">
                        <g:formatDate date="${stockMovement?.requisition?.requestedDeliveryDate}" format="dd MMM yyyy"/>
                    </td>
                    %{-- was a <button> nested inside an <a>, which is invalid
                         and made the whole cell an ambiguous tap target --}%
                    <td class="cell-action">
                        <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}" class="btn btn-primary">
                            <g:message code="default.button.view.label" default="View"/>
                            <i class="fa fa-chevron-right"></i>
                        </a>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <div class="paginateButtons">
            <g:paginate total="${stockMovements.totalCount}"/>
        </div>
    </div>
</body>
</html>
