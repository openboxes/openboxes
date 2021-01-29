
<%@ page import="org.pih.warehouse.core.PreferenceType" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'preferenceType.label', default: 'Preference Type')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
		<style>
			.chosen-container > a, .chosen-drop {
				max-width: 500px;
			}
		</style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${preferenceType}">
	            <div class="errors">
	                <g:renderErrors bean="${preferenceType}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
                	<img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="['Preference Type']"/>
				</g:link>
				<g:link class="button" action="create">
                	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="['Preference Type']"/>
				</g:link>
			</div>

			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="middle" class="name">
									<label for="name"><warehouse:message code="preferenceType.name.label" default="Name" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: preferenceType, field: 'name', 'errors')}">
									<g:textField class="text" size="80" name="name" value="${preferenceType?.name}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="validationCode"><warehouse:message code="preferenceType.validationCode.label" default="Validation Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: preferenceType, field: 'validationCode', 'errors')}">
									<g:select name="validationCode" from="${org.pih.warehouse.core.ValidationCode?.values()}"
											  value="${preferenceType?.validationCode}" noSelection="['': '']" class="chzn-select-deselect"/>
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle"></td>
								<td valign="middle">
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
