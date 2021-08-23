<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="mobile" />
    <title><warehouse:message code="dashboard.label" default="Dashboard"/></title>
</head>

<body>
    <div class="row">
        <g:each var="indicator" in="${data}">
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
