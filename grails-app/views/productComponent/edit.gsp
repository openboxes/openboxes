
<%@ page import="org.pih.warehouse.product.ProductComponent" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productComponent.label', default: 'ProductComponent')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productComponentInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productComponentInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productComponent']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productComponent']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${productComponentInstance?.id}" />
				<g:hiddenField name="version" value="${productComponentInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="componentProduct"><warehouse:message code="productComponent.componentProduct.label" default="Component Product" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'componentProduct', 'errors')}">
									<g:select class="chzn-select-deselect" name="componentProduct.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${productComponentInstance?.componentProduct?.id}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="quantity"><warehouse:message code="productComponent.quantity.label" default="Quantity" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'quantity', 'errors')}">
									<g:textField class="text" name="quantity" value="${fieldValue(bean: productComponentInstance, field: 'quantity')}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="unitOfMeasure"><warehouse:message code="productComponent.unitOfMeasure.label" default="Unit Of Measure" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'unitOfMeasure', 'errors')}">
									<g:select class="chzn-select-deselect" name="unitOfMeasure.id" from="${org.pih.warehouse.core.UnitOfMeasure.list()}" optionKey="id" value="${productComponentInstance?.unitOfMeasure?.id}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="assemblyProduct"><warehouse:message code="productComponent.assemblyProduct.label" default="Assembly Product" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'assemblyProduct', 'errors')}">
									<g:select class="chzn-select-deselect" name="assemblyProduct.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${productComponentInstance?.assemblyProduct?.id}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="dateCreated"><warehouse:message code="productComponent.dateCreated.label" default="Date Created" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'dateCreated', 'errors')}">
									<g:datePicker name="dateCreated" precision="minute" value="${productComponentInstance?.dateCreated}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="lastUpdated"><warehouse:message code="productComponent.lastUpdated.label" default="Last Updated" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productComponentInstance, field: 'lastUpdated', 'errors')}">
									<g:datePicker name="lastUpdated" precision="minute" value="${productComponentInstance?.lastUpdated}"  />
								</td>
							</tr>
						
						</tbody>
                        <tfoot>
                            <tr class="prop">
                                <td valign="top"></td>
                                <td valign="top left">
                                    <div class="buttons left">
                                        <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                        <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
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
