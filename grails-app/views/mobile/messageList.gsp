<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="messages.label" default="Messages"/></title>
</head>

<body>

    <div class="row g-0">
        <table class="table table-bordered">
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

    <g:uploadForm class="row g-3 needs-validation" action="messageUpload" enctype="multipart/form-data">
        <div class="col-md-4">
            <div class="card mb-3">
                <h5 class="card-title"><i class="fa fa-upload"></i> Upload Message </h5>
                <div class="mb-3">
                    <label for="messageFile" class="form-label"></label>
                    <input class="form-control" type="file" name="messageFile" id="messageFile" required>
                </div>
                <div class="col-12">
                    <button class="btn btn-primary" type="submit">Submit form</button>
                </div>
            </div>
        </div>
    </g:uploadForm>


</body>
</html>
