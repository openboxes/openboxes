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

    <div class="row g-0">
        <table class="table table-borderless table-striped">
            <thead>
                <tr>
                    <th><g:message code="filename.label" default="Filename"/></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
            <g:each var="message" in="${messages}">
                <tr>
                    <td>
                        ${message.filename}
                    </td>
                    <td class="col">
                        <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                            <g:link controller="mobile" action="messageDetails"
                                    params="[filename: message.filename]"
                                    class="btn btn-outline-primary">Download</g:link>
                            <g:link controller="mobile" action="messageProcess"
                                    params="[filename: message.filename]"
                                    class="btn btn-outline-secondary">Process</g:link>
                            <g:link controller="mobile" action="messageDelete"
                                    params="[filename: message.filename]"
                                    class="btn btn-outline-danger">Delete</g:link>
                        </div>
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
