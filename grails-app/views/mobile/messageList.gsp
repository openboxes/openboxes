<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="messages.label" default="Messages"/></title>
</head>

<body>

    <div class="clearfix">
        <button type="button" class="btn btn-outline-primary float-end" data-bs-toggle="modal"
                    data-bs-target="#messageUploadModal">Upload Message</button>
        </div>

    <div class="card">
        <div class="card-header">
            Server Details
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
                    <span class="text-muted d-block">Polling Directories</span>
                    <span class="text-5 font-weight-500 text-dark">
                        <ul class="list-group">
                            <g:each var="subdirectory" in="${grailsApplication.config.openboxes.integration.ftp.inbound.subdirectories}">
                                <li class="list-group-item">${grailsApplication.config.openboxes.integration.ftp.inbound.directory}/${subdirectory}</li>
                            </g:each>
                        </ul>
                    </span>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            Files
        </div>
        <div class="card-body">

            <table class="table table-borderless table-striped">
                <thead>
                    <tr>
                        <th><g:message code="message.label" default="Message"/></th>
                        <th><g:message code="message.atime.label" default="Access Time" /></th>
                        <th><g:message code="message.mtime.label" default="Modified Time" /></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                <g:each var="message" in="${messages}">
                    <tr class="${message.name.endsWith('log')?'table-danger':''}">
                        <td>
                            ${message.name}
                            <div class="text-sm-left text-muted">${message.path}</div>
                        </td>
                        <td>
                            ${message.atime}
                        </td>
                        <td>
                            ${message.mtime}
                        </td>
                        <td class="col">
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end btn-group">
                                <g:link controller="mobile" action="messageDetails"
                                        params="[path: message.path]"
                                        class="btn btn-outline-primary">Download</g:link>
                                <g:link controller="mobile" action="messageValidate"
                                        params="[path: message.path]"
                                        class="btn btn-outline-success">Validate</g:link>
                                <g:link controller="mobile" action="messageProcess"
                                        params="[path: message.path]"
                                        class="btn btn-outline-success">Process</g:link>
                                <g:link controller="mobile" action="messageDelete"
                                        params="[path: message.path]"
                                        class="btn btn-outline-danger">Delete</g:link>
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
</body>
</html>
