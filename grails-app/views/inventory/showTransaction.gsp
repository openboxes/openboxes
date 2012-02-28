
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title><warehouse:message code="default.view.label" args="[entityName.toLowerCase()]" /></title>    
        <style>
        /*
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
			.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; }         	
        */
        </style>
    </head>    

    <body>
        <div class="body">
     
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog">
			
				<fieldset>					
					<table class="summary">
						<tr>
							<td class="left middle" style="width: 50px;">
								<!-- Action menu -->
								<g:render template="../transaction/actions"/>
							</td>
							<td class="middle">
								<g:render template="../transaction/summary"/>
							</td>
						</tr>
					</table>
										
					<g:form>
						<g:hiddenField name="id" value="${transactionInstance?.id}"/>
						<g:hiddenField name="inventory.id" value="${transactionInstance?.inventory?.id}"/>
						<table>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="transaction.transactionNumber.label"/></label>
								</td>
								<td>
									<span class="value">
										<g:if test="${transactionInstance?.transactionNumber() }">
											${transactionInstance?.transactionNumber() }
										</g:if>
										<g:else><span class="fade"><warehouse:message code="transaction.new.label"/></span></g:else>
									</span>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="transaction.date.label"/></label>
								</td>
								<td>
									<span class="value">
										<format:date obj="${transactionInstance?.transactionDate}"/>
									</span>
								</td>										
							</tr>
							<g:if test="${transactionInstance?.outgoingShipment }">
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="shipping.shipment.label"/></label>
									</td>
									<td class="value">
										<g:link controller="shipment" action="showDetails" id="${transactionInstance?.outgoingShipment?.id }">
											${transactionInstance?.outgoingShipment?.name} 
										</g:link>
									</td>										
								</tr>
							</g:if>
							<g:if test="${transactionInstance?.incomingShipment }">
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="shipping.shipment.label"/></label>
									</td>
									<td class="value">
										<g:link controller="shipment" action="showDetails" id="${transactionInstance?.incomingShipment?.id }">
											${transactionInstance?.incomingShipment?.name} 
										</g:link>
									</td>										
								</tr>
							</g:if>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="transaction.type.label"/></label>
								</td>
								<td>
									<span class="value ${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
										<format:metadata obj="${transactionInstance?.transactionType}"/>
									</span>
									<g:if test="${transactionInstance?.source }">
										<span id="sourceSection" class="prop-multi">
											<label><warehouse:message code="default.from.label"/></label>
											<span>
												${transactionInstance?.source?.name }	
											</span>
										</span>									
									</g:if>
									
									<g:if test="${transactionInstance?.destination }">
										<span id="destinationSection" class="prop-multi">
											<label><warehouse:message code="default.to.label"/></label>
											<span>
												${transactionInstance?.destination?.name }	
											</span>
										</span>
									</g:if>	
								</td>	
							</tr>
							<tr id="inventory" class="prop">
								<td class="name">
									<label><warehouse:message code="inventory.label"/></label>
								</td>
								<td>
									<span class="value">
										<format:metadata obj="${transactionInstance?.inventory?.warehouse }"/>
									</span>								
								</td>										
							</tr>
							<g:if test="${transactionInstance?.comment }">
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="default.comment.label"/></label>
									</td>
									<td>
										<span class="value">
											${transactionInstance?.comment }
										</span>
									</td>										
								</tr>
							</g:if>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="transaction.createdBy.label"/></label>
								</td>
								<td class="value">
									${transactionInstance?.createdBy}
								</td>										
							</tr>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="default.dateCreated.label"/></label>
								</td>
								<td class="value">
									<format:datetime obj="${transactionInstance?.dateCreated}"/>
								</td>										
							</tr>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="default.lastUpdated.label"/></label>
								</td>
								<td class="value">
									<format:datetime obj="${transactionInstance?.lastUpdated}"/>
								</td>										
							</tr>
							<g:if test="${transactionInstance?.id }">
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="transaction.transactionEntries.label"/></label>
									</td>
									<td class="value" style="padding:0;">									
										<table id="prodEntryTable">
											<thead>
												<tr>
													<th><warehouse:message code="product.label"/></th>
													<th style="text-align: center"><warehouse:message code="product.lotNumber.label"/></th>
													<th style="text-align: center"><warehouse:message code="product.expirationDate.label"/></th>
													<th style="text-align: center"><warehouse:message code="default.qty.label"/></th>
												</tr>
											</thead>
											<tbody>
												<g:set var="transactionSum" value="${0 }"/>
												<g:set var="transactionCount" value="${0 }"/>
												<g:if test="${transactionInstance?.transactionEntries }">
													<g:each in="${transactionInstance?.transactionEntries.sort { it?.inventoryItem?.product?.name } }" var="transactionEntry" status="status">														
														<g:if test="${params?.showAll || !params?.product || transactionEntry?.inventoryItem?.product?.id == params?.product?.id}">
															<g:set var="transactionSum" value="${transactionSum + transactionEntry?.quantity}"/>
															<g:set var="transactionCount" value="${transactionCount+1 }"/>
															<tr class="${status%2?'odd':'even' }">
																<td style="text-align: left;">
																	<g:link controller="inventoryItem" action="showStockCard" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
																		<format:product product="${transactionEntry?.inventoryItem?.product}"/>
																	</g:link>
																	<g:if test="${params?.showAll || !params.product }">		
																		<g:link controller="inventory" action="showTransaction" id="${transactionInstance.id}" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="${warehouse.message(code: 'transaction.showSingleProduct.label') }" style="vertical-align: middle"/>
																		</g:link>
																	</g:if>
																	
																</td>										
																<td class="center">
																	${transactionEntry?.inventoryItem?.lotNumber }
																</td>		
																<td class="center">
																	<format:expirationDate obj="${transactionEntry?.inventoryItem?.expirationDate}"/>															
																</td>
																<td class="center">
																	${transactionEntry?.quantity}
																</td>
															</tr>
														</g:if>
													</g:each>
												</g:if>
												<g:else>
													<tr>
														<td colspan="4">
															<warehouse:message code="transaction.noEntries.message"/>
														</td>
													</tr>
												</g:else>
											</tbody>
											<tfoot>
												<tr>
													<td colspan="2">
														<g:if test="${transactionInstance?.transactionEntries?.size() > transactionCount || params?.product?.id }">
															<a href="?showAll=true">${warehouse.message(code: 'transaction.showAllProducts.label') }</a>
														</g:if>														
													
													</td>
													<td class="right">
														<label>Total</label>																
													</td>															
													<td class="center">
														${transactionSum }																			
													</td>			
												</tr>
											</tfoot>
										</table>	
									</td>
								</tr>
							</g:if>
						</table>
					</g:form>
				</fieldset>
										
			</div>
		</div>
    </body>
</html>
