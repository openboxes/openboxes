<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.pih.warehouse.inventory.StockMovementStatusCode;" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="mobile"/>
    <title><g:message code="default.outbound.label" default="Outbound"/> ${stockMovement.identifier}</title>
</head>

<body>

<g:if test="${flash.command}">
    <ul class="alert alert-danger">
        <g:renderErrors bean="${flash.command}" as="list"/>
    </ul>
</g:if>

<g:isSuperuser>
    <g:set var="isSuperuser" value="${true}"/>
</g:isSuperuser>

<div class="card">
    <div class="card-header">
        <div class="row align-items-center">

            <div class="col-2 text-center">
                <g:displayBarcode showData="${false}" data="${stockMovement.identifier}"/>
                <span class="badge bg-secondary">${stockMovement.identifier}</span>
            </div>

            <div class="col-9">
                <h5 class="ml-5">${stockMovement.name}</h5>
                <div class="small text-muted">
                    Last updated by ${stockMovement.updatedBy?.name} on ${g.formatDate(date: stockMovement.shipment?.mostRecentEvent?.eventDate?:stockMovement.lastUpdated, type: "datetime")}
                </div>
            </div>

            <div class="col-1">
                <div class="dropdown float-end">
                    <button type="button" id="actionMenu" class="btn dropdown-toggle float-end"
                            data-bs-toggle="dropdown"
                            aria-haspopup="true" aria-expanded="false">
                        <i class="fa fa-cog"></i>
                    </button>
                    <ul class="dropdown-menu">
                        <li>
                            <a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#outboundModal">
                                Import Order
                            </a>
                        </li>
                        <li>
                            <a href="${createLink(controller: 'mobile', action: 'exportData', id: stockMovement?.id)}"
                               class="dropdown-item">
                                Export Order
                            </a>
                        </li>
                        <g:isSuperuser>
                            <div class="dropdown-divider"></div>
                            <li>
                                <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}"
                                   class="dropdown-item">
                                    View Details
                                </a>
                            </li>
                            <li>
                                <a href="${createLink(controller: 'mobile', action: 'outboundDownload', id: stockMovement?.id)}"
                                   class="dropdown-item" target="_blank">
                                    Download Delivery Order Request (.xml)
                                </a>
                            </li>
                            <li>
                                <a href="${createLink(controller: 'mobile', action: 'outboundUpload', id: stockMovement?.id)}"
                                   class="dropdown-item" target="_blank">
                                    Upload Delivery Order Request (.xml)
                                </a>
                            </li>
                        </g:isSuperuser>
                        <div class="dropdown-divider"></div>
                        <li>
                            <g:link class="dropdown-item" controller="picklist" action="print" id="${stockMovement?.id}" target="_blank">
                                Download ${warehouse.message(code: 'picklist.label', default: 'Picklist')}
                            </g:link>
                        </li>
                        <li>
                            <g:link class="dropdown-item" controller="stockMovement" action="printPackingList" id="${stockMovement?.id}" target="_blank">
                                Download ${warehouse.message(code: "packingList.label", default: "Packing List")}
                            </g:link>
                        </li>
                        <div class="dropdown-divider"></div>
                        <a href="${createLink(controller: 'mobile', action: 'outboundDelete', id: stockMovement?.id)}"
                           class="dropdown-item text-danger">Delete Order
                        </a>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <div class="row">

            <div class="col-sm-12 col-md-3 text-center time-info mb-3 mt-sm-0">
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

            <div class="col-sm-12 col-md-3 text-center mb-3 mt-sm-0">
                <span class="text-muted d-block">Origin</span>
                <span class="text-5 font-weight-500 text-dark">
                    ${stockMovement?.origin?.locationNumber}
                </span>
            </div>

            <div class="col-sm-12 col-md-3 text-center mt-3 mt-sm-0">
                <span class="text-muted d-block">Destination</span>
                <span class="text-4 font-weight-500 text-dark">
                    ${stockMovement?.destination?.locationNumber}
                </span>
            </div>

            <div class="col-sm-12 col-md-3 text-center time-info mt-3 mt-sm-0">
                <span class="text-muted d-block">Tracking Number</span>
                <span class="text-5 font-weight-500 text-dark">
                    <g:if test="${stockMovement?.trackingNumber}">
                        <g:link url="${stockMovement.trackingUri}">${stockMovement?.trackingNumber}</g:link>
                        <g:displayBarcode showData="${false}" data="${stockMovement.trackingUri}" format="QR_CODE"/>
                    </g:if>
                    <g:else>
                        Not Available
                    </g:else>
                </span>
            </div>

            <div class="col-sm-12 col-md-3 text-center mb-3 mt-sm-0">
                <span class="text-muted d-block">Requested Delivery</span>
                <g:if test="${stockMovement?.requestedDeliveryDate}">
                    <div class="text-5 font-weight-500 text-dark">
                        ${g.formatDate(date: stockMovement.requestedDeliveryDate, type: "date")}
                    </div>
                </g:if>
                <g:else>Not Available</g:else>
            </div>

            <div class="col-sm-12 col-md-3 text-center mb-3 mt-sm-0">
                <span class="text-muted d-block">Expected Shipping</span>
                <g:if test="${stockMovement?.expectedShippingDate}">
                    <div class="text-5 font-weight-500 text-dark">
                        ${g.formatDate(date: stockMovement.expectedShippingDate, type: "datetime")}
                    </div>
                    <g:if test="${!stockMovement.isShipped && stockMovement.expectedShippingDate < new Date()}">
                        <div class="badge badge-pill bg-danger">Expected ${prettyDateFormat(date: stockMovement?.expectedShippingDate)}</div>
                    </g:if>
                </g:if>
                <g:else>TBD</g:else>
            </div>

            <div class="col-sm-12 col-md-3 text-center mb-3 mt-sm-0">
                <span class="text-muted d-block">Expected Delivery</span>
                <g:if test="${stockMovement?.expectedDeliveryDate}">
                    <div class="text-5 font-weight-500 text-dark">
                        ${g.formatDate(date: stockMovement.expectedDeliveryDate, type: "datetime")}
                    </div>
                    <g:if test="${!stockMovement?.isReceived && stockMovement.expectedDeliveryDate < new Date()}">
                        <div class="badge badge-pill bg-danger">Expected ${prettyDateFormat(date: stockMovement?.expectedDeliveryDate)}</div>
                    </g:if>
                </g:if>
                <g:else>TBD</g:else>
            </div>
            <div class="col-sm-12 col-md-3 text-center mb-3 mt-sm-0">
                <span class="text-muted d-block">Line Items</span>
                ${stockMovement?.lineItems?.size()?:0}
            </div>
        </div>
    </div>
