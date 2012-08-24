<%@page import="java.text.Format"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.pickRequestItems.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${picklistInstance}">
			<div class="errors">
				<g:renderErrors bean="${picklistInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<div class="dialog">
			<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
			<g:render template="header" model="['state':'pickRequestItems']"/>
			
			<g:form action="createRequest" autocomplete="false">			
				<div>			
					<table>
						<thead>
							<tr>
								<th width="1%"><!-- Icon --></th>
								<th width="1%">${warehouse.message(code:'request.quantity.label')}</th>
								<th style="border-right: 1px solid lightgrey;">${warehouse.message(code:'default.item.label') }</th>
								<th style="border-right: 1px solid lightgrey;">${warehouse.message(code:'picklist.item.label')}</th>
								<th>${warehouse.message(code:'picklist.itemToPick.label')}</th>
								<th></th>
								
								<%-- 
								<th>${warehouse.message(code:'inventory.quantity.label') }</th>
								<th>${warehouse.message(code:'product.unitOfMeasure.label')}</th>
								<th>${warehouse.message(code:'default.item.label') }</th>
								--%>
								
							</tr>
						</thead>
						<tbody>
							<g:each var="requestItem" in="${requestInstance?.requestItems}" status="i">
								<tr class="${i%2? 'even' : 'odd'}">
									<g:hiddenField name="requestItems[${i }].request.id" value="${requestItem?.request?.id }" size="5"/>
									
									<td>
										<g:if test="${requestItem?.type }">
											<img src="${createLinkTo(dir:'images/icons/type',file: requestItem?.type + '.png')}"/>
										</g:if>
										<g:else>
											<img src="${createLinkTo(dir:'images/icons/silk',file: 'page_white.png')}"/>
										</g:else>
									</td>
									<td class="center">
										<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
										<g:formatNumber number="${requestItem?.quantity }"/>									
									</td>
									
									<td style="border-right: 1px solid lightgrey;">
										<format:metadata obj="${requestItem.displayName()}"/>
									</td>
									<%-- 
									<td>
										<g:if test="${requestItem.product }">
											<g:formatNumber number="${quantityOnHandMap[requestItem.product]?:0}"/>
										</g:if>
										<g:else>
											
										</g:else>
									</td>
									--%>
									
									<td style="border-right: 1px solid lightgrey;">
										<g:set var="picklistItems" value="${requestItem.getPicklistItems() }"/>
										<g:set var="quantityPicked" value="${picklistItems?.sum { it.quantity }?:0 }"/>
										<g:set var="quantityRemaining" value="${requestItem.quantity - quantityPicked }"/>
										<g:if test="${requestItem.status == 'tick' }">
											<g:set var="status" value="success"/>
										</g:if>
										<g:elseif test="${requestItem.status == 'flag_yellow' }">
											<g:set var="status" value="notice"/>
										</g:elseif>
										<g:elseif test="${requestItem.status == 'flag_red' }">
											<g:set var="status" value="error"/>
										</g:elseif>
										
										<div class="${status }">
											<g:if test="${requestItem.status }">
												<img src="${createLinkTo(dir:'images/icons/silk',file: requestItem?.status + '.png')}"/>
											</g:if>
											<g:if test="${quantityRemaining >= 0 }">
												<span class="quantityRemaining">Need ${quantityRemaining } more.</span>
											</g:if>
											<g:if test="${quantityRemaining < 0 }">
												<span class="quantityRemaining">Please remove ${-quantityRemaining } items.</span>
											</g:if>
										</div>
										<ul>
											<g:each var="picklistItem" in="${picklistItems }">
												<li>
													<div class="box">
														<span style="float: right">											
														<g:link action="createRequest" event="deletePicklistItem" params="['picklistItem.id':picklistItem?.id]">
															<img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}" class="middle"/> 
														</g:link>
														</span>
														<div>
															<label>${picklistItem?.inventoryItem?.product?.name }</label>  
														</div>
														<div>
															Lot: ${picklistItem?.inventoryItem?.lotNumber?.toUpperCase() }  
														</div>
														<div>
															Exp: <g:formatDate date="${picklistItem.inventoryItem.expirationDate }" format="MMM yy"/>
														</div>
														<div>
															Qty: ${picklistItem?.quantity } ${picklistItem?.inventoryItem?.product?.unitOfMeasure?:"each" }
														</div>		
														
													</div>													
												</li>
											</g:each>
										</ul>
									</td>
									
									<g:picklistItem requestItem="${requestItem }" status="${i }" quantityRemaining="${quantityRemaining }"/>
									
									<td>
										<g:set var="quantityOnHand" value="${quantityOnHandMap[requestItem.product]?:0 }"/>
										<g:if test="${requestItem.quantity > quantityOnHand }">
											<span class="error">Quantity requested exceeds quantity on hand.</span>
										</g:if>
									</td>
									<%--
									<td>
										<g:if test="${requestItem.product }">
											${requestItem?.product?.unitOfMeasure }
										</g:if>
										<g:else>
											N/A
										</g:else>
									</td>
									 
									<td>
										<g:set var="quantityOnHand" value="${quantityOnHandMap[requestItem.product]?:0 }"/>
										<g:if test="${requestItem.quantity < quantityOnHand }">
											<g:picklistItem requestItem="${requestItem }"/>
										</g:if>
									</td>
									--%>												
									
								</tr>
							</g:each>
						</tbody>								
					</table>
				</div>
				
				
				<div class="buttons" style="border-top: 1px solid lightgrey;">
					<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
					<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
					<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
				</div>
			</g:form>
		</div>

	</div>

</body>
</html>