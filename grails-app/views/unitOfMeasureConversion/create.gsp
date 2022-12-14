<%@ page import="org.pih.warehouse.core.UnitOfMeasure" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'unitOfMeasureConversion.label', default: 'Unit of Measure Conversion')}" />
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
            <g:hasErrors bean="${unitOfMeasureConversion}">
	            <div class="errors">
	                <g:renderErrors bean="${unitOfMeasureConversion}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
                	<img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="['Unit of Measure Conversion']"/>
				</g:link>
				<g:link class="button" action="create">
                	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="['Unit of Measure Conversion']"/>
				</g:link>
			</div>

			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="middle" class="name">
									<label for="fromUnitOfMeasure.id"><warehouse:message code="unitOfMeasureConversion.fromUnitOfMeasure.label" default="From Unit Of Measure" /></label>
								</td>
								<td valign="middle" class="value">
									<g:select name="fromUnitOfMeasure.id"
											  id="fromUnitOfMeasure.id"
											  value="${unitOfMeasureConversion.fromUnitOfMeasure?.id}"
											  from="${UnitOfMeasure.list() }"
											  optionKey="id" optionValue="name"
											  data-placeholder="Choose a unit of measure"
											  class="chzn-select-deselect"
											  noSelection="['null':'']" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="middle" class="name">
									<label for="toUnitOfMeasure.id"><warehouse:message code="unitOfMeasureConversion.toUnitOfMeasure.label" default="To Unit Of Measure" /></label>
								</td>
								<td valign="middle" class="value">
									<g:select name="toUnitOfMeasure.id"
											  id="toUnitOfMeasure.id"
											  value="${unitOfMeasureConversion.toUnitOfMeasure?.id}"
											  from="${UnitOfMeasure.list() }"
											  optionKey="id" optionValue="name"
											  data-placeholder="Choose a unit of measure"
											  class="chzn-select-deselect"
											  noSelection="['null':'']" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="middle" class="name">
									<label for="conversionRate"><warehouse:message code="unitOfMeasureConversion.conversionRate.label" default="Conversion Rate" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: unitOfMeasureConversion, field: 'conversionRate', 'errors')}">
									<g:textField class="text" size="80" name="conversionRate" value="${unitOfMeasureConversion?.conversionRate}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="active"><g:message code="default.active.label" default="Active" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: unitOfMeasureConversion, field: 'active', 'errors')}">
									<g:checkBox name="active" value="${unitOfMeasureConversion?.active}" />
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
