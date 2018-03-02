
<%@ page import="org.pih.warehouse.core.Party" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'party.label', default: 'Party')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${partyInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${partyInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['party']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['party']"/></g:link>
			</div>


			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="partyType"><warehouse:message code="party.partyType.label" default="Party Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: partyInstance, field: 'partyType', 'errors')}">
									<g:select class="chzn-select-deselect" name="partyType.id" from="${org.pih.warehouse.core.PartyType.list()}" optionKey="id" value="${partyInstance?.partyType?.id}"  />
								</td>
							</tr>
						

							<tr class="prop">
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons left">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

									   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

									</div>
								</td>
							</tr>

						</tbody>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
