
<%@ page import="org.pih.warehouse.core.PreferenceType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'preferenceTypes.label', default: 'Preference Types')}" />
    <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    <style>
        .paginateButtons {
            border: 0;
            border-top: 2px solid lightgrey;
        }
    </style>
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
                <g:isUserAdmin>
                    <g:link class="button" action="create">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[entityName]"/>
                    </g:link>
                </g:isUserAdmin>
            </div>

            <div class="box">
                <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                <table>
                    <thead>
                        <tr>

                            <g:sortableColumn property="id" title="${warehouse.message(code: 'preferenceType.id.label', default: 'Id')}" />

                            <g:sortableColumn property="name" title="${warehouse.message(code: 'preferenceType.name.label', default: 'Name')}" />

                            <g:sortableColumn property="validationCode" title="${warehouse.message(code: 'preferenceType.validationCode.label', default: 'Validation Code')}" />

                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'preferenceType.dateCreated.label', default: 'Date Created')}" />

                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'preferenceType.lastUpdated.label', default: 'Date Updated')}" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${preferenceTypes}" status="i" var="preferenceType">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td><g:link action="edit" id="${preferenceType.id}">${fieldValue(bean: preferenceType, field: "id")}</g:link></td>

                            <td>${fieldValue(bean: preferenceType, field: "name")}</td>

                            <td>${fieldValue(bean: preferenceType, field: "validationCode")}</td>

                            <td><format:date obj="${preferenceType.dateCreated}" /></td>

                            <td><format:date obj="${preferenceType.lastUpdated}" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${preferenceTypesTotal}" />
                </div>
            </div>
        </div>
    </div>
</body>
</html>
