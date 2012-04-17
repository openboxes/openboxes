                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.addShipmentItems.label"/></title>  
         
         <style>
	         .ui-autocomplete { height: 250px; overflow-y: scroll; overflow-x: hidden;}
	 		.draggable { cursor: move; } 
	 		.droppable { /*padding: 10px; border: 0px dashed lightgrey;*/ } 
	 		.ui-state-highlight { font-weight: bold; color: black; }  
	 		.strikethrough { color: lightgrey; } 
	 		.containers td { border-right: 0px solid lightgrey; }
 			.sortable tr.container { 
 				cursor: move; 
 			}

 			.sortable tr.container td.containerName { 				
 				background-image: url("${resource(dir: 'images/icons/', file: 'sortable.png')}");
				background-repeat: no-repeat;				
				background-position: right;  				
 			}
 			/*
 			td.unpackedItems { 				
 				background-image: url('${resource(dir: "images/icons", file: "draggable_off2.png")}');
				background-repeat: no-repeat;				
				background-position: right;  				
 			}
 			*/
	 		.selected { 
	 			/*background-color: lightgrey;*/
	 			background-color: lightyellow;
	 			color: red;
	 		}
	 		.not-selected { 
		 		border-right: 0px solid lightgrey;
	 		}
	 		
		</style>
				
    </head>
    <body>
    
	 	<!-- 			 	
	 		Hack used to refresh the shipment containers when sorting (because we're using AJAX)	 		
	 		${shipmentInstance?.refresh() }
	 	-->
    
		<div class="body">
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${containerInstance}">
				<div class="errors">
					<g:renderErrors bean="${containerInstance}" as="list" />
				</div>				
			</g:hasErrors> 
			
			<g:hasErrors bean="${itemInstance}">
				<div class="errors">
					<g:renderErrors bean="${itemInstance}" as="list" />
				</div>				
			</g:hasErrors> 			         
			<fieldset>
				<g:render template="../shipment/summary" />	
				<g:render template="flowHeader" model="['currentState':'Pack']"/>		
		 		
		 		<!-- figure out what dialog box, if any, we need to render -->
		 		<g:if test="${containerToEdit || containerTypeToAdd}">
		 			<g:render template="editContainer" model="['container':containerToEdit, 'containerTypeToAdd':containerTypeToAdd]"/>
		 		</g:if>
		 		<g:if test="${containerToMove}">
		 			<g:render template="moveContainer" model="['container':containerToMove]"/>
		 		</g:if>
		 		<g:if test="${boxToEdit}">
		 			<g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
		 		</g:if>
		 		<g:if test="${addBoxToContainerId}">
		 			<g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
		 		</g:if>
		 		<g:if test="${itemToEdit && shipmentInstance?.destination?.id == session?.warehouse?.id}">
		 			<g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>		 			 		
		 		<g:elseif test="${itemToEdit}">
		 			<g:render template="editItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:elseif>
		 		<%-- 
		 		<g:if test="${addItemToContainerId}">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>
		 		<g:if test="${addItemToShipmentId}">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
		 		</g:if>
		 		--%>
		 		<g:if test="${addItemToContainerId && shipmentInstance?.destination?.id == session?.warehouse?.id}">
		 			<g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>		 		
		 		<g:elseif test="${addItemToContainerId}">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:elseif>
		 		<g:if test="${addItemToShipmentId && shipmentInstance?.destination?.id == session?.warehouse?.id}">
		 			<g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
		 		</g:if>
		 		<g:elseif test="${addItemToShipmentId }">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
		 		</g:elseif>		 		
		 		
		 		<g:if test="${itemToMove }">
		 			<g:render template="moveItem" model="['item':itemToMove]"/>
		 		</g:if>
			 	
				<%-- Main content section --%>
				<g:set var="containerList" value="${shipmentInstance?.containers?new ArrayList(shipmentInstance?.containers):shipmentInstance?.containers}" />
				<table>
			 		<tbody>			 		
				 		<tr class="prop">
					 		<%-- Display the pallets & boxes in this shipment --%> 
							<g:if test="${containerList }">
					 			<td valign="top" style="padding: 0px; margin: 0px; width: 250px; border-right: 1px solid lightgrey;" >
									<div class="list" >
										<g:set var="count" value="${0 }"/>	
										<table class="sortable" data-update-url="${createLink(controller:'json', action:'sortContainers')}">	
											<thead>
												<tr>
													<td colspan="3" class="center">
														<span class="title">
															<warehouse:message code="containers.label"/>
														</span>
													</td>
												</tr>

												<tr class="prop">
													<td class="left middle">
														<g:link class="button" action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'-1']">
															<img src="${resource(dir: 'images/icons/silk', file: 'resultset_previous.png')}" class="middle"/>
														</g:link>
													</td>
													<td class="center middle">														
														${containerList?.indexOf(selectedContainer)+2} of ${containerList?.size()+1 }
													</td>
													<td class="right middle">
														<g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'1']">
															<img src="${resource(dir: 'images/icons/silk', file: 'resultset_next.png')}" class="middle"/>
														</g:link>
													</td>
												</tr>															
											
												<tr class="prop odd">
													<td colspan="3" class="middle">	
								 						<table>
								 							<tr>
								 								<td>
											 						<label><warehouse:message code="shipment.numItems.label"/></label> 
											 					</td>
											 					<td>
																	${shipmentInstance?.shipmentItems?.size() }
																</td>
															</tr>
															<tr>
																<td>
											 						<label><warehouse:message code="shipping.totalWeight.label"/></label>
											 					</td>
											 					<td>
																	<g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ? shipmentInstance?.totalWeightInPounds() : 0.00 }" /> <warehouse:message code="default.lbs.label"/>
																</td>
															</tr>
														</table>
													</td>
												</tr>
																			
												<g:set var="styleClass" value="${selectedContainer == null ? 'selected' : 'not-selected' }"/>
												<tr class="${count++%2==0?'odd':'even' } ${styleClass } prop">
													<td class="left " >
														<span class="action-menu" >
															<button class="action-btn">
																<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
															</button>
															<div class="actions left">
																<g:render template="containerMenuItems" model="[container:containerInstance]"/>
															</div>
														</span>
													</td>
													<td class="droppable middle">
														<span>
															<g:if test="${!selectedContainer }">
																<warehouse:message code="shipping.unpackedItems.label"/>
																<span class="fade">
																	&nbsp;&rsaquo;&nbsp;
																	<g:set var="unpackedShipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container == null})}"/>
																	<warehouse:message code="shipment.numShipmentItems.label" args="[unpackedShipmentItems?.size()]"/>																	
																</span>																	
															</g:if>	
															<g:else>													
																<g:link action="createShipment" event="enterContainerDetails" style="display: block;"> 
																	<warehouse:message code="shipping.unpackedItems.label"/>
																	<span class="fade">
																		&nbsp;&rsaquo;&nbsp;
																		<g:set var="unpackedShipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container == null})}"/>
																		<warehouse:message code="shipment.numShipmentItems.label" args="[unpackedShipmentItems?.size()]"/>																	
																	</span>																	
																</g:link>
															</g:else>
														</span>
													</td>
													<td></td>
												</tr>											
											</thead>
											<tbody>					
												<g:if test="${shipmentInstance?.containers }">
													<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort { it?.sortOrder }}">
														<g:set var="styleClass" value="${containerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
														<tr id="container_${containerInstance?.id }" class="${styleClass } container prop ${count++%2==0?'odd':'even' }">
															<td class="left">
																<span class="action-menu">
																	<button class="action-btn">
																		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																	</button>
																	<div class="actions left">
																		<g:render template="containerMenuItems" model="[container:containerInstance]"/>
																	</div>
																</span>
															</td>
															<td style="vertical-align: middle;" id="${containerInstance?.id }" class="left droppable">													
																<a name="container-${containerInstance.id }"></a>
																<div>
																	<span>
																		<g:if test="${containerInstance?.id == selectedContainer?.id }">
																			${containerInstance?.name} 
																			<span class="fade">
																				&nbsp;&rsaquo;&nbsp;														
																				<warehouse:message code="shipment.numShipmentItems.label" args="[containerInstance?.shipmentItems?.size()]"/>																				
																			</span>
																		</g:if>
																		<g:else>
																			<g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]" style="display: block;">
																				${containerInstance?.name}
																				<span class="fade">
																					&nbsp;&rsaquo;&nbsp;
																					<warehouse:message code="shipment.numShipmentItems.label" args="[containerInstance?.shipmentItems?.size()]"/>
																				</span>
																			</g:link>
																		</g:else>
																	</span>
																</div>
															</td>
															<td class="containerName"></td> 
														</tr>
														
														<g:each var="childContainerInstance" in="${shipmentInstance?.containers?.findAll { it.parentContainer == containerInstance}?.sort() }">
															<g:set var="styleClass" value="${childContainerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
															<tr class="${count++%2==0?'even':'odd' }">
																<td class="droppable">									
																	<div style="margin-left: 30px;">
																		<span class="action-menu" >
																			<button class="action-btn">
																				<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																			</button>
																			<div class="actions" style="position: absolute; z-index: 1; display: none;">
																				<g:render template="containerMenuItems" model="[container:childContainerInstance]"/>
																			</div>
																		</span>
																		<span class="${styleClass }"> 
																			<a name="container-${childContainerInstance.id }"></a>
																			<g:if test="${childContainerInstance?.id == selectedContainer?.id }">
																				${childContainerInstance?.name}
																			</g:if>
																			<g:else>
																				<%-- fragment="container-${childContainerInstance?.id }" --%>
																				<g:link action="createShipment" event="enterContainerDetails" params="['containerId':childContainerInstance?.id]">
																					${childContainerInstance?.name}
																				</g:link>
																			</g:else>
																		</span>
																	</div>
																</td>
															</tr>
														</g:each>
													</g:each>
												</g:if>
											</tbody>
										</table>
									</div>			 			
					 			</td>
							</g:if>
				 			
				 			<%-- Display the contents of the currently selected container --%>			 			
				 			<td valign="top" style="padding: 0; margin: 0">
								<g:set var="count" value="${0 }"/>	
								<table>
									<tr>
										<td style="width: 20px;">
											<span class="action-menu" >
												<button class="action-btn">
													<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
												</button>
												<div class="actions">
													<g:render template="containerMenuItems" model="[container:selectedContainer]"/>
													<div class="action-menu-item">
														<hr/>
													</div>
													<g:render template="shipmentMenuItems"/>
													
												</div>
											</span>	
										</td>
										<td class="middle">
								
											<span class="title middle">
												<g:if test="${selectedContainer}">								
													<g:if test="${selectedContainer.parentContainer }">
														${selectedContainer?.parentContainer?.name } &rsaquo;
													</g:if>				
								 					${selectedContainer?.name }		 					
												</g:if>
												<g:else>
													<warehouse:message code="shipping.unpackedItems.label" />			 						
												</g:else>
											</span>
											<g:if test="${selectedContainer}">	
													<span class="fade">
										 				<warehouse:message code="shipment.numItems.label"/>: 
										 			</span>
								 					<span>
								 						${selectedContainer?.shipmentItems?.size() }
								 					</span>
								 					&nbsp;|&nbsp; 
													<span class="fade">
										 				<warehouse:message code="default.weight.label"/>: 
										 			</span>
									 				<g:if test="${selectedContainer?.weight }">
										 				<g:formatNumber format="#,##0.00" number="${selectedContainer?.weight }"/>
										 				 ${selectedContainer?.weightUnits}
									 				</g:if>
									 				<g:else>
										 				<g:formatNumber format="#,##0.00" number="${selectedContainer?.totalWeightInPounds() ? selectedContainer?.totalWeightInPounds() : 0.00 }" /> 
										 				 <warehouse:message code="default.lbs.label"/>
									 				</g:else>
									 				&nbsp;|&nbsp; 
													<span class="fade">
										 				<warehouse:message code="shipping.dimensions.label"/>:
										 			</span>
								 					<g:if test="${selectedContainer?.width ||  selectedContainer?.length || selectedContainer?.height}">
														
														${selectedContainer.height == null ? '?' : selectedContainer.height} ${selectedContainer?.volumeUnits}
														x
														${selectedContainer.width == null ? '?' : selectedContainer.width} ${selectedContainer?.volumeUnits}
														x
														${selectedContainer.length == null ? '?' : selectedContainer.length} ${selectedContainer?.volumeUnits}
													</g:if>
													<g:else>
														<warehouse:message code="default.none.label"/>
													</g:else>
													
													<span class="fade">
														&nbsp;|&nbsp; <warehouse:message code="shipping.recipient.label"/>:
													</span>
								 					<g:if test="${selectedContainer?.recipient }">
														${selectedContainer?.recipient?.name }	
													</g:if>
													<g:else>
														<warehouse:message code="default.none.label"/>
													</g:else>
											</g:if>								
										</td>
									</tr>
								</table>									
								<div class="list">
									<table>
										<thead>
											<tr style="height: 31px;" class="prop">
												<th class="middle"><!--<warehouse:message code="default.actions.label"/>--></th>
												<th class="center middle"><warehouse:message code="default.qty.label"/></th>
												<th class="middle"><warehouse:message code="default.item.label"/></th>
												<th class="center middle"><warehouse:message code="default.lotSerialNo.label"/></th>
												<th class="center middle"><warehouse:message code="inventoryItem.expirationDate.label"/></th>
												<th class="middle"><warehouse:message code="shipping.recipients.label"/></th>
											</tr>
										</thead>									
										<tbody>
											<g:set var="shipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == selectedContainer?.id})}"/>
											<g:if test="${shipmentItems }">
												<g:each var="shipmentItem" in="${shipmentItems?.sort()}">		
													<tr id="shipmentItemRow-${shipmentItem?.id }" class="${count++%2==0?'odd':'even' }">
														<td nowrap="nowrap" width="3%">
															<span class="action-menu">
																<button class="action-btn">
																	<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																</button>
																<div class="actions">
																	<g:render template="itemMenuItems" model="[itemInstance:shipmentItem]"/>
																</div>
															</span>		
															<%-- 						
															<span id="${shipmentItem?.id }" class="draggable">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}" class="middle"/>
															</span>
															--%>
														</td>
														<td class="center middle">
															${shipmentItem?.quantity}															
														</td>
														<td class="middle">
															<div>
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.product?.id]">
																	<format:product product="${shipmentItem?.product}"/> 
																</g:link>
															</div>
														</td>
														<td class="center middle">
															${shipmentItem?.lotNumber}
														</td>
														<td class="center middle">
															<format:date obj="${shipmentItem?.expirationDate}" format="MMM yyyy"/>
															
														</td>
														<td class="left middle">
															${shipmentItem?.recipient?.name}
														</td>
													</tr>
												</g:each>
											</g:if>
											<g:unless test="${shipmentItems }">
												<tr class="none">
													<td colspan="6" class="middle center" style="height: 150px;">
														<div class="middle center">
															<span class="fade">
																<warehouse:message code="shipment.noShipmentItems.message"/>
															</span>
														</div>																								
													</td>
												</tr>
											</g:unless>
										</tbody>
									</table>								
								</div>			 			
				 			</td>
				 		</tr>
				 	</tbody>
                    <tfoot>
                    	<tr class="prop">
                    		<td colspan="2">
								<div class="buttons">
									<g:form action="createShipment" method="post" >
										<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
										<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button> 
										<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
										<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>					
						            </g:form>
			 					</div>
			 				</td>
			 			</tr>
			 		</tfoot>			 		
			 	</table>
			</fieldset>
        </div>
        
        
        <g:render template="/createShipmentWorkflow/moveDraggableItem"/>
        
		<script>
			
			$(document).ready(function() {
			
			
				$(".sortable tbody").sortable({
				    //handle : '.handle', 
				    axis : "y",
				    helper: "clone",
				    placeholder: "ui-state-highlight",
				    update : function() { 
						var updateUrl = "${createLink(controller:'json', action:'sortContainers') }";						
						var sortOrder = $(this).sortable('serialize'); 
						$.post(updateUrl, sortOrder);
						$(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd");
				    } 				
				});
			
			
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
						//alert(ui.draggable.attr("id"));		
						
								
						$("#dlgMoveDraggableItem").dialog({ autoOpen: true, modal: true, width: '600px'});	
						
						//ui.draggable.hide();
						//ui.draggable.addClass( "strikethrough" );
						//$( this ).removeClass( "ui-state-highlight" );
						//var itemId = ui.draggable.attr("id");
						//var moveTo = $(this).attr("id");
						
						//var url = "${request.contextPath}/category/moveItem?child=" + child + "&newParent=" + parent;
						//window.location.replace(url);
						//moveItemToContainer(itemId, moveTo);
						//$("#shipmentItemRow-" + itemId).hide();						
					}
				});
				
						
				$(".updateQuantity").change(changeQuantity);					
				
			});
		
			function changeQuantity() {
				var totalQuantity = $("#totalQuantity").val(); 
				var updateQuantity = getUpdateQuantity();
				var currentQuantity = parseInt(totalQuantity) - parseInt(updateQuantity);
				if (currentQuantity >= 0) { 
					$("#currentQuantity").val(currentQuantity);
				} 
				else { 
					alert("Please specify values that total the initial quantity of " + totalQuantity);
					$(this).val(0);
					$(this).focus();
				}
			}			
					
			function getTotalQuantity() {
				var currentQuantity = $("#currentQuantity").val();
				var updateQuantity = getUpdateQuantity();
				return parseInt(currentQuantity) + parseInt(updateQuantity);
			}
			
			function getUpdateQuantity() { 
				var updateQuantity = 0;
				$(".updateQuantity").each(function() {
	                updateQuantity += Number($(this).val());
	            });
	            return updateQuantity;
			}

			function moveItemToContainer(item, container) { 
				$.post('${request.contextPath}/json/moveItemToContainer', 
	                {item: item, container: container}, 
		                function(data) {
							// do nothing for now
							//console(data);
                    		//var item = $("<li>");
                    		//var link = $("<a>").attr("href", "${request.contextPath}/person/show/" + data.id).html(data.firstName + " " + data.lastName);
                    		//item.append(link);
                    		//$('#messages').append("new message");
                		}, 'json');
			}


		</script> 				
        
        
    </body>
</html>
