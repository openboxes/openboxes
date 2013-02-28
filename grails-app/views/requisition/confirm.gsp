<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="picklist.confirm.label" /></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>


	<g:render template="summary" model="[requisition:requisition]"/>


	<div class="yui-gc">
		<div class="yui-u first">
			<table id="confirm-requisition" class="requisition">
				<thead>
					<tr>
						<th></th>
						<th>Product</th>
						<th>Lot number</th>
						<th class="right">Requested</th>
						<th class="right">Picked</th>
						<th class="right">Canceled</th>
						<th class="right">Remaining</th>
						<th>UOM</th>
						<th>Reason code</th>
						
					</tr>
				</thead>
				<tbody>
				
					<g:set var="status" value="${1}"/>
					<g:each var="requisitionItem" in="${requisition.requisitionItems }">
						<g:set var="quantityPicked" value="${requisitionItem?.calculateQuantityPicked() }"/>
						<g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems() }"/>
						<g:if test="${picklistItems}">
							<g:each var="picklistItem" in="${picklistItems }">
								<g:set var="isSubstitution" value="${picklistItem?.inventoryItem?.product!=picklistItem?.requisitionItem?.product }"/>
								<tr class="${(isSubstitution)?'notice':'success' }">
									<td>
										${status++ }
									</td>
									
									<td>
										<g:if test="${isSubstitution }">
											<strike>${picklistItem?.requisitionItem?.product?.name}</strike>
										</g:if>
										<p>
											<g:if test="${isSubstitution}">
												<img src="${createLinkTo(dir:'images/icons',file:'indent.gif')}" />
											</g:if>
											${picklistItem.inventoryItem.product?.name }
										</p>
									</td>
									<td>
										<span class="lotNumber">
											${picklistItem.inventoryItem.lotNumber }
										</span>
									</td>
									<td class="right" width="1%">
										${requisitionItem.quantity }
										
									</td>
									<td class="right" width="1%">
										${picklistItem.quantity }
										
									</td>
									<td class="right" width="1%">
										${requisitionItem.quantityCanceled }
										
									</td>
									<td class="right" width="1%">
										${requisitionItem.calculateQuantityRemaining() }
									</td>
									<td>
										${picklistItem.inventoryItem.product.unitOfMeasure?:"EA" }							
									</td>
									<td>
										${isSubstitution?'Substitution':''}
										${requisitionItem.cancelReasonCode} 
										<p>${requisitionItem?.cancelComments }</p>										
										<%-- 
										<g:if test="${requisitionItem.quantity > quantityPicked}">							
											<g:select name="reasonCode" from="['Stockout','Damaged','Expired','Reserved']"							
												id="reasonCode" name='reasonCode' value=""
											    noSelection="${['null':'Select One...']}"/>
										</g:if>
										--%>
									</td>
								</tr>
							</g:each>
						</g:if>
						<g:else>
							<tr class="error">
								<td>${status++ }</td>
								<td>
									${requisitionItem.product }
								</td>
								<td>
									
								</td>
								<td class="right">
									${requisitionItem?.quantity }
								</td>
								<td class="right">
									${requisitionItem.calculateQuantityRemaining() }
								</td>
								<td class="right">
									${requisitionItem.quantityCanceled }
								</td>
								<td class="right">
									0
								</td>
								<td>
									${requisitionItem.product.unitOfMeasure?:"EA" }							
								</td>
								<td>
									<span title="${requisitionItem?.cancelComments }">${requisitionItem?.cancelReasonCode?:"N/A" }</span>
									
								</td>
							</tr>				
						</g:else>
					</g:each>
				</tbody>
			</table>
			<div class="clear"></div>	
			
			<div class="buttons">
				<div class="right">
					<g:link controller="requisition" action="pick" id="${requisition.id }" class="button">
						<warehouse:message code="default.button.back.label"/>	
					</g:link>
					<g:link controller="requisition" action="transfer" id="${requisition.id }" class="button">
						<warehouse:message code="default.button.next.label"/>	
					</g:link>
				</div>
			</div>			
					
		</div>
		<div class="yui-u">
			
			<table class="requisition">
				<thead>
					<tr>
						<th>Key</th>						
					</tr>
				</thead>
				<tbody>
					<tr class="success">
						<td>
							Fulfilled
						</td>
					</tr>
				
					<tr class="notice">
						<td>
							Substitution
						</td>
					</tr>
					<tr class="error">
						<td>
							Canceled
						</td>
					</tr>
				</tbody>
			</table>		
		
		</div>
	</div>
	
	
	
</div>
</body>
</html>
