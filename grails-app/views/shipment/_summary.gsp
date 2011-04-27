<div style="padding: 10px;">
	<g:if test="${shipmentInstance?.id}">
		<table>
			<tbody>			
				<tr>
					<td width="24px;">
						<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
							alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 48px; height: 48px;" />					
					</td>
					<td>
						<div>
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${shipmentInstance?.name}</span>
							
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<g:render template="../shipment/sidebar" />
							&nbsp;|&nbsp;${shipmentInstance?.shipmentType?.name} Shipment <b>#${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}</b> 
							&nbsp;|&nbsp; ${shipmentInstance?.status?.name}
							
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
	</g:if>
</div>
