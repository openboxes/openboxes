
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">Shipment Details</content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>    
				
		<%-- 				
    	<script type="text/javascript" src="${createLinkTo(dir:'js/yui/2.7.0/yahoo-dom-event',file:'yahoo-dom-event.js')}" charset="utf-8"></script>
    	<script type="text/javascript" src="${createLinkTo(dir:'js/yui/2.7.0/container',file:'container.js')}" charset="utf-8"></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js/yui/2.7.0/element',file:'element-min.js')}"></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js/yui/2.7.0/tabview',file:'tabview-min.js')}"></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js/yui/2.7.0/history',file:'history-min.js')}"></script>
		--%>
		
		
		<%-- 
		<link type="text/css" href="${createLinkTo(dir:'js/jquery.ui/css/cupertino',file:'jquery-ui-1.8.2.custom.css')}" rel="stylesheet" />
		<script type="text/javascript" src="${createLinkTo(dir:'js/jquery.ui',file:'jquery-1.4.2.min.js')}"></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js/jquery.ui',file:'jquery-ui-1.8.2.custom.min.js')}"></script>
		--%>
	
	    <style type="text/css" media="screen">
	    	table {
	    		/*background-color: white;*/
	    	} 
	        .container {
	            width: 100%;
	        }
	
            .container h2 {
                font-size: 136%;
                font-weight: bold;
            }
	
			.switcher a {
			    display: block;
			    float: right;
			    font-weight: normal;
			    text-decoration: none;
			}

             .module {
                padding: 5px;
            }
            #yui-history-iframe {
			  position:absolute;
			  top:0; left:0;
			  width:1px; height:1px; /* avoid scrollbars */
			  visibility:hidden;
			}
            
	    </style>
	    
	    
    </head>
    <body>
    
        <div class="body" style="width:100%;">
          	
            <g:if test="${flash.message}">
           		<div class="message">${flash.message}</div>
            </g:if>            

			<div id="dialogBanner">
				<table class="withoutBorder">
					<tr>
						<td align="left">
									
							<span style="">
								<g:if test="${shipmentInstance?.name}">					
									<span style="padding-left: 5px; color:#aaa"><g:if test="${shipmentInstance?.shipmentNumber}">${shipmentInstance?.shipmentNumber}</g:if></span>		
									<span style="font-weight: bold;">${shipmentInstance?.name}</span>
								</g:if>
							</span>
						</td>
						<td align="right" style="text-align: right;">
							<span style="color: ${shipmentInstance.shipmentStatus.color}; font-weight: bold;"> 
								${shipmentInstance.shipmentStatus.name}
							</span>	
							
							
						</td>
					</tr>
				</table>			
			</div>	
			<div id="shipmentJqueryTabs">

			</div>			
            <div id="shipmentDetails">
				<gui:tabView id="shipmentTabs">										
					<gui:tab id="shipmentDetailsTab" label="Details" active="true">			
						<div id="detailsTab" class="tab">			
							
							<!-- 
							<div style="color:blue; background-color: #eee; padding: 10px; font-weight: bold; border-top: 1px solid #aaa ">
								<table class="withoutBorder">
									<tr>
										<td>Shipment Details</td>
										<td style="text-align: right"><a href="#" id="edit-details-link" ><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit"/></a></td>
									</tr>
								</table>
							</div>
							 -->						
							<table class="withoutBorder" border="0" style="background-color: white;">
			                    <tbody> 		                    
			                    	<tr>
										<td width="33%">
											<div class="tab">
												<table class="withoutBorder" >
													<tbody>
								                        <tr class="prop">
								                            <td valign="top" class="address" nowrap="nowrap" style="border-top: 1px solid #ccc; padding-bottom: 0px;">
								                            	<div style="color:#ccc">From:</div>
								                            	<div id="view-origin" style="padding: 10px;">
									                            	<table class="withoutBorder">
									                            		<tbody>
									                            			<tr>
									                            				<td><span class="large" style="font-weight: bold;">${shipmentInstance?.origin?.encodeAsHTML()}</span></td>
									                            				<td><a href="#" id="edit-origin-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit"/></a></td>
									                            			</tr>
									                            			<tr>
										                            			<td>${shipmentInstance?.origin?.address?.address}</td>
										                            		</tr>
									                            			<tr>
										                            			<td>${shipmentInstance?.origin?.address?.city}, 
										                            				${shipmentInstance?.origin?.address?.stateOrProvince}&nbsp;&nbsp; 
										                            				${shipmentInstance?.origin?.address?.postalCode}
										                            			</td>
										                            		</tr>
									                            			<tr>
										                            			<td>${shipmentInstance?.origin?.address?.country}</td>
										                            		</tr>
									                            		</tbody>
									                            	</table>						                            		
								                            	</div>					                            	
								                            	<div id="edit-origin" style="display:none; padding: 10px;">
									                            	<g:form method="post" action="update">
														                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
														                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
										                            	<span class=" ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
										                					<g:select name="origin.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.origin?.id}"  />						                					
																		</span>
																		<br/><br/>

																		<span class="buttons">
																			<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> ${message(code: 'default.button.save.label', default: 'Save')}</button>
																			<a href="#" id="edit-origin-link" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
																		</span>
																	</g:form>
								                            	</div>
								                            </td>          
								                        </tr>              
								                        <tr class="prop">
								                            <td valign="top" class="address" style="border-top: 1px solid #ccc; padding-bottom: 0px;">
			
								                            	<div style="color:#ccc">To:</div>
								                            	<div id="view-destination" style="padding: 10px;">
									                            	<table class="withoutBorder">
									                            		<tbody>
									                            			<tr>
									                            				<td><span class="large" style="font-weight: bold;">${shipmentInstance?.destination?.encodeAsHTML()}</span></td>
									                            				<td><a href="#" id="edit-destination-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit"/></a></td>
									                            			</tr>
									                            		
									                            			<tr>
										                            			<td>${shipmentInstance?.destination?.address?.address}</td>
										                            		</tr>
									                            			<tr>
										                            			<td>
											                            			${shipmentInstance?.destination?.address?.city}, 
											                            			${shipmentInstance?.destination?.address?.stateOrProvince}&nbsp;&nbsp; 
											                            			${shipmentInstance?.destination?.address?.postalCode}
										                            			</td>
										                            		</tr>
									                            			<tr>
										                            			<td>${shipmentInstance?.destination?.address?.country}</td>
										                            		</tr>
									                            		</tbody>
									                            	</table>
									                            </div>
								                            	<div id="edit-destination" style="display:none; padding: 10px;">					                            	
									                            	<g:form method="post" action="update">
														                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
														                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
										                            	<span class=" ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
										                					<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.destination?.id}"  />								                					
																		</span>
																		<br/><br/>
																		<span class="buttons">
																			<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> ${message(code: 'default.button.save.label', default: 'Save')}</button>
																			<a href="#" id="edit-destination-link" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>
																		</span>
																	</g:form>
								                            	</div>
								                            </td>  	
								                        </tr>									                    		
								                    </tbody>                    
												</table>
											</div>
			                    		</td>
			                    		<td >
			                    			
											<div id="detailsTab" class="tab">
												<table class="withoutBorder" style="background-color: white;">
													<tbody>
								                        <tr class="prop">
								                        	<td style="border-top: 1px solid #ccc; padding-bottom: 0px;">					    
								                        		<div style="color:#ccc">Shipment details:</div>		            		
												            	<div id="view-details" style="padding: 10px;">		
																	<table class="withoutBorder" cellspacing="5" cellpadding="5">		   
																		<tbody>                 			
												           					<tr class="prop">	                        	
													                            <td valign="top" class="name">
													                            	<label><warehouse:message code="shipment.shipmentStatus.label" default="Nickname" /></label>
													                            </td>        
													                            <td valign="top" class="value">
													                            	${fieldValue(bean: shipmentInstance, field: "name")}		  
													                            </td>
													                        </tr>                    
												           					<tr class="prop">	                        	
													                            <td valign="top" class="name">
													                            	<label><warehouse:message code="shipment.shipmentType.label" default="Type" /></label>
													                            </td>        
													                            <td valign="top" class="value">
													                            	${fieldValue(bean: shipmentInstance, field: "shipmentType.name")}		                            	
													                            </td>
													                        </tr>                    
												           					<tr class="prop">	                        	
													                            <td valign="top" class="name">
													                            	<label><warehouse:message code="shipment.shipmentStatus.label" default="Status" /></label>
													                            </td>        
													                            <td valign="top" class="value">
													                            	${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}										                            	                          	
													                            </td>
													                        </tr>                    
																			
																			<tr class="prop">
																				<td valign="top" class="name">
																					<label><warehouse:message code="shipment.expectedShippingDate.label" default="Ship date" /></label>
																				</td>                            
																				<td valign="top" class="value" nowrap="nowrap">
																					<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
																				</td>
																			</tr>
																			<tr class="prop">
																				<td valign="top" class="name">
																					<label><warehouse:message code="shipment.expectedDeliveryDate.label" default="Delivery date" /></label>
																				</td>   
																				<td valign="top" class="value" nowrap="nowrap">
																					<g:if test=""><format:date obj="${shipmentInstance?.expectedDeliveryDate}"/></g:if>
																					<g:else><span style="color: #aaa">(to be determined)</span></g:else>
																					
																				</td>
																			</tr>																
																			<tr>
																				<td colspan="2">
																					&nbsp;
																				</td>
																			</tr>										                        
												           					<tr class="prop">	                        	
													                            <td valign="top" class="name">
													                            	<label><warehouse:message code="shipment.method.label" default="Shipment method" /></label>
													                            </td>        
													                            <td valign="top" class="value">
													                            	${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")}
													                  				<img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.png')}" 
													                  					valign="top" style="vertical-align: middle;"/>	                            	
													                            </td>
													                        </tr>                    
													                        <tr class="prop">
													                            <td valign="top" class="name">
													                            	<label><warehouse:message code="shipment.trackingNumber.label" default="Tracking number" /></label>
													                            </td>                            
													                            <td valign="top" class="value">
													                            	<span style="color:#aaa">
														                            	<g:if test="${shipmentInstance?.trackingNumber}">
															                            	${fieldValue(bean: shipmentInstance, field: "trackingNumber")} &nbsp;&nbsp;
															                            	<a href="${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingUrl")}${fieldValue(bean: shipmentInstance, field: "trackingNumber")}" 
															                            		target="_blank">track</a>
														                            	</g:if>
														                            	<g:else>
													                                    	(none)
														                            	</g:else>
													                            	</span>
													                            	
													                            </td>               
													                        </tr>  	
																			<tr>
																				<td colspan="2">
																					&nbsp;
																				</td>
																			</tr>													                        
																			<g:each in="${shipmentInstance.referenceNumbers}" var="referenceNumber" status="status">
																				<tr class="prop">
																					<td valign="top" class="name">
																						<label>Reference Number ${referenceNumber?.referenceNumberType?.name}</label>
																					</td>   
																					<td valign="top" class="value">
																						${referenceNumber?.identifier}	
																					</td>                          
																				</tr>												 													
																			</g:each>
													                        													                        
													                               					                    																	
																			<tr>
																				<td colspan="2">
																					&nbsp;
																				</td>
																			</tr>										                        
																			
								        									<tr class="prop">
													                            <td valign="middle" class="name">
													                            	<label><warehouse:message code="shipment.shipmentNumber.label" default="Last Modified on" /></label>
													                            </td>                            
													                            <td valign="top" class="value" nowrap="nowrap">
													                            	<span style="color: #aaa"><format:datetime obj="${shipmentInstance?.lastUpdated}"/></span>
													                            </td>                            
													                        </tr>                    
													                        <tr class="prop">
													                            <td valign="middle" class="name">
													                            	<label><warehouse:message code="shipment.shipmentNumber.label" default="Created on" /></label>
													                            </td>                            
													                            <td valign="top" class="value" nowrap="nowrap">
													                            	<span style="color: #aaa"><format:datetime obj="${shipmentInstance?.dateCreated}"/></span>
													                            </td>                            
													                        </tr>		
																		    <tr>
																		    	<td>
																		    		&nbsp;
																		    	</td>																		    
																		    </tr>																		                        
																		</tbody>
																	</table>
																	
																	<div class="buttons" style="text-align: center">
																		<a href="#" id="edit-details-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit Shipment"/> Edit Shipment</a>
																		<a href="#" id="send-shipment-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="Send Shipment"/> Send Shipment</a>																		
																		<a href="#" id="add-reference-number-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Reference Number"/> Add Reference #</a>																				
																	</div>
																	
																</div>
																		
																<div id="edit-details" style="display: none; padding: 10px;">
																	<g:form action="update" method="post">
														                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
														                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
														                <table class="withoutBorder" border="0">
														                    <tbody>	            
														                        <tr class="prop">
														                            <td valign="top" class="name">
														                            	<label><warehouse:message code="shipment.name.label" default="Nickname" /></label>
														                           	</td>           
														                            <td colspan="3" valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
													                                    <g:textField name="name" value="${shipmentInstance?.name}" />
														                            </td>                            
														                        </tr>                    
														                        <tr class="prop">
														                            <td valign="middle" class="name"><label><warehouse:message code="shipment.shipmentType.label" default="Type" /></label></td>                            
														                            <td valign="middle" class="value" nowrap="nowrap">
																                    	<g:select name="shipmentType" from="${org.pih.warehouse.shipping.ShipmentType.list()}" optionKey="id" value="${shipmentInstance?.shipmentType?.id}"  /> 
														                            </td>                            
														                        </tr>                    						                                
														                        <tr class="prop">
														                            <td valign="middle" class="name"><label><warehouse:message code="shipment.shipmentStatus.label" default="Status" /></label></td>                            
														                            <td valign="middle" class="value" nowrap="nowrap">
																                    	<g:select name="shipmentStatus.id" from="${org.pih.warehouse.shipping.ShipmentStatus.list()}" optionKey="id" value="${shipmentInstance?.shipmentStatus?.id}"  /> 
														                            </td>                            
														                        </tr>                    						                                
																				<tr class="prop">
																					<td valign="top" class="name"><label><warehouse:message code="shipment.expectedShippingDate.label" default="Ship date" /></label></td>                            
													                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}" nowrap="nowrap">
														           						<g:datePicker name="expectedShippingDate" precision="day" value="${shipmentInstance?.expectedShippingDate}" />
													                                </td>
																				</tr>
																				<tr class="prop">
																					<td valign="top" class="name"><label><warehouse:message code="shipment.expectedDeliveryDate.label" default="Delivery date" /></label></td>   
													                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}" nowrap="nowrap">
														           						<g:datePicker name="expectedDeliveryDate" precision="day" value="${shipmentInstance?.expectedDeliveryDate}" />
													                                </td>
																				</tr>
																				<tr>
																					<td>
																						&nbsp;
																					</td>																	
																				</tr>
													           					<tr class="prop">	                        	
														                            <td valign="top" class="name"><label><warehouse:message code="shipment.method.label" default="Shipment method" /></label></td>        
														                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentMethod', 'errors')}">
																						<g:select name="shipmentMethod.id" from="${org.pih.warehouse.shipping.ShipmentMethod.list()}" optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentMethod?.id}"  />
														                            </td>
														                        </tr>                    
														                        <tr class="prop">
														                            <td valign="top" class="name"><label><warehouse:message code="shipment.trackingNumber.label" default="Tracking number" /></label></td>                            
														                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
												                                    	<g:textField name="trackingNumber" value="${shipmentInstance?.trackingNumber}" />
												                                    	<span style="color:#aaa">format: ${shipmentInstance?.shipmentMethod?.trackingFormat}</span>
													                                </td>
														                        </tr>  	     
														                        <tr>
																					<td>
																						&nbsp;
																					</td>															                        
														                        </tr>
														                        
																				<tr class="prop">		                        
														                        	<td></td>
														                        	<td>
																						<div class="buttons">
																							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																							<a href="#" id="edit-details-link" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="Cancel" /> Cancel </a>
																						</div>
																					</td>		                        
														                        </tr>													
																			</tbody>
																		</table>											
														 			</g:form>						        							        		
													        	</div>											        	
															</td>
														</tr>
													</tbody>
												</table>																									
											</div>
										</td>										
			                    	</tr>
			                    </tbody>
			                </table>	
						</div>		                		
					</gui:tab>
				
					<gui:tab id="shipmentPiecesTab" label="Pieces">
						<div id="itemsTab" class="tab">
							<g:if test="${shipment?.description}">
								<table class="withoutBorder">
									<tbody>
										<tr class="prop">
										    <td valign="top" class="name"><label><warehouse:message code="shipment.description.label" default="Description" /></label></td>   
										    <td valign="top" class="value" nowrap="nowrap">
												${shipment?.description}
										    </td>                          
										</tr>	 	
									</tbody>
								</table>
							</g:if>
							<g:if test="${!shipmentInstance.containers}">	
								<div class="notice">
									There are currently no shipping units in this shipment.																    										
								</div>								
							</g:if>
							<g:else>
							    <g:each in="${shipmentInstance.containers}" var="container" status="c">
								    
								    <table class="container" style="background-color: white;" border="0">
										<tbody>
											<tr>
												<td valign="top">
													<div style="margin-top: 10px; ">
														<span style="font-size: 1.2em">
															<g:if test="${container?.containerType?.name=='Box'}">
																<img src="${createLinkTo(dir:'images/icons',file:'box_24.gif')}" alt="box" style="vertical-align: middle; width: 24px; height: 24px;"/>
															</g:if>
															<g:elseif test="${container?.containerType?.name=='Pallet'}">
																<img src="${createLinkTo(dir:'images/icons',file:'pallet.jpg')}" alt="pallet" style="vertical-align: middle; width: 24px; height: 24px;"/>
															</g:elseif>
															<g:elseif test="${container?.containerType?.name=='Suitcase'}">
																<img src="${createLinkTo(dir:'images/icons',file:'suitcase.jpg')}" alt="suitcase" style="vertical-align: middle; width: 24px; height: 24px;"/>
															</g:elseif>
															<g:elseif test="${container?.containerType?.name=='Container'}">
																<img src="${createLinkTo(dir:'images/icons',file:'container.jpg')}" alt="container" style="vertical-align: middle; width: 24px; height: 24px;"/>
															</g:elseif>
															<b>${container?.name}</b>																	
														</span>
														<%-- 
														<span style="color: #aaa; font-size:.75em; padding-left: 30px;">${container?.containerType?.name}</span>--%>																
														<span style="color: #aaa; font-size:.75em; padding-left: 15px;">															
															Weight: 
															<g:if test="${container.weight}"><b>${container?.weight} ${container?.units}</b></g:if>
															<g:else> <b>unknown</b></g:else>  
														</span>
														<span style="color: #aaa; font-size:.75em; padding-left: 15px;">															
															Dimensions: 
															<g:if test="${container.dimensions}"><b>${container.dimensions}</b></g:if>
															<g:else> <b>unknown</b></g:else>  
														</span>
														<span style="color: #aaa; font-size:.75em; padding-left: 15px;">															
															Contains: 
															<b><%= container.getShipmentItems().size() %> items</b> 
														</span>
															
														<%-- FIXME Temporarily removed 
														<a href="#" id="show-container-contents-link-${container?.id}">Show Contents</a>										
														--%>
													</div>
													
												</td>		
												<td width="15%" valign="top" >
													<div style="margin-top: 10px; text-align: right">
														<a href="#" id="edit-container-link-${container?.id}">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit"/>
														</a>															
														<a href="#" id="copy-container-link-${container?.id}">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'page_copy.png')}" alt="Copy"/>
														</a>															
														<g:link action="deleteContainer" id="${container?.id}">
															<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete"/>
														</g:link>
													</div>
												</td>						
											</tr>
											<tr>
												<td colspan="2">
													<div id="container-contents-${container?.id}">
														<table class="withoutBorder">
															<tbody>
																<tr>
																	<td align="center" colspan="2">
													                    <table class="withBorder">
													                       	<thead>
													                       		<tr>
													                       			<th width="5%">#</th>
													                       			<th width="60%">Item</th>
													                       			<th width="5%">Qty</th>
													                       			<th width="5%">Weight</th>															                       			
													                       			<th width="5%"></th>
													                       		</tr>
													                       	</thead>
																			<tbody>
																				<g:if test="${!container.shipmentItems}">	
																				 	<tr class="odd">
																				 		<td colspan="5" style="text-align: center">
																					 		There are no shipping items in this ${container.containerType.name}.	
																					 	</td>
																					</tr>
																				 	<%--
																					<tr class="odd">
																						<td width="1%"><img src="${createLinkTo(dir:'images/icons',file:'pill.png')}" alt="Product" /></td>																	
																						<td colspan="5"> 
																					 		<a href="#" id="add-item-link-${container?.id}">Add a new item</a>																	
																				 		</td>
																				 	</tr>
																					 --%>
																				</g:if>
																				<g:else>					
																				    <g:each in="${container.shipmentItems}" var="item" status="i">
																						<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
																							<td width="1%">
																								${i+1}
																							</td>
																							<td>
																								${item?.product.name} 
																								<g:if test="${item?.donor}">
																									<span style="color:#aaa; padding-left: 10px">donated by ${item?.donor?.name}</span> 			
																								</g:if>
																							</td>
																							<td>																										
																								${item?.quantity}
																							</td>
																							<td>
																								<g:if test="${item?.grossWeight}">
																									<g:formatNumber number="${item?.grossWeight}" format="###,###.#" /> kgs
																								</g:if>									
																								<g:else>
																									<g:formatNumber number="${item?.product?.weight * item?.quantity}" format="###,###.#" /> kgs
																								</g:else>									
																							</td>
																							<td style="text-align: right">
																								<g:link class="remove" action="deleteItem" id="${item?.id}">
																									<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete"/>
																								</g:link>
																							</td>
																						</tr>																	
																				    </g:each>
																				    <%-- 
																				    <tr class="odd">
																						<td width="1%">
																							
																						</td>
																						<td colspan="4" align="center">																		
																							<a href="#" id="add-item-link-${container?.id}">Add a new item</a>	
																						</td>
																					</tr>
																					--%>
																				    
																				    <!--  Temporarily removed the two forms until I can figure out a better way to integrate them -->
																				    <%-- 
																					<tr class="prop">
																						<td colspan="4">
																							<label><warehouse:message code="shipment.contents.quickAdd.label" default="Quick Add" /></label></td>   
																							<div style="width:50%">
																								<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
																									<gui:autoComplete size="20" width="100" id="selectedItem" name="selectedItem" controller="json" action="availableItems"/>											
																									<g:submitButton name="addItem" value="Add to shipment"/>
																								</g:form>			
																							</div>						
																						</td>
																					</tr>
																					--%>																							
																			    </g:else>
													                        </tbody>
																		</table>   																				
																	</td>
																</tr>	
																<tr>
																	<td style="text-align: center">
																		<g:form action="addItem">
																			<g:hiddenField name="containerId" value="${container?.id}" />
																			<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
																			<img src="${createLinkTo(dir:'images/icons',file:'package.png')}" alt="Item" style="vertical-align: middle;"/>
																			<g:textField name="quantity" size="2" /> x 
																			<g:select 
																				id="productId" 
																				name='productId'
																			    noSelection="${['null':'']}"
																			    from='${Product.list()}' optionKey="id" optionValue="name">
																			</g:select>
																	    	<span class="buttons">
																	    		<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Add item</button>
																	    	<b>-or-</b> &nbsp;
																	    		<a href="#" id="add-item-link-${container?.id}"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add a donated item</a>																							    	

																	    	</span>
																		</g:form>																    																								
																	</td>
																</tr>																												
															</tbody>
														</table>	
													</div>
												</td>
											</tr>
										</tbody>
									</table>										
									<br/>		
													
									<script type="text/javascript">
										jQuery(document).ready(function($){
											$("a#show-container-contents-link-${container?.id}").click(function() {
												$("#container-contents-${container?.id}").toggle(0);													
											});										
											$('#copy-container-dialog-${container?.id}').dialog({
												autoOpen: false,
												width: 500,  modal: true
											});										
											$('#copy-container-link-${container?.id}').click(function(){
												$('#copy-container-dialog-${container?.id}').dialog('open');
												return false;
											});				
											$('#edit-container-dialog-${container?.id}').dialog({
												autoOpen: false,
												width: 500,  modal: true
											});										
											$('#edit-container-link-${container?.id}').click(function(){
												$('#edit-container-dialog-${container?.id}').dialog('open');
												return false;
											});				
										});
										
									</script>													
																
									<div id="copy-container-dialog-${container?.id}" title="Copy ${container?.name}" style="display: none;" >
										<g:form action="copyContainer">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="containerId" value="${container?.id}" />
											<table>
												<tbody>
													<tr class="prop">
							                            <td valign="top" class="name"><label><warehouse:message code="container.name.label" default="Copy ..." /></label></td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
															${container?.name}
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name"><label><warehouse:message code="container.copies.label" default="# of Copies" /></label></td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
															<g:textField name="copies" value="1" />
						                                </td>
							                        </tr>  	          
							                        <tr>
													    <td colspan="2">
															<div class="buttons" style="text-align: right;">
																<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Copy</button>
															</div>
	
													    </td>					                        
							                        </tr>         
							                    </tbody>
							                </table>
									    </g:form>
									</div>								
									<div id="edit-container-dialog-${container?.id}" title="Edit ${container?.name}" style="display: none;" >
										<g:form action="editContainer">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="containerId" value="${container?.id}" />
											<table>
												<tbody>
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><warehouse:message code="container.name.label" default="Nickname" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
															<g:textField name="name" value="${container?.name}" />
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><warehouse:message code="container.name.label" default="Dimensions" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'dimensions', 'errors')}">
															<g:textField name="dimensions" value="${container?.dimensions}" />
						                                </td>
							                        </tr>  	          
													<tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><warehouse:message code="container.name.label" default="Weight" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'weight', 'errors')}">
															<g:textField name="weight" value="${container?.weight}" />
						                                </td>
							                        </tr>  	          
							                        <tr>
													    <td colspan="2">
															<div class="buttons" style="text-align: right;">
																<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
															</div>
													    </td>					                        
							                        </tr>         
							                    </tbody>
							                </table>
									    </g:form>
									</div>								
	
									<%-- TODO:  SHOULD BE HIDDEN UNTIL USER REQUESTS TO ADD A NEW ITEM --%>														
									<div id="add-item-dialog-${container?.id}" style="display: none;" title="Add a new item to ${container?.name}">
										<g:form action="addItem">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<g:hiddenField name="containerId" value="${container?.id}" />
											<table>
												<tbody>
													<tr>  
														<td valign="top" class="name">
															<label><warehouse:message code="shipmentItem.quantity.label" default="Quantity" /></label>
														</td>                            
									                    <td valign="top" class="value ${hasErrors(bean: shipmentItem, field: 'quantity', 'errors')}">
															<g:textField name="quantity" size="5" /> 
														</td>
													</tr>
													<tr>
														<td valign="top" class="name">
															<label><warehouse:message code="shipmentItem.product.label" default="Product" /></label>
														</td>                            
									                    <td valign="top" class="value ${hasErrors(bean: shipmentItem, field: 'product', 'errors')}">												
															<g:select 
																id="productId" 
																name='productId'
															    noSelection="${['null':'Select One...']}"
															    from='${Product.list()}' optionKey="id" optionValue="name">
															</g:select>
													    </td>
													</tr>
													<tr>  
														<td valign="top" class="name">
															<label><warehouse:message code="shipmentItem.donor.label" default="Donor" /></label>
														</td>                            
									                    <td valign="top" class="value ${hasErrors(bean: shipmentItem, field: 'donor', 'errors')}">
															<g:select 
																id="donorId" 
																name='donorId'
															    noSelection="${['null':'']}"
															    from='${Organization.list()}' optionKey="id" optionValue="name">
															</g:select>
														</td>
													</tr>
													<tr>
														<td>
															<div class="buttons">
																<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
															</div>
														</td>										
												    </tr>
											    </tbody>
										    </table>
										</g:form>
									</div>											
									<script type="text/javascript">
										jQuery(document).ready(function($){
											$('#add-item-dialog-${container?.id}').dialog({
												autoOpen: false,
												width: 500,  modal: true
											});										
											$('#add-item-link-${container?.id}').click(function(){
												$('#add-item-dialog-${container?.id}').dialog('open');
												return false;
											});				
										});
									</script>																			
								</g:each><!-- iterate over each container -->							
								
								<%-- 
								<div style="margin-top: 10px; margin-bottom: 10px;">
										<g:form action="addContainer">															
											<span style="font-size: 1.2em">
										 		(#<%= shipmentInstance.getContainers().size()+1 %>) 
											</span>
											Add a new <g:select 
												id="containerTypeId" 
												name='containerTypeId'
											    noSelection="${['null':'Select One...']}"
											    from='${ContainerType?.list()}' optionKey="id" optionValue="name">
											</g:select> 
											with name										
											<g:textField name="name" />
											and weighs
											<g:textField name="weight" /> kg
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											
											
											
											<div class="buttons">
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
											</div>
										</g:form>														
								</div>
								--%>												
							</g:else>														
							<div class="buttons" style="margin-top: 10px; margin-bottom: 10px;">							
								<a href="#" id="add-container-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add a new piece</a>		
							</div>								
					
					
						</div>
						
					</gui:tab>
							
					<gui:tab id="shipmentEventsTab" label="Events">
						<h2><warehouse:message code="shipment.events.label" default="Tracking" /></h2>
						<div id="eventsTab" class="tab">
							<g:if test="${!shipmentInstance.events}">	
								<div class="notice">
									<span style="padding-right: 15px;">There are no events for this shipment.</span>
								</div>
							</g:if>
							<g:else>							
								<table>
									<thead>
			                       		<tr>
			                       			<th width="5%"></th>
			                       			<th>Date</th>
			                       			<th>Activity</th>
			                       			<th>Location</th>
			                       			<th width="5%"></th>
			                       		</tr>
			                       	</thead>
			                       	<tbody>
									    <g:each in="${shipmentInstance.events}" var="event" status="i">
											<tr id="event-${event.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><img src="${createLinkTo(dir:'images/icons',file:'event.png')}" alt="Event" /></td>
												<td><format:datetime obj="${event?.eventDate}"/></td>
												<td>${event?.eventType?.eventCode?.status}</td>
												<td>${event?.eventLocation?.name}</td>
												<td>
													<g:link action="deleteEvent" id="${event?.id}">
														<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Event"/>
													</g:link>
												</td>
											</tr>
									    </g:each>
										<g:form action="addEvent">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
										    <tr>
												<td></td>
												<td>
													<g:datePicker id="eventDate" name="eventDate" value="" />
												</td>
												<td>
													<g:select id="eventTypeId" name='eventTypeId' noSelection="${['':'Select one ...']}" from='${EventType.list()}' optionKey="id" optionValue="name"></g:select>
												</td>
												<td>
													<g:select id="eventLocationId" name='eventLocationId' noSelection="${['':'Select one ...']}" from='${Location.list()}' optionKey="id" optionValue="name"></g:select>									
												</td>
												<%-- 
												<td>
													<g:textField name="description" size="15" /> 
											    </td>
											    --%>
											    <td>
													<div class="buttons">
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
														<a href="#" class="negative"> <img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
													</div>
												</td>
											</tr>
									    </g:form>										    
									    
									</tbody>
				                </table>
				            </g:else>
				            
				            <div class="buttons" style="margin-top: 10px; margin-bottom: 10px;">							
								<a href="#" id="add-event-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add a new event</a>		
							</div>		
				            								
				            
				    	</div>
				    </gui:tab>						
							
				    
				    <gui:tab id="shipmentDocumentsTab" label="Documents">				    
						<h2><warehouse:message code="shipment.document.label" default="Documents" /></h2>
						<div id="documentsTab" class="tab">
							<g:if test="${!shipmentInstance.documents}">	
								<div class="notice">
									There are no documents for this shipment. &nbsp; &nbsp;
																	
								</div>
							</g:if>
							<g:else>
								<table>
									<thead>
			                       		<tr>
			                       			<th width="5%"></th>
			                       			<th>Document Type</th>
			                       			<th>Document</th>
			                       			<th>Size</th>
			                       			<th width="5%"></th>
			                       		</tr>
			                       	</thead>
			                       	<tbody>
									    <g:each in="${shipmentInstance.documents}" var="document" status="i">
											<tr id="document-${document.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="Document" /></td>
												<td>${document?.documentType?.name}</td>
												<td>${document?.filename} (<g:link controller="document" action="download" id="${document.id}">download</g:link>)</td>
												<td>${document?.size} bytes</td>
												<td>
													<g:link class="remove" action="deleteDocument" id="${document?.id}">
														<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Document"/>
													</g:link>
												</td>													
											</tr>
									    </g:each>
									    <tr>
									    	<td><img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="Document" /></td>
									    	<td colspan="4">
									    		<a href="#" id="add-document-link">Add a new document</a>
									    	
									    	</td>
									    
									    </tr>
									    <%-- 
										<g:uploadForm controller="document" action="upload">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<tr>
												<td></td>
												<td>	
													<g:select name="type" from="${Document.constraints.type.inList}" valueMessagePrefix="document.type"  />							    
												</td>																								
												<td>
													<input name="contents" type="file" />
													<g:submitButton name="upload" value="Upload"/>
												</td>
											</tr>
									    </g:uploadForm>
									    --%>											    
									</tbody>
				                </table>
							</g:else>
							
							
							<div class="buttons" style="margin-top: 10px; margin-bottom: 10px;">							
								<a href="#" id="add-document-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add a new document</a>	
							</div>									
							
							
						</div>
				    </gui:tab>
				    <gui:tab id="shipmentCommentsTab" label="Comments">				
				    	    
				    	<h2><warehouse:message code="shipment.comments.label" default="Comments" /></h2>
				    
					    <div id="commentsTab" class="tab">	
					    	<g:if test="${!shipmentInstance?.comments}">
								<div class="notice">
									There are no comments for this shipment. &nbsp; &nbsp;
																	
								</div>
					    	</g:if>
					    	<g:else>
								<table>
									<thead>
			                       		<tr>
			                       			<th width="5%"></th>
			                       			<th>From</th>
			                       			<th>To</th>
			                       			<th>Comment</th>
			                       			<th></th>
			                       		</tr>
			                       	</thead>
			                       	<tbody>
									    <g:each in="${shipmentInstance.comments}" var="comment" status="i">
											<tr id="comment-${comment.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><img src="${createLinkTo(dir:'images/icons',file:'tag.png')}" alt="Comment" /></td>
												<td>${comment?.commenter}</td>
												<td>${comment?.recipient} </td>
												<td>${comment?.comment}</td>
												<td>
													<g:link action="deleteComment" id="${comment?.id}">
														<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Comment"/>
													</g:link>
												</td>													
												
												
											</tr>
									    </g:each>
									    <tr>
									    	<td><img src="${createLinkTo(dir:'images/icons',file:'tag.png')}" alt="Comment" /></td>
									    	<td colspan="4">
									    		<a href="#" id="add-comment-link">Add a new comment</a>
									    	</td>
									    
									    </tr>
									    <%-- 
										<g:uploadForm controller="document" action="upload">
											<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
											<tr>
												<td></td>
												<td>	
													<g:select name="type" from="${Document.constraints.type.inList}" valueMessagePrefix="document.type"  />							    
												</td>																								
												<td>
													<input name="contents" type="file" />
													<g:submitButton name="upload" value="Upload"/>
												</td>
											</tr>
									    </g:uploadForm>
									    --%>											    
									</tbody>
				                </table>
					    	</g:else>
					    	
					    	<div class="buttons" style="margin-top: 10px; margin-bottom: 10px;">							
								<a href="#" id="add-comment-link"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" /> Add a new comment</a>	
							</div>	
					    	
					    	
						</div>
				    </gui:tab>					    				    
				    <gui:tab id="shipmentTestTab" label="Test">
			            <div id="testTab" class="tab">
			            	<!-- All hidden DIVs have been temporarily place into the 'test' tab in case the jquery-ui libraries fail to load  -->
							<div id="add-container-dialog" title="Add a new piece">
								<g:form action="addContainer">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<table>
										<tbody>
											<tr class="prop">
					                            <td valign="top" class="name">
					                            	<label><warehouse:message code="container.containerType.label" default="Grouping" /></label>
					                            </td> 						                                        
					                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'containerType', 'errors')}">							                            
					                            	<g:if test="${shipmentInstance?.shipmentType?.containerTypes}">
					                            		<g:select 
															id="containerTypeId" 
															name='containerTypeId'
														    noSelection="${['null':'Select One...']}"
														    from='${shipmentInstance?.shipmentType?.containerTypes}' optionKey="id" optionValue="name">
														</g:select>
					                            	</g:if>
				                                    <g:else>
					                            		<g:select 
															id="containerTypeId" 
															name='containerTypeId'
														    noSelection="${['null':'Select One...']}"
														    from='${ContainerType?.list()}' optionKey="id" optionValue="name">
														</g:select>					                                    
				                                    </g:else>
				                                    
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name">
					                            	<label><warehouse:message code="container.name.label" default="Dimensions" /></label>
					                            </td>                            
					                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
													<g:textField name="dimensions" />
				                                </td>
					                        </tr>
											<tr class="prop">
					                            <td valign="top" class="name">
					                            	<label><warehouse:message code="container.name.label" default="Weight" /></label>
					                            </td>                            
					                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
													<g:textField name="weight" /> kgs
													<g:hiddenField name="units" value="kgs"/>
				                                </td>
					                        </tr>
					                        
					                          	 
					                        <%-- Should default to Pallet #1 or Container #2 
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="container.name.label" default="Nickname" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
													<g:textField name="name" />
				                                </td>
					                        </tr>  	 
					                        --%>         
					                        <tr>
											    <td colspan="2">
													<div class="buttons" style="text-align: right;">
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Add</button>
													</div>

											    </td>					                        
					                        </tr>         
					                    </tbody>
					                </table>
							    </g:form>
							</div>								
							
							
							<div id="add-event-dialog" title="Add a new event">
								<g:form action="addEvent">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<table>
										<tbody>
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="event.eventDate.label" default="Event Date" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
				                                    <g:datePicker id="eventDate" name="eventDate" value="" />
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="event.eventType.label" default="Event Type" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'eventDate', 'errors')}">
				                                    <g:select id="eventTypeId" name='eventTypeId' noSelection="${['':'Select one ...']}" 
				                                    	from='${EventType.list()}' optionKey="id" optionValue="name"></g:select>
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="event.eventDate.label" default="Location" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'location', 'errors')}">
													<g:select id="eventLocationId" name='eventLocationId' noSelection="${['':'Select one ...']}" 
														from='${Location.list()}' optionKey="id" optionValue="name">
														</g:select>									
				                                </td>
					                        </tr>  	          
										<%-- 
											          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="event.description.label" default="Comment" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: eventInstance, field: 'description', 'errors')}">
													<g:textField name="description" size="15" /> 
				                                </td>
					                        </tr>  	 
									    --%>
					                        <tr>
											    <td colspan="2">
													<div class="buttons" style="text-align: right;">
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Add Event</button>
													</div>
											    </td>					                        
					                        </tr>         
					                    </tbody>
					                </table>
							    </g:form>
							</div>
									
							<div id="add-document-dialog" title="Add a new document">
								<g:uploadForm controller="document" action="upload">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<table>
										<tbody>
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="document.documentType.label" default="Document Type" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">																
													<g:select name="type" from="${DocumentType.list()}" valueMessagePrefix="document.type"  />							    												
												</td>
											</tr>
											<tr>
												<td valign="top" class="name"><label><warehouse:message code="document.file.label" default="Select a file" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: documentInstance, field: 'contents', 'errors')}">																
													<input name="contents" type="file" />												
												</td>
											</tr>
											<tr>
												<td valign="top" colspan="2">														
													<div class="buttons" style="text-align: right;">
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Upload Document</button>
													</div>														
												</td>
											</tr>
										</tbody>
					                </table>
							    </g:uploadForm>													
							</div>										
											
							<div id="add-comment-dialog" title="Add a new comment">
								<g:form action="addComment">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<table>
										<tbody>
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="comment.commenter.label" default="From" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'commenter', 'errors')}">
				                                    ${session.user.username}
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="comment.recipient.label" default="To" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'recipient', 'errors')}">
													<g:select id="recipientId" name='recipientId' noSelection="${['':'Select one ...']}" 
				                                    	from='${User.list()}' optionKey="id" optionValue="username"></g:select>
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="comment.comment.label" default="Comment" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
				                                    <g:textArea name="comment" size="15" />
				                                </td>
					                        </tr>  	        
					                        <tr>
												<td valign="top" colspan="2" style="text-align: right">
													<g:submitButton name="add" value="Add Comment"/>
												</td>
											</tr>				                          
										</tbody>
									</table>
								</g:form>												
							</div>

							<div id="add-reference-number-dialog" title="Add a new reference number">
								<g:form action="addReferenceNumber">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<table>
										<tbody>
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="shipment.referenceNumberType.label" default="Type" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: referenceNumber, field: 'referenceNumber', 'errors')}">
													<g:select id="recipientId" name='recipientId' noSelection="${['':'Select one ...']}" 
				                                    	from='${ReferenceNumberType.list()}' optionKey="id" optionValue="name"></g:select>
				                                </td>
					                        </tr>  	          
											<tr class="prop">
					                            <td valign="top" class="name"><label><warehouse:message code="comment.comment.label" default="Number" /></label></td>                            
					                            <td valign="top" class="value ${hasErrors(bean: referenceNumber, field: 'identifier', 'errors')}">
				                                    <g:textField name="identifier" size="15" /> 
				                                </td>
					                        </tr>  	        
					                        <tr>
												<td valign="top" colspan="2" style="text-align: right">
													<div class="buttons">		
														<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="save" /> Add Number</button>
													</div>
												</td>
											</tr>				                          
										</tbody>
									</table>
								</g:form>												
							</div>
							
						</div>
					</gui:tab>
				</gui:tabView>
            </div>	
    	</div>			
	    	
	    

	<script type="text/javascript">
		jQuery(document).ready(function($){
			$('a#edit-details-link').click(function() { 
				$('#edit-details').toggle();
				$('#view-details').toggle();
			});
			$('a#edit-origin-link').click(function() { 
				$('#view-origin').toggle();
				$('#edit-origin').toggle();
			});
			$('a#edit-destination-link').click(function() { 
				$('#view-destination').toggle();
				$('#edit-destination').toggle();
			});								
	
			/* add an event to a shipment */
			$('#add-event-dialog').dialog({ autoOpen: false, width: 500,  modal: true });				
			$('#add-event-link').click(function(){
				$('#add-event-dialog').dialog('open');
				return false;
			});				

			/* add an event to a shipment */
			$('#add-reference-number-dialog').dialog({ autoOpen: false, width: 500,  modal: true });				
			$('#add-reference-number-link').click(function(){
				$('#add-reference-number-dialog').dialog('open');
				return false;
			});	
			
			/* add a container to a shipment */
			$('#add-container-dialog').dialog({ autoOpen: false, width: 500,  modal: true });										
			$('#add-container-link').click(function(){
				$('#add-container-dialog').dialog('open');
				return false;
			});						
	
			/* add a document to a shipment */
			$('#add-document-dialog').dialog({ autoOpen: false, width: 500,  modal: true });										
			$('#add-document-link').click(function(){
				$('#add-document-dialog').dialog('open');
				return false;
			});						
	
			/* add an event to a shipment */
			$('#add-comment-dialog').dialog({ autoOpen: false, width: 800,  modal: true });				
			$('#add-comment-link').click(function(){
				$('#add-comment-dialog').dialog('open');
				return false;
			});				

			/* send shipment dialog */
			$('#send-shipment-dialog').dialog({ autoOpen: false, width: 500,  modal: true });				
			$('#send-shipment-link').click(function(){
				$('#send-shipment-dialog').dialog('open');
				return false;
			});	


			
		});
	</script>				            
        
        
    </body>
