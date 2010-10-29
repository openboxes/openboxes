<style>
	.currentState { font-weight: bold; text-decoration: underline; background-color: #ccc} 
</style>

<div style="float: center; border: 1px solid #ccc; ">
	<div style="text-align: center; padding: 1px;">
		<g:if test="${shipmentInstance?.id}">
			<table border="0" style="color: #ccc">
				<tr>
					<td width="20%" style="text-align:center" nowrap class="${currentState=='Details'?'currentState':''}">
						<g:link action="suitcase" event="enterShipmentDetails">Details</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap class="${currentState=='Traveler'?'currentState':''}">
						<g:link action="suitcase" event="enterTravelerDetails" >Traveler</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap class="${currentState=='Pack'?'currentState':''}">
						<g:link action="suitcase" event="enterContainerDetails">Pack</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap class="${currentState=='Review'?'currentState':''}">
						<g:link action="suitcase" event="reviewShipment">Review</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap class="${currentState=='Ship'?'currentState':''}">
						<g:link action="suitcase" event="sendShipment">Ship</g:link>
					</td>
				</tr>
			</table>
		</g:if>
		<g:else>
		
			<table border="0" style="border: 0px solid #ccc; color: #ccc">
				<tr>
					<td width="20%" style="text-align:center" nowrap>1. 
						<span class="${currentState=='Details'?'currentState':''}">Details</span>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>2. 
						<span class="${currentState=='Traveler'?'currentState':''}">Traveler</span>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>3. 
						<span class="${currentState=='Pack'?'currentState':''}">Pack</span>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>4. 
						<span class="${currentState=='Review'?'currentState':''}">Review</span>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>5. 
						<span class="${currentState=='Ship'?'currentState':''}">Ship</span>
					</td>
				</tr>
			</table>		
		
		</g:else>
	</div>
</div>				
<br clear="all"/>