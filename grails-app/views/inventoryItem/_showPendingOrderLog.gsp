<div>	
	<div style="text-align: center;">
		<g:form method="GET" action="showStockCard">
			<g:hiddenField name="product.id" value="${commandInstance?.product?.id }"/>
			<div>
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
							<th style="width: 10%; text-align: center">
								${warehouse.message(code: 'inventory.qtyin.label')}
							</th>
							<th style="width: 10%; text-align: center">
								${warehouse.message(code: 'inventory.qtyout.label')}
							</th>
							<th style="width: 15%; text-align: center">
								${warehouse.message(code: 'order.status.label')}
							</th>
						</tr>

					</thead>
					<!--  Order Log -->
					<tbody>			
						<g:set var="anyPendingOrders" value="${false }"/>							
						<g:each var="entry" in="${orderMap}" status="status">
							<g:set var="order" value="${entry.key }"/>
							<g:if test="${order.isPending() }">
								<g:set var="anyPendingOrders" value="${true }"/>	
								<tr class="${(status%2==0)?'even':'odd' } prop">
									<td style="width: 10%;" nowrap="nowrap">	
										<g:if test="${order?.dateOrdered }">
											<g:formatDate date="${order.dateOrdered }" format="dd/MMM/yyyy"/>
										</g:if>
									</td>
									<td>
										<g:link controller="order" action="show" id="${order?.id }">
											${order?.name }
										</g:link>
									</td>
									<td>	
										${order?.origin?.name }
									</td>
									<td>
										${order?.destination?.name }
									</td>
									<td class="center">
										<g:if test="${order?.destination?.id == session?.warehouse?.id }">${orderMap[order] }</g:if>
									</td>
									<td class="center">
										<g:if test="${order?.origin?.id == session?.warehouse?.id }">${orderMap[order] }</g:if>
									</td>
									<td class="center">
										${order.status }
									</td>
								</tr>
							</g:if>
						</g:each>
						<g:if test="${!anyPendingOrders }">
							<tr>
								<td colspan="7" class="even center" style="min-height: 100px;">		
									<div class="fade padded">
										<warehouse:message code="order.noPendingOrders.label"/>
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
