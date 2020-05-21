<%@ page import="org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'organization.label', default: 'Organization')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="['organization']"/>
                </g:link>
                <g:link class="button" action="create">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" args="['organization']"/>
                </g:link>
            </div>

            <div class="yui-gf">
				<div class="yui-u first">
					<g:render template="filters" model="[]"/>
				</div>
				<div class="yui-u">
                    <div class="box">
                        <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                        <table>
                            <thead>
                                <tr>

                                    <g:sortableColumn property="id" title="${warehouse.message(code: 'organization.id.label', default: 'Id')}" />

                                    <g:sortableColumn property="code" title="${warehouse.message(code: 'organization.code.label', default: 'Code')}" />

                                    <g:sortableColumn property="name" title="${warehouse.message(code: 'organization.name.label', default: 'Name')}" />

                                    <th><g:message code="roles.label" default="Roles"/></th>

                                </tr>
                            </thead>
                            <tbody>
                            <g:each in="${organizationInstanceList}" status="i" var="organizationInstance">
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                    <td><g:link action="edit" id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "id")}</g:link></td>

                                    <td><g:link action="edit" id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "code")}</g:link></td>

                                    <td><g:link action="edit" id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "name")}</g:link></td>

                                    <td>${organizationInstance.roles.join(",")}</td>

                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        <div class="paginateButtons">
                            <g:set var="pageParams" value="${pageScope.variables['params']}"/>
                            <g:paginate total="${organizationInstanceTotal}" params="${params}" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
