
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productType.label', default: 'ProductType')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
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

			<g:form method="post" >
				<g:hiddenField name="id" value="${productTypeInstance?.id}" />
				<g:hiddenField name="version" value="${productTypeInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
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


							<g:if test="${org.pih.warehouse.product.Product.findAllByProductType(productTypeInstance)?.size() == 0}">
								<tr class="prop">
									<td valign="top" class="name">
										<label><warehouse:message code="productType.productIdentifierFormat.label" default="Product Identifier Format" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productIdentifierFormat', 'errors')}">
										${productTypeInstance?.productIdentifierFormat}

									</td>
								</tr>
							</g:if>

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
