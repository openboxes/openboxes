<div style="padding: 10px; border-bottom: 1px solid lightgrey;">
	<g:if test="${orderInstance?.id}">
		<table border="0">
			<tbody>			
				<tr>
					<td>
						<div>
							<span class="order-title" style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${orderInstance?.description}</span>							
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<!-- Hide action menu menu if the user is in the shipment workflow -->						
							<g:if test="${!params.execution }">
								<g:render template="/order/actions" model="[orderInstance:orderInstance]"/> &nbsp;|&nbsp;
							</g:if>
							<span class="order-number">
								Order #: <b>${orderInstance?.orderNumber}</b>  
							</span>
							<span class="fade">&nbsp;|&nbsp;</span> 
							<span class="ordered-date">
								Date ordered: <b><format:date obj="${orderInstance?.dateOrdered}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="ordered-by">Ordered by: 
								<g:if test="${orderInstance?.orderedBy }"><b>${orderInstance?.orderedBy?.name }</b></g:if>
								
								<g:if test="${orderInstance?.origin }">(${orderInstance?.destination?.name })</g:if>
							</span>
						</div>
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							<%-- ${orderInstance?.isComplete() ? "Complete" : "Pending" } --%>
							${orderInstance?.status() }
						</div>
						<br/>
						<g:if test="${!params.execution}">						
							<g:if test="${!orderInstance?.isComplete() && orderInstance?.status != org.pih.warehouse.order.OrderStatus.PLACED }">
								<g:form action="placeOrder">
									<g:hiddenField name="id" value="${orderInstance?.id }"/>
									<button>Place Order</button>
								</g:form>
							</g:if>
							<g:elseif test="${!orderInstance?.isComplete() && orderInstance?.status == org.pih.warehouse.order.OrderStatus.PLACED }">
								
								<g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${orderInstance?.id}">
									<button>
										${message(code: 'order.receive.label', default: 'Receive order')}
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
									Untitled order
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							New
						</div>
					</td>
				</tr>
			</tbody>
		</table>			
	
	</g:else>
</div>