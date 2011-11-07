<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title>
	        <g:if test="${transactionInstance?.id }">
		        <warehouse:message code="default.edit.label" args="[entityName.toLowerCase()]" />  
	    	</g:if>
	    	<g:else>
		        <warehouse:message code="default.create.label" args="[entityName.toLowerCase()]" />    
			</g:else>    	    
		</title>
        <style>
        	/*.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; } */
        </style>
    </head>    
    <body>
        <div class="body">

            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${command}">
	            <div class="errors">
	                <g:renderErrors bean="${command}" as="list" />
	            </div>
            </g:hasErrors>    
            <g:hasErrors bean="${command?.transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${command?.transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog" >
				<fieldset>
					<table style="height: 100%;">
						<tr>
							<td>			
								<div class="summary">
									<!-- Action menu -->
									<span class="action-menu">
										<button class="action-btn">
											<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
											<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
										</button>
										<div class="actions">
											<div class="action-menu-item">
												<g:link controller="inventory" action="browse">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_list.png')}"/>
													${warehouse.message(code: 'transaction.backToInventory.label')}
												</g:link>			
											</div>			
											<%-- 				
											<g:if test="${params?.product?.id }">
												<div class="action-menu-item">
													<g:link controller="inventoryItem" action="showStockCard" params="['product.id':params?.product?.id]">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_left.png')}"/>
														${warehouse.message(code: 'transaction.backToStockCard.label', default: 'Back to Stock Card')}
													</g:link>		
												</div>	
											</g:if>
											--%>
											<%-- 				
											<div class="action-menu-item">
												<g:link controller="inventory" action="listTransactions">
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_up.png')}"/>
													${warehouse.message(code: 'transaction.backToTransactions.label')}
												</g:link>			
											</div>
											--%>
										</div>
									</span>
								</div>					
							</td>
						</tr>
					</table>
					
					<div>
						<g:if test="${command?.transactionInstance?.transactionType?.id == 9 }">
							<g:render template="outgoingTransfer"></g:render>
						</g:if>
						<g:elseif test="${command?.transactionInstance?.transactionType?.id == 8}">
							<g:render template="incomingTransfer"></g:render>
						</g:elseif>
						<g:elseif test="${command?.transactionInstance?.transactionType?.id == 7}">
							<g:render template="inventoryAdjustment"></g:render>
						</g:elseif>
						<g:elseif test="${command?.transactionInstance?.transactionType?.id == 4}">
							<g:render template="inventoryExpired"></g:render>
						</g:elseif>
						<g:elseif test="${command?.transactionInstance?.transactionType?.id == 5}">
							<g:render template="inventoryDamaged"></g:render>
						</g:elseif>
						<g:elseif test="${command?.transactionInstance?.transactionType?.id == 2}">
							<g:render template="inventoryConsumption"></g:render>
						</g:elseif>
						<g:else>
							Unknown transaction type
						</g:else> 
						
					</div>
					
				</fieldset>
			</div>
		</div>
	</body>
</html>