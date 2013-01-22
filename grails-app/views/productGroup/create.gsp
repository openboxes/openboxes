
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
			<g:link class="button" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
	        <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'productGroup.label').toLowerCase()]"/></g:link>
		</div>
		<g:form action="save" method="post">
			<fieldset>
				<div class="dialog">
				
				
					
					<table>
						<tbody>

							<tr class="prop">
								<td valign="middle" class="name"><label for="category"><warehouse:message
											code="productGroup.category.label" default="Category" /></label></td>
								<td valign="middle" class="value ${hasErrors(bean: productGroupInstance, field: 'category', 'errors')}">
									<%-- Show category if coming from Inventory Browser --%>									
									<g:if test="${productGroupInstance?.category }">
							        	<format:category category="${productGroupInstance?.category }"/>
										<g:hiddenField id="category" name="category.id" 
											value="${productGroupInstance?.category?.id }" />
									</g:if>
									<g:else>
										<g:categorySelect id="category" name="category.id" 
											value="${productGroupInstance?.category?.id}"/>									
									</g:else>
									
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name"><label for="description"><warehouse:message
											code="productGroup.name.label" default="Generic product" /></label>
								</td>
								<td valign="middle"
									class="value ${hasErrors(bean: productGroupInstance, field: 'description', 'errors')}">
									
									<g:if test="${productGroups }">	
										<div>										
											<g:select name="id" from="${productGroups }" 
												optionKey="id" optionValue="description" value="${productGroupInstance?.id }" noSelection="['null':'']"/>
										</div>
									</g:if>
									<g:else>
										<div>
											<g:textField name="description" class="text"
												value="${productGroupInstance?.description}" size="60" />
										</div>
									</g:else>
										
									
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
											<g:each var="product" in="${productGroupInstance?.products }" status="status">
												<tr class="${status%2?'even':'odd' }">
													<td class="middle" width="1%">
														<g:checkBox id="productId" name="product.id" value="${product?.id }"/>
													</td>
													<td class="checkable middle">
														<span class="fade">${product?.productCode }</span>	
													</td>
													<td class="checkable middle">			
														<g:link name="productLink" controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory" style="z-index: 999">
															<span title="${product?.description }">				
																<g:if test="${product?.name?.trim()}">
																	${product?.name}
																</g:if>
																<g:else>
																	<warehouse:message code="product.untitled.label"/>
																</g:else>
															</span>
														</g:link> 
													</td>
													<td class="checkable middle left">
														<span class="fade">${product?.manufacturer }</span>
													</td>
													<td class="checkable middle left">
														<span class="fade">${product?.brandName}</span>	
													</td>
													<td class="checkable middle left">
														<span class="fade">${product?.manufacturerCode }</span>
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
