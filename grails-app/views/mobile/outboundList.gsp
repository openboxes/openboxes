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

<div class="row g-0 mb-2">
    <div class="col col-md-12 ">

        <div class="btn-group float-end">
            <button type="button" class="btn btn-outline-primary "
                    data-bs-toggle="modal" data-bs-target="#outboundModal"><i class="fa fa-file-import"></i> Import Delivery Orders</button>

            <g:isSuperuser>
                <button type="button" class="btn btn-outline-secondary "
                        data-bs-toggle="modal" data-bs-target="#uploadModal"><i class="fa fa-file-upload"></i> Send Delivery Orders
                    <label class="badge bg-danger">debug</label>
                </button>
            </g:isSuperuser>

        </div>
    </div>
</div>

<div class="row g-0">
    <div class="col">
        <g:form controller="mobile" action="outboundList" method="GET">
            <table class="table table-borderless table-striped">
                <thead>
                <tr>
                    <th><g:message code="stockMovement.orderStatus.label" default="WMS Status"/></th>
                    <th><g:message code="stockMovement.identifier.label"/></th>
                    <th><g:message code="stockMovement.destination.label"/></th>
                    <th><g:message code="stockMovement.dateRequested.label" default="Requested Delivery"/></th>
                    <th><g:message code="stockMovement.trackingNumber.label" /></th>
                    <th><g:message code="stockMovement.shippingStatus.label" default="TMS Status"/></th>
                    <th class="col-1 text-center"></th>
                </tr>
                <tr>
                    <th>
                        <g:selectRequisitionStatus name="status" value="${params.status}"
                                                   class="form-control" noSelection="['':warehouse.message(code:'default.all.label')]"/>
                    </th>
                    <th>
                        <g:textField name="identifier" value="${params.identifier}" class="form-control" size="3"/>
                    </th>
                    <th>
                        <g:selectLocation id="destination" name="destination.id" value="${params?.destination?.id}"
                                          class="form-control"
                                          noSelection="['null':warehouse.message(code:'default.all.label')]"/>
                    </th>
                    <th>
                        <g:textField name="requestedDeliveryDateFilter" class="date-filter form-control" size="10"
                                     value="${params.requestedDeliveryDateFilter}"/>
                    </th>
                    <th>
                        <g:textField name="trackingNumber" value="${params.trackingNumber}" class="form-control" size="8"/>
                    </th>
                    <th>
                        <g:selectEventType name="eventType" value="${params.eventType}" class="form-control" noSelection="['':warehouse.message(code:'default.all.label')]" />
                    </th>
                    <th class="col-1 text-center">
                        <button type="submit" class="btn btn-primary"><i class="fa fa-filter"></i> Filter</button>
                    </th>
                </tr>
                </thead>
                <tbody>
                <g:each var="stockMovement" in="${stockMovements}">
                    <tr>
                        <td>
                            <div class="badge bg-primary">${stockMovement?.status}</div>
                            <p class="small text-muted"><g:formatDate date="${stockMovement.lastUpdated}" format="MMM dd HH:mm"/></p>
                        </td>
                        <td>
                            <a href="${createLink(controller: 'mobile', action: 'outboundDetails', id: stockMovement?.id)}" class="text-decoration-none text-reset">
                                ${stockMovement.identifier}
                            </a>
                        </td>
                        <td>
                            ${stockMovement?.destination?.name}
                        </td>
                        <td class="text-center">
                            <g:if test="${stockMovement?.requestedDeliveryDate == stockMovement?.expectedDeliveryDate}">
                                <g:formatDate date="${stockMovement?.expectedDeliveryDate}" format="dd/MMM/yyyy HH:mm"/>
                            </g:if>
                            <g:else>
                                <del>
                                    <g:formatDate date="${stockMovement?.requestedDeliveryDate}" format="dd/MMM/yyyy HH:mm"/>
                                </del>
                                <g:formatDate date="${stockMovement?.requestedDeliveryDate}" format="dd/MMM/yyyy HH:mm"/>
                            </g:else>
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
                            <g:if test="${stockMovement?.shipment?.mostRecentEvent?.eventType}">
                                <div class="badge bg-secondary">
                                    ${stockMovement.shipment?.mostRecentEvent?.eventType?.name?.toUpperCase()}
                                </div>
                                <p class="small text-muted"><g:formatDate date="${stockMovement.shipment?.currentEvent?.eventDate}" format="MMM dd HH:mm"/></p>
                            </g:if>
                        </td>
                        <td>
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                <a href="${createLink(controller: 'mobile', action: 'outboundDetails', id: stockMovement?.id)}" class="btn btn-outline-primary">
                                    Details <i class="fa fa-chevron-right"></i>
                                </a>
                            </div>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:form>
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

<div class="modal fade" id="uploadModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
    <g:form class="needs-validation" action="uploadDeliveryOrders">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Send Delivery Orders</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <g:set var="numberOfDaysInAdvance" value="${grailsApplication.config.openboxes.jobs.uploadDeliveryOrdersJob.numberOfDaysInAdvance}"/>
                    <g:set var="requestedDeliveryDate" value="${(new Date() + numberOfDaysInAdvance)}"/>
                    <p>Send all delivery orders to eTruckNow that have a requested delivery date that is ${numberOfDaysInAdvance} days from now. </p>

                    <g:isSuperuser>
                        <g:set var="isSuperuser" value="true"/>
                    </g:isSuperuser>
                    <g:if test="${isSuperuser}">
                        <div class="text-center">
                            <g:datePicker name="requestedDeliveryDate" precision="day" value="${requestedDeliveryDate}" />
                        </div>
                    </g:if>
                    <g:else>
                        <div class="text-center">
                            ${requestedDeliveryDate.format("MMMMM dd, yyyy")}
                        </div>
                    </g:else>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    <button type="submit" class="btn btn-primary">Send</button>
                </div>
            </div>
        </div>
    </g:form>
</div>
</body>
</html>
