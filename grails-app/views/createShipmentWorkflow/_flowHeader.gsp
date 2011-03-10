<style>
	.currentState { font-weight: bold; text-decoration: underline; background-color: #eee; color: blue; } 
</style>

<div style="float: center; border: 1px solid #ccc; ">
	<div style="text-align: center; padding: 1px;">
		<table border="0" style="color: #ccc">
			<tr>
				<td width="33%" style="text-align:center" nowrap class="${currentState=='Details'?'currentState':''}">
					<g:link action="createShipment" event="enterShipmentDetails">1. Details</g:link>
				</td>
				<td rowspan="2" style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td width="34%" style="text-align:center" nowrap class="${currentState=='Traveler'?'currentState':''}">
					<g:link action="createShipment" event="enterTrackingDetails" >2. Tracking</g:link>
				</td>
				<td rowspan="2" style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td width="33%" style="text-align:center" nowrap class="${currentState=='Pack'?'currentState':''}">
					<g:link action="createShipment" event="enterContainerDetails">3. Pack</g:link>
				</td>
				
				<!-- 
				<td rowspan="2" style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td width="20%" style="text-align:center" nowrap class="${currentState=='Review'?'currentState':''}">
					<g:link action="createShipment" event="reviewShipment">4. Review</g:link>
				</td>
				<td rowspan="2" style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td width="20%" style="text-align:center" nowrap class="${currentState=='Ship'?'currentState':''}">
					<g:link action="createShipment" event="sendShipment">5. Ship</g:link>
				</td>
				-->
				
			</tr>
		</table>
	</div>
</div>				
<br clear="all"/>