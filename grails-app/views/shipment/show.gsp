
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
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
           		<div class="message">${flash.message}</div>
            </g:if>            

			<div id="dialogBanner">
				<span class="title">Show Shipment</span>					
				<span class="listcontainer" style="float:right; text-align:right; ">				
					<ul class="list">						
						<li class="first active"><span class="large">show</span></li>
						<li><g:link action="edit" id="${shipmentInstance?.id}"><span class="large">edit</span></g:link></li>	
					</ul>
				</span>
			</div>	

            <div class="dialog" style="clear: both;">            
            	
            	<fieldset>

                    <div style="text-align:center" class="errors">                            
                    	<span class="large">${fieldValue(bean: shipmentInstance, field: "shipmentStatus.name")}</span><br/>                          	
                    </div>
                    
					<br/>
	            	<h2><g:message code="shipment.shipment.label" default="Shipment Details" /></h2>
	            	<hr/>
	            	<br/>
	            	
	                <table class="withoutBorder" border="0">
	                    <tbody>        	     
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="shipment.origin.label" default="From" /></label></td>                            
	                            <td valign="top" class="" nowrap="nowrap">
	                            	${shipmentInstance?.origin?.encodeAsHTML()}
	                            	(<g:link controller="warehouse" action="show" id="${shipmentInstance?.origin?.id}">show</g:link>)
	                            </td>                            
	                            <td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Expected ship date" /></label></td>                            
	                            <td valign="top" class="" nowrap="nowrap">
	                            	<g:formatDate date="${shipmentInstance?.expectedShippingDate}" /></td>                            
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="shipment.destination.label" default="To" /></label></td>
	                            <td valign="top" class="" nowrap="nowrap">
	                            ${shipmentInstance?.destination?.encodeAsHTML()}
	                            	(<g:link controller="warehouse" action="show" id="${shipmentInstance?.destination?.id}">show</g:link>)
	                            </td>  
	                            <td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Expected delivery date" /></label></td>   
	                            <td valign="top" class="" nowrap="nowrap"><g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" /></td>                          
	                        </tr>
	                        
	                        <%--                     
	                        <tr class="prop">
	                            <td colspan="2" valign="top" class="name"></td>
	                            <td valign="top" class="value">${shipmentInstance?.destination?.encodeAsHTML()}
	                            	(<g:link controller="warehouse" action="show" id="${shipmentInstance?.destination?.id}">show</g:link>)
	                            </td>                            
	                            <td></td>
	                            <td></td>
	                            <td></td>                         
	                        </tr>
	                        
	                        <tr class="prop">
	                            <td colspan="2" valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Expected to be delivered" /></label></td>                            
	                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" /></td>                            
	                            <td valign="top" align="right"></td>
	                            <td></td>
	                            <td></td>                         
	                        </tr>                    
	                        <tr class="prop">
	                            <td colspan="2" valign="top" class="name"><label><g:message code="shipment.recipient.label" default="Recipient/Signer" /></label></td>                            
	                            <td valign="top" class="value">(none)</td>                            
	                            <td valign="top" align="right"></td>
	                            <td></td>
	                            <td></td>                         
	                        </tr>
	                        --%>
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
	                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "shipmentMethod.name")}</td>
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></label></td>                            
	                            <td valign="top" class="value">
	                            	${fieldValue(bean: shipmentInstance, field: "trackingNumber")}&nbsp;(<a href="${fieldValue(bean: shipmentInstance, field: "shipmentMethod.trackingUrl")}${fieldValue(bean: shipmentInstance, field: "trackingNumber")}" 
	                            		target="_blank">track</a>)	
	                            </td>               
	                            <td></td>
	                        </tr>  	          
							<tr class="prop">
	                        	<td rowspan="3"></td>
	                            <td valign="top" class="name"></td>        
	                            <td valign="top" class="value"></td>
	                        </tr>         	                        
	                                      
						</tbody>
					</table>
				</fieldset>
				
			</div>
	
			<div class="buttons" align="left">
				<g:form>
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
				</g:form>
			</div>	
	
			<br/><br/>
			
			
			<div id="dialogBanner">
				<span class="title">Show Event History, Documents & Contents</span>					
				<span class="listcontainer" style="float:right; text-align:right; ">				
					<ul class="list">						
						<li class="first active"><span class="large">show</span></li>
						<li><g:link action="edit" id="${shipmentInstance?.id}"><span class="large">edit</span></g:link></li>	
					</ul>
				</span>
			</div>	
			
			<div class="dialog" style="clear: both;">
									
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
									<td><g:link action="deleteEvent" id="${event?.id}">delete</g:link></td>
								</tr>
						    </g:each>
						    
						    <%-- 
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
									<td>
										<g:select id="targetLocationId" name='targetLocationId' noSelection="${['':'Select one ...']}" from='${Location.list()}' optionKey="id" optionValue="name">
										</g:select>									
									</td>									
									<td>
										<g:textField name="description" size="15" /> 
								    </td>
								    <td>
										<g:submitButton name="add" value="Add"/>
									</td>
								</tr>
						    </g:form>
						    --%>
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

					<br/>
					<br/>
	  				<h2><g:message code="shipment.shipmentItem.label" default="Shipment Contents" /></h2>
	  				<hr/>
	  				<br/>
				    <g:each in="${shipmentInstance.containers}" var="container" status="c">
						<fieldset>
						<div style="margin-top: 5px; margin-bottom: 5px; font-size: 1.2em">
							<g:if test='${container.name}'>
								<b>#${c+1} ${container?.name}</b> 
							</g:if>
							| ${container?.containerType?.name } | ${container?.weight} ${container?.units} | <%= container.getShipmentItems().size() %> items | <g:link action="deleteContainer" id="${container?.id}">delete</g:link>
						</div>
	                    <table class="withBorder">
	                       	<thead>
	                       		<tr>
	                       			<th width="20px"></th>
	                       			<th>Quantity</th>
	                       			<th>Shipment Item</th>
	                       			<th></th>
	                       		</tr>
	                       	</thead>
							<tbody>
								<g:if test="${!container.shipmentItems}">

									<tr class="odd">
										<td colspan="4">There are no shipping items in this ${container.containerType.name}.</td>
									</tr>
								</g:if>
								
							    <g:each in="${container.shipmentItems}" var="item" status="i">
									<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td><img src="${createLinkTo(dir:'images/icons',file:'product.png')}" alt="Product" /></td>
										<td>
											${item?.quantity} units									
										</td>
										<td>
											${item?.product.name} (<g:link controller="shipmentItem" action="show" id="${item.id}">show</g:link>)			
										</td>
									<td><g:link action="deleteItem" id="${item?.id}">delete</g:link></td>
									</tr>
							    </g:each>	
							    <%-- 								    
							    <g:form action="addProduct">
									<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
									<tr>  
										<td></td>
										<td>
											<g:textField name="quantity" size="5" /> units of 
										</td>
										<td>
											<g:select 
												id="productId" 
												name='productId'
											    noSelection="${['null':'Select One...']}"
											    from='${Product.list()}' optionKey="id" optionValue="name">
											</g:select>
									    	<g:submitButton name="add" value="Add"/>
									    </td>
								    </tr>
								</g:form>
								--%>
	                        </tbody>
						</table>   
						</fieldset>                         							                                
					</g:each>
				</fieldset>
            </div>            
			
            
            
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
		

				
        </div>
    </body>
</html>
