<style>
	.currentState { font-weight: bold; } 
</style>

<div style="float: right;">
	<div style="text-align: center; padding: 10px;">
		<g:if test="${shipmentInstance?.id}">
			<table border="0" style="border: 0px solid #ccc; color: #ccc">
				<tr>
					<td width="20%" style="text-align:center" nowrap>1. 
						<g:link action="suitcase" event="enterShipmentDetails" class="${currentState=='Details'?'currentState':''}">Details</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>2. 
						<g:link action="suitcase" event="enterTravelerDetails" class="${currentState=='Traveler'?'currentState':''}">Traveler</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>3. 
						<g:link action="suitcase" event="enterContainerDetails" class="${currentState=='Pack'?'currentState':''}">Pack</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>4. 
						<g:link action="suitcase" event="reviewShipment" class="${currentState=='Review'?'currentState':''}">Review</g:link>
					</td>
					<td rowspan="2" style="vertical-align: middle">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
					</td>
					<td width="20%" style="text-align:center" nowrap>5. 
						<g:link action="suitcase" event="sendShipment" class="${currentState=='Ship'?'currentState':''}">Ship</g:link>
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