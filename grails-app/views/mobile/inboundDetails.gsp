<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><g:message code="stockMovement.label"/></title>
</head>

<body>
    <g:hasErrors bean="${flash.command}">
        <div class="alert alert-danger">
            <g:renderErrors bean="${flash.command}" as="list" />
        </div>
    </g:hasErrors>
    <div class="card">
        <div class="card-header">
            <div class="row align-items-center">
                <div class="col-2 text-center">
                    <g:displayBarcode showData="${false}" data="${stockMovement.identifier}"/>
                    <span class="badge bg-secondary">${stockMovement.identifier}</span>
                </div>
                <div class="col-9 text-left">
                    <h5 class="ml-5">${stockMovement.name}</h5>
                    <div class="small text-muted">
                        Last updated by ${stockMovement.updatedBy?.name} on ${g.formatDate(date: stockMovement.shipment?.mostRecentEvent?.eventDate?:stockMovement.lastUpdated, type: "datetime")}
                    </div>
                </div>

                <div class="col-1">
                    <span class="dropdown float-end">
                        <button type="button" id="actionMenu" class="btn dropdown-toggle"
                                data-bs-toggle="dropdown"
                                aria-haspopup="true" aria-expanded="false">
                            <i class="fa fa-cog"></i>
                        </button>

                        <ul class="dropdown-menu">
                            <g:isSuperuser>
                                <li>
                                    <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}"
                                       class="dropdown-item">
                                        View Details
                                    </a>
                                </li>
                            </g:isSuperuser>
                            <li>
                                <a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#inboundModal">
                                    Import Order
                                </a>
                            </li>
                            <li>
                                <a href="${createLink(controller: 'mobile', action: 'exportData', id: stockMovement?.id)}"
                                   class="dropdown-item">
                                    Export Order
                                </a>
                            </li>
                            <div class="dropdown-divider"></div>
                            <a href="${createLink(controller: 'mobile', action: 'inboundDelete', id: stockMovement?.id)}"
                               class="dropdown-item text-danger">
                                Delete Order
                            </a>
                        </ul>
                    </span>
                </div>
            </div>
        </div>

        <div class="card-body">
            <div class="row">
                <div class="col-12 col-sm-2 col-md-4 text-center time-info mb-3 mt-sm-0">
                    <span class="text-muted d-block">Status</span>
                    <span class="text-5 font-weight-500 text-dark">
                        <div class="badge bg-primary">
                            <g:if test="${stockMovement?.shipment?.mostRecentEvent?.eventType}">
                                ${stockMovement.shipment?.mostRecentEvent?.eventType?.name?.toUpperCase()}
                            </g:if>
                            <g:else>
                                ${stockMovement?.status}
                            </g:else>
                        </div>
                    </span>
                </div>

                <div class="col-12 col-sm-2 col-md-4 text-center time-info mb-3 mt-sm-0">
                    <span class="text-muted d-block">Origin</span>
                    <span class="text-5 font-weight-500 text-dark">
                        ${stockMovement?.origin?.locationNumber}
                    </span>
                </div>
                <div class="col-12 col-sm-2 col-md-4 text-center mb-3 mt-sm-0">
                    <span class="text-muted d-block">Destination</span>
                    <span class="text-4 font-weight-500 text-dark">
                        ${stockMovement?.destination?.locationNumber}
                    </span>
                </div>
                <div class="col-12 col-sm-2 col-md-4 text-center mb-3">
                    <span class="text-muted d-block">Ordered On</span>
                    <span class="text-4 font-weight-500 text-dark mt-1 mt-lg-0">
                        ${g.formatDate(date: stockMovement.dateRequested, type: "datetime")}
                    </span>
                </div>
                <div class="col-12 col-sm-2 col-md-4 text-center mb-3 mt-sm-0">
                    <span class="text-muted d-block">Expected Delivery</span>

                    <div class="text-5 font-weight-500 text-dark">
                        ${g.formatDate(date: stockMovement.expectedDeliveryDate, type: "datetime")}
                    </div>
                    <g:if test="${stockMovement?.expectedDeliveryDate && stockMovement.expectedDeliveryDate < new Date()}">
                        <div class="badge badge-pill bg-danger">Delayed - Expected ${prettyDateFormat(date: stockMovement?.expectedDeliveryDate)}</div>
                    </g:if>
                </div>
                <div class="col-12 col-sm-2 col-md-4 text-center mb-3 mt-sm-0">
                    <span class="text-muted d-block">Tracking Number</span>
                    <span class="text-5 font-weight-500 text-dark">
                        ${stockMovement?.trackingNumber?:"Not Available"}
                    </span>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-0 mt-4">
        <ul class="nav nav-tabs mb-3" id="myTab" role="tablist">
            <li class="nav-item" role="presentation">
                <a class="nav-link active" id="details-tab" aria-current="page" href="#details" data-bs-toggle="tab">Details</a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link" id="events-tab" href="#events" data-bs-toggle="tab">Timeline</a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link" id="receipts-tab" href="#receipts" data-bs-toggle="tab">Receipts</a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link" id="documents-tab" href="#documents" data-bs-toggle="tab">Documents</a>
            </li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane fade show active" id="details" role="tabpanel" data-bs-toggle="tab" aria-labelledby="details-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="row align-items-center trip-title">
                            <div class="col-5 col-md-auto text-center text-md-left">
                                <h5 class="m-0">Items</h5>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <table class="table table-borderless table-striped">
                            <thead>
                                <tr>
                                    <th>
                                    </th>
                                    <th>
                                        <g:message code="product.productCode.label"/>
                                    </th>
                                    <th>
                                        <g:message code="product.label"/>
                                    </th>
                                    <th class="text-center">
                                        <g:message code="default.quantity.label"/>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="item" in="${stockMovement.lineItems}">
                                    <tr>
                                        <td class="col-1">
                                            <g:if test="${item?.product?.images}">
                                                <g:set var="image" value="${item?.product?.images?.sort()?.first()}"/>
                                                <img src="${createLink(controller:'product', action:'renderImage', id:image?.id)}" class="img-fluid"/>
                                            </g:if>
                                            <g:else>
                                                <img src="${resource(dir: 'images', file: 'default-product.png')}" class="img-fluid"/>
                                            </g:else>
                                        </td>
                                        <td class="col-2">
                                            <g:displayBarcode showData="${true}" data="${item?.product?.productCode}" />
                                        </td>
                                        <td class="col-6">
                                            ${item?.product?.name}
                                            <g:if test="${item?.comments}">
                                                <div class="text-muted">
                                                    Special Instructions: ${item?.comments}
                                                </div>
                                            </g:if>
                                        </td>
                                        <td class="col-1 text-center">
                                            ${item.quantityRequested}
                                            ${item?.product?.unitOfMeasure?:"EA"}
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                        <g:unless test="${stockMovement.lineItems}">
                            <div class="alert alert-primary text-center text-muted">
                                There are no items
                            </div>
                        </g:unless>

                    </div>
                </div>

            </div>
            <div class="tab-pane fade" id="events" role="tabpanel" aria-labelledby="events-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="row align-items-center trip-title">
                            <h5 class="h5">Timeline</h5>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-12">
                                <g:each var="event" in="${events}">
                                    <div class="row">
                                        <div class="col-auto text-center flex-column d-none d-sm-flex">
                                            <div class="row h-50">
                                                <div class="col border-end">&nbsp;</div>
                                                <div class="col">&nbsp;</div>
                                            </div>
                                            <h5 class="m-2">
                                                <span class="badge rounded-circle bg-primary border-primary">&nbsp;</span>
                                            </h5>
                                            <div class="row h-50">
                                                <div class="col border-end">&nbsp;</div>
                                                <div class="col">&nbsp;</div>
                                            </div>
                                        </div>
                                        <div class="col py-2">
                                            <div class="card">
                                                <div class="card-body">
                                                    <div class="card-title text-muted h5">
                                                        <g:link controller="shipment" action="editEvent" id="${event?.id}"
                                                            params="[shipmentId:stockMovement?.shipment?.id]">${event?.name?:'Unspecified'}</g:link>
                                                    </div>
                                                    <div class="text-muted">${formatDate(date: event?.date, format: "MMM dd, yyyy hh:mm:ss")}</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tab-pane fade" id="receipts" role="tabpanel" aria-labelledby="receipts-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="row align-items-center trip-title">
                            <h5 class="h5">Receipts</h5>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-12">
                                <g:render template="receipts" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tab-pane fade" id="documents" role="tabpanel" aria-labelledby="documents-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="row align-items-center trip-title">
                            <h5 class="h5">Documents</h5>
                        </div>
                    </div>
                    <div class="card-body">
                        <table class="table table-striped table-borderless">
                            <thead>
                                <tr>
                                    <th class="col-10">
                                        <g:message code="document.name.label"/>
                                    </th>
                                    <th class="text-right">
                                        <g:message code="default.actions.label"/>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="document" in="${stockMovement.documents.findAll { it.id }}">
                                    <tr>
                                        <td>
                                            ${document.name}
                                        </td>
                                        <td class="text-right col-4">
                                            <a href="${document.uri}" target="_blank" class="btn btn-outline-primary">Download</a>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

<!-- Modal -->
<div class="modal fade" id="inboundModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <g:uploadForm class="needs-validation" action="importData" enctype="multipart/form-data">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="staticBackdropLabel">Edit Outbound Order ${stockMovement.identifier}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input name="type" type="hidden" value="inbound"/>
                    <input name="id" type="hidden" value="${params.id}"/>
                    <input name="redirectUrl" type="hidden" value="${g.createLink(controller: 'mobile', action: 'inboundDetails', id: params.id)}"/>
                    <g:hiddenField name="location.id" value="${session.warehouse.id }"/>
                    <input class="form-control" type="file" name="xlsFile[]" required>
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
