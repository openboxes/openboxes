                                            
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
	 		.selected { 
	 			border-right: 0px; 
				padding-left:20px;
		 		background:transparent url('${resource(dir: 'images/icons/silk', file: 'bullet_yellow.png')}') center left no-repeat;
	 		}
	 		.not-selected { 
	 			border-right: 0px; 
				padding-left:20px;
		 		background:transparent url('${resource(dir: 'images/icons/silk', file: 'bullet_white.png')}') center left no-repeat;
	 		}
		</style>
				
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
		 		<g:if test="${itemToEdit}">
		 			<g:render template="editItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>
		 		<g:if test="${addItemToContainerId}">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
		 		</g:if>
		 		<g:if test="${addItemToShipmentId}">
		 			<g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
		 		</g:if>
		 		<g:if test="${itemToMove }">
		 			<g:render template="moveItem" model="['item':itemToMove]"/>
		 		</g:if>
			 	
			 	
				<%-- Main content section --%>
			 	<table>
			 		<tr>
				 		<%-- Display the pallets & boxes in this shipment --%> 
			 			<td valign="top" style="padding: 0px; margin: 0px; width: 250px; height: 100px; border-right: 1px solid lightgrey;" >
							<div class="list" style="text-align: left; border: 0px solid lightgrey;">
								<g:set var="count" value="${0 }"/>	
															
								<table style="border: 0px" border="0">	
									<thead>
										<tr class="odd"><%-- --%>
											<td colspan="2">
												<h3><warehouse:message code="shipping.allShipmentContainers.label"/></h3>
											</td>
										</tr>
										<tr class="odd">
											<td colspan="2">
												<div style="border: 0px;">
													<span class="action-menu" >
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
														</button>
														<div class="actions" style="position: absolute; z-index: 1; display: none;">
															<g:render template="shipmentMenuItems"/>
															<%-- 
															<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
																<div class="action-menu-item">														
																	<g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]" fragment="container-${containerInstance?.id }">
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="View" style="vertical-align: middle"/>&nbsp;${containerInstance?.name }
																	</g:link>
																</div>
															</g:each>
															--%>
														</div>
													</span>				
								 					<span class="fade">&nbsp;|&nbsp; <warehouse:message code="shipping.totalWeight.label"/>:</span> 
													<g:formatNumber format="#,##0.00" number="${shipmentInstance?.totalWeightInPounds() ? shipmentInstance?.totalWeightInPounds() : 0.00 }" /> <warehouse:message code="default.lbs.label"/>
										 		</div>
										 	</td>
										</tr>
										<tr class="${count++%2==0?'odd':'even' }">
											<th><warehouse:message code="container.label"/></th>
										</tr>
									</thead>
									<tbody>
										
										<tr class="${count++%2==0?'even':'odd' }">
											<g:set var="styleClass" value="${selectedContainer == null ? 'selected' : 'not-selected' }"/>
											<td class="droppable">
												<div class="${styleClass }">
													<span class="action-menu" >
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
														</button>
														<div class="actions" style="position: absolute; z-index: 1; display: none;">
															<g:render template="containerMenuItems" model="[container:containerInstance]"/>
														</div>
													</span>
												
												
													<g:link action="createShipment" event="enterContainerDetails"><warehouse:message code="shipping.unpackedItems.label"/></g:link>
												</div>
											</td>
										</tr>										
										<g:if test="${shipmentInstance?.containers }">
											<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort { it?.name?.toLowerCase() }}">
												<g:set var="styleClass" value="${containerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
												<tr style="border: 0px solid lightgrey;" class="${count++%2==0?'even':'odd' }">
													<td style="vertical-align: middle;" id="${containerInstance?.id }" class="droppable">													
														<a name="container-${containerInstance.id }"></a>
														<div class="${styleClass }">
															<span class="action-menu" >
																<button class="action-btn">
																	<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																</button>
																<div class="actions" style="position: absolute; z-index: 1; display: none;">
																	<g:render template="containerMenuItems" model="[container:containerInstance]"/>
																</div>
															</span>
														
															<g:if test="${containerInstance?.id == selectedContainer?.id }">
																${containerInstance?.name}
															</g:if>
															<g:else>
																<%-- fragment="container-${containerInstance?.id }"  --%>
																<g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]">
																	${containerInstance?.name}
																</g:link>
															</g:else>
															<span class="fade rounded">
																(${containerInstance?.shipmentItems?.size() })
															</span>
														</div>
													</td>
												</tr>
												
												<g:each var="childContainerInstance" in="${shipmentInstance?.containers?.findAll { it.parentContainer == containerInstance}?.sort() }">
													<g:set var="styleClass" value="${childContainerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
													<tr class="${count++%2==0?'even':'odd' }">
														<td class="droppable">
															
															<div class="${styleClass }" style="margin-left: 25px;">
																<span class="action-menu" >
																	<button class="action-btn">
																		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																	</button>
																	<div class="actions" style="position: absolute; z-index: 1; display: none;">
																		<g:render template="containerMenuItems" model="[container:childContainerInstance]"/>
																	</div>
																</span>
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
																<span class="fade rounded">
																	(${childContainerInstance?.shipmentItems?.size() })
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
			 			
			 			
			 			<%-- Display the contents of the currently selected container --%>			 			
			 			<td valign="top" style="padding: 0; margin: 0">
							<div class="list">
								<g:set var="count" value="${0 }"/>	
								<table style="height: 100%">
									<thead>	
										<tr class="odd">
											<td colspan="5">
												<h3>
													<g:if test="${selectedContainer}">								
														<g:if test="${selectedContainer.parentContainer }">
															${selectedContainer?.parentContainer?.name } &rsaquo;
														</g:if>						
									 					${selectedContainer?.name }				 					
													</g:if>			 			
													<g:else>
														<warehouse:message code="shipping.unpackedItems.label" />			 						
													</g:else>
												</h3>
											</td>
										</tr>
										<tr>
											<td colspan="5" class="odd">
								 				<div>
													<span class="action-menu" >
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
														</button>
														<div class="actions" style="position: absolute; z-index: 1; display: none;">
															<g:render template="containerMenuItems" model="[container:selectedContainer]"/>
														</div>
													</span>				
													<span class="fade">
										 				&nbsp;|&nbsp; <warehouse:message code="default.weight.label"/>: 
										 			</span>
									 				<g:if test="${selectedContainer?.weight }">
										 				<g:formatNumber format="#,##0.00" number="${selectedContainer?.weight }"/>
										 				 ${selectedContainer?.weightUnits}
									 				</g:if>
									 				<g:else>
										 				<g:formatNumber format="#,##0.00" number="${selectedContainer?.totalWeightInPounds() ? selectedContainer?.totalWeightInPounds() : 0.00 }" /> 
										 				 <warehouse:message code="default.lbs.label"/>
									 				</g:else>
									 				
													<span class="fade">
										 				&nbsp;|&nbsp; <warehouse:message code="shipping.dimensions.label"/>:
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
												</div>
											</td>
										</tr>
									</thead>
								</table>
							</div>
							<div class="list">
								<table>
									<thead>
										<tr>
											<th><warehouse:message code="default.actions.label"/></th>
											<th class="center"><warehouse:message code="default.qty.label"/></th>
											<th><warehouse:message code="default.item.label"/></th>
											<th nowrap="nowrap"><warehouse:message code="default.lotSerialNo.label"/></th>
											<th><warehouse:message code="shipping.recipients.label"/></th>
										</tr>
									</thead>									
									<tbody>
										<g:set var="shipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == selectedContainer?.id})}"/>
										<g:if test="${shipmentItems }">
											<g:each var="itemInstance" in="${shipmentItems?.sort()}">		
												<tr id="shipmentItemRow-${itemInstance?.id }" class="${count++%2==0?'odd':'even' }">
													<td nowrap="nowrap" width="3%">
														<div>
															<span class="action-menu">
																<button class="action-btn">
																	<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																</button>
																<div class="actions" style="position: absolute; z-index: 1; display: none; min-width: 200px">
																	<g:render template="itemMenuItems" model="[itemInstance:itemInstance]"/>
																</div>
															</span>								
														</div>
													</td>
													<td style="text-align:center;">
														${itemInstance?.quantity}
													</td>
													<td>
														<div>
															<g:link controller="inventoryItem" action="showStockCard" params="['product.id':itemInstance?.product?.id]">
																<format:product product="${itemInstance?.product}"/> 
															</g:link>
															<%-- 
															<span id="${itemInstance?.id }" class="draggable">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
															</span>
															--%>
														</div>
													</td>
													<td>
														${itemInstance?.lotNumber}
													</td>
													<td style="text-align:left;">
														${itemInstance?.recipient?.name}
													</td>
												</tr>
											</g:each>
										</g:if>
									
									</tbody>
									<%-- 
									<tfoot>
										<tr style="border-top: 1px solid lightgrey">
											<td>
												<span class="action-menu">
													<button class="action-btn">
														<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
														<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
													</button>
													<div class="actions" style="position: absolute; z-index: 1; display: none;">
														<g:render template="containerMenuItems" model="[container:selectedContainer]"/>
													</div>
												</span>
											</td>
										</tr>
									</tfoot>
									--%>
								</table>								
							</div>			 			
			 			</td>
			 		</tr>
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
        
		<script>
			
			$(document).ready(function() {
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
