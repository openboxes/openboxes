                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Edit Product Details</title>  
    </head>
    <body>
    	<div class="body">
    	
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${command}">
				<div class="errors">
					<g:renderErrors bean="${command}" as="list" />
				</div>				
			</g:hasErrors> 				
			
			<g:render template="header" model="['currentState':'verify']"/>
			
			<g:form action="create" method="post" >
				<div class="dialog">

					<table>
						<tbody>
						
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
												<g:textField name="gtin" value="${product.gtin }" class="text" size="80"/>
											</li>
										</g:each>
									</ul>
								</td>
							</tr>		
							<%-- 								
							<tr class="">
								<td class="name">
									<label class="clear">Link(s):</label>												
								</td>
								<td class="value">
									<g:each in="${product.links }" var="link">
										<div>
											<a target="_blank" href="${link.value }">${link.value }</a>
										</div>
									</g:each>
								</td>
							</tr>
							--%>										
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
									<label class="clear">Images:</label>
								</td>
								<td class="value">
									<g:each in="${product.images }" var="image" status="i">
										<g:checkBox name="image[i]" value="${image }"/><img src="${image }" class="top"/>
									</g:each>
								</td>
							</tr>		
						</tbody>								
					</table>			
				</div>			
				<div class="buttons center">
					<%-- 
					<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
					<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button>
					<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
					<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>					
					--%>
					
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="next" name="next" value="Next" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
					
				</div>
            </g:form>
        </div>
        
        
        
		<script>			
			//$(document).ready(function() {
			//});
		</script> 				
    </body>
</html>
