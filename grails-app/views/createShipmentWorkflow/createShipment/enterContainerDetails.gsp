                                            
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
			<fieldset>

				<g:render template="../shipment/summary" />	
				<g:render template="flowHeader" model="['currentState':'Pack']"/>		
		 		
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
		 		<g:if test="${itemToMove }">
		 			<g:render template="moveItem" model="['item':itemToMove]"/>
		 		</g:if>
			 	
			 	<style>
			 		.draggable { cursor: move; } 
			 		.droppable { padding: 10px; border: 1px dashed lightgrey; } 
			 		.ui-state-highlight { font-weight: bold; color: black; }  
			 		.strikethrough { color: lightgrey; } 
			 	</style>
				
				
				
			 	<table style="border: 0px;" border="0">
			 		<tr>
			 			<td valign="top" style="border-right: 0px solid lightgrey;">
							<div style="background-color: #525d76; color: white; padding: 5px;">	
								<span class="action-menu" >
									<button class="action-btn">
										<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
										<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
									</button>
									<div class="actions" style="position: absolute; z-index: 1; display: none;">
										<g:render template="shipmentMenuItems"/>
									</div>
								</span>
								&nbsp;
								Shipment Containers 
							</div>
							<div class="list" style="text-align: left; border: 1px solid lightgrey;">
								<g:set var="count" value="${0 }"/>	
								<div style="min-height: 50px; max-height: 330px; overflow: auto; ">
									<table style="border: 0px solid #CCC;" border="0">									
										<tbody>
											<g:if test="${!shipmentInstance?.containers }">
												<tr class="">
													<td colspan="1"  style="height: 300px; text-align: center; vertical-align: middle;">
														<span class="fade">empty</span>
													</td>
												</tr>
											</g:if>
											<g:else>
												
												<tr class="odd" style="border: 0px solid lightgrey;">
													<g:set var="styleClass" value="${selectedContainer == null ? 'selectedOff' : '' }"/>
													<td class="droppable">
														<div class="${styleClass}">
															<g:if test="${selectedContainer == null  }">Unpacked</g:if>
															<g:else>
																<g:link action="createShipment" event="enterContainerDetails">Unpacked items</g:link>
															</g:else>
														</div>
													</td>
												</tr>
												
												<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
													<g:set var="styleClass" value="${containerInstance?.id == selectedContainer?.id ? 'selectedOff' : '' }"/>
													<tr class="${count++%2==0?'even':'odd' }" style="border: 0px solid lightgrey;" >
														<td style="vertical-align: middle;" id="${containerInstance?.id }" class="droppable">													
															<a name="container-${containerInstance.id }"></a>
															<div>
																<span class="${styleClass }">
																	<g:if test="${containerInstance?.id == selectedContainer?.id }">
																		${containerInstance?.name}
																	</g:if>
																	<g:else>
																		<g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]" fragment="container-${containerInstance?.id }">
																			${containerInstance?.name}
																		</g:link>
																	</g:else>
																</span>
															</div>
														</td>
													</tr>
													
													<g:each var="childContainerInstance" in="${shipmentInstance?.containers?.findAll { it.parentContainer == containerInstance}?.sort() }">
														<g:set var="styleClass" value="${childContainerInstance?.id == selectedContainer?.id ? 'selectedOff' : '' }"/>
														<tr class="${count++%2==0?'even':'odd' }" style="border: 0px solid lightgrey;">
															<td class="droppable">
																<span class="${styleClass }" style="margin-left: 25px;">
																	<a name="container-${childContainerInstance.id }"></a>
																	<g:if test="${childContainerInstance?.id == selectedContainer?.id }">
																		${childContainerInstance?.name}
																	</g:if>
																	<g:else>
																		<g:link action="createShipment" event="enterContainerDetails" params="['containerId':childContainerInstance?.id]" fragment="container-${childContainerInstance?.id }">
																			${childContainerInstance?.name}
																		</g:link>
																	</g:else>
																</span>
															</td>
														</tr>
													</g:each>
												</g:each>
											</g:else>
										</tbody>
									</table>
								</div>
							</div>			 			
			 			</td>
			 			<td valign="top">
			 				<div style="background-color: #525d76; color: white; padding: 5px">
								<span class="action-menu">
									<button class="action-btn">
										<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
										<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
									</button>
									<div class="actions" style="position: absolute; z-index: 1; display: none;">
										<g:render template="containerMenuItems" model="[container:selectedContainer]"/>
									</div>
								</span>
								<span class="fade">
					 				<g:if test="${selectedContainer?.weight }">
						 				Weight: ${selectedContainer?.weight } lbs. 
					 				</g:if>
				 					<g:if test="${selectedContainer?.description }">
										&nbsp;|&nbsp; Contents: ${selectedContainer?.description}
									</g:if>
								</span>
								<g:if test="${selectedContainer}">								
									<g:if test="${selectedContainer.parentContainer }">
										${selectedContainer?.parentContainer?.name } &rsaquo;
									</g:if>								
				 					<span>${selectedContainer?.name } </span>				 					
								</g:if>			 			
								<g:else>
									Unpacked items			 						
								</g:else>
							</div>
							
							<div class="list" style="text-align: center;">
								<g:set var="count" value="${0 }"/>	
								<table border="0" style="border: 1px solid lightgrey"  width="100%">
									<thead>	
										<tr class="${count++%2==0?'odd':'even' }">
											<th>Actions</th>
											<th>Item</th>
											<th>Qty</th>
											<th>Lot/Serial No</th>
											<th>Recipient</th>
										</tr>
									</thead>
									<tbody>
										<g:set var="shipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == selectedContainer?.id})}"/>
										<g:if test="${shipmentItems }">
											<g:each var="itemInstance" in="${shipmentItems?.sort()}">		
												<tr id="shipmentItemRow-${itemInstance?.id }" class="${count++%2==0?'odd':'even' }">
													<td>
														<div>
															<span class="action-menu">
																<button class="action-btn">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'cog.png')}" alt="Actions" style="vertical-align: middle"/>
																	<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																</button>
																<div class="actions" style="position: absolute; z-index: 1; background-color: #f5f5f5; border: 1px solid lightgrey; padding: 0px; display: none;">
																	<g:render template="itemMenuItems" model="[itemInstance:itemInstance]"/>
																</div>
															</span>								
														</div>
													</td>
													<td>
														<div>
															<span id="${itemInstance?.id }" class="draggable">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
																${itemInstance?.product?.name } 
															</span>
														</div>
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
												</tr>
											</g:each>
										</g:if>
										<g:else>
											<tr style="height: 50px; ">
												<td colspan="5" style="text-align: center; vertical-align: middle">
													<div class="fade" style="">empty</div>
												</td>
											</tr>
										</g:else>
									</tbody>
								</table>								
							</div>			 			
			 			</td>
			 		</tr>
			 	</table>
				<div class="">
					<g:form action="createShipment" method="post" >
						<table>
							<tr>
								<td width="100%" style="text-align: right;">
									<button type="submit" name="_eventId_back">&lsaquo; Back</button>	
									<button type="submit" name="_eventId_next">Next &rsaquo;</button> 
									<button type="submit" name="_eventId_save">Save & Exit</button>
									<button type="submit" name="_eventId_cancel">Cancel</button>						
								</td>
							</tr>
						</table>
		            </g:form>
				</div>
			</fieldset>
        </div>
        
				<script>

					function moveItemToContainer(item, container) { 
						$.post('/warehouse/json/moveItemToContainer', 
				                {item: item, container: container}, 
				                function(data) {
									// do nothing
									console(data)
		                    		//var item = $("<li>");
		                    		//var link = $("<a>").attr("href", "/warehouse/person/show/" + data.id).html(data.firstName + " " + data.lastName);
		                    		//item.append(link);
		                    		//$('#messages').append("new message");
		                		}, 'json');
					}
				

					$(function(){ 
						//$( ".draggable" ).draggable();
						//$('.selectable').selectable();
						$('.draggable').draggable(
							{
								revert		: true,
								zIndex		: 2700,
								autoSize	: true,
								ghosting	: true,
								onStop		: function()
								{
									$('.droppable').each(
										function()
										{
											this.expanded = false;
										}
									);
								}
							}
						);
			
						$('.droppable').droppable( {
							accept: '.draggable',
							tolerance: 'intersect',
							//greedy: true,
							over: function(event, ui) { 
								$( this ).addClass( "ui-state-highlight" );
							},
							out: function(event, ui) { 
								$( this ).removeClass( "ui-state-highlight" );
							},
							drop: function( event, ui ) {
								//ui.draggable.hide();
								ui.draggable.addClass( "strikethrough" );
								$( this ).removeClass( "ui-state-highlight" );
								var itemId = ui.draggable.attr("id");
								var moveTo = $(this).attr("id");
								//var url = "/warehouse/category/moveItem?child=" + child + "&newParent=" + parent;
								//window.location.replace(url);
								//moveItemToContainer(itemId, moveTo);
								//$("#shipmentItemRow-" + itemId).hide();
								
							}
						});



					});
				</script> 				
        
        
    </body>
</html>
