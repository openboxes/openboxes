<style>
	h2 { letter-spacing: 0.1em; }
	body { font-face: verdana; }  
</style>

<div style="padding: 10px; border: 1px solid black;" >
	<g:if test="${shipmentInstance?.id}">
		<table border="0">
			<tbody>
				<tr>				
					<td width="10%" style="text-align: center;">
						<g:link action="showDetails" id="${shipmentInstance.id}">
							<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
								alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
						</g:link>
							
					</td>
					<td valign="top">
						<%-- <g:link action="showDetails" id="${shipmentInstance.id}"></g:link>--%>
						<div style="display: block;">						
							<b>${shipmentInstance?.name}</b><br/>
							<span class="fade">
								<%--${shipmentInstance?.shipmentType?.name} Shipment <br/> --%>
								${shipmentInstance?.origin?.name}
								<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle;"/> ${shipmentInstance?.destination?.name}
							</span>
						</div>
					</td>
					<td>
						<ul>
							<li>
								<g:link action="showDetails" id="${shipmentInstance.id}">
									 <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" style="vertical-align: middle;"/> Details
								</g:link>
							</li>
						</ul>
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;" colspan="2">
						<div>
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
									<div class="error">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'clock_red.png')}" style="vertical-align: middle;"/> 
										Shipment is late, was supposed to ship <b><g:relativeDate date="${shipmentInstance?.expectedShippingDate }"/></b>
										(<g:formatDate format="MMM dd" date="${shipmentInstance?.expectedShippingDate}"/>)
										
									</div>
								</g:else>									
							</g:elseif>
							<g:else>
								<div class="message">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'calendar_add.png')}" style="vertical-align: middle;"/>
									Shipment needs an expected shipping date	
								</div>
							</g:else>
						</div>
					
					</td>
				</tr>
				
				
				<tr class="prop">
					<td colspan="2">
						<span style="font-weight: bold;">Status:</span>		
						<span>${shipmentInstance?.mostRecentEvent?.eventType?.eventStatus?.name}</span>									
						<br/>
						<g:if test="${shipmentInstance?.mostRecentEvent?.eventType }">
							<span class="fade">
								${shipmentInstance?.mostRecentEvent?.eventType?.name }
								<b><g:relativeDate date="${shipmentInstance?.mostRecentEvent?.eventDate}"/></b>
								(<g:formatDate format="MMM dd" date="${shipmentInstance?.mostRecentEvent?.eventDate}"/>)
							</span>
						</g:if>
						
									
					</td>
					<td style="text-align: right;" colspan="1">
						<div style="color: #aaa; font-size: 0.75em;">
							Last modified <b><g:formatDate format="MMM dd" date="${shipmentInstance?.lastUpdated}"/></b> at								
							<b><g:formatDate format="hh:mm:ss a" date="${shipmentInstance?.lastUpdated}"/></b>
						</div>
					</td>					
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>
