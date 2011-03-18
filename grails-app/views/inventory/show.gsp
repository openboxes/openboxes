
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>    
    </head>    

	<body>
       <div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

			<span style="font-size:1.5em">
				<b>${inventoryInstance?.warehouse?.name }</b>
			</span>
			<br/><br/>
			<%-- 
			<div class="dialog">
				<fieldset>
					<table>
						<thead>
							<tr>
								<th>Alerts</th>  
								<th style="text-align: right"><a href="">ignore all</a></th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td colspan="2">
									<table>
										<g:set var="rowCount" value="${0 }"/>
										<g:each var="itemInstance" in="${inventoryInstance?.inventoryItems}" status="i">   
											<g:if test="${itemInstance?.quantityAvailable <= 0 }">
												<tr class="${(rowCount++ % 2 == 0) ? 'odd' : 'even' }">
													<td>
														<img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png') }" style="vertical-align: middle" />
														 ${itemInstance?.product?.name }, Lot #${itemInstance?.lotNumber }
													</td>
													<td style="text-align:right;">
														<a href="">Reorder</a> 
														&nbsp;
														<a href="">Ignore</a> 
														&nbsp;
														<a href="">Remind me later</a> 
													</td>
												</tr>
											</g:if>	
												
																
										</g:each>												
									</table>
								</td>
							</tr> 
						</tbody>
					</table>
				</fieldset>			
			</div>
			--%>
			
		
			<div class="dialog">
					<table border="1" style="border: 1px solid #ccc;">
	                    <thead>
	                        <tr>  
	                        	<th>Product</th>
	                        	<th>Total Qty</th>                         	
								<th style="text-align: center">
									<table>
										<tr>
											<thead>
												<th width="15%">Lot Number</th>
												<th width="25%">Actions</th>
												<th width="5%">Qty</th>
												<th width="10%">Expires</th>
												<th width="40%">Warnings</th>
											</thead>
										</tr>
									</table>
								</th>
	                        </tr>
	                    </thead>
	       	           	<tbody>
							<g:if test="${!inventoryMapping}">
								<tr>								
									<td colspan="3">no inventory items</td>
								</tr>
							</g:if>
				       	    <g:else>       	
	       	           			<g:each var="key" in="${inventoryMapping?.keySet() }">
									<tr class="prop">
										<td>${key?.name }</td>
										<td style="text-align: center;">${inventoryMapping?.get(key)*.quantityAvailable.sum() }</td>
										<td>
							       	    	<g:set var="counter" value="${0 }"/>
							       	    	<table >
												<g:each var="itemInstance" in="${inventoryMapping?.get(key)}" status="i">   
													<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">            
														<td width="15%">
															${itemInstance?.lotNumber}
														</td>
														<td width="25%">
															<g:link controller="inventoryItem" action="show" id="${itemInstance?.id }" params="['inventory.id':inventoryInstance?.id]">
																Show 
															</g:link>		
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
															<g:link controller="inventoryItem" action="edit" id="${itemInstance?.id }" params="['inventory.id':inventoryInstance?.id]">
																Edit 
															</g:link>
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
															<g:link controller="inventoryItem" action="delete" id="${itemInstance?.id }" params="['inventory.id':inventoryInstance?.id]">
																Remove
															</g:link>											
														
														
														</td>
														<td width="5%">${itemInstance?.quantityAvailable }</td>
														<td width="10%"><g:formatDate date="${itemInstance?.expirationDate }" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}"/></td>
														<td width="40%">
															<ul>
																<g:each in="${itemInstance?.warnings }" var="warning">
																	<li>
																		<g:if test="${warning?.contains('error') || warning?.contains('alert')}">
																			<img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png') }" />
																		</g:if>
																		<g:elseif test="${warning?.contains('warning')}">
																			<img src="${resource(dir: 'images/icons/silk', file: 'error.png') }" />
																		</g:elseif>
																		<g:elseif test="${warning?.contains('info')}">
																			<img src="${resource(dir: 'images/icons/silk', file: 'information.png') }" />
																		</g:elseif>														
																		&nbsp;${message(code: warning)}<br/>
																	</li>
																</g:each>
															</ul>
														</td>			
													</tr>
												</g:each>
											</table>
										</td>
									</tr>
								</g:each>
							</g:else>
						</tbody>
					</table>					
				</div>
				<br/>															
				<g:form action="addItem">	
					<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
					
					<fieldset>
						<h2>Add Lot</h2>
					
						<table bgcolor="#efdfb7" border=0 cellspacing=0 cellpadding=2>
							<tr>
								<th>Product</th>
								<th>Lot Number</th>
								<th>Expiration Date</th>
								<th>Initial Qty</th>	
								<th></th>
							</tr>
							<tr>
								<td>
									<g:autoSuggest id="product" 
										name="product" 
										jsonUrl="/warehouse/json/findProductByName" 
										width="250" 
										valueId="" 
										valueName=""/>									
								
								</td>
								<td>
									<g:textField name="lotNumber" size="10"/>
								</td>
								<td>
									<g:jqueryDatePicker id="expirationDate" name="expirationDate" value="" format="MM/dd/yyyy"/>
								</td>
								<td>
									<g:textField name="quantityOnHand" size="3"/>
								</td>				
								<td>
									<div class="buttons">
										<g:submitButton name="submit" value="Submit"/>
									</div>
								</td>
							</tr>
						
						
						</table>
					</fieldset>
				</g:form>				

				
		</div>
	</body>

</html>
