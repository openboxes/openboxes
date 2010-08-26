<div>
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
					&nbsp; 
					<br/>
					<span style="color: #aaa; font-size: 0.8em;">
						last modified: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />	&nbsp;							
						created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" />			
					</span>	
				</td>		
				<td style="text-align: right;">
					<span class="fade">[Shipment No. ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}]</span>
				</td>
			</tr>
		</tbody>
	</table>			
</div>
