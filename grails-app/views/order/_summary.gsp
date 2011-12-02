<div style="padding: 10px; border-bottom: 1px solid lightgrey;">
	<g:if test="${orderInstance?.id}">
	
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
		<table border="0">
			<tbody>			
				<tr>
					<td>
						<div>
							<span class="order-title" style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${orderInstance?.description}</span>							
						</div> 
						<div>
							<!-- Hide action menu menu if the user is in the shipment workflow -->						
							<g:if test="${!params.execution  && !isAddingComment && !isAddingDocument }">
								<g:render template="/order/actions" model="[orderInstance:orderInstance]"/> &nbsp;|&nbsp;
							</g:if>
							<span class="order-number">
								<warehouse:message code="order.orderNumber.label"/>: <b>${orderInstance?.orderNumber}</b>  
							</span>
							<span class="fade">&nbsp;|&nbsp;</span> 
							<span class="ordered-date">
								<warehouse:message code="order.dateOrdered.label"/>: <b><format:date obj="${orderInstance?.dateOrdered}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="ordered-by"><warehouse:message code="order.orderedBy.label"/>: 
								<g:if test="${orderInstance?.orderedBy }"><b>${orderInstance?.orderedBy?.name }</b></g:if>
								
								<g:if test="${orderInstance?.origin }">(${orderInstance?.destination?.name })</g:if>
							</span>
						</div>
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							<format:metadata obj="${orderInstance?.status}"/>
						</div>
						<br/>
						<g:if test="${!params.execution && !isAddingComment && !isAddingDocument}">						
							<g:if test="${!orderInstance?.isPlaced()}">
								<g:form action="placeOrder">
									<g:hiddenField name="id" value="${orderInstance?.id }"/>
									<button>${warehouse.message(code: 'order.placeOrder.label')}</button>
								</g:form>
							</g:if>
							<g:elseif test="${!orderInstance?.isReceived() && orderInstance?.isPlaced() }">
								<g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}">
									<button>
										${warehouse.message(code: 'order.receiveOrder.label')}
									</button> 
								</g:link>										
							</g:elseif>
						</g:if>					
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
	<g:else>
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">
								<g:if test="${orderInstance?.description}">
									${orderInstance?.description }
								</g:if>
								<g:else>
									<warehouse:message code="order.untitled.label"/>
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							<warehouse:message code="default.new.label"/>
						</div>
					</td>
				</tr>
			</tbody>
		</table>			
	
	</g:else>
</div>