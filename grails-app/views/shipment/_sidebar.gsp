<!-- Only allow the originating warehouse to edit the shipment -->

	<span class="shipment-action-menu" >
		<span>Actions<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" /></span>
		<div class="actions">
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_left.png')}" alt="Show Shipment" style="vertical-align: middle" />&nbsp;
				<g:link controller="shipment" action="listShipping">List Shipments</g:link>
			</div>
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Show Details" style="vertical-align: middle" />&nbsp;
				<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}"> 						
					<g:if test="${request.request.requestURL.toString().contains('showDetails')}"><b>Show Details</b></g:if>
					<g:else>Show Details</g:else>
				</g:link>
			</div>
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id}">				
				<div class="action-menu-item">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Shipment" style="vertical-align: middle" />&nbsp; 
					<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">
						<g:if test="${request.request.requestURL.toString().contains('createShipment')}"><b>Edit Shipment</b></g:if>
						<g:else>Edit Shipment</g:else>
					</g:link>
				</div>
			</g:if>
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
				<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" 
					id="${shipmentInstance?.id }" params="[skipTo:'Packing']">Edit Packing List</g:link>					
			</div>
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;
				<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}">
					<g:if test="${request.request.requestURL.toString().contains('showPackingList')}"><b>View Packing List</b></g:if>
					<g:else>View Packing List</g:else>
				</g:link>		
			</div>
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
				<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">Download Letter</g:link> (.docx)
			</div>
			<div class="action-menu-item">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'report.png')}" alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
				<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">Download Packing List</g:link> (.xls)
			</div>
		
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id || 
							(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">

				<div class="action-menu-item">		
					<img src="${createLinkTo(dir:'images/icons',file:'truck.png')}" alt="Send Shipment" style="vertical-align: middle" />&nbsp; 
					<g:if test="${!shipmentInstance?.hasShipped()}">
						<g:link controller="shipment" action="sendShipment" id="${shipmentInstance.id}">
								<g:if test="${request.request.requestURL.toString().contains('sendShipment')}"><b>Send Shipment</b></g:if>
								<g:else>Send Shipment</g:else>
						</g:link>				
					</g:if>
					<g:else>
						<span class="fade">Send Shipment</span>
					</g:else>
				</div>
			</g:if>

			<g:if test="${session?.warehouse?.id == shipmentInstance?.destination?.id ||
						(!(shipmentInstance?.destination?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.origin?.id)}">

				<div class="action-menu-item">
					<img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" alt="Receive Shipment" style="vertical-align: middle" />&nbsp;
					<g:if test="${shipmentInstance.hasShipped() && !shipmentInstance.wasReceived()}">
						<g:link controller="shipment" action="receiveShipment" params="${ [shipmentId : shipmentInstance.id] }">
							<g:if test="${request.request.requestURL.toString().contains('receiveShipment')}"><b>Receive Shipment</b></g:if>
							<g:else>Receive Shipment</g:else>
						</g:link>				
					</g:if>
					<g:else>
						<span class="fade">Receive Shipment</span>
					</g:else>
				</div>
			</g:if>
			<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id ||
				(!(shipmentInstance?.origin?.isWarehouse()) && session?.warehouse?.id == shipmentInstance?.destination?.id)}">	
				<div class="action-menu-item">		
					<g:link controller="shipment" action="deleteShipment" id="${shipmentInstance.id}"><img
					src="${createLinkTo(dir:'images/icons',file:'trash.png')}"
					alt="Delete Shipment" style="vertical-align: middle" />&nbsp; 
						<g:if test="${request.request.requestURL.toString().contains('deleteShipment')}"><b>Delete Shipment</b></g:if>
						<g:else>Delete Shipment</g:else>
					</g:link>				
				</div>
			</g:if>
				

		</span>
	</span>
	<script type="text/javascript">
	 	$(function(){
			function show() {
				$(this).children(".actions").show();
			}
			
			function hide() { 
				$(this).children(".actions").hide();
			}
			
			$(".shipment-action-menu").hoverIntent({
				sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
				interval: 5,   // number = milliseconds for onMouseOver polling interval
				over: show,     // function = onMouseOver callback (required)
				timeout: 100,   // number = milliseconds delay before onMouseOut
				out: hide       // function = onMouseOut callback (required)
			});  
			$( ".actions" ).position({ my: "left top", at: "left bottom", of: $(".shipment-action-menu") });	
		});
	</script>	        		
<%--  
<br/>
<div style="width: 250px" class="menu" >
	<fieldset>
		<table>
			<tr>
				<th>Package</th>
				<th>Items</th>
				<th>Weight (lbs)</th>
			</tr>
			<g:if test="${!shipmentInstance.containers }">
				<tr class="odd">
					<td colspan="3">
						<span class="fade">No contents</span>
					</td>
				</tr>			
			</g:if>
			<g:each in="${shipmentInstance.containers}" var="container" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>
						<span style="font-size: 0.8em; color: #aaa;">${container?.containerType?.name} ${container?.name}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container?.shipmentItems?.size()}</span>
					</td>
					<td style="text-align: center;">
						<span style="font-size: 0.8em; color: #aaa;">${container?.weight} ${container?.weightUnits}</span>
					</td>
				</tr>
			</g:each>
			<tr>
				<th>

				</th>
				<th>
				
				</th>
				<th>
					
				</th>			
			</tr>
			
		</table>
	</fieldset>
</div>
<br/>
--%>


<%--
<div style="width: 250px" class="menu" >
	<fieldset>
		<table>
			<tr>
				<th>Date</th>
				<th>Description</th>
			</tr>
			<g:if test="${!shipmentInstance.events }">
				<tr class="odd">
					<td colspan="2">
						<span class="fade">No events</span>
					</td>
				</tr>			
			</g:if>
			<g:each in="${shipmentInstance.events}" var="event" status="i">				
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td nowrap="nowrap">						
						<span style="font-size: 0.9em; color: #aaa;"><g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_HOUR_MONTH_DATE_FORMAT}" date="${event.eventDate}"/></span>						
					</td>
					<td>
						<span style="font-size: 0.9em; color: #aaa;">
							<b>${event?.eventType?.eventCode?.name}</b> at ${event?.eventLocation?.name}
						</span>
					</td>
				</tr>
			</g:each>
		</table>
	</fieldset>
</div>
--%>
