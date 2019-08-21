                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProduct.label"/></title>  
    </head>
    <body>
    	<div class="body">
    
			<g:render template="header" model="['currentState':'complete']"/>
						 	
			<g:form action="create" method="post" >
				<div class="dialog">
					<table>
		                <tbody>
		                    <tr class="prop">
		                        <td valign="top" class="name">
		                            <for="name">Name:</label>
		                        </td>
		                        <td valign="top">
									${product?.name }		                        
		                        </td>
		                    </tr>
		                    
		                    
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="category.label"/></label>
								</td>
								<td class="value">
									<div class="value">
										<g:if test="${product?.category?.name }">
											<format:category category="${product?.category}"/>
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</div>
									<g:each var="category" in="${product?.categories }">						
										<div>
											<format:category category="${category}"/>
										</div>
									</g:each>
									
								</td>
							</tr>
							<g:if test="${product?.productGroups }">
								<tr class="prop">	
									<td class="name">
										<label><warehouse:message code="productGroup.label"/></label>
									</td>
									<td>
										<g:each var="productGroup" in="${product?.productGroups }">
											<g:link controller="productGroup" action="edit" id="${productGroup.id }">
											${productGroup?.name }
											</g:link>
										</g:each>			
									</td>
								</tr>
							</g:if>
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.units.label"/></label>
								</td>
								<td colspan="2">
									<span class="value">
										<g:if test="${product?.unitOfMeasure }">
											<format:metadata obj="${product?.unitOfMeasure}"/>
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</span>
								</td>
							</tr>
							
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.manufacturer.label"/></label>
								</td>
								<td>
									<span class="value">
										<g:if test="${product?.manufacturer }">
											${product?.manufacturer }
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</span>
								</td>
							</tr>
							
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.manufacturerCode.label"/></label>
								</td>
								<td>
									<span class="value">
										<g:if test="${product?.manufacturerCode }">
											${product?.manufacturerCode }
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</span>
								</td>
							</tr>
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.upc.label"/></label>
								</td>
								<td>
									<span class="value">
										<g:if test="${product?.upc }">
											${product?.upc }
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</span>
								</td>
							</tr>
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.ndc.label"/></label>
								</td>
								<td>
									<span class="value">
										<g:if test="${product?.ndc }">
											${product?.ndc }
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</span>
								</td>
							</tr>
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="product.coldChain.label"/></label>
								</td>
								<td>
									<span class="value">${product?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
								</td>
							</tr>
							<g:set var="status" value="${0 }"/>
							<g:each var="productAttribute" in="${product?.attributes}">
								<tr class="prop">
									<td class="name">
										<label><format:metadata obj="${productAttribute?.attribute}"/></label>
									</td>
									<td>
										<span class="value">${productAttribute.value }</span>
									</td>
								</tr>													
							</g:each>		                    
		                    
		                    
		                </tbody>
	                </table>			
				
				
				</div>			
				<div class="buttons center">					
                    <g:submitButton class="back" name="next" value="Next" />
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
					
				</div>
            </g:form>
        </div>				
    </body>
</html>
