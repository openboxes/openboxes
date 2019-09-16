                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProductFromTemplate.label"/></title>  
    </head>
    <body>
    	<div class="body">
    	
			
			<g:render template="header" model="['currentState':'confirmDetails']"/>
			
			<g:form action="create" method="post" >
				<div class="dialog box">
					<table>
						<tbody>
							<tr class="prop">
								<td class="name">
									<label class="clear">Name:</label>
								</td>
								<td class="value">
									<g:textField name="name" value="${product.title }" readonly="readonly" class="text" size="80"/>
								</td>
							</tr>										
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="category.label"/></label>
								</td>
								<td class="value">
									<div class="value">
										<g:if test="${product?.category }">
											<format:category category="${product?.category}"/>
										</g:if>
										<g:else>
											<span class="fade"><warehouse:message code="default.none.label"/></span>
										</g:else>
									</div>
								</td>
							</tr>						
							
							<tr class="prop">
								<td class="name">
									<label class="clear">Description:</label>
								</td>
								<td class="value">		
									<g:textArea name="description" value="${product.description }" class="text" cols="80" rows="5"/>
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">Application:</label>
								</td>
								<td class="value">		
									${product.application }
								</td>
							</tr>				
							<tr class="prop">
								<td class="name">
									<label class="clear">Latex:</label>
								</td>
								<td class="value">		
									${product.latex }
								</td>
							</tr>				
							<tr class="prop">
								<td class="name">
									<label class="clear">Powder:</label>
								</td>
								<td class="value">		
									${product.powder }
								</td>
							</tr>				
							<tr class="prop">
								<td class="name">
									<label class="clear">Sterility:</label>
								</td>
								<td class="value">		
									${product.sterility }
								</td>
							</tr>				
							
							<tr class="prop">
								<td class="name">
									<label class="clear">Size:</label>
								</td>
								<td class="value">		
									${product.size }
								</td>
							</tr>															
							
							
						</tbody>								
					</table>			
				</div>			
				<div class="buttons">
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="next" name="next" value="Save" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
					
				</div>
            </g:form>
        </div>    				
    </body>
</html>
