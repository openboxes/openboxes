<style>
	.currentState { font-weight: bold; background-color: #eee; color: blue; } 
.circle {
  display:inline;
  padding-left:4px;
  padding-right:4px;
  color: white;
  background: lightgrey;
  -moz-border-radius:100px;
  -webkit-border-radius:100px;
}

.circle a {
  font-size:9px;
  text-decoration:none;
  color: white;
  position:relative; top:-2px;
}
.currentState > .circle { background-color: blue; }
.currentState > .circle a { font-weight: bold; }  
</style>

<div style="float: center; border: 1px solid lightgrey; ">
	<div style=" padding: 1px;">
		<table width="100%" style="color: #ccc;">
			<tr>
				<td style="text-align:center; padding-right: 15px;" nowrap class="${currentState=='Details'?'currentState':''}">
					<div class="circle ${currentState=='Details'?'currentState':''}">1</div> 
					<g:link action="createShipment" event="enterShipmentDetails">Enter shipment details</g:link>
				</td>
<%-- 				
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
--%>
				<td style="text-align:center; padding-right: 15px;" nowrap class="${currentState=='Tracking'?'currentState':''}">
					<div class="circle ${currentState=='Tracking'?'currentState':''}">2</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="enterTrackingDetails">Enter tracking info</g:link>
					</g:if>
					<g:else>Enter tracking info</g:else>
				</td>
<%--				
				<td style="vertical-align: middle">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" style="vertical-align: middle;"/>
				</td>
 --%>				
				<td style="text-align:center; padding-right: 15px;" nowrap class="${currentState=='Pack'?'currentState':''}">
					<div class="circle ${currentState=='Pack'?'currentState':''}">3</div> 
					<g:if test="${shipmentInstance.id}">
						<g:link action="createShipment" event="enterContainerDetails">Pack shipment items</g:link>
					</g:if>
					<g:else>Pack shipment items</g:else>
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
				<td style="border-left: 1px solid lightgrey; text-align: right;">
					<g:if test="${currentState!='Details'}">
						<g:link action="createShipment" event="back">&lsaquo; Back</g:link>
					</g:if>
					<g:else>
						&lsaquo; Back
					</g:else>
					&nbsp;<span class="fade">|</span>&nbsp;
					<g:link action="createShipment" event="next">Next &rsaquo;</g:link> 
				</td>
				
			</tr>
		</table>
	</div>
</div>				
<br clear="all"/>