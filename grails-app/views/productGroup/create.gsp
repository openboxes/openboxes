
<%@ page import="org.pih.warehouse.product.ProductGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'productGroup.label', default: 'ProductGroup')}" />
<title><warehouse:message code="default.create.label"
		args="[entityName]" /></title>
<!-- Specify content to overload like global navigation links, page titles, etc. -->
<content tag="pageTitle">
<warehouse:message code="default.create.label" args="[entityName]" /></content>
<style type="text/css">
.CheckBoxList {
	height: 300px;
	overflow: auto;
	overflow-x: hidden;
	border: 0px solid #000;
	list-style-type: none;
	margin: 0;
	padding: 0px
}

.CheckBoxList li {
	padding: 2px;
}

.CheckBoxList input {
	padding-right: 5px;
}
</style>

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

		<g:form action="save" method="post">
			<fieldset>
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
								<td valign="top" class="value ${hasErrors(bean: productGroupInstance, field: 'category', 'errors')}">
									
						        	<format:category category="${productGroupInstance?.category }"/>
									<g:hiddenField id="category" name="category.id" 
										value="${productGroupInstance?.category?.id }" />
								</td>
							</tr>
							<g:if test="${productGroupInstance?.products }">
								<tr class="prop">
									<td valign="top" class="name">
										<label for="products"><warehouse:message
												code="productGroup.products.label" default="Products" /></label>
									</td>
									<td class="value ${hasErrors(bean: productGroupInstance, field: 'products', 'errors')}">
										<table>
											<g:each var="product" in="${productGroupInstance?.products }">
												<tr>
													<td>
														<g:hiddenField name="product.id" value="${product.id }"/>
														${product?.name }
													</td>
												</tr>																					
											</g:each>
										</table>
									</td>
								</tr>
							</g:if>
							
							
							<%-- 
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="dateCreated"><warehouse:message code="productGroup.dateCreated.label" default="Date Created" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productGroupInstance, field: 'dateCreated', 'errors')}">
	                                    <g:datePicker name="dateCreated" precision="minute" value="${productGroupInstance?.dateCreated}"  />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="lastUpdated"><warehouse:message code="productGroup.lastUpdated.label" default="Last Updated" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productGroupInstance, field: 'lastUpdated', 'errors')}">
	                                    <g:datePicker name="lastUpdated" precision="minute" value="${productGroupInstance?.lastUpdated}"  />
	                                </td>
	                            </tr>
	                        	--%>

							<tr class="prop">
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons">
										<g:submitButton name="create" class="save"
											value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

										<g:link action="list">
											${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
										</g:link>

									</div>
								</td>
							</tr>

						</tbody>
					</table>
				</div>
			</fieldset>
		</g:form>
	</div>
	
</body>
</html>
