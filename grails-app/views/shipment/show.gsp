
<%@ page import="org.pih.warehouse.Attachment" %>
<%@ page import="org.pih.warehouse.Product" %>
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
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
            <div class="dialog">
            	<fieldset>
	            	<legend><g:message code="shipment.shipment.label" default="Shipment Details" /></legend>
	                <table class="withBorder">
	                    <tbody>                    
	                        <tr>
	                        	<td rowspan="3"><img src="${createLinkTo(dir:'images/icons',file: shipmentInstance.shippingMethod.methodName + '.jpg')}"/></td>
	                        	<td></td>
	                        	<td></td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="shipment.shippingMethod.label" default="Shipping Method" /></label></td>        
	                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "shippingMethod.name")}</td>                
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><label><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></label></td>                            
	                            <td valign="top" class="value">
	                            	${fieldValue(bean: shipmentInstance, field: "trackingNumber")} &nbsp; &nbsp; 
	                            	(<a href="${fieldValue(bean: shipmentInstance, field: "shippingMethod.trackingUrl")}${fieldValue(bean: shipmentInstance, field: "trackingNumber")}" target="_blank">Track Shipment</a>)
	                            </td>                            
	                        </tr>                    
	                        <tr class="prop">
	                        	<td></td>
	                            <td valign="top" class="name"><label><g:message code="shipment.status.label" default="Shipping Status" /></label></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "status")}</td>                            
	                        </tr>                    
	                        <tr class="prop">
	                        	<td></td>
	                            <td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Expected Shipping Date" /></label></td>                            
	                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.expectedShippingDate}" /></td>                            
	                        </tr>                    
	                        <tr class="prop">
	                        	<td></td>
	                            <td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Expected Delivery Date" /></label></td>                            
	                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.expectedDeliveryDate}" /></td>                            
	                        </tr>                    
	                        <tr>
	                        	<td colspan="3">
	                        		<hr/>
	                        	</td>
	                        </tr>
	                        <tr class="prop">
	                        	<td></td>
	                            <td valign="top" class="name"><label><g:message code="shipment.source.label" default="Supplier" /></label></td>                            
	                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.source?.id}">${shipmentInstance?.source?.encodeAsHTML()}</g:link></td>                            
	                        </tr>                    
	                        <tr class="prop">
	                        	<td></td>
	                            <td valign="top" class="name"><label><g:message code="shipment.target.label" default="Destination" /></label></td>
	                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.target?.id}">${shipmentInstance?.target?.encodeAsHTML()}</g:link></td>                            
	                        </tr>
						</tbody>
					</table>
				</fieldset>
					
				<fieldset>
					<legend><g:message code="shipment.document.label" default="Shipment Documents" /></legend>
					<table>
						<thead>
                       		<tr>
                       			<th width="20px"></th>
                       			<th>Document</th>
                       			<th>Type</th>
                       			<th>Size</th>
                       		</tr>
                       	</thead>
                       	<tbody>
						    <g:each in="${shipmentInstance.documents}" var="document" status="i">
								<tr id="document-${document.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
									<td><img src="${createLinkTo(dir:'images/icons',file:'document.png')}" alt="Document" /></td>
									<td><g:link controller="document" action="download" id="${document.id}">${document?.filename}</g:link></td>
									<td>${document?.type}</td>
									<td>${document?.size} bytes</td>
								</tr>
						    </g:each>
							<tr>
								<td></td>
								<td>								    
									<g:uploadForm controller="document" action="upload">
										<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
										<g:select name="type" from="${Attachment.constraints.type.inList}" valueMessagePrefix="document.type"  />													
										<input name="contents" type="file" />
										<g:submitButton name="upload" value="Upload"/>
								    </g:uploadForm>											
								</td>
								<td><!-- emtpy cell --></td>
								<td><!-- emtpy cell --></td>
							</tr>
						</tbody>
	                </table>
				</fieldset>
				<fieldset>
					<legend><g:message code="shipment.shipmentitem.label" default="Shipment Items" /></legend>
                    <table class="withBorder">
                       	<thead>
                       		<tr>
                       			<th width="20px"></th>
                       			<th>Shipment Item</th>
                       			<th>Quantity</th>
                       			<th></th>
                       		</tr>
                       	</thead>
						<tbody>
						    <g:each in="${shipmentInstance.shipmentLineItems}" var="item" status="i">
								<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
									<td><img src="${createLinkTo(dir:'images/icons',file:'product.png')}" alt="Product" /></td>
									<td>
										<g:link controller="shipmentLineItem" action="show" id="${item.id}">${item?.product.name}</g:link>
										
									</td>
									<td>
										${item?.quantity} units									
									</td>
									<td><!-- empty --></td>
								</tr>
						    </g:each>									    
							<tr>  
								<td></td>
								<td>
								    <g:form action="addProduct">
										<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
										<g:textField name="quantity" size="5" /> x 
										<g:select 
											id="productId" 
											name='productId'
										    noSelection="${['null':'Select One...']}"
										    from='${Product.list()}' optionKey="id" optionValue="name">
										</g:select>
										<g:submitButton name="add" value="Add"/>
								    </g:form>
							    </td>
							    <td><!-- empty --></td>
							    <td><!-- empty --></td>
						    </tr>
                        </tbody>
					</table>                            							                                
				</fieldset>
				
            </div>            
			<div class="buttons">
				<g:form>
					<g:hiddenField name="id" value="${shipmentInstance?.id}" />
					<span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
				</g:form>
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
