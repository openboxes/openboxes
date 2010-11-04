<div style="padding: 20px;">
	<g:if test="${shipmentInstance?.id}">
		<table>
			<tbody>
				<tr>
					<td width="24px;">
						<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
							alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 48px; height: 48px;" />						
					</td>
					<td>
						<div style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${shipmentInstance?.name}</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							#${fieldValue(bean: shipmentInstance, field: "shipmentNumber")} | ${shipmentInstance?.shipmentType?.name}
						</div>	
					</td>	
										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							<g:if test="${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}">
								<img src="${createLinkTo(dir:'images/icons/eventStatus',file: shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name?.toLowerCase() + '.png')}"
									alt="${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}" style="vertical-align: middle"/>							
							</g:if>
							<g:else>
								<img src="${createLinkTo(dir:'images/icons/eventStatus',file: 'invalid.png')}"
									alt="Invalid" style="vertical-align: middle"/>							
							</g:else>
							${shipmentInstance?.mostRecentEvent?.eventType?.name}
						</div>
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>
