<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="messages.label" default="Messages"/></title>
</head>

<body>

    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#messageUploadModal">Import Message</button>

    <div class="row g-0">
        <table class="table table-borderless table-striped">
            <thead>
                <tr>
                    <th><g:message code="filename.label" default="Filename"/></th>
                    <th><g:message code="default.actions.label"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each var="message" in="${messages}">
                <tr>
                    <td>
                        ${message.filename}
                    </td>
                    <td class="col-2">
                        <g:link controller="mobile" action="messageProcess" params="[filename: message.filename]" class="btn btn-outline-primary">Process</g:link>
                        <g:link controller="mobile" action="messageDetails" params="[filename: message.filename]" class="btn btn-outline-secondary">Download</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
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
                        <label for="messageFile" class="form-label"></label>
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
