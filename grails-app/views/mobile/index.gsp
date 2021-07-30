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
</body>
</html>
