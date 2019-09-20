<%@ page import="org.pih.warehouse.core.LocationRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationRole.label', default: 'LocationRole')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationRoleInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationRoleInstance}" as="list" />
	            </div>
            </g:hasErrors>
			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['locationRole']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['locationRole']"/></g:link>
			</div>
			<g:form action="save" method="post">
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<g:render template="form"/>
						</tbody>
                        <tfoot>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
									<div class="buttons left">
										<g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
										<g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
									</div>
								</td>
							</tr>
                        </tfoot>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
