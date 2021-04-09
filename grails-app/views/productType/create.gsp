
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productType.label', default: 'ProductType')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productType']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productType']"/></g:link>
			</div>


			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><warehouse:message code="productType.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'name', 'errors')}">
									<g:textField class="text" size="80" name="name" value="${productTypeInstance?.name}" />

								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label><warehouse:message code="productType.productTypeCode.label" default="Product Type Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productTypeCode', 'errors')}">
									${productTypeInstance?.productTypeCode}

								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="productIdentifierFormat"><warehouse:message code="productType.productIdentifierFormat.label" default="Product Identifier Format" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productIdentifierFormat', 'errors')}">
									<g:textField class="text" size="80" name="productIdentifierFormat" value="${productTypeInstance?.productIdentifierFormat}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="code"><warehouse:message code="productType.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'code', 'errors')}">
									<g:textField class="text" size="80" name="code" value="${productTypeInstance?.code}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="supportedActivities"><warehouse:message code="productType.supportedActivities.label" default="Supported Activities" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'supportedActivities', 'errors')}">
									<g:set var="activityList" value="${org.pih.warehouse.product.ProductActivityCode.values()}"/>
									<g:select name="supportedActivities" multiple="true" from="${activityList}"
											  size="${activityList.size()}" class="chzn-select-deselect"
											  optionValue="${{ format.metadata(obj: it) }}"
											  value="${productTypeInstance?.supportedActivities}"/>

								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="requiredFields"><warehouse:message code="productType.requiredFields.label" default="Required Fields" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'requiredFields', 'errors')}">
									<g:set var="productFields" value="${org.pih.warehouse.product.ProductField.values()}"/>
									<g:select name="requiredFields" multiple="true" from="${productFields}"
											  size="${productFields.size()}" class="chzn-select-deselect"
											  optionValue="${{ format.metadata(obj: it) }}"
											  value="${productTypeInstance?.requiredFields}"
											  disabled="${true}"/>

								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="displayedFields"><warehouse:message code="productType.displayedFields.label" default="Displayed Fields" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'displayedFields', 'errors')}">
									<g:set var="productFields" value="${org.pih.warehouse.product.ProductField.values()}"/>
									<g:select name="displayedFields" multiple="true" from="${productFields}"
											  size="${productFields.size()}" class="chzn-select-deselect"
											  optionValue="${{ format.metadata(obj: it) }}"
											  value="${productTypeInstance?.displayedFields}"/>

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
