<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom"/>
    <title><warehouse:message code="default.dataExports.label" default="Data Exports"/></title>
</head>

<body>
<div role="main" class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.error}">
        <div class="error">${flash.error}</div>
    </g:if>
    <div class="button-bar">
    </div>

    <div class="yui-ga">
        <div class="yui-u first">
            <div class="box">
                <h2><warehouse:message code="default.dataExports.label" default="Data Exports"/></h2>
                <div class="filter-list-item">
                    <g:each in="${documents}" var="document">
                        ${document.name}
                        <g:link controller="dataExport" action="render" id="${document.id}" params="[format:'csv']" class="button">
                             CSV
                        </g:link>
                        <g:link controller="dataExport" action="render" id="${document.id}" params="[format:'json']" class="button">
                             JSON
                        </g:link>
                    </g:each>
                </div>
            </div>
        </div>
    </div>

</div>
</body>
</html>
