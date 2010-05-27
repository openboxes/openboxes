
<%@ page import="org.pih.warehouse.ContainerType" %>
<%@ page import="org.pih.warehouse.Document" %>
<%@ page import="org.pih.warehouse.EventType" %>
<%@ page import="org.pih.warehouse.Product" %>
<%@ page import="org.pih.warehouse.Location" %>
<%@ page import="org.pih.warehouse.Shipment" %>
<%@ page import="org.pih.warehouse.ShipmentMethod" %>
<%@ page import="org.pih.warehouse.ShipmentStatus" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${shipmentInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${shipmentInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div id="dialogBanner">
				<a name="shipment"></a>
				<span class="title">Edit Shipment</span>					
				<span class="listcontainer" style="float:right; text-align:right; ">				
					<ul class="list">						
						<li class="first">
							<g:link action="show" id="${shipmentInstance?.id}"><span class="large">show</span></g:link>							
						</li>
						<li class="active"><span class="large">edit</span></li>				
					</ul>
				</span>
			</div>	

            <div class="dialog" style="clear: both;">             	
	  			<g:form method="post">
	                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
	                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
	            	<fieldset>
		            	<div style="text-align:center" class="errors">                            
	                    	<g:select name="shipmentStatus.id" from="${org.pih.warehouse.ShipmentStatus.list()}" optionKey="id" value="${shipmentInstance?.shipmentStatus?.id}"  />                          	
	                    </div>
						<br/>
		            	<h2><g:message code="shipment.shipment.label" default="Shipment Details" /></h2>
		            	<hr/>
		            	<br/>
		                <table class="withoutBorder" border="0">
		                    <tbody>	                        
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.origin.label" default="From" /></label></td>                            
		                            <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
					                	<g:select name="origin.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.origin?.id}"  />
		                            </td>                            
		                            <td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Ship date" /></label></td>                            
	                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}" nowrap="nowrap">
		           						<g:datePicker name="expectedShippingDate" precision="day" value="${shipmentInstance?.expectedShippingDate}" />
	                                </td>
		                        </tr>                    
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.destination.label" default="To" /></label></td>
		                            <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
					                	<g:select name="destination.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.destination?.id}"  />
		                            </td>                            
		                            <td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Delivery date" /></label></td>   
	                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}" nowrap="nowrap">
		           						<g:datePicker name="expectedDeliveryDate" precision="day" value="${shipmentInstance?.expectedDeliveryDate}" />
	                                </td>
		                        </tr>	                       
		  					</tbody>
						</table>
							            	        
						<br/>
		  				<br/>
		  				<h2><g:message code="shipment.shipment.label" default="Shipper" /></h2>
		  				<hr/>
		  				<br/>
		  				
		  				<table class="withoutBorder">
		  					<tbody>
		                        <tr class="prop">
		                        	<td rowspan="3"><img src="${createLinkTo(dir:'images/icons',file: shipmentInstance?.shipmentMethod?.methodName + '.jpg')}"/></td>
		                            <td valign="top" class="name"><label><g:message code="shipment.shipmentMethod.label" default="Shipment Method" /></label></td>        
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentMethod', 'errors')}">
										<g:select name="shipmentMethod.id" from="${org.pih.warehouse.ShipmentMethod.list()}" optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentMethod?.id}"  />
		                            </td>
		                        </tr>                    
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></label></td>                            
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
	                                    <g:textField name="trackingNumber" value="${shipmentInstance?.trackingNumber}" />
	                                </td>
		                        </tr>  	          
							</tbody>
						</table>	            	        				            	        
		                			
			 		</fieldset>
	                <div class="buttons">
	                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.save.label', default: 'Save')}" /></span>
	                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
	                </div>
	 			</g:form>	
	 			
	 			
	 			<br/><br/>
	 			
				<div id="dialogBanner">
					<a name="history"></a>
					<span class="title">Edit Event History, Documents & Contents</span>					
					<span class="listcontainer" style="float:right; text-align:right; ">				
						<ul class="list">						
							<li class="first">
								<g:link action="show" id="${shipmentInstance?.id}"><span class="large">show</span></g:link>
								
							</li>
							<li class="active"><span class="large">edit</span></li>				
						</ul>
					</span>
				</div>				
	 			<div class="dialog" style="clear:both"></div>
		 			<fieldset>
						<br/>					
						<h2><g:message code="shipment.events.label" default="Event History" /></h2>
						<hr/>
						<br/>
						
						<div id="eventMessage"></div>
						<table>
							<thead>
	                       		<tr>
	                       			<th width="20px"></th>
	                       			<th>Date</th>
	                       			<th>Activity</th>
	                       			<th>Location</th>
	                       			<%-- 
	                       			<th></th>
	                       			<th>Details</th>
	                       			--%>
	                       			<th></th>
	                       		</tr>
	                       	</thead>
	                       	<tbody>
							    <g:each in="${shipmentInstance.events}" var="event" status="i">
									<tr id="event-${event.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td><img src="${createLinkTo(dir:'images/icons',file:'event.png')}" alt="Event" /></td>
										<td><g:formatDate format="yyyy-MM-dd hh:mm:ss" date="${event?.eventDate}"/></td>
										<td>${event?.eventType?.name}</td>
										<td>${event?.eventLocation?.name}</td>
										<%-- 
										<td>${event?.targetLocation?.name}</td>
										<td>${event?.description}</td>
										--%>
										<td><g:link action="deleteEvent" id="${event?.id}">delete</g:link></td>
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
											<g:select id="eventTypeId" name='eventTypeId' noSelection="${['':'Select one ...']}" from='${EventType.list()}' optionKey="id" optionValue="name">
											</g:select>
										</td>
										<td>
											<g:select id="eventLocationId" name='eventLocationId' noSelection="${['':'Select one ...']}" from='${Location.list()}' optionKey="id" optionValue="name">
											</g:select>									
										</td>
										<%-- 
										<td>
											<g:select id="targetLocationId" name='targetLocationId' noSelection="${['':'Select one ...']}" from='${Location.list()}' optionKey="id" optionValue="name">
											</g:select>									
										</td>									
										<td>
											<g:textField name="description" size="15" /> 
									    </td>
									    --%>
									    <td>
											<g:submitButton name="add" value="Add"/>
										</td>
									</tr>
							    </g:form>
							</tbody>
		                </table>
	
						<br/>
						<br/>
		  				<h2><g:message code="shipment.document.label" default="Documents" /></h2>
		  				<hr/>
		  				<br/>
		  				
						<table>
							<thead>
	                       		<tr>
	                       			<th width="20px"></th>
	                       			<th>Type</th>
	                       			<th>Document</th>
	                       			<th>Size</th>
	                       			<th></th>
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
							</tbody>
		                </table>
	
						<br/>
						<br/>
		  				<h2><g:message code="shipment.shipmentItem.label" default="Shipment Contents" /></h2>
		  				<hr/>
		  				<br/>
					    <g:each in="${shipmentInstance.containers}" var="container" status="c">
							<div style="margin-top: 25px; margin-bottom: 5px; font-size: 1.2em">
								<g:if test='${container.name}'>
									<b>#${c+1} ${container?.name}</b> 
								</g:if>
								| ${container?.containerType?.name } | ${container?.weight} ${container?.units} | <%= container.getShipmentItems().size() %> items | <g:link action="deleteContainer" id="${container?.id}">delete</g:link>
							</div>
		                    <table class="withBorder">
		                       	<thead>
		                       		<tr>
		                       			<th width="20px"></th>
		                       			<th>Shipment Item</th>
		                       			<th></th>
		                       		</tr>
		                       	</thead>
								<tbody>
								    <g:each in="${container.shipmentItems}" var="item" status="i">
										<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
											<td><img src="${createLinkTo(dir:'images/icons',file:'product.png')}" alt="Product" /></td>
											<td>
												${item?.quantity} units of
												${item?.product.name} (<g:link controller="shipmentItem" action="show" id="${item.id}">show</g:link>)			
											</td>
											<td>
												<g:link action="deleteItem" id="${item?.id}">delete</g:link>
											</td>
										</tr>
								    </g:each>	
								    <g:form action="addItem">
										<g:hiddenField name="containerId" value="${container?.id}" />
										<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
										<tr>  
											<td></td>
											<td>
												<g:textField name="quantity" size="5" /> units of 
												<g:select 
													id="productId" 
													name='productId'
												    noSelection="${['null':'Select One...']}"
												    from='${Product.list()}' optionKey="id" optionValue="name">
												</g:select>
										    	<g:submitButton name="add" value="Add"/>
										    </td>
										    <td></td>
										    <td></td>
									    </tr>
									</g:form>
		                        </tbody>
							</table>                            							                                
						</g:each>
						
						<g:form action="addContainer">
							Add a new <g:select 
								id="containerTypeId" 
								name='containerTypeId'
							    noSelection="${['null':'Select One...']}"
							    from='${ContainerType.list()}' optionKey="id" optionValue="name">
							</g:select> with name
							<g:textField name="name" />
							<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
							<g:submitButton name="add" value="Add"/>
						</g:form>
						
					</fieldset>
            	</div>            
            <%-- 
			<div class="buttons" align="left">
				<g:form>
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
				</g:form>
			</div>
			--%>			
			

          
        </div>
    </body>
</html>
