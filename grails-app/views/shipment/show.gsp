
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
            	<h3><g:message code="shipment.shipment.label" default="Shipment Details" /></h3>
                <table>
                    <tbody>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.status.label" default="Status" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "status")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></td>                            
                            <td valign="top" class="value">${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.expectedShippingDate.label" default="Expected Shipping Date" /></td>                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.expectedShippingDate}" /></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.actualShippingDate.label" default="Actual Shipping Date" /></td>                            
                            <td valign="top" class="value"><g:formatDate date="${shipmentInstance?.actualShippingDate}" /></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.source.label" default="Source" /></td>                            
                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.source?.id}">${shipmentInstance?.source?.encodeAsHTML()}</g:link></td>                            
                        </tr>                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="shipment.target.label" default="Target" /></td>
                            <td valign="top" class="value"><g:link controller="warehouse" action="show" id="${shipmentInstance?.target?.id}">${shipmentInstance?.target?.encodeAsHTML()}</g:link></td>                            
                        </tr>
					</tbody>
				</table>
				<br/>
				<table>
					<tr>
						<td>						
							<h2><g:message code="shipment.products.label" default="Products" /></h2>
							<table>
								<tbody> 
			                        <tr class="prop">
			                            <td valign="top" style="text-align: left;" class="value">
			                            	<table>
			                                	<thead>
			                                		<tr>
			                                			<th>Quantity</th>
			                                			<th>Product</th>
			                                		</tr>
			                                	</thead>
			                            		<tbody>
												    <g:each in="${shipmentInstance.shipmentLineItems}" var="item" status="i">
														<tr id="item-${item.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
															<td>
																${item?.quantity} units
															</td>
															<td>
																<g:link controller="shipmentLineItem" action="show" id="${item.id}">${item?.product.name}</g:link>
															</td>
														</tr>
												    </g:each>									    
													<tr>  
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
												    </tr>
			                            		</tbody>
											</table>                            							                                
			                            </td>                            
			                        </tr>                        
								</tbody>
							</table>						
						</td>					
						<td>
							<h2><g:message code="shipment.documents.label" default="Documents" /></h2>
							<table>
								<tbody>                                                 
			                        <tr class="prop">
			                            <td valign="top" style="text-align: left;" class="value">
			                                <table>
			                                	<thead>
			                                		<tr>
			                                			<th></th>
			                                			<th>Document</th>
			                                			<th>Type</th>
			                                			<th>Size</th>
			                                		</tr>
			                                	</thead>
											    <g:each in="${shipmentInstance.documents}" var="document" status="i">
													<tr id="document-${document.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}">
														<td><img src="${createLinkTo(dir:'images',file:'document.png')}" alt="Document" /></td>
														<td><g:link controller="shipment" action="download" id="${document.id}">${document?.filename}</g:link></td>
														<td>${document?.type}</td>
														<td>${document?.size} bytes</td>
													</tr>
											    </g:each>
													<tr>
														<td><img src="${createLinkTo(dir:'images',file:'document.png')}" alt="Document" /></td>
														<td>								    
															<g:uploadForm action="upload">
			                                    				<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
			                                    				<g:select name="type" from="${Attachment.constraints.type.inList}" valueMessagePrefix="document.type"  />													
																<input name="contents" type="file" />
																<g:submitButton name="upload" value="Upload"/>
														    </g:uploadForm>											
														</td>
													</tr>
			                                </table>
			                            </td>
			                        </tr>
			                    </tbody>
			                </table>						
						</td>						
					</tr>
				</table>

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
