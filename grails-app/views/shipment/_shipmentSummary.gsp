<style>
	h2 { letter-spacing: 0.1em; }
	body { font-face: verdana; }  
</style>

<div style="padding: 10px; border: 0px solid black;" >
	<g:if test="${shipmentInstance?.id}">
		<table border="0">
			<tbody>
				<tr>
					<th>Name</th>
				</tr>
				<tr>
					<td  style="padding-left:20px;" colspan="1">
						<b>${shipmentInstance?.name}</b>		
					</td>
					<td width="10%" style="text-align: center;" rowspan="2">
						<img src="${createLinkTo(dir:'images/icons/shipmentType/',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
							alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />
						<br/><br/>
						<g:link action="showDetails" id="${shipmentInstance.id}"><b>details</b>&nbsp;</g:link>							
					</td>
				</tr>
				<tr>
					<td>
						<table>					
							<tr>
								<th>Status</th>
							</tr>
							<tr>
								<td style="padding-left:20px;">
									<g:if test="${shipmentInstance?.mostRecentEvent?.eventType }">
										<span class="fade">
											${shipmentInstance?.mostRecentEvent?.eventType?.eventCode?.status }
											<b><g:relativeDate date="${shipmentInstance?.mostRecentEvent?.eventDate}"/></b>
											(<g:formatDate format="MMM dd" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>)
											
										</span>
									</g:if>
									<g:else>
										<span class="fade">None</span>
									</g:else>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>				
					<td valign="top" colspan="2">
						<%-- <g:link action="showDetails" id="${shipmentInstance.id}"></g:link>--%>
						<div style="display: block;">
							<span class="fade">
								<table>
									<tr>
										<th>Departing</th>
										<th></th>
										<th>Arriving</th>
									</tr>
									<tr>
										<td style="padding-left:20px;">${shipmentInstance?.origin?.name} on <g:formatDate format="MMM dd" date="${shipmentInstance?.expectedShippingDate}"/></td>	
										<td rowspan="2" valign="middle">
											<!-- <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle;"/> -->
										</td>							
										<td style="padding-left:20px;">${shipmentInstance?.destination?.name} on <g:formatDate format="MMM dd" date="${shipmentInstance?.expectedDeliveryDate}"/></td>
									</tr>
								</table>
							</span>
						</div>
					</td>
				</tr>	
				<tr>
					<td style="text-align: left;" colspan="2">
						<table>
							<tr>
								<th>Alerts</th>
							</tr>
							<tr>
								<td style="padding-left:20px;">
									<g:if test="${shipmentInstance.actualShippingDate}">
										Shipment was shipped <b><g:relativeDate date="${shipmentInstance?.actualShippingDate }"/></b>
											(<g:formatDate format="MMM dd" date="${shipmentInstance?.actualShippingDate}"/>)
									</g:if>						
									<g:elseif test="${shipmentInstance?.expectedShippingDate}">
										<g:if test="${shipmentInstance?.expectedShippingDate > new Date()}">
											<div class="fade">
												Expected to ship <b><g:relativeDate date="${shipmentInstance?.expectedShippingDate }"/></b>
												(<g:formatDate format="MMM dd" date="${shipmentInstance?.expectedShippingDate}"/>)
											</div>
										</g:if>
										<g:else>
											<div class="">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'clock_red.png')}" style="vertical-align: middle;"/> 
												Shipment is late, was supposed to ship <b><g:relativeDate date="${shipmentInstance?.expectedShippingDate }"/></b>
												(<g:formatDate format="MMM dd" date="${shipmentInstance?.expectedShippingDate}"/>)
												
											</div>
										</g:else>									
									</g:elseif>
									<g:else>
										<div class="">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar_add.png')}" style="vertical-align: middle;"/>
											Shipment needs an expected shipping date	
										</div>
									</g:else>
								</td>
							</tr>
							<tr class="prop">
								<td style="text-align: right;" colspan="3">
									<div style="color: #aaa; font-size: 0.75em;">
										Last modified <b><g:formatDate format="MMM dd" date="${shipmentInstance?.lastUpdated}"/></b> at								
										<b><g:formatDate format="hh:mm:ss a" date="${shipmentInstance?.lastUpdated}"/></b>
									</div>
								</td>					
							</tr>
							

						</table>					
						
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>
