
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
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
        
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
					<fieldset>	              		
					
						<g:render template="summary"/>
					
		                <table>
	                      <tbody>                
								<tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="categories"><g:message code="product.categories.label" default="Category" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}">
	                                	<%-- <g:render template="../category/chooser"/>--%>
	                                	
										<select name="category.id">
											<option value="">no parent</option>
											<g:render template="../category/selectOptions" model="[root:rootCategory, selected:productInstance?.category, level: 0]"/>
										</select>	      
									</td>
								</tr>	
								<tr class="prop">
									<td valign="top" class="name"><label for="name"><g:message
										code="product.name.label" default="Product Description" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
									<g:textField name="name" value="${productInstance?.name}" size="40" />
									</td>
								</tr>
								
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
								<td valign="top" class="">
								</td>
								<td>
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
