
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
        
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
		
    </head>    
    <body>    
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${commandInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <g:form action="batchEdit" method="post">
				<div class="dialog" style="background-color: #f7f7f7; border: 1px solid lightgrey; padding: 10px;">
					<label>Select a category of products to edit</label>            
					<select name="category.id">
						<option value=""></option>
						<g:render template="../category/selectOptions" model="[category:commandInstance?.rootCategory, selected:categoryInstance, level: 0]"/>
					</select> 
					
					<button type="submit" class="positive"><img
						src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" style="vertical-align: middle;"
						alt="Save" /> ${warehouse.message(code: 'default.button.find.label', default: 'Find')}
					</button>
					          
				</div> 
			</g:form>
			<br/>
            <g:form action="batchSave" method="post">
            	<g:if test="${commandInstance?.productInstanceList }">
	                <div class="dialog">
		                <table>
							<thead>                
								<tr class="odd">
									<th valign="top"><warehouse:message code="product.name.label" default="Product Description" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.manufacturer.label" default="Manufacturer" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.manufacturerCode.label" default="Mfg Code" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.upc.label" default="UPC" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.ndc.label" default="NDC" /></th>
										
									<th valign="top">
										<label for="name"><warehouse:message code="product.unitOfMeasure.label" default="Unit of Measure" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.productCode.label" default="Product Code" /></th>
	                               <th valign="top">
		                               	<warehouse:message code="product.primaryCategory.label" default="Primary Category" /></th>
									<th valign="top">
										<label for="name"><warehouse:message code="product.coldChain.label" default="Cold Chain" /></th>
								</tr>
							</thead>
							<tbody>
		                      	<g:each var="productInstance" in="${commandInstance?.productInstanceList }" status="status">
									<tr class="${status%2?'odd':'even' }">
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'name', 'errors')}">
											<g:textField name="productInstanceList[${status }].name" value="${productInstance?.name}" size="25"/>
											
										</td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
											<g:textField name="productInstanceList[${status }].manufacturer" value="${productInstance?.manufacturer}" size="10"/>
											
										</td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
											<g:textField name="productInstanceList[${status }].manufacturerCode" value="${productInstance?.manufacturerCode}" size="3"/>
											
										</td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
											<g:textField name="productInstanceList[${status }].upc" value="${productInstance?.upc}" size="3"/>
											
										</td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
											<g:textField name="productInstanceList[${status }].ndc" value="${productInstance?.ndc}" size="3"/>
											
										</td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
											<g:textField name="productInstanceList[${status }].unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="5"/>
											
										</td>
										<td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
											<g:textField name="productInstanceList[${status }].productCode" value="${productInstance?.productCode}" size="5" />
										</td>		
		                                <td valign="top" class="${hasErrors(bean: productInstance, field: 'category', 'errors')}">
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
										<td valign="top" style="text-align: center;" class="${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
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
								</g:each>
							</tbody>
							<tfoot>
								<tr class="prop">
									<td colspan="9" style="text-align: center;">
										<button type="submit" class="positive"><img
											src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" style="vertical-align: middle;"
											alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
										</button>
										&nbsp;
										<g:link controller='inventory' action='browse' class="negative">
											${warehouse.message(code: 'default.button.done.label', default: 'Done')}			
										</g:link>  
									</td>
								</tr>
					
							</tfoot>
						</table>
					</div>
				</g:if>
				
				
			</g:form>
        </div>
        <g:render template='category' model="['category':null,'i':'_clone','hidden':true]"/>
    </body>
</html>
