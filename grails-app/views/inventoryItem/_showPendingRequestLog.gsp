<div>	
	<h2 class="fade"><warehouse:message code="request.pendingRequestLog.label"/></h2>
	<div style="text-align: center; border: 1px solid lightgrey;">
		<g:form method="GET" action="showStockCard">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>
			<div class="list">
				<table>
					<thead>
						<tr class="odd prop">
							<th style="width: 10%;">
								${warehouse.message(code: 'default.date.label')}
							</th>
							<th style="width: 20%;">
								${warehouse.message(code: 'default.name.label')}
							</th>
							<th style="width: 15%;">
								${warehouse.message(code: 'transaction.source.label')}
							</th>
							<th style="width: 15%;">
								${warehouse.message(code: 'transaction.destination.label')}
							</th>
							<th style="width: 10%; text-align: center;">
								${warehouse.message(code: 'inventory.qtyin.label')}
							</th>
							<th style="width: 10%; text-align: center;">
								${warehouse.message(code: 'inventory.qtyout.label')}
							</th>
							<th style="width: 15%; text-align: center;">
								${warehouse.message(code: 'request.status.label')}
							</th>
						</tr>

					</thead>
					<!--  Order Log -->
					<tbody>			
						<g:set var="anyPendingRequests" value="${false }"/>						
						<g:each var="entry" in="${requestMap}" status="status">
							<g:set var="requestInstance" value="${entry.key }"/>								
							<g:if test="${requestInstance.isPending()}">
								<g:set var="anyPendingRequests" value="${true }"/>
								<tr class="${(status%2==0)?'even':'odd' } prop">
									<td style="width: 10%;" nowrap="nowrap">	
										<g:if test="${requestInstance?.dateRequested }">
											<g:formatDate date="${requestInstance.dateRequested }" format="dd/MMM/yyyy"/>
										</g:if>
									</td>
									<td>
										<g:link controller="request" action="show" id="${requestInstance?.id }">
											${requestInstance?.description }
										</g:link>
									</td>
									<td>	
										${requestInstance?.origin?.name }
									</td>
									<td>
										${requestInstance?.destination?.name }
									</td>
									<td class="center">
										<g:if test="${requestInstance?.destination?.id == session?.warehouse?.id }">${requestMap[requestInstance] }</g:if>
									</td>
									<td class="center">
										<g:if test="${requestInstance?.origin?.id == session?.warehouse?.id }">${requestMap[requestInstance] }</g:if>
									</td>
									<td class="center">
										${requestInstance.status }
									</td>
								</tr>
							</g:if>
						</g:each>
						<g:if test="${!anyPendingRequests }">
							<tr>
								<td colspan="7" class="even center">		
									<div class="fade">
										<warehouse:message code="request.noPendingRequests.label"/>
									</div>
								</td>
							</tr>
						</g:if>
						

					</tbody>
				</table>
			</div>
		</g:form>
	</div>
</div>
