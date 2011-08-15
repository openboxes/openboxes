
<%@ page import="org.pih.warehouse.core.User" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="users.label" /></title>
</head>
<body>
    <div class="body">
		    <g:if test="${flash.message}">
		    	<div class="message">${flash.message}</div>
	        </g:if>

            <div>
				<span class="menuButton">
           			<g:link controller="user" action="create" class="new"><warehouse:message code="user.addNewUser.label"/></g:link>
	           	</span>
           	</div>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'default.id.label')}" />
                            <g:sortableColumn property="username" title="${warehouse.message(code: 'user.username.label')}" />
                            <g:sortableColumn property="firstName" title="${warehouse.message(code: 'default.name.label')}" />
                            <g:sortableColumn property="email" title="${warehouse.message(code: 'user.email.label')}" />
                            <g:sortableColumn property="locale" title="${warehouse.message(code: 'default.locale.label')}" />
                        <!--      <g:sortableColumn property="email" title="${warehouse.message(code: 'user.role.label', default: 'Roles')}" />  -->
                            <g:sortableColumn property="active" title="${warehouse.message(code: 'user.active.label')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userInstanceList}" status="i" var="userInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>${fieldValue(bean: userInstance, field: "id")}</td>
                            <td><g:link action="show" id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></td>
                            <td>${fieldValue(bean: userInstance, field: "name")}</td>
                            <td>${fieldValue(bean: userInstance, field: "email")}</td>
                            <td>${fieldValue(bean: userInstance, field: "locale.displayName")}</td>
                            <td>
                            	<g:if test="${userInstance?.active }"><warehouse:message code="default.yes.label"/></g:if>
                            	<g:else><warehouse:message code="default.no.label"/></g:else>
                            </td>
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
