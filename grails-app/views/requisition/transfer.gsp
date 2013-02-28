<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'request.label', default: 'Requisition')}" />
        <title><warehouse:message code="default.fulfill.label" default="Fulfill {0}" args="[entityName]" /></title>       
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${requisition}">
				<div class="errors">
					<g:renderErrors bean="${requisition}" as="list" />
				</div>
			</g:hasErrors>            
			
            
			<g:render template="summary" model="[requisition:requisition]"/>

		
			<div class="yui-gc">
				<div class="yui-u first">
	            	<g:form controller="requisition" action="complete" method="POST">
	            		<g:hiddenField name="id" value="${requisition?.id }"/>
	            	
		            
		                <table>
		                    <tbody>
		                    	<tr class="prop">
		                    		<td class="name">
		                    			<label>Issue date:</label>
		                    		</td>
									<td>	
										${new Date() }
									</td>			                    	
		                    	</tr>
		                    	<tr class="prop">
		                    		<td class="name">
		                    			<label>Issued from:</label>
		                    		</td>
									<td>	
										${requisition?.destination}
									</td>			                    	
		                    	</tr>
		                    	<tr class="prop">
		                    		<td class="name">
		                    			<label>Issued to:</label>
		                    		</td>
									<td>	
										${requisition.origin}
									</td>			                    	
		                    	</tr>
		                    	<tr class="prop">
		                    		<td class="name">
		                    			<label>Issued by</label>
		                    		</td>
									<td>	
										${session.user.name}
									</td>			                    	
		                    	</tr>
		                    	<tr class="prop">
		                    	
		                    		<td class="name">
		                    			<label>Comments</label>
		                    		</td>
		                    		<td>
		                    			<g:textArea name="comments" cols="80" rows="6"></g:textArea>
		                    		</td>
		                    		
		                    	</tr>
		                    	<tr class="prop">
		                    		<td class="name">
		                    		</td>
		                    		<td>
		                    			<table class="requisition">
		                    				<thead>
		                    					<tr class="odd">
		                    						<th></th>
		                    						<th><warehouse:message code="product.label"/></th>
		                    						<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
		                    						<th><warehouse:message code="default.quantity.label"/></th>
		                    						<th><warehouse:message code="product.unitOfMeasure.label"/></th>
		                    					</tr>
		                    				</thead>
		                    				<tbody>
				                    			<g:each var="picklistItem" in="${picklist.picklistItems }" status="status">
				                    				<tr class="${status%2?'odd':'even' }">
				                    					<td>
				                    						${status+1 }
				                    					</td>
														<td>
															${picklistItem?.inventoryItem?.product }
														</td>				                    			
														<td>
															${picklistItem?.inventoryItem?.lotNumber }
														</td>				                    			
														<td>
															${picklistItem?.quantity }
														</td>				                    			
														<td>
															${picklistItem?.inventoryItem?.product?.unitOfMeasure?:"EA" }
														</td>				                    			
													</tr>
				                    			</g:each>
				                    		</tbody>
		                    			</table>
		                    		</td>
		                    	</tr>
		                    </tbody>
		                </table>
			                
		                
						<div class="buttons">
							<div class="right">
								<g:link controller="requisition" action="confirm" id="${requisition.id }" class="button">
									<warehouse:message code="default.button.back.label"/>	
								</g:link>

								<g:link controller="requisition" action="complete" id="${requisition.id }" class="button">
									<warehouse:message code="default.button.finish.label"/>	
								</g:link>
							</div>
						</div>			
								                
					</g:form>	
					
					
					               
				</div>
				<div class="yui-u">
				
				</div>
				
            </div>
        </div>
    </body>
</html>
