%{--<%
    import org.pih.warehouse.inventory.StockMovementStatusCode;
%>--}%
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.pih.warehouse.inventory.StockMovementStatusCode; org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="default.inbound.label" default="Inbound"/></title>
</head>

<body>

<g:if test="${flash.command}">
    <div class="row g-0">
        <div class="col">
            <div class="alert alert-danger">
                <g:renderErrors bean="${flash.command}" as="list"/>
            </div>
        </div>
    </div>
</g:if>

<div class="row g-0 mb-2">
    <div class="col col-md-12">
        <button type="button" class="btn btn-outline-primary float-end"
            data-bs-toggle="modal" data-bs-target="#inboundModal"><i class="fa fa-file-import"></i> Import Orders</button>
    </div>
</div>
<div class="row g-0">
    <div class="col">
        <g:set var="pageParams" value="${pageScope.variables['params']}"/>
        <g:form controller="mobile" action="inboundList" method="GET">
            <table class="table table-borderless table-striped">
                <thead>
                    <tr>
                        <th class="col-2"><g:message code="stockMovement.status.label" default="Status"/></th>
                        <th class="col-2"><g:message code="requisition.orderNumber.label"/></th>
                        <th class="col-4"><g:message code="stockMovement.origin.label"/></th>
                        <g:sortableColumn property="requestedDeliveryDate" title="${warehouse.message(code: 'stockMovement.requestedDeliveryDate.label', default: 'Requested')}" defaultOrder="desc" params="${pageParams}"/>
                        <g:sortableColumn property="expectedDeliveryDate" title="${warehouse.message(code: 'stockMovement.expectedDeliveryDate.label', default: 'Expected')}" defaultOrder="desc" params="${pageParams}"/>
                        <th><g:message code="stockMovement.trackingNumber.label" /></th>
                        <th></th>
                    </tr>
                    <tr>
                        <th>
                            <g:select name="status" class="form-select"
                                      from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
                                      optionKey="name"
                                      optionValue="${format.metadata(obj:it)}"
                                      value="${params.status}"
                                      noSelection="['':warehouse.message(code:'default.all.label')]" />
                        </th>
                        <th>
                            <g:textField name="identifier" value="${params.identifier}" class="form-control" size="3"/>
                        </th>
                        <th>
                            <g:selectLocation id="origin" name="originId" value="${params?.originId}"
                                  class="form-control"
                                  noSelection="['null':warehouse.message(code:'default.all.label')]"/>
                        </th>
                        <th>
                            <g:textField name="requestedDeliveryDateFilter" class="date-filter form-control" size="10"
                                         value="${params.requestedDeliveryDateFilter}"/>
                        </th>
                        <th>
                            <g:textField name="expectedDeliveryDateFilter" class="date-filter form-control" size="10"
                                         value="${params.expectedDeliveryDateFilter}"/>
                        </th>
                        <th class="col-1 text-center">
                            <button type="submit" class="btn btn-primary"><i class="fa fa-filter"></i> Filter</button>
                        </th>
                    </tr>

                </thead>
                <tbody>
                <g:each var="stockMovement" in="${stockMovements}">
                    <tr>
                        <td class="text-center">
                            <g:if test="${stockMovement?.shipment?.currentStatus}">
                                <div class="badge bg-primary">
                                    ${stockMovement?.shipment?.currentStatus}
                                </div>
                                <p class="small text-muted">
                                    <g:if test="${stockMovement?.shipment?.currentEvent?.eventDate}">
                                        <g:formatDate date="${stockMovement?.shipment?.currentEvent?.eventDate}" format="MMM dd hh:mm a"/>
                                    </g:if>
                                    <g:else>
                                        <g:formatDate date="${stockMovement?.shipment?.lastUpdated}" format="MMM dd hh:mm a"/>
                                    </g:else>
                                </p>
                            </g:if>
                        </td>
                        <td>
                            <a href="${createLink(controller: 'mobile', action: 'inboundDetails', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                                ${stockMovement.identifier}
                            </a>
                        </td>
                        <td>
                            ${stockMovement?.origin?.name}
                        </td>
                        <td>
                            <g:formatDate date="${stockMovement?.requestedDeliveryDate}" format="dd MMM yyyy"/>
                        </td>
                        <td>
                            <div><g:formatDate date="${stockMovement?.expectedDeliveryDate}" format="dd MMM yyyy"/></div>
                            <small><g:formatDate date="${stockMovement?.expectedDeliveryDate}" format="hh:mm a"/></small>
                        </td>
                        <td>
                            <g:if test="${stockMovement?.trackingNumber}">
                                <g:link url="${stockMovement.trackingUri}">${stockMovement?.trackingNumber}</g:link>
                            </g:if>
                            <g:else>
                                Not Available
                            </g:else>
                        </td>
                        <td>
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                <a href="${createLink(controller: 'mobile', action: 'inboundDetails', id: stockMovement?.id)}" class="btn  btn-outline-primary">
                                    Details
                                </a>
                            </div>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
            <g:unless test="${stockMovements}">
                <div class="text-center text-muted">
                    There are no inbound stock movements matching that criteria.
                </div>
            </g:unless>
        </g:form>
        <div class="pagination">
            <g:pagination total="${stockMovements.totalCount}" params="${pageParams}"/>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="inboundModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <g:uploadForm class="needs-validation" action="importData" enctype="multipart/form-data">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="createInboundBack">Create Inbound Order</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <g:hiddenField name="type" value="inbound"/>
                    <g:hiddenField name="location.id" value="${session.warehouse.id }"/>
                    <input class="form-control" type="file" name="xlsFile[]" multiple required>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    <button type="submit" class="btn btn-primary">Upload</button>
                </div>
            </div>
        </div>
    </g:uploadForm>
</div>

</body>
</html>
