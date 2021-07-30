<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="mobile"/>
    <title><g:message code="default.login.label" default="Login"/></title>
</head>

<body>
    <div class="container">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${userInstance}">
            <div class="errors">
                <g:renderErrors bean="${userInstance}" as="list" />
            </div>
        </g:hasErrors>
        <div class="row">
            <div class="col-sm-12">
                <g:form controller="auth" action="handleLogin">
                    <div class="mb-3">
                        <label for="username" class="form-label">Email address</label>
                        <input class="form-control" id="username" name="username">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" class="form-control" id="password" name="password">
                    </div>
                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-outline-primary">Login</button>
                    </div>
                </g:form>
            </div>
        </div>
    </div>
</body>
</html>
