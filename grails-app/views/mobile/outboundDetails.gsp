<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="mobile"/>
    <title><g:message code="default.outbound.label" default="Outbound"/></title>
</head>

<body>
<div class="card">
    <div class="card-header">
        <div class="row align-items-center">

            <div class="col-2 text-center">
                <g:displayBarcode showData="${false}" data="${qrCodeLink}"/>
                <span class="badge bg-secondary">${stockMovement.identifier}</span>
            </div>

            <div class="col-8">
                <h5 class="ml-5 d-inline">${stockMovement.name}</h5>
            </div>

            <div class="col-1">
                <h1 class="badge bg-primary mt-2 mr-2 pl-5">${stockMovement?.status}</h1>
            </div>

            <div class="col-1">
                <span class="dropdown">
                    <button type="button" id="actionMenu" class="btn dropdown-toggle"
                            data-bs-toggle="dropdown"
                            aria-haspopup="true" aria-expanded="false">
                        <i class="fa fa-cog"></i>
                    </button>

                    <ul class="dropdown-menu">
                        <li>
                            <a href="${createLink(controller: 'stockMovement', action: 'show', id: stockMovement?.id)}"
                               class="dropdown-item">
                                View Details
                            </a>
                        </li>
                        <li>
                            <a href="${createLink(controller: 'mobile', action: 'outboundDownload', id: stockMovement?.id)}"
                               class="dropdown-item" target="_blank">
                                Generate Create Order (.xml)
                            </a>
                        </li>

                        <div class="dropdown-divider"></div>
                        <a href="${createLink(controller: 'mobile', action: 'outboundDelete', id: stockMovement?.id)}"
                           class="dropdown-item text-danger">
                            Delete
                        </a>
                    </ul>
                </span>

            </div>
        </div>
    </div>

    <div class="card-body">
        <div class="row">
            <div class="col-sm-12 col-md-2 text-center time-info mt-3 mt-sm-0">
                <span class="text-muted d-block">Origin</span>
                <span class="text-5 font-weight-500 text-dark">
                    ${stockMovement?.origin?.locationNumber}
                </span>
            </div>

            <div class="col-sm-12 col-md-2 text-center time-info mt-3 mt-sm-0">
                <span class="text-muted d-block">Destination</span>
                <span class="text-4 font-weight-500 text-dark">
                    ${stockMovement?.destination?.locationNumber}
                </span>
            </div>

            <div class="col-sm-12 col-md-2 text-center company-info">
                <span class="text-muted d-block">Ordered On</span>
                <span class="text-4 font-weight-500 text-dark mt-1 mt-lg-0">
                    ${g.formatDate(date: stockMovement.requisition.dateCreated)}
                </span>
            </div>

            <div class="col-sm-12 col-md-2 text-center time-info mt-3 mt-sm-0">
                <span class="text-muted d-block">Expected Delivery</span>

                <div class="text-5 font-weight-500 text-dark">
                    ${g.formatDate(date: stockMovement.expectedDeliveryDate)}
                </div>
                <g:if test="${stockMovement?.expectedDeliveryDate && stockMovement.expectedDeliveryDate < new Date()}">
                    <div class="badge badge-pill bg-danger">Delayed - Expected ${prettyDateFormat(date: stockMovement?.expectedDeliveryDate)}</div>
                </g:if>
                <g:else>Not Available</g:else>
            </div>

            <div class="col-sm-12 col-md-2 text-center time-info mt-3 mt-sm-0">
                <span class="text-muted d-block">Tracking Number</span>
                <span class="text-5 font-weight-500 text-dark">
                    ${stockMovement?.trackingNumber ?: "Not Available"}
                </span>
            </div>
        </div>

        <!--
            <div class="row">
                <div class="col-sm-4"><strong class="font-weight-600">Label</strong>
                    <p>Value</p>
                </div>

                <div class="col-sm-4"><strong class="font-weight-600">Label</strong>
                    <p>Value</p>
                </div>
                <div class="col-sm-4"><strong class="font-weight-600">Label</strong>
                    <p><span class="badge badge-pill badge-dark badge-success alert-success py-1 px-2 font-weight-normal">Confirmed <i
                            class="fas fa-check-circle"></i></span></p>
                </div>
            </div>
            -->

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
            <a class="nav-link" id="documents-tab" href="#documents"
               data-bs-toggle="tab">Documents</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="properties-tab" href="#properties"
               data-bs-toggle="tab">Properties</a>
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
                                <td class="col-1">
                                    <g:displayBarcode showData="${false}"
                                                      data="${item?.product?.productCode}"/>
                                </td>
                                <td class="col-2">
                                    ${item?.product?.productCode}
                                </td>
                                <td class="col-6">
                                    ${item?.product?.name}
                                </td>
                                <td class="col-1 text-center">
                                    ${item.quantityRequested}
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
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
                                                    <g:link controller="shipment" action="editEvent"
                                                            id="${event?.id}"
                                                            params="[shipmentId: stockMovement?.shipment?.id]">${event?.name ?: 'Unspecified'}</g:link>
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
                        <g:each var="document" in="${stockMovement.documents}">
                            <tr>
                                <td>
                                    ${document.name}
                                </td>
                                <td class="text-right col-4">
                                    <a href="${document.uri}" target="_blank"
                                       class="btn btn-outline-primary">Download</a>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="tab-pane fade" id="properties" role="tabpanel" aria-labelledby="properties-tab">

            <div class="card">
                <div class="card-header">
                    <div class="row align-items-center">
                        <h5 class="h5">Properties</h5>
                    </div>
                </div>

                <div class="card-body">
                    <table class="table table-bordered table-striped">
                        <thead>
                        <tr>
                            <th class="col-9">
                                <g:message code="default.property.label" default="Property"/>
                            </th>
                            <th class="text-right">
                                <g:message code="default.value.label" default="Value"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="property" in="${stockMovement.properties}">
                            <tr>
                                <td>${property.key}</td>
                                <td>${property.value}</td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>

</div>
</body>
</html>
