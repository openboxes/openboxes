<div>	
	<div style="text-align: center; border: 1px solid lightgrey;">
		<g:form method="GET" action="showStockCard">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>
			<div class="list">
				<table>
					<thead>
						<tr class="odd prop">
							<th>
								${warehouse.message(code: 'default.date.label')}
							</th>
							<th>
								${warehouse.message(code: 'default.type.label')}
							</th>
							<th>
								${warehouse.message(code: 'transaction.source.label')}
							</th>
							<th>
								${warehouse.message(code: 'transaction.destination.label')}
							</th>
							<th style="text-align: center">
								${warehouse.message(code: 'transaction.quantityChange.label')}
							</th>
						</tr>

					</thead>
					<!--  Transaction Log -->
					<tbody>			
						<g:if test="${!shipmentMap }">
							<tr>
								<td colspan="5" class="even center" style="min-height: 100px;">		
									<div class="fade">
										<warehouse:message code="default.none.label"/>
									</div>
								</td>
							</tr>
						</g:if>
						<g:else>
							<g:set var="totalQuantityChange" value="${0 }"/>							
							<g:each var="entry" in="${shipmentMap}" status="status">
								<g:set var="shipment" value="${entry.key }"/>
								<tr class="${(status%2==0)?'even':'odd' } prop">
									<td style="width: 10%;" nowrap="nowrap">	
										<g:if test="${shipment?.expectedShippingDate }">
											<g:formatDate date="${shipment.expectedShippingDate }" format="dd/MMM/yyyy"/><br/>
											<span class="fade">${prettyDateFormat(date: shipment.expectedShippingDate)}</span> 
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
									<td style="text-align: center">
										${shipmentMap[shipment] }
									</td>
									<td>
										${shipment.status }
									</td>
								</tr>
							</g:each>
						</g:else>
					</tbody>
				</table>
			</div>
		</g:form>
	</div>
</div>
