
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        
        <g:if test="${productInstance?.id}">
	        <title><g:message code="default.edit.label" args="[entityName]" /></title>
			<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
		</g:if>
		<g:else>
	        <title><g:message code="default.create.label" args="[entityName]" /></title>
			<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>		
		</g:else>
    </head>    
    <body>    
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<g:set var="formAction"><g:if test="${productInstance?.id}">update</g:if><g:else>save</g:else></g:set>			

            <g:form action="${formAction}" method="post">
				<g:hiddenField name="action" value="save"/>                					
            	<g:if test="${productInstance?.id}">
	                <g:hiddenField name="id" value="${productInstance?.id}" />
	                <g:hiddenField name="version" value="${productInstance?.version}" />
				</g:if>
                <div class="dialog">
					<table>
						<tbody>
							<tr>
								<td>
									<fieldset>
					              		<legend>Basic</legend>
						                  <table>
						                      <tbody>                        
						                          <tr class="prop">
						                              <td valign="top" class="name">
						                                <label for="name"><g:message code="product.name.label" default="Name" /></label>
						                              </td>
						                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
						                                  <g:textField name="name" value="${productInstance?.name}" />
						                              </td>
						                          </tr>
						                          <tr class="prop">
						                              <td valign="top" class="name">
						                                <label for="type.id"><g:message code="product.type.label" default="Type" /></label>
						                              </td>
						                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productType', 'errors')}">
														${productInstance?.class?.simpleName}
						                              </td>
						                          </tr>
						                          <tr class="prop">
						                              <td valign="top" class="name">
						                                <label for="productType.id"><g:message code="product.productType.label" default="Product Type" /></label>
						                              </td>
						                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productType', 'errors')}">
						                                  <g:select name="productType.id" from="${org.pih.warehouse.product.ProductType.list()}" optionKey="id" value="${productInstance?.productType?.id}"  />
						                              </td>
						                          </tr>
						                          <tr class="prop">
						                              <td valign="top" class="name">
						                                <label for="ean"><g:message code="product.ean.label" default="UPC" /></label>
						                              </td>
						                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'ean', 'errors')}">
						                                  <g:textField name="ean" value="${productInstance?.ean}" />
						                              </td>
						                          </tr>
						                          <tr class="prop">
						                              <td valign="top" class="name">
						                                <label for="description"><g:message code="product.description.label" default="Description" /></label>
						                              </td>
						                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'description', 'errors')}">
						                                  <g:textField name="description" value="${productInstance?.description}" />
						                              </td>
						                          </tr>
						                      </tbody>
						                  </table>                    
									</fieldset>				
								</td>
								<td>
									<fieldset>
					               		<legend>Classification</legend>
					                    <table>
					                        <tbody>                        
					                            <tr class="prop">
					                                <td valign="top" class="name">
					                                  <label for="genericType"><g:message code="product.genericType.label" default="Generic Type" /></label>
					                                </td>
					                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'genericType', 'errors')}">
					                                    <g:select name="genericType.id" from="${org.pih.warehouse.product.GenericType.list()}" optionKey="id" value="${productInstance?.genericType?.id}" noSelection="['null': '']" />
					                                </td>
					                            </tr>
					                            <tr class="prop">
					                                <td valign="top" class="name">
					                                  <label for="categories"><g:message code="product.categories.label" default="Categories" /></label>
					                                </td>
					                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}">
					                                    <g:select name="categories" from="${org.pih.warehouse.product.Category.list()}" multiple="yes" optionKey="id" size="5" value="${productInstance?.categories}" />
					                                </td>
					                            </tr>
					                            <tr class="prop">
					                                <td valign="top" class="name">
					                                  <label for="tags"><g:message code="product.tags.label" default="Tags" /></label>
					                                </td>
					                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'tags', 'errors')}">
					                                    <g:textField name="tags" value="${productInstance?.tags}" />
					                                </td>
					                            </tr> 
					                        </tbody>
					                    </table>
									</fieldset>				
								</td>
							</tr>
							<tr>
								<td>                
										<g:if test="${productInstance.class.simpleName == 'DrugProduct' }">	
						                    <fieldset>
											
						                    	<legend>Drug</legend>
												<table>
													<tbody>
							                            <tr class="prop">
							                                <td valign="top" class="name">
							                                    <label for="genericName"><g:message code="drugProduct.genericName.label" default="Generic Name" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'genericName', 'errors')}">
							                                    <g:textField name="genericName" value="${drugProductInstance?.genericName}" />
							                                </td>
							                                <td valign="top" class="name">
							                                    <label for="drugRouteType"><g:message code="drugProduct.drugRouteType.label" default="Drug Route Type" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'drugRouteType', 'errors')}">
							                                    <g:select name="drugRouteType.id" from="${org.pih.warehouse.product.DrugRouteType.list()}" optionKey="id" value="${drugProductInstance?.drugRouteType?.id}" noSelection="['null': '']" />
							                                </td>
							                            </tr>
							                        
							                            <tr class="prop">
							                                <td valign="top" class="name">
							                                    <label for="dosageStrength"><g:message code="drugProduct.dosageStrength.label" default="Dosage Strength" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageStrength', 'errors')}">
							                                    <g:textField name="dosageStrength" value="${drugProductInstance?.dosageStrength}" />
							                                </td>
							                                <td valign="top" class="name">
							                                    <label for="dosageForm"><g:message code="drugProduct.dosageForm.label" default="Dosage Form" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageForm', 'errors')}">
							                                    <g:textField name="dosageForm" value="${drugProductInstance?.dosageForm}" />
							                                </td>
							                            </tr>
							                        
							                        
							                            <tr class="prop">
							                                <td valign="top" class="name">
							                                    <label for="dosageRegimen"><g:message code="drugProduct.dosageRegimen.label" default="Dosage Regimen" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'dosageRegimen', 'errors')}">
							                                    <g:textField name="dosageRegimen" value="${drugProductInstance?.dosageRegimen}" />
							                                </td>
							                                <td valign="top" class="name">
							                                    <label for="drugClass"><g:message code="drugProduct.drugClass.label" default="Drug Class" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: drugProductInstance, field: 'drugClass', 'errors')}">
							                                    <g:textField name="drugClass" value="${drugProductInstance?.drugClass}" />
							                                </td>
							                            </tr>
							                        
													</tbody>
												</table>
											</fieldset>												
										</g:if>
										<g:elseif test="${productInstance.class.simpleName == 'DurableProduct'}">
											<fieldset>
						                    	<legend>Equipment</legend>
												<table>
													<tbody>
							                            <tr class="prop">
							                                <td valign="top" class="name">
							                                  <label for="make"><g:message code="product.make.label" default="Make" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'make', 'errors')}">
							                                    <g:textField name="make" value="${productInstance?.make}" />
							                                </td>
							                            </tr>								
							                            <tr class="prop">
							                                <td valign="top" class="name">
							                                  <label for="model"><g:message code="product.model.label" default="Model" /></label>
							                                </td>
							                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'model', 'errors')}">
							                                    <g:textField name="model" value="${productInstance?.model}" />
							                                </td>
							                            </tr>								
													</tbody>
												</table>
						                    </fieldset>
										</g:elseif>
									</td>
								</tr>
							</tbody>
						</table>

						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="">
									
										<div class="buttons">
											<button type="submit" class="positive"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
												alt="Save" /> ${message(code: 'default.button.save.label', default: 'Save')}
											</button>
											
											<g:if test="${productInstance?.id}">
												<g:link controller='product' action='show' id='${productInstance.id}' class="negative">			
													<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> ${message(code: 'default.button.save.label', default: 'Cancel')}			
												</g:link>  
											</g:if>
											<g:else>
												<g:link controller='product' action='browse' class="negative">			
													<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> ${message(code: 'default.button.save.label', default: 'Cancel')}			
												</g:link>  											
											</g:else>
											<!-- 
									           <button type="submit" class="negative" action="delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
									                <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" /> ${message(code: 'default.button.delete.label', default: 'Delete')}</button>
											-->
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
