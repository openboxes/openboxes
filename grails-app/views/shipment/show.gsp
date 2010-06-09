
<%@ page import="org.pih.warehouse.Document" %>
<%@ page import="org.pih.warehouse.EventType" %>
<%@ page import="org.pih.warehouse.Product" %>
<%@ page import="org.pih.warehouse.Location" %>
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
		<gui:resources components="richEditor, dialog, tabView, autoComplete" mode="debug"/>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
           		<div class="message">${flash.message}</div>
            </g:if>            

			<div id="dialogBanner">
				<span class="title">Shipment Summary</span>
				<%-- 
				<span class="listcontainer" style="float:right; text-align:right; ">				
					<ul class="list">						
						<li class="first active"><span class="large">show</span></li>
						<li><g:link action="edit" id="${shipmentInstance?.id}"><span class="large">edit</span></g:link></li>	
					</ul>
				</span>
				--%>
			</div>	
			<div id="shipment" style="border: 1px solid black;">
								
				<div id="summary">
					<table border="0" width="100%">
	                    <tbody>       
	                    	<tr>
	                    		<td width="50%">
									<table>
					                	<tbody>     	
					                        <tr class="prop">
					                            <td valign="top" class="name"><label><g:message code="shipment.origin.label" default="Shipping from" /></label></td>                            
					                            <td valign="top" class="value" nowrap="nowrap">
					                            	<span class="large">${shipmentInstance?.origin?.encodeAsHTML()}</span>
					                            	(<g:link controller="warehouse" action="show" id="${shipmentInstance?.origin?.id}">show</g:link>)
					                            	<br/>	                            
					                            	${shipmentInstance?.origin?.address?.address}<br/>
					                            	${shipmentInstance?.origin?.address?.city}
					                            	${shipmentInstance?.origin?.address?.stateOrProvince} 
					                            	${shipmentInstance?.origin?.address?.postalCode} <br/>
					                            	${shipmentInstance?.origin?.address?.country} <br/>
					                            </td>                            
					                        </tr>                    
					                    </tbody>
					                </table>
	                    		</td>
	                    		<td width="50%">
									<table>
					                	<tbody>     	
					                        <tr class="prop">
					                            <td valign="top" class="name"><label><g:message code="shipment.destination.label" default="Delivering to" /></label></td>
					                            <td valign="top" class="value" nowrap="nowrap">
					                            	<span class="large">${shipmentInstance?.destination?.encodeAsHTML()}</span>
					                            	(<g:link controller="warehouse" action="show" id="${shipmentInstance?.destination?.id}">show</g:link>)
					                            	<br/>	                            
					                            	${shipmentInstance?.destination?.address?.address}<br/>
					                            	${shipmentInstance?.destination?.address?.city}
					                            	${shipmentInstance?.destination?.address?.stateOrProvince} 
					                            	${shipmentInstance?.destination?.address?.postalCode} <br/>
					                            	${shipmentInstance?.destination?.address?.country} <br/>
				
					                            </td>  
					                        </tr>
					                       
					                    </tbody>
					                </table>
	                    		</td>	                    	
	                    	</tr>
	                    </tbody>
	                </table>			
	            </div>
	            
	            <div id="details">
					<gui:tabView>
						
						
						<gui:tab label="Events" active="true">
							<h2><g:message code="shipment.events.label" default="Event History" /></h2>
							<g:if test="${!shipmentInstance.events}">	
								<span>There are no event for this shipment.</span>
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
												<td><g:formatDate format="yyyy-MM-dd hh:mm:ss" date="${event?.eventDate}"/></td>
												<td>${event?.eventType?.name}</td>
												<td>${event?.eventLocation?.name}</td>
												<td>
													<g:link action="deleteEvent" id="${event?.id}">
														<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Event"/>
													</g:link>
												</td>
											</tr>
									    </g:each>
									</tbody>
				                </table>
				            </g:else>
					    </gui:tab>
						
						<gui:tab label="Contents">
							<h2><g:message code="shipment.contents.label" default="Shipment Contents" /></h2>
							<table class="withoutBorder">
								<tbody>
									<tr class="prop">
									    <td valign="top" class="name"><label><g:message code="shipment.description.label" default="Description" /></label></td>   
									    <td valign="top" class="value" nowrap="nowrap">
											<g:if test="${shipment?.description}">${shipment?.description}</g:if>
											<g:else>None</g:else>
									    </td>                          
									</tr>	 	
									<tr class="prop">
									    <td valign="top" class="name"><label><g:message code="shipment.contents.quickAdd.label" default="Quick Add" /></label></td>   
										<td valign="top">  
											<div style="width:50%">
												<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
													<gui:autoComplete size="20" width="100" id="selectedItem" name="selectedItem" controller="shipment" action="availableItems"/>											
													<g:submitButton name="addItem" value="Add to shipment"/>
												</g:form>			
											</div>						
										</td>
									</tr>					
								</tbody>
							</table>
							
							
							
							<g:if test="${!shipmentInstance.containers}">	
								<span>There are no containers in this shipment.</span>
							</g:if>
							<g:else>
							    <g:each in="${shipmentInstance.containers}" var="container" status="c">
								    <table style="background-color: white;">
										<tbody>
											<tr>
												<td valign="top">
													<div style="margin-top: 10px; margin-bottom: 10px;">
														<span style="font-size: 1.2em">
															<g:if test='${container.name}'><b>#${c+1} ${container?.name}</b></g:if>
														</span>
														<span>
															&nbsp;|&nbsp;
															Type: ${container?.containerType?.name } &nbsp;|&nbsp;  
															Weighs: ${container?.weight} ${container?.units} &nbsp;|&nbsp;  
															Contains: <%= container.getShipmentItems().size() %> items
														</span> 
													</div>
												</td>		
												<td width="5%" valign="top" align="right">
													<g:link action="deleteContainer" id="${container?.id}">
														<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Container"/>
													</g:link>
												</td>						
											</tr>
											<tr>
												<td align="center" colspan="2">
								                    <table class="withBorder">
								                       	<thead>
								                       		<tr>
								                       			<th width="20px"></th>
								                       			<th width="40%">Cargo</th>
								                       			<th width="20%">Pieces</th>
								                       			<th width="20%">Gross Weight</th>
								                       			<th width="5%"></th>
								                       		</tr>
								                       	</thead>
														<tbody>
															<g:if test="${!container.shipmentItems}">	
																<tr class="odd">
																	<td colspan="4">There are no shipping items in this ${container.containerType.name}.</td>
																</tr>
															</g:if>
															<g:else>					
															    <g:each in="${container.shipmentItems}" var="item" status="i">
																	<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
																		<td><img src="${createLinkTo(dir:'images/icons',file:'product.png')}" alt="Product" /></td>
																		<td>
																			${item?.product.name} (<g:link controller="shipmentItem" action="show" id="${item.id}">show</g:link>)			
																		</td>
																		<td>
																			${item?.quantity} units									
																		</td>
																		<td>
																			${item?.grossWeight}									
																		</td>
																		<td>
																			<g:link class="remove" action="deleteItem" id="${item?.id}">
																				<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete Shipment Item"/>
																			</g:link>
																		</td>
																	</tr>
															    </g:each>	
														    </g:else>
								                        </tbody>
													</table>   
												</td>
											</tr>											
										</tbody>
									</table>
							
								</g:each>	
							</g:else>
						</gui:tab>

					    <gui:tab label="Method">				    
							<h2><g:message code="shipment.method.label" default="Shipping Method" /></h2>
				            <div class="dialog" style="clear: both;">            
				            	   
								<table class="withoutBorder" border="0">
				                    <tbody>       
			           					<tr class="prop">	                        	
				                            <td valign="top" class="name"><label><g:message code="shipment.method.label" default="Shipment method" /></label></td>        
				                            <td valign="top" class="value">
				                            	<img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.jpg')}" valign="absmiddle" width="24px" height="24px"/>
				                            	${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")}
				                            </td>
				                        </tr>                    
				                        <tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="shipment.trackingNumber.label" default="Tracking number" /></label></td>                            
				                            <td valign="top" class="value">
				                            	${fieldValue(bean: shipmentInstance, field: "trackingNumber")}&nbsp;(<a href="${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingUrl")}${fieldValue(bean: shipmentInstance, field: "trackingNumber")}" 
				                            		target="_blank">track</a>)	
				                            </td>               
				                        </tr>  	          
				                        <tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="shipment.shipmentStatus.label" default="Current status" /></label></td>                            
				                            <td valign="top" class="value" nowrap="nowrap">
				                            	${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}
				                            </td>                            
				                        </tr>                    
				                    	
										<tr class="prop">
											<td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>                            
											<td valign="top" class="value" nowrap="nowrap">
												<g:formatDate date="${shipmentInstance?.expectedShippingDate}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Expected delivery date" /></label></td>   
											<td valign="top" class="value" nowrap="nowrap">
												<g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message code="shipment.referenceNumbers.label" default="Reference Numbers" /></label></td>   
											<td valign="top" class="value">
												<g:each in="${shipmentInstance.referenceNumbers}" var="referenceNumber" status="status">
													${referenceNumber?.referenceNumberType?.name} ${referenceNumber?.identifier}<br/>
													
												</g:each>
											</td>                          
										</tr>
										<tr class="prop">
										    <td valign="top" class="name"><label><g:message code="shipment.comments.label" default="Additional Comments" /></label></td>   
										    <td valign="top" class="value" nowrap="nowrap">
										    	<g:if test="${shipmentInstance?.comments}">${shipmentInstance?.comments}</g:if>
										    	<g:else>No comments</g:else>
										    </td>                          
										</tr>				             
				                    </tbody>
				                </table>								
							</div>					
					    </gui:tab>
					    
					    
					    
					    <gui:tab label="Documents">				    
							<h2><g:message code="shipment.document.label" default="Documents" /></h2>
							<g:if test="${!shipmentInstance.documents}">	
								<span>There are no documents for this shipment.</span>
							</g:if>
							<g:else>
								<table>
									<thead>
			                       		<tr>
			                       			<th width="5%"></th>
			                       			<th>Type</th>
			                       			<th>Document</th>
			                       			<th>Size</th>
			                       			<th width="5%"></th>
			                       		</tr>
			                       	</thead>
			                       	<tbody>
									    <g:each in="${shipmentInstance.documents}" var="document" status="i">
											<tr id="document-${document.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
												<td><img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="Document" /></td>
												<td>${document?.type}</td>
												<td>${document?.filename} (<g:link controller="document" action="download" id="${document.id}">download</g:link>)</td>
												<td>${document?.size} bytes</td>
												<td><g:link action="deleteDocument" id="${document?.id}">delete</g:link></td>
											</tr>
									    </g:each>
									</tbody>
				                </table>
							</g:else>
					    </gui:tab>
					    <gui:tab label="Comments">					    
					    	<h2><g:message code="shipment.comments.label" default="Additional Comments" /></h2>
					    	
					    	<g:if test="${!shipment?.comments}">
					    		There are no comments for this shipment.
					    	</g:if>
					    	<g:else>
					    		${shipment?.comments}
					    	</g:else>
					    	
					        <%-- 
					        <gui:richEditor id='editor' value="${shipmentInstance?.comments}"/>
					        
					        <g:editInPlace id="comments-${shipmentInstance.id}" 
					        	paramName="comments" 
					        	url="[action:'updateComments', id:shipmentInstance.id]" 
					        	rows="5" cols="10">${shipmentInstance?.comments}</g:editInPlace>
					        --%>
					    </gui:tab>					    				    
					</gui:tabView>
	            </div>	
	    	</div>			
			<div class="buttons" align="left">
				<g:form>
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
				</g:form>
			</div>	
        </div>
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
