
<%@ page import="org.pih.warehouse.product.ProductAssociation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productAssociationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productAssociationInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productAssociation']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productAssociation']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${productAssociationInstance?.id}" />
				<g:hiddenField name="version" value="${productAssociationInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="code"><warehouse:message code="productAssociation.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'code', 'errors')}">
									<g:select class="chzn-select-deselect" name="code" from="${org.pih.warehouse.product.ProductAssociationTypeCode?.values()}" value="${productAssociationInstance?.code}"  />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="product"><warehouse:message code="productAssociation.product.label" default="Product" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'product', 'errors')}">
									<g:autoSuggest id="product" name="product" valueId="${productAssociationInstance?.product?.id}"
												   valueName="${productAssociationInstance?.product?.name}"
												   jsonUrl="${request.contextPath }/json/findProductByName" styleClass="text" />
									%{--<g:select class="chzn-select-deselect" name="product.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${productAssociationInstance?.product?.id}"  />--}%
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="associatedProduct"><warehouse:message code="productAssociation.associatedProduct.label" default="Associated Product" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'associatedProduct', 'errors')}">
									<g:autoSuggest id="associatedProduct" name="associatedProduct" valueId="${productAssociationInstance?.associatedProduct?.id}"
												   valueName="${productAssociationInstance?.associatedProduct?.name}" jsonUrl="${request.contextPath }/json/findProductByName" styleClass="text" />
									%{--<g:select class="chzn-select-deselect" name="associatedProduct.id" from="${org.pih.warehouse.product.Product.list()}" optionKey="id" value="${productAssociationInstance?.associatedProduct?.id}"  />--}%
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="quantity"><warehouse:message code="productAssociation.quantity.label" default="Quantity" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'quantity', 'errors')}">
									<g:textField class="text" name="quantity" value="${fieldValue(bean: productAssociationInstance, field: 'quantity')}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="comments"><warehouse:message code="productAssociation.comments.label" default="Comments" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'comments', 'errors')}">
									<g:textArea class="text" name="comments" value="${productAssociationInstance?.comments}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="dateCreated"><warehouse:message code="productAssociation.dateCreated.label" default="Date Created" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'dateCreated', 'errors')}">
									<g:datePicker name="dateCreated" precision="minute" value="${productAssociationInstance?.dateCreated}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="lastUpdated"><warehouse:message code="productAssociation.lastUpdated.label" default="Last Updated" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productAssociationInstance, field: 'lastUpdated', 'errors')}">
									<g:datePicker name="lastUpdated" precision="minute" value="${productAssociationInstance?.lastUpdated}"  />
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
