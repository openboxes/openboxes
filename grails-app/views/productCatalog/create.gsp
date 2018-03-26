
<%@ page import="org.pih.warehouse.product.ProductCatalog" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productCatalogInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productCatalogInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productCatalog']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productCatalog']"/></g:link>
			</div>


			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="code"><warehouse:message code="productCatalog.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'code', 'errors')}">
									<g:textField class="text" size="80" name="code" value="${productCatalogInstance?.code}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><warehouse:message code="productCatalog.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'name', 'errors')}">
									<g:textField class="text" size="80" name="name" value="${productCatalogInstance?.name}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="description"><warehouse:message code="productCatalog.description.label" default="Description" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'description', 'errors')}">
									<g:textField class="text" size="80" name="description" value="${productCatalogInstance?.description}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="active"><warehouse:message code="productCatalog.active.label" default="Active" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'active', 'errors')}">
									<g:checkBox name="active" value="${productCatalogInstance?.active}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="dateCreated"><warehouse:message code="productCatalog.dateCreated.label" default="Date Created" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'dateCreated', 'errors')}">
									<g:datePicker name="dateCreated" precision="minute" value="${productCatalogInstance?.dateCreated}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="lastUpdated"><warehouse:message code="productCatalog.lastUpdated.label" default="Last Updated" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productCatalogInstance, field: 'lastUpdated', 'errors')}">
									<g:datePicker name="lastUpdated" precision="minute" value="${productCatalogInstance?.lastUpdated}"  />
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
