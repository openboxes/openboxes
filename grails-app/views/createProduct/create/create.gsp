                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Review product details</title>  
    </head>
    <body>
    	<div class="body">
			
			<g:render template="header" model="['currentState':'confirm']"/>			
						 	
			<g:form action="create" method="post" >
				<div class="dialog box">
					<table>
						<tbody>
							<tr class="prop">
								<td class="name">
									<label class="clear">Category:</label>
								</td>
								<td class="value">
									${product.category }
								</td>
							</tr>										
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
									<label class="clear">Description:</label>
								</td>
								<td class="value">
									${product.description }
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">Brand:</label>												
								</td>
								<td class="value">
									${product.brand }
								</td>
							</tr>										
							<tr class="prop">
								<td class="name">
									<label class="clear">GTIN(s):</label>												
								</td>
								<td class="value">
									${product.gtin }
								</td>
							</tr>							
							<%-- 
							<tr class="prop">
								<td class="name">
									<label class="clear">Google ID</label>
								</td>
								<td class="value">
									${product.googleId }
								</td>
							</tr>
							--%>		
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
