
<%@ page import="org.pih.warehouse.product.ProductGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'productGroup.label', default: 'ProductGroup')}" />
<title><warehouse:message code="default.edit.label"
		args="[entityName]" /></title>
<!-- Specify content to overload like global navigation links, page titles, etc. -->
<content tag="pageTitle"> <warehouse:message
	code="default.edit.label" args="[entityName]" /></content>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${productGroupInstance}">
			<div class="errors">
				<g:renderErrors bean="${productGroupInstance}" as="list" />
			</div>
		</g:hasErrors>
		<div class="buttonBar">            	
           	<span class="linkButton">
           		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
           	</span>
           	<span class="linkButton">
           		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
           	</span>
		</div>
		<g:form method="post" action="update">
				<g:hiddenField name="id" value="${productGroupInstance?.id}" />
				<g:hiddenField name="version" value="${productGroupInstance?.version}" />
				<div class="dialog">
					
				
				
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label for="description"><warehouse:message
											code="productGroup.description.label" default="Description" /></label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: productGroupInstance, field: 'description', 'errors')}">
									<g:textField name="description" class="text"
										value="${productGroupInstance?.description}" size="60" />
								</td>
							</tr>


							<tr class="prop">
								<td valign="top" class="name"><label for="category"><warehouse:message
											code="productGroup.category.label" default="Category" /></label></td>
								<td valign="top" class="value">
									<%-- 
									<g:selectCategoryMcDropdown id="category" name="category.id" 
										value="${productGroupInstance?.category?.id}"/>									
									--%>
									<g:hiddenField name="oldCategory.id" value="${productGroupInstance?.category?.id }"/>
									<g:categorySelect id="category" name="category.id" 
										value="${productGroupInstance?.category?.id}"/>									

								</td>									
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateCreated"><warehouse:message code="productGroup.dateCreated.label" default="Date Created" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productGroupInstance, field: 'dateCreated', 'errors')}">
                                   ${productGroupInstance?.dateCreated} 
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="lastUpdated"><warehouse:message code="productGroup.lastUpdated.label" default="Last Updated" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productGroupInstance, field: 'lastUpdated', 'errors')}">
                                    ${productGroupInstance?.lastUpdated}
                                </td>
                            </tr>
							<tr class="prop">
								<td valign="top" class="name"><label for="products"><warehouse:message
											code="productGroup.products.label" default="Products" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: productGroupInstance, field: 'products', 'errors')}">
									<g:selectProducts 
										id="products"
										name="product.id" 
										category="${productGroupInstance?.category }"
										value="${productGroupInstance?.products }"/>
								</td>
							</tr>									

							<%-- 
							<tr class="prop">
								<td valign="top" class="name"><label for="products"><warehouse:message
											code="productGroup.products.label" default="Products" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: productGroupInstance, field: 'products', 'errors')}">
									<g:select name="products"
										from="${org.pih.warehouse.product.Product.list()}"
										multiple="yes" optionKey="id" size="5"
										value="${productGroupInstance?.products}" />
								</td>
							</tr>
							--%>

							<tr class="prop">
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons">
										<g:actionSubmit class="save" action="update"
											value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
										<g:actionSubmit class="delete" action="delete"
											value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}"
											onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
		</g:form>
	</div>
<script>
	$(document).ready(function() {			
		$("#category").change(function() {
		    $(this).closest("form").submit();
		});
	});		
</script>	
	
</body>
</html>
