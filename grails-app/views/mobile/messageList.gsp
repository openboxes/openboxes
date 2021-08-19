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
                    <td>
                        <g:link controller="mobile" action="messageDetails" params="[filename: message.filename]">Retrieve</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</body>
</html>
