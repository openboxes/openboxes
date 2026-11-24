<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'productComponent.label', default: 'ProductComponent')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <g:render template="/common/scaffoldButtonBar" model="[entityName: entityName]" />
            <div id="list-productComponent" class="box" role="main">
                <h2><g:message code="default.list.label" args="[entityName]" /></h2>
                <f:table collection="${productComponentList}" properties="['id', 'assemblyProduct', 'componentProduct', 'quantity', 'unitOfMeasure']" />
                <g:set var="totalCount" value="${productComponentCount ?: 0}"/>
                <g:if test="${totalCount > params.max}">
                    <div class="paginateButtons border-top">
                        <g:paginate total="${totalCount}" />
                    </div>
                </g:if>
            </div>
        </div>
    </body>
</html>
