                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProductFromTemplate.label"/></title>  
    </head>
    <body>
    	<div class="body">
    	
			<g:render template="header" model="['currentState':'showProduct']"/>			
						 	
			<g:form action="create" method="post" >
			
				<div class="buttonBar">
					<span class="linkButton">
						<g:link controller="inventoryItem" action="showStockCard" class="stockCard" params="['product.id':productInstance?.id]">
							Show stock card
						</g:link>
					</span>
					<span class="linkButton">
						<g:link controller="product" action="edit" id="${productInstance?.id }" class="edit">
							Edit product
						</g:link>
					</span>
					<span class="linkButton">
						<g:link controller="createProductFromTemplate" action="index" class="create">
							Create another product
						</g:link>
					</span>
				</div>			
				<div class="dialog box">
					<table>
						<tbody>
							<tr class="prop">
								<td class="name">
									<label class="clear">Name:</label>
								</td>
								<td class="value">
									${product.title }
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">Category:</label>
								</td>
								<td class="value">
									<g:if test="${product?.category }">
										<format:category category="${product?.category}"/>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="default.none.label"/></span>
									</g:else>
								</td>
							</tr>	
							<tr class="prop">
								<td class="name">
									<label class="clear">Description:</label>
								</td>
								<td class="value">
									${product.description }
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
            </g:form>
        </div>				
    </body>
</html>
