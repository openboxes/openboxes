<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="dashboard.label" default="Dashboard"/></title>
</head>

<body>

    <div class="row g-0 mb-2">
        <div class="col col-md-12">
            <div class="float-end">
                <button type="button" class="btn btn-outline-primary"
                    data-bs-toggle="modal" data-bs-target="#inboundModal"><i class="fa fa-dolly"></i> Import Inbound</button>
                <button type="button" class="btn btn-outline-primary"
                    data-bs-toggle="modal" data-bs-target="#outboundModal"><i class="fa fa-truck-loading"></i> Import Outbound</button>
            </div>
        </div>
    </div>

    <div class="row">
        <g:each var="indicator" in="${indicators}">
            <div class="col-md-4">
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title"><i class="${indicator.class}"></i> ${indicator.name}</h5>
                        <h2 class="card-text">
                            <a href="${indicator.url}" class="text-decoration-none">${indicator.count}</a>
                        </h2>
                    </div>
                </div>
            </div>
        </g:each>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title"> Upcoming deliveries to ${session.warehouse.name} (${inboundPending?.size()?:0})</h5>
                    <table class="table table-borderless table-striped">
                        <thead>
                            <tr>
                                <th class="col-1">Order Number</th>
                                <th class="col-3">Supplier</th>
                                <th class="col-3">Delivery for FA</th>
                                <th class="col-2">Delivery Date</th>
                                <th class="col-2">Status</th>
                                <th class="col-1">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="inboundOrder" in="${inboundPending}">
                            <tr>
                                <td><g:link controller="mobile" action="inboundDetails" id="${inboundOrder?.id}" class="card-link">${inboundOrder.identifier}</g:link></td>
                                <td>${inboundOrder.origin}</td>
                                <td>${inboundOrder.destination}</td>
                                <td>${g.formatDate(date: inboundOrder.expectedDeliveryDate, format: "dd MMM yyyy")}</td>
                                <td>${inboundOrder.currentStatus}</td>
                                <td><g:link controller="mobile" action="inboundDetails" id="${inboundOrder?.id}" class="card-link">Details</g:link></td>
                            </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${inboundPending}">
                        <div class="alert alert-primary text-muted text-center">
                            There are no upcoming deliveries to ${session.warehouse.name}
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">Upcoming deliveries to venues (${outboundPending?.size()?:0})</h5>
                    <table class="table table-borderless table-hover table-striped">
                        <thead>
                            <tr>
                                <th class="col-1">Order Number</th>
                                <th class="col-3">Delivery for FA</th>
                                <th class="col-3">Venue</th>
                                <th class="col-2">Delivery Date</th>
                                <th class="col-2">Status</th>
                                <th class="col-1">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="outboundOrder" in="${outboundPending}">
                                <tr>
                                    <td><g:link controller="mobile" action="outboundDetails" id="${outboundOrder?.id}" class="card-link">${outboundOrder.identifier}</g:link></td>
                                    <td>${outboundOrder.origin}</td>
                                    <td>${outboundOrder.destination}</td>
                                    <td>${g.formatDate(date: outboundOrder.expectedDeliveryDate, format: "dd MMM yyyy")}</td>
                                    <td>${outboundOrder.status}</td>
                                    <td><g:link controller="mobile" action="outboundDetails" id="${outboundOrder?.id}" class="card-link">Details</g:link></td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${outboundPending}">
                        <div class="alert alert-primary text-muted text-center">
                            There are no upcoming deliveries to venues
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">Orders to be picked (${readyToBePicked?.size()?:0})</h5>
                    <table class="table table-borderless table-striped">
                        <thead>
                            <tr>
                                <th class="col-1">Order Number</th>
                                <th class="col-3">FA</th>
                                <th class="col-3">Delivery Destination (Venue)</th>
                                <th class="col-2">Delivery Date</th>
                                <th class="col-2">Status</th>
                                <th class="col-1">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="outboundOrder" in="${readyToBePicked}">
                                <tr>
                                    <td>${outboundOrder.identifier}</td>
                                    <td>${outboundOrder.origin}</td>
                                    <td>${outboundOrder.destination}</td>
                                    <td>${g.formatDate(date: outboundOrder.expectedDeliveryDate, format: "dd MMM yyyy")}</td>
                                    <td>${outboundOrder.status}</td>
                                    <td><g:link controller="mobile" action="outboundDetails" id="${outboundOrder?.id}" class="card-link">Details</g:link></td>
                                    <td></td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${readyToBePicked}">
                        <div class="alert alert-primary text-muted text-center">
                            There are no orders ready to be picked
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">Orders in transit (${inTransit?.size()?:0})</h5>
                    <table class="table table-borderless table-hover table-striped">
                        <thead>
                            <tr>
                                <th class="col-1">Order Number</th>
                                <th class="col-3">Delivery for FA</th>
                                <th class="col-3">Venue</th>
                                <th class="col-2">Delivery Date</th>
                                <th class="col-2">Status</th>
                                <th class="col-1">Tracking</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="outboundOrder" in="${inTransit}">
                                <tr>
                                    <td><g:link controller="mobile" action="outboundDetails" id="${outboundOrder?.id}" class="card-link">${outboundOrder.identifier}</g:link></td>
                                    <td>${outboundOrder.origin}</td>
                                    <td>${outboundOrder.destination}</td>
                                    <td>${g.formatDate(date: outboundOrder.expectedDeliveryDate, format: "dd MMM yyyy")}</td>
                                    <td>${outboundOrder.currentStatus}</td>
                                    <td>
                                        <g:if test="${stockMovement?.trackingNumber}">
                                            <g:link url="${stockMovement.trackingUri}">${stockMovement?.trackingNumber}</g:link>
                                            <g:displayBarcode showData="${false}" data="${stockMovement.trackingUri}" format="QR_CODE"/>
                                        </g:if>
                                        <g:else>
                                            Not Available
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${inTransit}">
                        <div class="alert alert-primary text-muted text-center">
                            There are no orders in transit
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">Inventory summary (${inventorySummary?.size()?:0})</h5>
                    <table class="table table-borderless table-striped">
                        <thead>
                            <tr>
                                <th class="col-2">SKU Code</th>
                                <th class="col-4">Description</th>
                                <th class="col-1">On Hand</th>
                                <th class="col-1">On Order</th>
                                <th class="col-2">Status</th>
                                <th>Shipment Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each var="productSummary" in="${inventorySummary}">
                                <g:set var="product" value="${productSummary.product}"/>
                                <tr>
                                    <td>
                                        <a href="${createLink(controller: 'mobile', action: 'productDetails', id: product?.id)}">
                                            ${product.productCode}
                                        </a>
                                    </td>
                                    <td>
                                        ${product.name}
                                    </td>
                                    <td>
                                        <g:formatNumber number="${productSummary.quantityOnHand}" maxFractionDigits="0"/>
                                        <small>${product?.unitOfMeasure?:"EA"}</small>
                                    </td>
                                    <td>
                                        <g:formatNumber number="${productSummary.quantityOnOrder}" maxFractionDigits="0"/>
                                        <small>${product?.unitOfMeasure?:"EA"}</small>
                                    </td>
                                    <td>
                                        <g:if test="${productSummary?.quantityOnHand > 0}">
                                            In stock
                                        </g:if>
                                        <g:else>
                                            Out of stock
                                        </g:else>
                                    </td>
                                    <td>

                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${inventorySummary}">
                        <div class="alert alert-primary text-muted text-center">
                            There are no items in inventory
                        </div>
                    </g:unless>
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
