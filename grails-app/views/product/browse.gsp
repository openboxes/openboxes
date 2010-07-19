
<%@ page import="org.pih.warehouse.product.Product" %>
<%@ page import="org.pih.warehouse.product.ConditionType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.browse.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>
    </head>    
    <body>
        <div class="body" style="width: 95%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
			<table width="100%">
				<tr>
					<td>					
						<g:form method="post">
			                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
			                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
			            	<fieldset>
			            		<legend>Filter by</legend>
				            
								<table width="100%">
									<tr>
										<td width="10%"><label>Product Type</label></td>
										<td>
											<g:select name="productType" from="${productTypes}" value="${selectedProductType}"
												optionValue="name"
												noSelection="['':'-Choose a product type']">
											</g:select>										
										</td>
									</tr>
									<tr>
										<td><label>Attribute</label></td>
										<td>
											<g:select name="attribute" from="${attributes}" value="${selectedAttribute}"
												optionValue="name"
												noSelection="['':'-Choose an attribute']">
											</g:select>
										</td>
									</tr>
									<tr>
										<td colspan="2" style="text-align: right">
											<span class="buttons">
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Filter" /> 
													${message(code: 'default.button.filter.label', default: 'Filter')}</button>
											</span>										
										</td>
																				
															
									</tr>
									
								</table>				
							</fieldset>
						</g:form>
					</td>					
				</tr>
				
				<tr>
					<td colspan="2">
			            <g:if test="${productInstanceList.size > 0}">
					
							<div>
							
								<fieldset>
				            		<legend>Search results</legend>						
									<div>
										Returned ${productInstanceList.size} products				            		         				            	
						            </div>					            
					                <table width="100%">
					                    <thead>
					                        <tr>                        
					                            <g:sortableColumn property="id" title="${message(code: 'product.id.label', default: 'ID')}" />
					                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
					                            <g:sortableColumn property="productType" title="${message(code: 'product.productType.label', default: 'Product Type')}" />
					                        </tr>
					                    </thead>
					                    <tbody>
						                    <g:each in="${productInstanceList}" status="i" var="productInstance">
						                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
													<td align="center">										
														<g:link action="show" id="${productInstance.id}">${fieldValue(bean: productInstance, field: "id")}</g:link>
													</td>
													<td align="center">
														${fieldValue(bean: productInstance, field: "name")}
													</td>
													<td>${fieldValue(bean: productInstance, field: "productType.name")}</td>					                            
						                        </tr>
						                    </g:each>		                    
					                    </tbody>
					                </table>                
				                
								</fieldset>
				            </div>
			            
		                </g:if>
					
					</td>
				</tr>
			
			
			</table>
           	

            
        </div>
    </body>
</html>
