
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

		<style>
			.category-div { 
				padding: 5px;
				}
		</style>

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
                <g:hiddenField name="id" value="${productInstance?.id}" />
                <g:hiddenField name="version" value="${productInstance?.version}" />
            	<g:hiddenField name="categoryId" value="${params?.category?.id }"/><!--  So we know which category to show on browse page after submit -->
					<div class="dialog">
		                <table style="display: inline;">
	                      <tbody>                
							<tr class="prop">
								<td valign="top" class="name"><label for="name"><g:message
									code="product.name.label" default="Description" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
								<g:textField name="name" value="${productInstance?.name}" size="40" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label for="name"><g:message
									code="product.unitOfMeasure.label" default="Unit of Measure" /></label></td>
								<td valign="top"
									class="${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
								<g:textField name="unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="15" />
								</td>
							</tr>								
							<tr class="prop">
								<td valign="top" class="name"><label for="manufacturer"><g:message
									code="product.manufacturer.label" default="Manufacturer" /></label></td>
								<td valign="top"
									class="${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
								<g:textField name="manufacturer" value="${productInstance?.manufacturer}" size="15" />
								</td>
							</tr>								
							<tr class="prop">
								<td valign="top" class="name"><label for="name"><g:message
									code="product.manufacturerCode.label" default="Manufacturer Code" /></label></td>
								<td valign="top"
									class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
								<g:textField name="manufacturerCode" value="${productInstance?.manufacturerCode}" size="15" />
								</td>
							</tr>								
							<tr class="prop">
								<td valign="top" class="name"><label for="upc"><g:message
									code="product.upc.label" default="UPC" /></label></td>
								<td valign="top"
									class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
								<g:textField name="upc" value="${productInstance?.upc}" size="15" />
								</td>
							</tr>								
							<tr class="prop">
								<td valign="top" class="name"><label for="ndc"><g:message
									code="product.ndc.label" default="NDC" /></label></td>
								<td valign="top"
									class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
								<g:textField name="ndc" value="${productInstance?.ndc}" size="15" />
								</td>
							</tr>								
							<tr class="prop">
								<td valign="top" class="name"><label for="name"><g:message
									code="product.coldChain.label" default="Cold Chain" /></label></td>
								<td valign="top"
									class=" ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
								<g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
								</td>
							</tr>								
							
							<%--
							<tr class="prop">
                                <td valign="top" class="name">
                                  <label for="categories"><g:message code="product.otherCategories.label" default="Other Categories" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}">
                                	<ul>
	                                	<g:each var="category" in="${productInstance?.categories}">
											<li>${category?.name }</li>
	                                	</g:each>
	                                </ul>
									<select name="categories.id" >
										<g:render template="../category/selectOptions" model="[category:rootCategory, selected:[], level: 0]"/>
									</select>	      
									${productInstance?.categories }
								</td>
							</tr>	
							 --%>
							 
							<g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
								<tr class="prop">
									<td valign="top" class="name"><label for="attributes">
									${attribute.name }
									</label> <g:hiddenField name="attributes[${status }].attribute.id"
										value="${attribute?.id }" /></td>
									<td valign="top" class="value">
										<g:if test="${attribute.options }">
											<g:select
												name="attributes[${status }].value" from="${attribute?.options}"
												value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }" />
												
											<g:if test="${attribute.allowOther}">
												<g:textField name="attributes[${status }].otherValue" value="" />
											</g:if>
										</g:if>
										<g:else>
											<g:textField name="attributes[${status }].value"
												value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }"/>
										</g:else>
									</td>
								</tr>
							</g:each>
							<tr class="prop">
                                <td valign="top" class="name">
                                  <label for="categories"><g:message code="product.primaryCategory.label" default="Primary Category" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
                                	<%-- <g:render template="../category/chooser"/>--%>
                                	
									<select name="category.id">
										<option value=""></option>
										<g:render template="../category/selectOptions" model="[category:rootCategory, selected:productInstance?.category, level: 0]"/>
									</select>	
								</td>
							</tr>	
							 
							<tr class="prop">
							   <td valign="top" class="name">
							      <label for="categories"><g:message code="product.categories.label" default="Categories" /></label>
							   </td>
							   <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}">
							       <g:render template="categories" model="['productInstance':productInstance]" />
							   </td>
							</tr>					
							
										
							<tr class="prop">
								<td valign="top" class="">
								</td>
								<td>
									<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
										alt="Save" /> ${message(code: 'default.button.save.label', default: 'Save')}
									</button>
									&nbsp;
									<!-- we only can delete products that 1) exist, and 2) dont have associated transaction entries or shipment items -->
									<g:if test="${productInstance.id && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()}">
									<g:link action="delete" id="${productInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
							                <button type="button" class="negative"><img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" /> ${message(code: 'default.button.delete.label', default: 'Delete')}</button></g:link>
									</g:if>
									&nbsp;
									<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="negative">			
										${message(code: 'default.button.cancel.label', default: 'Cancel')}			
									</g:link>  
								</td>
							</tr>
				
						</tbody>
					</table>
				</div>
			</g:form>
        </div>
        <g:render template='category' model="['category':null,'i':'_clone','hidden':true]"/>
    </body>
</html>
