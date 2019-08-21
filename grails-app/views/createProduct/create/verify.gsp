                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProduct.label"/></title>  
    </head>
    <body>
    	<div class="body">
   		
			<g:render template="header" model="['currentState':'verify']"/>
			
			<g:form action="create" method="post" >
				<div class="dialog box">

					<table>
						<tbody>
							<tr class="prop">
								<td class="name">
									<label class="clear">Google ID</label>
								</td>
								<td class="value">
									${product.googleId }
								</td>
							</tr>								
							<tr class="prop">	
								<td class="name">
									<label><warehouse:message code="category.label"/></label>
								</td>
								<td class="value">
									<div class="value">
										<g:categorySelect cssClass="comboBox" id="category.id" name="category.id" excludeSpaces="false" value="${product?.category?.id }"/>
									</div>
								</td>
							</tr>						
							
							<tr class="prop">
								<td class="name">
									<label class="clear">Name:</label>
								</td>
								<td class="value">
									<g:textField name="title" value="${product.title }" class="text" size="80"/>
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
									<label class="clear">Source:</label>
								</td>
								<td class="value">
									<a href="${product.link }" target="_blank">${product.author }</a>
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">Brand:</label>												
								</td>
								<td class="value">
									<g:textField name="brand" value="${product.brand }" class="text" size="80"/>
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">GTIN(s):</label>												
								</td>
								<td class="value">
									<ul>
										<g:each in="${product.gtins }" var="gtin">
											<li>										
												<g:textField name="gtin" value="${gtin }" class="text" size="80"/>
											</li>
										</g:each>
									</ul>
								</td>
							</tr>
						</tbody>								
					</table>			
				</div>			
				<div class="buttons center">
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="next" name="next" value="Next" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
				</div>
            </g:form>
        </div>        
        <g:comboBox/>			
    </body>
</html>
