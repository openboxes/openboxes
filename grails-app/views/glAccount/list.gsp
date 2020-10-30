
<%@ page import="org.pih.warehouse.core.GlAccount" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'glAccounts.label', default: 'GL Accounts')}" />
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

                            <g:sortableColumn property="id" title="${warehouse.message(code: 'glAccount.id.label', default: 'Id')}" />

                            <g:sortableColumn property="code" title="${warehouse.message(code: 'glAccount.code.label', default: 'Code')}" />

                            <g:sortableColumn property="name" title="${warehouse.message(code: 'glAccount.name.label', default: 'Name')}" />

                            <g:sortableColumn property="description" title="${warehouse.message(code: 'glAccount.description.label', default: 'Description')}" />

                            <g:sortableColumn property="glAccountType" title="${warehouse.message(code: 'glAccount.glAccountType.label', default: 'GL Account Type')}" />

                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'glAccount.dateCreated.label', default: 'Date Created')}" />

                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'glAccount.lastUpdated.label', default: 'Date Updated')}" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${glAccounts}" status="i" var="glAccount">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td><g:link action="edit" id="${glAccount.id}">${fieldValue(bean: glAccount, field: "id")}</g:link></td>

                            <td>${fieldValue(bean: glAccount, field: "code")}</td>

                            <td>${fieldValue(bean: glAccount, field: "name")}</td>

                            <td>${fieldValue(bean: glAccount, field: "description")}</td>

                            <td>${fieldValue(bean: glAccount.glAccountType, field: "code")}</td>

                            <td><format:date obj="${glAccount.dateCreated}" /></td>

                            <td><format:date obj="${glAccount.lastUpdated}" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${glAccountsTotal}" />
                </div>
            </div>
        </div>
    </div>
</body>
</html>
