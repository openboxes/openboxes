
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productType.label', default: 'ProductType')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="box">
                <h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.id.label" default="Id" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "id")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.name.label" default="Name" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "name")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.productTypeCode.label" default="Product Type Code" /></td>

                            <td valign="top" class="value">${productTypeInstance?.productTypeCode?.encodeAsHTML()}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.productIdentifierFormat.label" default="Product Identifier Format" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "productIdentifierFormat")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.supportedActivities.label" default="Supported Activities" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "supportedActivities")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.requiredFields.label" default="Required Fields" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "requiredFields")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.displayedFields.label" default="Displayed Fields" /></td>

                            <td valign="top" class="value">${fieldValue(bean: productTypeInstance, field: "displayedFields")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.dateCreated.label" default="Date Created" /></td>

                            <td valign="top" class="value"><format:datetime obj="${productTypeInstance?.dateCreated}" /></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="productType.lastUpdated.label" default="Last Updated" /></td>

                            <td valign="top" class="value"><format:datetime obj="${productTypeInstance?.lastUpdated}" /></td>

                        </tr>

						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">
					            <div class="buttons left">
					                <g:form>
					                    <g:hiddenField name="id" value="${productTypeInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
