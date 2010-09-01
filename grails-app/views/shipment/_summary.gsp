<div style="padding: 10px;">
	<table>
		<tbody>
			<tr>
				<td width="24px;">
					<%-- 
					<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
						valign="top" style="vertical-align: middle;" /> 
					--%>
					<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
						alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
				</td>
				<td>
					<span style="font-size: 1.2em;">${shipmentInstance.name}</span> 
					<div style="color: #aaa; font-size: 0.9em;">
						Shipment Number: &nbsp; ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
					</div>	
				</td>		
				<td style="text-align: right;">
					<span style="font-weight: bold;">${fieldValue(bean: shipmentInstance, field: "mostRecentEvent.eventType.name")}</span>
				</td>
			</tr>
		</tbody>
	</table>			
</div>
