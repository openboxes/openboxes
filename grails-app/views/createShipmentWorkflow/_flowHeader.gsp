<div style="float: center; border: 1px solid lightgrey; ">
	<div style=" padding: 1px;">
		<table width="100%" style="color: #ccc; display: inline;">
			<tr>
				<td style="border-right: 1px solid lightgrey; text-align: right;">
					<span class="fade">Progress</span>
				</td>
			
				<td style="text-align:center;border-right: 1px solid lightgrey;" nowrap class="${currentState=='Details'?'currentState':''}">
					<div class="circle ${currentState=='Details'?'currentState':''}">1</div> 
					<g:link action="createShipment" event="enterShipmentDetails">Details</g:link>
				</td>
<%-- 				
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
--%>
				<td style="text-align:center; border-right: 1px solid lightgrey;" nowrap class="${currentState=='Tracking'?'currentState':''}">
					<div class="circle ${currentState=='Tracking'?'currentState':''}">2</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="enterTrackingDetails">Tracking</g:link>
					</g:if>
					<g:else>Tracking</g:else>
				</td>
<%--				
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
 --%>				
				<td style="text-align:center; border-right: 1px solid lightgrey;" nowrap class="${currentState=='Pack'?'currentState':''}">
					<div class="circle ${currentState=='Pack'?'currentState':''}">3</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="enterContainerDetails">Packing</g:link>
					</g:if>
					<g:else>Packing</g:else>
				</td>
				
				
				<%-- 
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td style="text-align:center" nowrap class="${currentState=='Picking'?'currentState':''}">
					<div class="circle ${currentState=='Picking'?'currentState':''}">3</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="pickShipmentItems">Pick shipment items</g:link>
					</g:if>
					<g:else>Pick shipment items</g:else>
				</td>
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
				<td style="text-align:center" nowrap class="${currentState=='Packing'?'currentState':''}">
					<div class="circle ${currentState=='Packing'?'currentState':''}">4</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="enterContainerDetails">Pack shipment</g:link>
					</g:if>
					<g:else>Pack shipment</g:else>
				</td>
				--%>
				
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
				 <%-- 
				<td style="border-left: 1px solid lightgrey; text-align: center;">
					<g:if test="${currentState!='Details'}">
						<g:link action="createShipment" event="back"><img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" style="vertical-align: middle;"/></g:link>
					</g:if>
					<g:else>
						&lsaquo; Back
					</g:else>
				</td>
				<td style="border-left: 1px solid lightgrey; text-align: center;">
					<g:link action="createShipment" event="next">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle;"/>
					</g:link> 
				</td>
				--%>
			</tr>
		</table>
	</div>
</div>				
