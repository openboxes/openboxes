
<%@ page import="org.pih.warehouse.user.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
	<content tag="globalLinks"><!-- Specify global navigation links -->
	    <span class="menuButton">
		<g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link>
	    </span>
	</content>
	<content tag="pageTitle"><!-- Specify page title -->
	    <g:message code="default.list.label" args="[entityName]" />
	</content>

    </head>
    <body>
        <div class="body">
	    <g:if test="${flash.message}">
	      <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${message(code: 'user.id.label', default: 'Id')}" />
                            <g:sortableColumn property="username" title="${message(code: 'user.username.label', default: 'Username')}" />
                            <g:sortableColumn property="name" title="${message(code: 'user.name.label', default: 'Name')}" />
                            <g:sortableColumn property="password" title="${message(code: 'user.password.label', default: 'Password')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userInstanceList}" status="i" var="userInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${userInstance.id}">${fieldValue(bean: userInstance, field: "id")}</g:link></td>
                            <td>${fieldValue(bean: userInstance, field: "username")}</td>
                            <td>${fieldValue(bean: userInstance, field: "lastName")}, ${fieldValue(bean: userInstance, field: "firstName")}</td>
                            <td>${fieldValue(bean: userInstance, field: "password")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${userInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