</div>

<div class="row g-0 mt-4">
    <ul class="nav nav-tabs mb-3" id="myTab" role="tablist">
        <li class="nav-item" role="presentation">
            <a class="nav-link active" id="details-tab" aria-current="page" href="#details"
               data-bs-toggle="tab">Details</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="events-tab" href="#events" data-bs-toggle="tab">Timeline</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="qrcode-tab" href="#qrcode" data-bs-toggle="tab">QR Code</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="documents-tab" href="#documents"
               data-bs-toggle="tab">Documents</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="comments-tab" href="#comments"
               data-bs-toggle="tab">Comments</a>
        </li>
    </ul>

    <div class="tab-content">
        <div class="tab-pane fade show active" id="details" role="tabpanel" data-bs-toggle="tab"
             aria-labelledby="details-tab">

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
                                <g:message code="requisitionItem.quantityRequested.label"/>
                            </th>
                            <th class="text-center">
                                <g:message code="requisitionItem.quantityRequired.label"/>
                            </th>
                            <th class="text-center">
                                <g:message code="requisition.quantityOnHand.label"/>
                            </th>
                            <th>
                                <g:message code="requisitionItem.status.label"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                            <g:each var="item" in="${stockMovement.lineItems}">
                                <g:set var="requisitionItem" value="${item.requisitionItem}"/>
                                <tr>
                                    <td class="col-1">
                                        <g:if test="${item?.product?.images}">
                                            <g:set var="image"
                                                   value="${item?.product?.images?.sort()?.first()}"/>
                                            <img src="${createLink(controller: 'product', action: 'renderImage', id: image?.id)}"
                                                 class="img-fluid"/>
                                        </g:if>
                                        <g:else>
                                            <img src="${resource(dir: 'images', file: 'default-product.png')}"
                                                 class="img-fluid"/>
                                        </g:else>
                                    </td>
                                    <td class="col-1 text-center">
                                        <g:displayBarcode showData="${false}"
                                                          data="${item?.product?.productCode}"/>

                                    </td>
                                    <td class="col-4">
                                        <small>${item?.product?.productCode}</small>
                                        ${item?.product?.name}
                                        <div class="small">${item?.product?.category?.name}</div>
                                        <g:if test="${item?.comments}">
                                            <div class="text-muted">
                                                Special Instructions: ${item.comments}
                                            </div>
                                        </g:if>
                                    </td>
                                    <td class="col-1 text-center">
                                        ${item.quantityRequested}
                                        ${item?.product?.unitOfMeasure?:"EA"}
                                    </td>
                                    <td class="col-1 text-center">
                                        ${item.quantityRequired}
                                        ${item?.product?.unitOfMeasure?:"EA"}
                                    </td>
                                    <td class="col-1 text-center">
                                        ${quantityOnHand[item?.product?.id]?:0}
                                        ${item?.product?.unitOfMeasure?:"EA"}
                                    </td>
                                    <td class="col-1">
                                        <g:set var="pickReasonCode" value="${requisitionItem?.modificationItem?.pickReasonCode ?: requisitionItem?.substitutionItem?.pickReasonCode ?: requisitionItem?.pickReasonCode}"/>
                                        <g:if test="${requisitionItem?.cancelReasonCode || pickReasonCode }">
                                            <g:set var="tooltipMessage" value="${requisitionItem?.cancelReasonCode ? 'Edit reason code: ' + requisitionItem?.cancelReasonCode : ''}${pickReasonCode ? 'Pick reason code: ' + pickReasonCode : ''}"/>
                                        </g:if>
                                        <span data-bs-toggle="tooltip" data-bs-placement="top" title="${tooltipMessage}"
                                              class="badge ${requisitionItem.isCanceled() || requisitionItem.isCanceledDuringPick() ? 'bg-danger' :
                                                requisitionItem.isSubstituted() || requisitionItem.isReduced() ? 'bg-warning' : 'bg-success'}">
                                            <g:if test="${requisitionItem.isCanceled() || requisitionItem.isCanceledDuringPick()}">
                                                <g:message code="enum.RequisitionItemStatus.CANCELED"/>
                                            </g:if>
                                            <g:elseif test="${requisitionItem.isReduced() && !requisitionItem.isSubstituted()}">
                                                <g:message code="enum.RequisitionItemStatus.REDUCED"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem.isIncreased() && !requisitionItem.isSubstituted()}">
                                                <g:message code="enum.RequisitionItemStatus.INCREASED"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem?.status==org.pih.warehouse.requisition.RequisitionItemStatus.APPROVED
                                                    && requisitionItem?.requisition?.status == org.pih.warehouse.requisition.RequisitionStatus.ISSUED}">
                                                <format:metadata obj="${requisitionItem?.requisition?.status}"/>
                                            </g:elseif>
                                            <g:else>
                                                <format:metadata obj="${requisitionItem?.status}"/>
                                            </g:else>
                                        </span>
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

        <div class="tab-pane fade" id="qrcode" role="tabpanel" aria-labelledby="qrcode-tab">
            <div class="card">
                <div class="card-header">
                    <div class="row align-items-center trip-title">
                        <h5 class="h5">QR Code</h5>
                    </div>
                </div>

                <div class="card-body">
                    <div class="text-center">
                    <g:displayBarcode showData="${false}" data="${stockMovement.generateLink()}" format="QR_CODE" width="400" height="400"/>

                    </div>
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
                                                    <g:if test="${isSuperuser}">
                                                        <g:link controller="shipment" action="editEvent"
                                                                id="${event?.id}"
                                                                params="[shipmentId: stockMovement?.shipment?.id]">${event?.name ?: 'Unspecified'}</g:link>
                                                    </g:if>
                                                    <g:else>
                                                        ${event?.name ?: 'Unspecified'}
                                                    </g:else>
                                                </div>
                                                <div class="text-muted">${formatDate(date: event?.date, format: "MMM dd, yyyy HH:mm:ss z")}</div>
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
                                <td>
                                    <g:if test="${document.id}">
                                        <g:link action="documentDownload" id="${document.id}" target="_blank"
                                           class="btn btn-outline-primary">Download</g:link>
                                    </g:if>
                                    <g:else>
                                        <a href="${document.uri}" target="_blank"
                                           class="btn btn-outline-primary">Download</a>
                                    </g:else>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${stockMovement.documents.findAll { it.id }}">
                        <div class="alert alert-primary text-center text-muted">
                            There are no documents
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
        <div class="tab-pane fade" id="comments" role="tabpanel" aria-labelledby="comments-tab">
            <div class="card">
                <div class="card-header">
                    <div class="row align-items-center trip-title">
                        <h5 class="h5">Comments</h5>
                    </div>
                </div>

                <div class="card-body">
                    <table class="table table-striped table-borderless">
                        <thead>
                        <tr>
                            <th class="col-10">
                                <g:message code="comment.label"/>
                            </th>
                            <th class="text-right">
                                <g:message code="default.actions.label"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="comment" in="${stockMovement.comments}">
                            <tr>
                                <td>
                                    ${comment}
                                </td>
                                <td>

                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${stockMovement.comments}">
                        <div class="alert alert-primary text-center text-muted">
                            There are no comments
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="outboundModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <g:uploadForm class="needs-validation" action="importData" enctype="multipart/form-data">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="staticBackdropLabel">Edit Outbound Order ${stockMovement.identifier}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input name="type" type="hidden" value="outbound"/>
                    <input name="id" type="hidden" value="${params.id}"/>
                    <input name="redirectUrl" type="hidden" value="${g.createLink(controller: 'mobile', action: 'outboundDetails', id: params.id)}"/>
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
