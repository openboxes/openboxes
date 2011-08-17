<div>	
	<h2 class="fade"><warehouse:message code="shipment.pendingShipmentLog.label"/></h2>
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
							<th style="width: 10%; text-align: center">
								${warehouse.message(code: 'inventory.qtyin.label')}
							</th>
							<th style="width: 10%; text-align: center">
								${warehouse.message(code: 'inventory.qtyout.label')}
							</th>
							<th style="width: 15%; text-align: center;">
								${warehouse.message(code: 'shipment.status.label')}
							</th>
						</tr>

					</thead>
					<!--  Shipment Log -->
					<tbody>			
						<g:set var="anyPendingShipments" value="${false }"/>							
						<g:each var="entry" in="${shipmentMap}" status="status">
							<g:set var="shipment" value="${entry.key }"/>
							<g:if test="${shipment.isPending() }">
								<g:set var="anyPendingShipments" value="${true }"/>	
								<tr class="${(status%2==0)?'even':'odd' } prop">
									<td style="width: 10%;" nowrap="nowrap">	
										<g:if test="${shipment?.expectedShippingDate }">
											<g:formatDate date="${shipment.expectedShippingDate }" format="dd/MMM/yyyy"/>
										</g:if>
									</td>
									<td>
										<g:link controller="shipment" action="showDetails" id="${shipment?.id }">
											${shipment?.name }
										</g:link>
									</td>
									<td>	
										${shipment?.origin?.name }
									</td>
									<td>
										${shipment?.destination?.name }
									</td>
									<td class="center">
										<g:if test="${shipment?.destination?.id == session?.warehouse?.id }">${shipmentMap[shipment] }</g:if>
									</td>
									<td class="center">
										<g:if test="${shipment?.origin?.id == session?.warehouse?.id }">${shipmentMap[shipment] }</g:if>
									</td>
									<td class="center">
										${shipment.status } 
									</td>
								</tr>
							</g:if>
						</g:each>
						<g:if test="${!anyPendingShipments }">
							<tr>
								<td colspan="7" class="even center">		
									<div class="fade">
										<warehouse:message code="shipment.noPendingShipments.label"/>
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
