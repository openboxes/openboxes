<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationGroups.label', default: 'Location Groups')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

            <div class="button-bar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[g.message(code:'locationGroups.label')]"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[g.message(code:'locationGroup.label')]"/></g:link>
            </div>

			<div class="box">
                <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                <table>
                    <thead>
                        <tr>      
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" class="bottom"/>
                            <g:sortableColumn property="description" title="${warehouse.message(code: 'default.description.label')}" class="bottom"/>
                            <g:sortableColumn property="locations" title="${warehouse.message(code: 'locations.label')}" class="bottom"/>
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${locationGroupInstanceList}" status="i" var="locationGroupInstance">
							<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>
									<g:link action="edit" id="${locationGroupInstance.id}">${fieldValue(bean: locationGroupInstance, field: "name")}</g:link>
								</td>
								<td>
                                    ${locationGroupInstance?.address?.description}
								</td>
                                <td>
                                    ${locationGroupInstance.locations.size()}
                                </td>
							</tr>
	                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${locationGroupInstanceTotal}" />
                </div>
            </div>
        </div>
    </body>
</html>
