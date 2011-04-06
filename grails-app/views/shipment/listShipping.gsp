
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipping')}" />
        <title>Shipments originating at <b>${session.warehouse.name}</b></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            	
			<table>            	
            	<tr>
            		<td style="border-right: 0px solid lightgrey; width: 200px; border-right: 1px solid lightgrey; background-color: #f5f5f5;">
            	
			            <g:form action="listShipping" method="post">
				           	<div class="filter-list-item">
				           		<label class="block">Type</label> 
				           		<g:select name="shipmentType"
												from="${org.pih.warehouse.shipping.ShipmentType.list()}"
												optionKey="id" optionValue="name" value="${shipmentType}" 
												noSelection="['':'--All--']" />&nbsp;&nbsp;    
							</div>
				           	<div class="filter-list-item">
					           	<label class="block">Destination </label>
					           	<g:select name="destination" 
					           							from="${org.pih.warehouse.core.Location.list().sort()}"
					           							optionKey="id" optionValue="name" value="${destination}" 
					           							noSelection="['':'--All--']" />&nbsp;&nbsp;
							</div>
				           	<div class="filter-list-item">
					           								
					           	<label class="block">Status</label> 
					           	<g:select name="status" 
					           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
					           					   optionKey="name" optionValue="name" value="${status}" 
					           					   noSelection="['':'--All--']" />&nbsp;&nbsp;	
							</div>
				           	<div class="filter-list-item">
					           					   
					           	<label class="block">From</label> 
					           	<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
																	value="${statusStartDate}" format="MM/dd/yyyy"/>
							</div>
				           	<div class="filter-list-item">
								<label class="block">To</label> 
								<g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
																	value="${statusEndDate}" format="MM/dd/yyyy"/>
							</div>
				           	<div class="filter-list-item right">
								<button name="filter">
									<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
									Filter </button>
							</div>
							
			            </g:form>
            		</td>
            	
					<td>            
			            <g:if test="${shipments.size()==0}">
			           		<div>
			           			<g:if test="${shipmentType || destination || status || statusStartDate || statusEndDate}">
			           				There are no shipments matching your conditions.
			           			</g:if>
			           			<g:else>
			   		        		There are no shipments originating at <b>${session.warehouse.name}</b>.
			            		</g:else>
			           		</div>
			           	</g:if>
			            
			            <g:else>
				            <div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">   
				                        	<th>${message(code: 'shipment.shipmentType.label', default: 'Type')}</th>
				                            <th>${message(code: 'shipment.shipment.label', default: 'Shipment')}</th>							
				                            <th>${message(code: 'shipment.destination.label', default: 'Destination')}</th>
				                        	<th>${message(code: 'shipment.expectedShippingDate.label', default: 'Shipping Date')}</th>
				                         	<th>${message(code: 'shipment.status.label', default: 'Status')}</th>
				                         	<th>${message(code: 'shipment.actions.label', default: 'Actions')}</th>
				                        </tr>
				                    </thead>
				                   	<tbody>
										<g:each var="shipmentInstance" in="${shipments}" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
												<td width="3%" style="text-align: center">
													<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
													alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
												</td>										
												<td width="10%">
													<g:link action="showDetails" id="${shipmentInstance.id}">
														${fieldValue(bean: shipmentInstance, field: "name")}
													</g:link>																														
												</td>
												<td width="10%" align="center">
													${fieldValue(bean: shipmentInstance, field: "destination.name")}
												</td>
												<td width="10%" align="center">
													<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.expectedShippingDate}"/>
												</td>
												<td width="10%">												
													${shipmentInstance?.status.name}
													<g:if test="${shipmentInstance?.status.date}">
													 - <g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.status.date}"/>
													 </g:if>									
												</td>
												<td nowrap="true" style="text-align: left;">
													<script type="text/javascript">
														$(document).ready(function() { 
															function show() {
																$(this).children(".actions").show();
															}
															
															function hide() { 
																$(this).children(".actions").hide();
															}
															
															$(".action-menu").hoverIntent({
																sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
																interval: 5,   // number = milliseconds for onMouseOver polling interval
																over: show,     // function = onMouseOver callback (required)
																timeout: 10,   // number = milliseconds delay before onMouseOut
																out: hide       // function = onMouseOut callback (required)
															});  
														});
													</script>									
													<div class="action-menu">
														<span>Actions<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" /></span>
														<div class="actions no-style" style="position: absolute; right: 0px; z-index:999; background-color: #f5f5f5; border: 1px solid lightgrey; padding: 10px; display: none;">
															<div class="action-menu-item">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}"
																alt="Show Shipment" style="vertical-align: middle" />&nbsp;
																<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">Show details</g:link>
															</div>
															<g:if test="${session?.warehouse?.id == shipmentInstance?.origin?.id}">				
																<div class="action-menu-item">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"
																		alt="Edit shipment" style="vertical-align: middle" />&nbsp;
																	<g:link controller="createShipmentWorkflow" action="createShipment" id="${shipmentInstance.id}">Edit shipment</g:link>
																</div>
															</g:if>
															<div class="action-menu-item">															
																<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" 
																	alt="Edit Packing List" style="vertical-align: middle"/>&nbsp;
																<g:link controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id }" params="[skipTo:'Packing']">													
																	Edit packing list
																</g:link>
															</div>
															<div class="action-menu-item">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" 
																	alt="View Packing List" style="vertical-align: middle"/>&nbsp;
																<g:link controller="shipment" action="showPackingList" id="${shipmentInstance.id}" > 
																	View packing list
																</g:link>		
															</div>
															<div class="action-menu-item">
																<fieldset>
																	<legend>Documents</legend>
																
																	<g:each in="${shipmentInstance.documents}" var="document" status="j">
																		<div class="action-menu-item">
																			<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Document" style="vertical-align: middle"/>
																			<g:link controller="document" action="download" id="${document.id}">
																				<g:if test="${document?.filename}">
																					Download ${document?.filename } <span class="fade">${document?.documentType?.name}</span>
																				</g:if>
																				<g:else>
																					Download ${document?.documentType?.name}  
																				</g:else>
																			</g:link>
																		</div>
																	</g:each>							
																	<div class="action-menu-item">														
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" 
																			alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
																		<g:link controller="doc4j" action="downloadLetter" id="${shipmentInstance?.id }">													
																			Download letter
																		</g:link>
																	</div>
																	<div class="action-menu-item">														
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'report_word.png')}" 
																			alt="View Packing List" style="vertical-align: middle"/>&nbsp;	
																		<g:link controller="doc4j" action="downloadPackingList" id="${shipmentInstance?.id }">													
																			Download packing list
																		</g:link>
																	</div>

																
																</fieldset>
															</div>
															
															
														</div>
													</div>	
												</td>															
					                        </tr>
										</g:each>                    		
				                    </tbody>
								</table>
				            </div>
			            </g:else>
					</td>
				</tr>
			</table>
        </div>		
    </body>
</html>
