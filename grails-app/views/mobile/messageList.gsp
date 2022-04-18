<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="messages.label" default="Messages"/></title>
</head>

<body>

    <div class="row g-0 mb-2">
        <div class="col col-md-12">
            <div class="float-end">
                <button type="button" class="btn btn-outline-primary" data-bs-toggle="modal"
                            data-bs-target="#messageUploadModal">
                    <i class="fa fa-file-upload"></i> Upload Message
                </button>
                <g:link controller="mobile" action="messageListProcess" class="btn btn-outline-success">
                    <i class="fa fa-bullseye"></i> Process Messages
                </g:link>
                <button type="button" class="btn btn-outline-secondary"
                        data-bs-toggle="modal" data-bs-target="#uploadModal"><i class="fa fa-paper-plane"></i> Send Delivery Orders
                </button>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            Message Queue Details
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-sm-12 col-md-4 text-center time-info mt-3 mt-sm-0">
                    <span class="text-muted d-block">Server</span>
                    <span class="text-5 font-weight-500 text-dark">
                        ${grailsApplication.config.openboxes.integration.ftp.server}
                    </span>
                </div>

                <div class="col-sm-12 col-md-4 text-center company-info">
                    <span class="text-muted d-block">User</span>
                    <span class="text-5 font-weight-500 text-dark">
                        ${grailsApplication.config.openboxes.integration.ftp.user}
                    </span>
                </div>

                <div class="col-sm-12 col-md-4 text-center time-info mt-3 mt-sm-0">
                    <span class="text-muted d-block">Message Queues</span>
                    <span class="text-5 font-weight-500 text-dark">
                        <ul class="list-group">
                            <g:each var="subdirectory" in="${grailsApplication.config.openboxes.integration.ftp.inbound.subdirectories}">
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    ${grailsApplication.config.openboxes.integration.ftp.inbound.directory}/${subdirectory}
                                    <g:set var="messageCount" value="${messages?.findAll { it.path.contains(subdirectory) }.size()}"/>
                                    <div class="badge bg-primary badge-pill">${messageCount}</div>
                                </li>
                            </g:each>
                        </ul>
                    </span>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            Message Queue <div class="badge badge-primary bg-primary">${messages.size()?:0}</div>
        </div>
        <div class="card-body">

            <table class="table table-borderless table-striped">
                <thead>
                    <tr>
                        <th><g:message code="message.label" default="Message"/></th>
                        <th><g:message code="message.mtime.label" default="Modified Time" /></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                <g:each var="message" in="${messages}">
                    <tr class="${message.name.endsWith('log')?'table-danger':''}">
                        <td class="col col-md-8">
                            ${message.name}
                            <div class="text-sm-left text-muted">${message.path}</div>
                        </td>
                        <td class="col col-md-3">
                            <div data-bs-toggle="tooltip" data-bs-placement="top" title="${g.formatDate(date: message.mtime)}">
                                <g:prettyDateFormat date="${message.mtime}"/>
                            </div>
                        </td>
                        <td class="col col-md-1">
                            <div class="dropdown">
                                <button class="btn btn-outline-primary dropdown-toggle" type="button"
                                        id="dropdownMenuButton" data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="fa fa-cog"></i> Actions
                                </button>
                                <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                                    <li><g:link controller="mobile" action="messageDetails"
                                                class="dropdown-item" params="[path: message.path]">
                                        <i class="fa fa-file-download"></i> Download</g:link></li>
                                    <li><g:link controller="mobile" action="messageValidate"
                                                class="dropdown-item" params="[path: message.path]">
                                        <i class="fa fa-spell-check"></i> Validate</g:link></li>
                                    <li><g:link controller="mobile" action="messageProcess"
                                                class="dropdown-item" params="[path: message.path]">
                                        <i class="fa fa-bullseye"></i> Process</g:link></li>
                                    <li><g:link controller="mobile" action="messageArchive"
                                                class="dropdown-item" params="[path: message.path]">
                                        <i class="fa fa-archive"></i> Archive</g:link></li>
                                    <li><g:link controller="mobile" action="messageDelete"
                                                class="dropdown-item text-danger" params="[path: message.path]">
                                        <i class="fa fa-ban"></i> Delete</g:link></li>
                                    </ul>
                                </div>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>


    <!-- Modal -->
    <div class="modal fade" id="messageUploadModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <g:uploadForm class="row g-3 needs-validation" action="messageUpload" enctype="multipart/form-data">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="staticBackdropLabel"><i class="fa fa-upload"></i> Upload Message </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <label for="subdirectory" class="form-label">Subdirectory</label>
                        <g:select name="subdirectory"  class="form-control"
                                  from="${grailsApplication.config.openboxes.integration.ftp.inbound.subdirectories}"/>

                        <label for="messageFile" class="form-label">File</label>
                        <input class="form-control" type="file" name="messageFile" id="messageFile" required>
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
