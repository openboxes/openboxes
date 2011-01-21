
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
		<content tag="pageTitle"><g:message code="product.batchEdit.label" args="[entityName]"  default="Batch Edit"/></content>
    </head>    
    <body>    
        <div class="body">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${commandInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <g:form action="batchSave" method="post">
                <div class="dialog">
					<fieldset>	              							
		                <table>
							<tbody>                
								<tr>
									<th valign="top"><g:message
										code="product.id.label" default="ID" />
									</th>
									
									<th valign="top"><g:message
										code="product.name.label" default="Description" /></th>
									<th valign="top">
										<label for="name"><g:message code="product.productCode.label" default="Product Code" /></th>
	                                <th valign="top">
	                                	<g:message code="product.primaryCategory.label" default="Categories" /></th>
									<th valign="top">
										<label for="name"><g:message code="product.coldChain.label" default="Cold Chain" /></th>
								</tr>
							</tbody>
	                      	<g:each var="productInstance" in="${commandInstance?.productInstanceList }" status="status">
								<tr class="${status%2?'even':'odd' }">
									<td>
						                <g:hiddenField name="productInstanceList[${status }].id" value="${productInstance?.id}" />
						                <g:hiddenField name="productInstanceList[${status }].version" value="${productInstance?.version}" />
										${productInstance?.id }									
									</td>
								
									<td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
										<g:textField name="productInstanceList[${status }].name" value="${productInstance?.name}" size="20"/>
										
									</td>
	
									<td valign="top"
										class="value ${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
									<g:textField name="productInstanceList[${status }].productCode" value="${productInstance?.productCode}" size="10" />
									</td>
	
	
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
	                                	<%-- 
										<g:autoSuggest 
											 	id="categoryInstanceList${status }" 
											 	name="categoryInstanceList[${status }]" 
											 	jsonUrl="/warehouse/json/findCategoryByName" 
												width="100" 
												valueId="${productInstance?.category?.id }" 
												valueName="${productInstance?.category?.name}"/>
										--%>
										<select name="categoryInstanceList[${status }].id">
											<option value=""></option>
											<g:render template="../category/selectOptions" model="[category:commandInstance?.rootCategory, selected:productInstance?.category, level: 0]"/>
										</select>	
										
										
										<%-- 
	                                	<g:select 
	                                		name="categoryInstanceList[${status }].id"
	                                		optionKey="id" 
	                                		optionValue="name"
	                                		value="${productInstance?.category?.id}"
	                                		from="${org.pih.warehouse.product.Category.list() }"
	                                		noSelection="[0:'']"
	                                	/>
										--%>
	                                	<%--
	                                	<g:set var="name" value="categoryInstanceList[${status }].id"/>
											<g:render template="categories" model="['productInstance':productInstance, rootCategory: commandInstance?.rootCategory]" />
	                                	--%>
									</td>
									<td valign="top" style="text-align: center;"
										class="value ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
										<g:checkBox name="productInstanceList[${status }].coldChain" value="${productInstance?.coldChain}" />
									</td>
								</tr>					
							
								<%-- 										
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
								--%>
							</tr>
						</g:each>
						</tbody>
						<tfoot>
							<tr class="prop">
								<td colspan="5" style="text-align: center;">
									<span class="buttons">
										<button type="submit" class="positive"><img
											src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" style="vertical-align: middle;"
											alt="Save" /> ${message(code: 'default.button.save.label', default: 'Save')}
										</button>
										
									</span>
									<g:link controller='product' action='browse' class="negative">
										${message(code: 'default.button.done.label', default: 'Done')}			
									</g:link>  
								
								</td>
							</tr>
				
						</tfoot>
					</table>
					</fieldset>
				</div>
			</g:form>
        </div>
        <g:render template='category' model="['category':null,'i':'_clone','hidden':true]"/>
    </body>
</html>