</html>


           <%-- 
            <div class="dialog">
    		    <h1>AJAX Testing</h1>
				<h1>Add Product to Shipment (g:submitToRemote):</h1>            
			    <g:form action="addProductToShipmentAjax">
				 
					<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
					<input name="trackingNumber" type="text"></input>
					<g:submitToRemote update="updateMe" 
						  value="Add"
						  url="[controller: 'shipment', action: 'addShipmentAjax']"/>
			    </g:form>

				<h1>Add Product to Shipment (g:remoteForm):</h1>
				<g:remoteForm action="addProductToShipmentAjax">
					<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
					<g:textField name="quantity" size="5" /> x 
					<g:select 
						id="productId" 
						name='productId'
					    noSelection="${['null':'Select One...']}"
					    from='${Product.list()}' optionKey="id" optionValue="name">
					</g:select>
					<g:submitButton name="add" value="Add"/>
			    </g:remoteForm>

			    <div id="updateMe">this div is updated by the form</div>
			    
			    Quick add:
			    <g:form action="ajaxAdd">
				   <g:textArea id='shipmentContent' name="content" rows="3" cols="50"/><br/>
				   <g:submitToRemote 
				       value="Add shipment"
				       url="[controller: 'shipment', action: 'addShipmentAjax']"
				       update="allShipments"
				       onSuccess="clearShipment(e)"
				       onLoading="showSpinner(true)"
				       onComplete="showSpinner(false)"/>
				   <img id="spinner" style="display: none" src="<g:createLinkTo dir='/images' file='spinner.gif'/>"/>
			    </g:form>
            </div>
		--%>
