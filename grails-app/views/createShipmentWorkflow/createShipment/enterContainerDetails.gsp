                                            
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
			 		
				<div class="list" style="text-align: center;">
					<g:set var="count" value="${0 }"/>	
					<table style="border: 0px solid #CCC;display: inline;" border="0">
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
							<g:if test="${!shipmentInstance?.containers }">
								<tr class="odd" style="height: 50px; text-align: center; vertical-align: middle;">
									<td colspan="6">
										This shipment contains no items.
									</td>
								</tr>
							</g:if>
							<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
							
								<tr class="${count++%2==0?'odd':'even' }" style="border: 1px solid lightgrey;">
									<td style="vertical-align: middle;">
										<img src="${createLinkTo(dir:'images/icons/shipmentType',file:containerInstance.containerType.name.toLowerCase() + '.jpg')}" style="vertical-align: middle"/>
										<g:link action="createShipment" event="editContainer" params="[containerToEditId:containerInstance?.id]">
											&nbsp;<span class="large">${containerInstance?.name}</span>
										</g:link>
									</td>
									<td></td>
									<td></td>
									<td></td>
									<td nowrap="nowrap">
										<g:link action="createShipment" event="addItemToContainer" params="['container.id':containerInstance.id]">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>
										</g:link> 													
									</td>
									<td nowrap="nowrap">
										<g:link action="createShipment" event="addBoxToContainer" params="['container.id':containerInstance.id]">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>
										</g:link>
									</td>
								</tr>
								
								<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == containerInstance?.id})?.sort()}">		
									<tr class="${count++%2==0?'odd':'even' }">
										<td>
											<span style="padding-left: 32px;">
												<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
													&nbsp;${itemInstance?.product?.name } 	
												</g:link>
											</span>
										</td>
										<td style="text-align:center;">
											${itemInstance?.quantity}
										</td>
										<td style="text-align:center;">
											${itemInstance?.lotNumber}
										</td>
										<td style="text-align:left;">
											${itemInstance?.recipient?.name}
										</td>
										<td style="text-align: left;">		
											<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">	
											<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="remove item" style="vertical-align: middle"/>
											</g:link>	
										</td>
										<td>
										
										</td>
									</tr>
								</g:each>
	
								<g:each  var="boxInstance" in="${containerInstance?.containers?.sort()}">
									<tr class="${count++%2==0?'odd':'even' }">
										<td>
											<span style="padding-left: 32px;">
											<g:link action="createShipment" event="editBox" params="[boxToEditId:boxInstance?.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Package" style="vertical-align: middle"/>
												&nbsp;${boxInstance?.name}
											</g:link>
											</span>
										</td>
										<td></td>
										<td></td>
										<td></td>
										<td style="text-align: left;" nowrap="nowrap">
											<g:link action="createShipment" event="addItemToContainer" params="['container.id':boxInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>
											</g:link> 		
										</td>
										<td nowrap="nowrap">
											<g:link action="createShipment" event="deleteBox" params="['box.id':boxInstance?.id]" onclick="return confirm('Are you sure you want to delete this box?')">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Add an item" style="vertical-align: middle"/>
											</g:link>				
										</td>
									</tr>
									
									<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == boxInstance?.id})?.sort()}">	
										<tr class="${count++%2==0?'odd':'even' }">
											<td>
												<span style="padding-left: 64px;">
													<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
														&nbsp;${itemInstance?.product?.name }
													</g:link>																	
												</span>
											</td>
											<td style="text-align:center;">
												${itemInstance?.quantity}
											</td>
											<td style="text-align:center;">
												${itemInstance?.lotNumber}
											</td>
											<td style="text-align:left;">
												${itemInstance?.recipient?.name}
											</td>
											<td style="text-align: left;" nowrap="true">		
												<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]" onclick="return confirm('Are you sure you want to delete this item?')">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="remove item" style="vertical-align: middle"/>
												</g:link>	
											</td>
											<td>
											
											</td>
										</tr>
									</g:each>												
								</g:each>
							</g:each>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="6" style="text-align: center; border: 1px dotted lightgrey">
									<g:each var="containerType" in="${shipmentWorkflow?.containerTypes}">
										<span>
											<g:link action="createShipment" event="addContainer" params="[containerTypeToAddName:containerType.name]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" style="vertical-align: middle"/>
												&nbsp;Add a ${containerType.name}
											</g:link>
										</span>
										&nbsp;
									</g:each>
								</td>
							</tr>
						</tfoot>						
					</table>
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
