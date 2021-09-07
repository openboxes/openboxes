<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="stockMovements.outbound.label" default="Stock Movements (Outbound)"/></title>
</head>

<body>

<g:hasErrors bean="${commandInstance}">
    <div class="errors">
        <g:renderErrors bean="${commandInstance}" as="list" />
    </div>
</g:hasErrors>
<div class="clearfix">
    <button type="button" class="btn btn-outline-primary float-end"
            data-bs-toggle="modal" data-bs-target="#outboundModal"><i class="fa fa-file-import"></i> Import Orders</button>
</div>

<div class="row g-0">
    <div class="col">
        <table class="table table-borderless table-striped">
            <thead>
                <tr>
                    <th><g:message code="stockMovement.status.label"/></th>
                    <th><g:message code="stockMovement.identifier.label"/></th>
                    <th><g:message code="stockMovement.destination.label"/></th>
                    <th><g:message code="stockMovement.requestedDeliveryDate.label" default="Requested Delivery Date"/></th>
                    <th><g:message code="stockMovement.expectedDeliveryDate.label" default="Expected Delivery Date"/></th>
                    <th><g:message code="stockMovement.trackingNumber.label" /></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
            <g:each var="stockMovement" in="${stockMovements}">
                <tr>
                    <td>
                        <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                            <g:if test="${stockMovement?.shipment?.currentEvent?.eventType?.eventCode}">
                                <div class="badge bg-primary">
                                    ${stockMovement.shipment?.currentEvent?.eventType?.eventCode}
                                </div>
                                <div>
                                    <small><g:formatDate date="${stockMovement.shipment?.currentEvent?.eventDate}" format="MMM dd hh:mm a"/></small>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="badge bg-primary">${stockMovement?.status}</div>
                            </g:else>
                        </a>
                    </td>
                    <td>
                        <a href="${createLink(controller: 'mobile', action: 'outboundDetails', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                            ${stockMovement.identifier}
                        </a>
                    </td>
                    <td>
                        ${stockMovement?.destination?.name} (${stockMovement?.destination?.locationNumber})
                    </td>
                    <td>
                        <g:formatDate date="${stockMovement?.requisition?.requestedDeliveryDate}" format="dd MMM yyyy"/>
                    </td>
                    <td>
                        <g:formatDate date="${stockMovement?.expectedDeliveryDate}" format="dd MMM yyyy"/>
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
                            <a href="${createLink(controller: 'mobile', action: 'outboundDetails', id: stockMovement?.id)}" class="btn btn-outline-primary">
                                Details
                            </a>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <div class="pagination">
            <g:paginate total="${stockMovements.totalCount}"/>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="outboundModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <g:uploadForm class="needs-validation" action="importData" enctype="multipart/form-data">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="staticBackdropLabel">Create Outbound Order</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input name="type" type="hidden" value="outbound"/>
                    <g:hiddenField name="location.id" value="${session.warehouse.id }"/>
                    <input class="form-control" type="file" name="xlsFile" required>
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
