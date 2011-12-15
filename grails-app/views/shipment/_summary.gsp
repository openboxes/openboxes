<div style="padding: 10px;">
	<table style="width: 0%">
		<tbody>			
			<tr>
				<td>
					<div>
						<g:if test="${!params.execution }">
							<g:render template="../shipment/sidebar" />
						</g:if>
					</div> 
				</td>
				<td style="width: 24px; vertical-align: middle;">				
					<g:if test="${shipmentInstance?.shipmentType }">
						<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
							alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle; width: 24px; height: 24px;" />					
					</g:if>
					<g:else>
						<img src="${createLinkTo(dir:'images/icons/silk',file: 'new.png')}"
							alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle;" />					
					</g:else>
				</td>
				<td>
					<div>
						<span class="title" style="vertical-align: middle">
							${shipmentInstance?.name}
						</span>							
					</div>
					<div>
						<span class="shipmentType">
							<warehouse:message code="shipping.shipmentType.label"/>:						
							<label>${format.metadata(obj:shipmentInstance?.shipmentType)}</label> 
						</span>
						<span class="status">
							<warehouse:message code="shipping.status.label"/>:
							<label><format:metadata obj="${shipmentInstance?.status?.code}"/></label>
						</span>
						<span class="expectedShippingDate">
							<warehouse:message code="shipping.expectedShippingDate.label"/>:						
							<label><format:date obj="${shipmentInstance?.expectedShippingDate}"/></label> 
						</span>
					</div>
											
						
				</td>	
									
				<td style="text-align: right;">
					<div class="fade" style="font-weight: bold; font-size:1.5em;">
						<%-- 
						<img src="${createLinkTo(dir:'images/icons/eventStatus',file: shipmentInstance?.status?.name?.toLowerCase() + '.png')}"
								alt="${shipmentInstance?.status?.name}" style="vertical-align: middle"/>							
						${shipmentInstance?.status?.name}
								--%>
					</div>
					
					
				</td>
			</tr>
		</tbody>
	</table>
</div>
