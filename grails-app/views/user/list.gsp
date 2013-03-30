
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


			<div class="buttonBar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'users.label').toLowerCase()]"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'user.label').toLowerCase()]"/></g:link>
           	</div>

            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="dialog box">
                        <g:form action="list" method="get">
                            <ul class="filter-list">
                                <li class="filter-list-item">
                                    <label><warehouse:message code="user.search.label"/></label>
                                </li>
                                <li class="filter-list-item">
                                    <g:textField name="q" size="40" value="${params.q }" class="text"/>
                                </li>
                                <li class="filter-list-item">
                                    <button type="submit" class="button">${warehouse.message(code: 'default.button.find.label')}</button>
                                </li>
                            </ul>
                        </g:form>
                    </div>


                </div>
                <div class="yui-u">

                    <div class="list box dialog">
                        <table>
                            <thead>
                                <tr>
                                    <g:sortableColumn property="username" title="${warehouse.message(code: 'user.username.label')}" />
                                    <g:sortableColumn property="firstName" title="${warehouse.message(code: 'default.name.label')}" />
                                    <g:sortableColumn property="email" title="${warehouse.message(code: 'user.email.label')}" />
                                    <g:sortableColumn property="locale" title="${warehouse.message(code: 'default.locale.label')}" />
                                <!--      <g:sortableColumn property="email" title="${warehouse.message(code: 'user.role.label', default: 'Roles')}" />  -->
                                    <g:sortableColumn property="role" title="${warehouse.message(code: 'user.roles.label')}" />
                                    <g:sortableColumn property="active" title="${warehouse.message(code: 'user.active.label')}" />
                                </tr>
                            </thead>
                            <tbody>
                            <g:each in="${userInstanceList}" status="i" var="userInstance">
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td><g:link action="show" id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></td>
                                    <td>${fieldValue(bean: userInstance, field: "name")}</td>
                                    <td>${fieldValue(bean: userInstance, field: "email")}</td>
                                    <td>${fieldValue(bean: userInstance, field: "locale.displayName")}</td>
                                    <td>${fieldValue(bean: userInstance, field: "roles")}</td>
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
            </div>
        </div>

    </body>
</html>
