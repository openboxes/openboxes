                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="shipping.addShipmentItems.label"/></title>  
         
         <style>
	         .ui-autocomplete { height: 250px; overflow-y: scroll; overflow-x: hidden;}
	 		.draggable { cursor: move; } 
	 		.droppable { /*padding: 10px; border: 0px dashed lightgrey;*/ } 
	 		.sortable { }
	 		.ui-state-highlight { font-weight: bold; color: black; }  
	 		.strikethrough { color: lightgrey; } 

			tr.selected { 
	 			border-top: 1px solid lightgrey;
	 			border-bottom: 1px solid lightgrey;			
			}
	 		tr.selected .containerName a { 
	 			color: red;
	 			font-weight: bold;
	 		}	 		
	 		tr.not-selected { 
	 			border-top: 1px solid lightgrey;
	 			border-bottom: 1px solid lightgrey;
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
			<div>
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
			 			<tr>
				 			<td colspan="2">
				 				<g:render template="shipmentButtons"/>				 						 				
					 			<g:render template="containerButtons" model="[container:selectedContainer]"/>	
				 			</td>
			 			</tr>
			 		
				 		<tr class="">
					 		<%-- 
					 		
					 				Display the pallets & boxes in this shipment 
					 		
					 		--%> 
							<g:if test="${containerList }">
					 			<td valign="top" style="width: 250px; border-right: 0px solid lightgrey; padding: 0px;">
									<div class="box" >
										<g:set var="count" value="${0 }"/>	
										<table class="sortable" data-update-url="${createLink(controller:'json', action:'sortContainers')}">	
											<thead>
												<tr>
													<td colspan="5" class="center">
														<div class="title"><warehouse:message code="containers.label"/></div>
													</td>										
												</tr>
											
												<tr class="">													
													<th class="left middle">
														<g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'-1']">
															<img src="${resource(dir: 'images/icons/silk', file: 'resultset_previous.png')}" class="middle"/>
														</g:link>
													</th>
													<th colspan="3" class="center middle">														
														${containerList?.indexOf(selectedContainer)+2} of ${containerList?.size()+1 }
													</th>
													<th class="right middle">
														<g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'1']">
															<img src="${resource(dir: 'images/icons/silk', file: 'resultset_next.png')}" class="middle"/>
														</g:link>
													</th>
												</tr>															
											</thead>
											<tbody>
												<!-- 
												
													UNPACKED ITEMS 
												
												-->				
												<g:set var="styleClass" value="${selectedContainer == null ? 'selected' : 'not-selected' }"/>
												<tr class="droppable ${count++%2==0?'odd':'even' } ${styleClass }" container="null">
													<td class="left">
														<span class="action-menu" >
															<button id="unpackedItemsActionBtn" class="action-btn">
																<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
															</button>
															<div class="actions left">
																<g:render template="containerMenuItems" model="[container:containerInstance]"/>																
																<g:render template="shipmentMenuItems"/>
															</div>
														</span>
													</td>
													<td class="middle"><span class="fade">${count }.</span></td>
													<td class="middle">
														<div class="containerName">														
															<g:link action="createShipment" event="enterContainerDetails" style="display: block;"> 
																<warehouse:message code="shipping.unpackedItems.label"/>
															</g:link>
														</div>
													</td>
													<td class="middle center">
														<div class="numberCircle">
															<g:set var="unpackedShipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container == null})}"/>
											        		${unpackedShipmentItems?.size() }
											        	</div> 
													</td>
													<td class="right">	
														<span class="sorthandle"></span>
													</td> 
												</tr>		
												
												<!-- 
												
													ALL OTHER PALLETS, CRATES, BOXES 
													
												-->													
												<g:if test="${shipmentInstance?.containers }">
													<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort { it?.sortOrder }}">
														<g:set var="styleClass" value="${containerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
														<tr id="container_${containerInstance?.id }" class="droppable ${styleClass } ${count++%2==0?'odd':'even' } connectable" container="${containerInstance?.id }">
															<td class="left">
																<span class="action-menu">
																	<button id="containerActionBtn-${containerInstance?.id }" class="action-btn">
																		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																	</button>
																	<div class="actions left">
																		<g:render template="containerMenuItems" model="[container:containerInstance]"/>
																	</div>
																</span>
															</td>
															<td class="middle"><span class="fade">${count }.</span></td>
															<td style="vertical-align: middle;" id="${containerInstance?.id }" class="left">													
																<div class="containerName">
																	<a name="container-${containerInstance.id }"></a>
																	<g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]" style="display: block;">
																		${containerInstance?.name}
																	</g:link>
																</div>
															</td>
															<td class="middle right">
													        	<div class="numberCircle">
													        		${containerInstance?.shipmentItems?.size() }
													        	</div> 
															</td>
															<td class="right">	
																<span class="sorthandle"></span>
															</td> 
														</tr>
														
														<g:each var="childContainerInstance" in="${shipmentInstance?.containers?.findAll { it.parentContainer == containerInstance}?.sort() }">
															<g:set var="styleClass" value="${childContainerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
															<tr id="container_${containerInstance?.id }" class="${styleClass } ${count++%2==0?'odd':'even' }">
																<td class="left">									
																	<span class="action-menu" >
																		<button id="childContainerActionBtn-${childContainerInstance?.id }" class="action-btn">
																			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																		</button>
																		<div class="actions" style="position: absolute; z-index: 1; display: none;">
																			<g:render template="containerMenuItems" model="[container:childContainerInstance]"/>
																		</div>
																	</span>
																</td>
																<td class="middle"><span class="fade">${count }.</span></td>
																<td class="middle">
																	<div class="containerName">
																		<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
																		<a name="container-${childContainerInstance.id }"></a>
																		<%-- fragment="container-${childContainerInstance?.id }" --%>
																		<g:link action="createShipment" event="enterContainerDetails" params="['containerId':childContainerInstance?.id]">
																			${childContainerInstance?.name}
																		</g:link>
																	</div>
																</td>
																<td class="middle right">
																	<div class="numberCircle">
														        		${childContainerInstance?.shipmentItems?.size() }
														        	</div> 																
																
																</td>
																<td class="right">
																	<%-- <span class="sorthandle"></span>--%>
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
				 			
				 			<%-- 

		 							Display the contents of the currently selected container 

				 			--%>			 			
				 			<td valign="top" style="padding: 0px;">		
								<div class="box">
									<table style="border: 0px solid lightgrey">
										
										<tr>
											<td class="left" style="width:1%;">
												<div class="action-menu" >
													<button id="selectedContainerActionBtn" class="action-btn">
														<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
													</button>
													<div class="actions">
														<g:render template="containerMenuItems" model="[container:selectedContainer]"/>													
														<g:render template="shipmentMenuItems" />													
														
													</div>
												</div>	
											</td>
											<td class="middle left">
												
												<span class="middle title">
													<warehouse:message code="containers.label"/>
													&rsaquo;
												</span>
												<span class="middle title">
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
													<div class="">										
														<g:render template="/container/summary" model="[container:selectedContainer]"/>
										 			</div>	
												</g:if>								
											</td>
										</tr>
									</table>									
								
									<table>
										<thead>
											<tr>
												<th class="middle"><warehouse:message code="default.actions.label"/></th>
												<th class="left middle"><warehouse:message code="default.qty.label"/></th>
												<th class="middle"><warehouse:message code="default.item.label"/></th>
												<th class="center middle"><warehouse:message code="default.lotSerialNo.label"/></th>
												<th class="center middle"><warehouse:message code="inventoryItem.expirationDate.label"/></th>
												<th class="middle"><warehouse:message code="shipping.recipients.label"/></th>
											</tr>
										</thead>									
										<tbody>
											<g:set var="shipmentItems" value="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == selectedContainer?.id})}"/>
											<g:if test="${shipmentItems }">
												<g:set var="count" value="${0 }"/>
												<g:each var="shipmentItem" in="${shipmentItems?.sort()}">		
													<tr id="shipmentItemRow-${shipmentItem?.id }" class="${(count++%2)?'odd':'even' }">
														
														<td nowrap="nowrap" class="left">
															<div class="action-menu">
																<button id="shipmentItemActionBtn-${shipmentItem?.id }" class="action-btn">
																	<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
																</button>
																<div class="actions">
																	<g:render template="itemMenuItems" model="[itemInstance:shipmentItem]"/>
																</div>
															</div>		
															<%-- 						
															<span id="${shipmentItem?.id }" class="draggable">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'package_go.png')}" class="middle"/>
															</span>
															--%>
														</td>
														<td class="left middle">
															<span class="draggable draghandle" shipmentItem="${shipmentItem?.id }">
																<img src="${resource(dir: 'images/icons/silk', file: 'arrow_out_longer.png')}" class="middle"/>
																&nbsp;
																${shipmentItem?.quantity} 
																${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
															</span>													
														</td>
														<td class="middle">
															<div>
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.inventoryItem?.product?.id]">
																	<format:product product="${shipmentItem?.inventoryItem?.product}"/> 
																</g:link>
															</div>
														</td>
														<td class="center middle">
															<div class="lotNumber">
															${shipmentItem?.inventoryItem?.lotNumber}
															</div>
														</td>
														<td class="center middle">
															<format:date obj="${shipmentItem?.inventoryItem?.expirationDate}" format="MMMMM yyyy"/>
															
														</td>
														<td class="left middle">
															${shipmentItem?.recipient?.name}
														</td>
													</tr>
												</g:each>
											</g:if>
											<g:unless test="${shipmentItems }">
												<tr class="none">
													<td colspan="7" class="middle center" style="height: 200px;">
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
			 	</table>
			</div>
			<div class="buttons">
				<g:form action="createShipment" method="post" >
					<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
					<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button> 
					<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
					<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>					
	            </g:form>
			</div>
        </div>        
        
        
        
        <%--
        <g:render template="/createShipmentWorkflow/moveDraggableItem"/>
		--%>        
		<script>
			$(document).ready(function() {
				
				$(".sortable tbody").sortable({
				    handle : '.sorthandle', 
				    //axis : "y",
				    //helper: "clone",
				    //forcePlaceholderSize: true,
				    //placeholder: "ui-state-highlight",
					//connectWith: ".connectable",				    
				    update : function() { 
						var updateUrl = "${createLink(controller:'json', action:'sortContainers') }";						
						var sortOrder = $(this).sortable('serialize'); 
						$.post(updateUrl, sortOrder);
						$(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd")
							.filter(":even").addClass("even");
				    }
				});
			
			
				//$('.selectable').selectable();
				$('.draggable').draggable({
					handle		: ".draghandle",
					helper		: "clone",
					//helper		: function( event ) { return $("<div class='ui-widget-header'>I'm a custom helper</div>"); },
					revert		: true,
					zIndex		: 2700,
					autoSize	: true,
					ghosting	: true,
					onStart		: function ( event ) { alert("started") },
					onStop		: function() { $('.droppable').each(function() { this.expanded = false; }); }				
				});
	
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
						//alert("dropped");
						//ui.draggable.hide();
						ui.draggable.addClass( "strikethrough" );
						$( this ).removeClass( "ui-state-highlight" );
						var shipmentItem = ui.draggable.attr("shipmentItem");
						var container = $(this).attr("container");
						$("#shipmentItemRow-" + shipmentItem).hide();
						moveShipmentItemToContainer(shipmentItem, container);
						window.location.reload();
						//alert("Move item " + shipmentItem + " to container " + container);
						/*								
						//ui.draggable.hide();
						//ui.draggable.addClass( "strikethrough" );
						//$( this ).removeClass( "ui-state-highlight" );
						
						//var moveTo = $(this).attr("id");
						
						//var url = "${request.contextPath}/category/moveItem?child=" + child + "&newParent=" + parent;
						//window.location.replace(url);
						//moveItemToContainer(itemId, moveTo);
						//$("#shipmentItemRow-" + itemId).hide();						
						*/
					}
				});
				
						
				//$(".updateQuantity").change(changeQuantity);					
				
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

			function moveShipmentItemToContainer(shipmentItem, container) { 
				$.post('${request.contextPath}/json/moveShipmentItemToContainer', 
	                {shipmentItem: shipmentItem, container: container}, 
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
