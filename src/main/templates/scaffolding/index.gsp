<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${message(code: '${propertyName}.label', default: '${className}')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <asset:stylesheet src="main.css"/>
        <asset:stylesheet src="mobile.css"/>
    </head>
    <body>
        <div class="button-bar">
            <g:link class="button" action="index">
                <img src="\${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[entityName]"/>
            </g:link>
            <g:link class="button" action="create">
                <img src="\${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.add.label" args="[entityName]"/>
            </g:link>
        </div>
        <div id="list-${propertyName}" class="box content scaffold-create" role="main">
            <h1><warehouse:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="\${flash.message}">
                <div class="message" role="status">\${flash.message}</div>
            </g:if>
            <f:table collection="\${${propertyName}List}" />
            <g:set var="totalCount" value="\${${propertyName}Count ?: 0}"/>
            <g:if test="\${totalCount > params.max}">
                <div class="pagination">
                    <g:paginate total="\${${propertyName}Count ?: 0}" />
                </div>
            </g:if>
        </div>
    </body>
</html>
