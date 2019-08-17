
<%@ page import="org.pih.warehouse.product.ProductCatalog" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog')}" />
    <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div class="list">

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[entityName]"/>
                </g:link>
                <g:isSuperuser>
                    <g:link class="button" action="create">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[entityName]"/>
                    </g:link>
                </g:isSuperuser>
            </div>

            <div class="box">
                <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                <table>
                    <thead>
                        <tr>

                            <g:sortableColumn property="id" title="${warehouse.message(code: 'productCatalog.id.label', default: 'Id')}" />

                            <g:sortableColumn property="code" title="${warehouse.message(code: 'productCatalog.code.label', default: 'Code')}" />

                            <g:sortableColumn property="name" title="${warehouse.message(code: 'productCatalog.name.label', default: 'Name')}" />

                            <g:sortableColumn property="description" title="${warehouse.message(code: 'productCatalog.description.label', default: 'Description')}" />

                            <g:sortableColumn property="active" title="${warehouse.message(code: 'productCatalog.active.label', default: 'Active')}" />

                            <g:sortableColumn property="color"
                                              title="${warehouse.message(code: 'productCatalog.color.label', default: 'Color')}"/>

                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'productCatalog.dateCreated.label', default: 'Date Created')}" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productCatalogInstanceList}" status="i" var="productCatalogInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td><g:link action="edit" id="${productCatalogInstance.id}">${fieldValue(bean: productCatalogInstance, field: "id")}</g:link></td>

                            <td>${fieldValue(bean: productCatalogInstance, field: "code")}</td>

                            <td>${fieldValue(bean: productCatalogInstance, field: "name")}</td>

                            <td>${fieldValue(bean: productCatalogInstance, field: "description")}</td>

                            <td><g:formatBoolean boolean="${productCatalogInstance.active}" /></td>

                            <td>${productCatalogInstance.color}</td>

                            <td><format:date obj="${productCatalogInstance.dateCreated}" /></td>

                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="paginateButtons">
            <g:paginate total="${productCatalogInstanceTotal}" />
        </div>
    </div>
</body>
</html>
