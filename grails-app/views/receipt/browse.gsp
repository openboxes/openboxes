
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.browse.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.browse.label" args="[entityName]" /></content>
    
    	<style>
    		.selected { font-weight: bold; border: 2px solid black; background-color: whitesmoke; padding: 5px; } 
    	
    	</style>
    </head>    


    <body>
    
        <div class="body" style="width: 95%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>                        
			<g:if test="${!inventoryInstance}">

			</g:if>
	 		<g:else>
				<fieldset>
					<legend></legend>						
					<table>
						<tr>
							<td>			
								<div id="inventoryBrowser">
									<g:form method="get" action="browse">
										<table>
											<tr class="prop">
												<td class="name">
													<label>Name contains</label>
												</td>
												<td>
													<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
												</td>
												<td>
													<span class="buttons">
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Filter" /> 
															${warehouse.message(code: 'default.button.filter.label', default: 'Filter')}</button>
													</span>											
												</td>
												
											</tr>
							
											<tr class="prop">
												<td class="name"><label>Types</label></td>
												<td class="value" colspan="2">										
													<table>
														<tr>
															<g:each in="${productTypes}" status="i" var="productType">
																<td>
																	<span class="${(productType==selectedProductType)?'selected':''}">
																		<a href="${createLink(action:'browse',params:["productTypeId":productType.id])}">${productType.name}</a>
																	</span>
																</td>
																<g:if test="${(i+1)%6==0}"></tr><tr></g:if>
															</g:each>
														</tr>
													</table>												
												</td>
											</tr>
										</table>							
									</g:form>			
								</div>
							</td>					
						</tr>				
						<tr>
							<td colspan="2">
								<div>
						            <g:if test="${productInstanceList}">
							 			<div>Your search returned ${productInstanceList.size} products.  </div>					            
						                <table width="100%">
						                    <thead>
						                        <tr>             
						                            <g:sortableColumn property="name" title="${warehouse.message(code: 'inventory.product.label', default: 'Product')}" />
						                            <g:sortableColumn property="name" title="${warehouse.message(code: 'inventory.quantity.label', default: 'Quantity')}" />
						                        </tr>
						                    </thead>
						                    <tbody>
							                    <g:each in="${productInstanceList}" status="i" var="productInstance">
							                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 3.5em">
							                        	            
														<td style="text-align: left">										
															<g:if test="${productInstance.class.simpleName == 'DrugProduct'}">															
																<img src="${createLinkTo(dir:'images/icons/silk',file: 'pill.png')}"/>
															</g:if>
															<g:elseif test="${productInstance.class.simpleName == 'DurableProduct' }">
																<img src="${createLinkTo(dir:'images/icons/silk',file: 'computer.png')}"/>
															</g:elseif>
															<g:else>
																<img src="${createLinkTo(dir:'images/icons/silk',file: 'page_white.png')}"/>
															</g:else>
															&nbsp;
															<g:link action="show" id="${productInstance.id}">
																${fieldValue(bean: productInstance, field: "upc")}
																${fieldValue(bean: productInstance, field: "name")}
															</g:link>
															
														</td>
														
														<td>
															<span style="font-size: 2em;">													
																<g:if test="${inventory}">
																	${inventory.inventoryMap.get(productInstance) }
																</g:if>
															</span>
														</td>
							                        </tr>
							                    </g:each>		                    
						                    </tbody>
						                </table>       
						        	</g:if>   						        
						        </div>
							</td>
						</tr>
					</table>
				</fieldset>
			</g:else>
		</div>
    </body>
</html>
