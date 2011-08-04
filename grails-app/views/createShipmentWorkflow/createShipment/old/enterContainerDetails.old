                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Add Shipment Items</title>  
    </head>
    <body>
		<div class="body">
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${containerInstance}">
				<div class="errors">
					<g:renderErrors bean="${containerInstance}" as="list" />
				</div>				
			</g:hasErrors>          
	
			<g:render template="flowHeader" model="['currentState':'Pack']"/>		
			 		
	
			<fieldset>
				<legend>Step 3&nbsp;Add shipment items</legend>	
				<g:render template="../shipment/summary" />	
		 		
		 		<!-- figure out what dialog box, if any, we need to render -->
		 		<g:if test="${containerToEdit || containerTypeToAdd}">
		 			<g:render template="editContainer" model="['container':containerToEdit, 'containerTypeToAdd':containerTypeToAdd]"/>
		 		</g:if>
		 		<g:if test="${boxToEdit || addBoxToContainerId}">
		 			<g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
		 		</g:if>
		 		<g:if test="${itemToEdit || addItemToContainerId}">
		 			<g:render template="editItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>
			 		
				<div class="" style="text-align: center;">
					<g:set var="count" value="${0 }"/>	
					
							<div style="float: left; margin: 10px; padding: 10px; border: 1px dashed black; min-height: 200px; width: 400px;">
								<h3>Loose Items </h3>
								<%-- Display each top level item --%>
								<table style="border: 1px solid #f7f7f7; display: inline" border="0" width="100%">
									<thead>	
										<tr class="${count++%2==0?'odd':'even' }">
											<th>Item</th>
											<th>Qty</th>
											<th>Lot/Serial No</th>
											<th>Recipient</th>
											<th colspan="2">Actions</th>
										</tr>
									</thead>
									<tbody>			
										<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == null})?.sort()}">		
											<g:render template="itemTableRow" model="[itemInstance:itemInstance,  count: count]"/>
										</g:each>	
									</tbody>
									<tfoot>
										<tr>
											<td>
												add an item
											</td>
										</tr> 
									</tfoot>
								</table>
							</div>								
							<g:if test="${!shipmentInstance?.containers }">
								<div style="margin: 10px; padding: 10px; border: 1px dashed black; min-height: 200px; width: 400px;">
									<span class="fade">This shipment does not contain any items.</span>
								</div>
							</g:if>
							<g:else>
								<%-- Display each top level container --%>
								<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
									<div style="float: left; margin: 10px; padding: 10px; border: 1px dashed black; min-height: 200px; width: 400px;">
										<img src="${createLinkTo(dir:'images/icons/shipmentType',file:containerInstance.containerType.name.toLowerCase() + '.jpg')}" style="vertical-align: middle"/>
										&nbsp;<span class="large bold"><h1>${containerInstance?.name}</h1></span>
										<g:link action="createShipment" event="editContainer" params="[containerToEditId:containerInstance?.id]">
											edit
										</g:link>
										
										<table style="border: 1px solid #f7f7f7; display: inline;" border="0" width="100%">
											<thead>	
												<tr class="">
													<th>Item</th>
													<th>Qty</th>
													<th>Lot/Serial No</th>
													<th>Recipient</th>
													<th colspan="2">Actions</th>
												</tr>
											</thead>
											<tbody>								
												<%-- 									
												<g:render template="containerTableRow" model="[containerInstance:containerInstance, count: count]"/>
												--%>
												<%-- Display each item in this container --%>
												<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == containerInstance?.id})?.sort()}">		
													<g:render template="itemTableRow" model="[itemInstance:itemInstance,  count: count]"/>
												</g:each>
			
			
												<%-- Display each box in this container --%>
												<g:each  var="boxInstance" in="${containerInstance?.containers?.sort()}">
													<g:render template="boxTableRow" model="[boxInstance:boxInstance, count: count]"/>										
												</g:each>
											</tbody>
											<thead>
												<tr>
													<td>
													</td>
												</tr>
											</thead>
										</table>
									</div>
									<g:if test="${(count++%2)}"><br clear="all"/></g:if>
								</g:each>
								
							</g:else>
							
						</tbody>
					</table>
					
					<g:each var="containerType" in="${shipmentWorkflow.containerTypes}">
						<span>
							<g:link action="createShipment" event="addContainer" params="[containerTypeToAddName:containerType.name]">
								<button>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" style="vertical-align: middle"/>
									&nbsp;Add a ${containerType.name}
								</button>
							</g:link>
						</span>
						&nbsp;
					</g:each>
				</div>		
				<div class="buttons">
					<g:form action="createShipment" method="post" >
						<table>
							<tr>
								<td width="100%" style="text-align: right;">
									<g:submitButton name="back" value="Back"></g:submitButton>	
									<g:submitButton name="next" value="Next"></g:submitButton> 
									<g:submitButton name="save" value="Save and Exit"></g:submitButton>
									<g:submitButton name="cancel" value="Cancel"></g:submitButton>						
								</td>
							</tr>
						</table>
		            </g:form>
				</div>
			</fieldset>
        </div>
    </body>
</html>
