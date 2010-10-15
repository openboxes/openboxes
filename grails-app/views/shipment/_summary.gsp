<div style="padding: 20px;">
	<g:if test="${shipmentInstance?.id}">
		<table>
			<tbody>
				<tr>
					<td width="24px;">
						<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
							alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
					</td>
					<td>
						
						<span style="font-size: 1.2em;">${shipmentInstance?.name}</span> 
						<div style="color: #aaa; font-size: 0.9em;">
							Shipment Number: &nbsp; ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
						</div>	
					</td>	
										
					<td style="text-align: right;">
						<g:if test="${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}">
							<img src="${createLinkTo(dir:'images/icons/eventStatus',file: shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name?.toLowerCase() + '.png')}"
								alt="${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}" style="vertical-align: middle"/>							
						</g:if>
						<g:else>
							<img src="${createLinkTo(dir:'images/icons/eventStatus',file: 'invalid.png')}"
								alt="Invalid" style="vertical-align: middle"/>							
						</g:else>
						<span class="fade">${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}</span>
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>
