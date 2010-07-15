<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<%@ page import="org.pih.warehouse.shipping.Document" %>
<%@ page import="org.pih.warehouse.shipping.ContainerType" %>
<%@ page import="org.pih.warehouse.shipping.EventType" %>
<%@ page import="org.pih.warehouse.shipping.Shipment" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentType" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentMethod" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatus" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <g:set var="pageTitle"><g:message code="default.create.label" args="[entityName]" /></g:set>
        <title>${pageTitle}</title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${pageTitle}</content>
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
				<span class="title"><g:message code="shipment.shipment.label" default="Shipment Details" /></span>
			</div>	

			<script type="text/javascript">
				jQuery(function() {
					jQuery("#expectedShippingDatePicker").datepicker({ 
						showOn: 'both',
						buttonImage: '../images/icons/silk/calendar.png',
						buttonImageOnly: true,
						altField: "#expectedShippingDate",
						altFormat: "mm/dd/yy",
						dateFormat: "dd M yy", 
						autoSize: true, closeText: "Done", 
						buttonText: "...", 
						showButtonPanel: true,
						showOtherMonths: true, 
						selectOtherMonths: true
					});
					
					jQuery("#expectedDeliveryDatePicker").datepicker({ 
						showOn: 'both',
						buttonImage: '../images/icons/silk/calendar.png',
						buttonImageOnly: true,
						altField: "#expectedDeliveryDate",
						altFormat: "mm/dd/yy",
						dateFormat: "dd M yy", 
						autoSize: true, closeText: "Done", 
						buttonText:"...", 
						showButtonPanel: true,
						showOtherMonths: true, 
						selectOtherMonths: true
						
					});
		
			    	jQuery("#datepicker").datepicker({
						showOn: 'both',
						buttonImage: '../images/icons/silk/calendar.png',
						buttonImageOnly: true
					});
					
				});
			</script>
			
            <div class="dialog" style="clear: both;">             	
	  			<g:form action="save" method="post">
	                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
	                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
	            	<fieldset>
		                <table class="withoutBorder" border="0">
		                    <tbody>	
								<tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.shipmentType.label" default="Shipment Type" /></label>
		                            </td>                            
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
										<g:select name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" optionKey="id" value="${shipmentInstance?.shipmentType?.id}" noSelection="['0':'']" />
		                            </td>                            
		                        </tr>         
		                                
		                        <tr class="prop">
		                        	<td valign="top" class="name">
		                        		<label><g:message code="shipment.name.label" default="Nickname" /></label>
		                        	</td>
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
	                                    <g:textField class="large" name="name" size="30" value="${shipmentInstance?.name}" />
		                            </td>                            
		                        </tr>
		                    
<!--  		                    
								<tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.status.label" default="Current Status" /></label>
		                            </td>                            
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'status', 'errors')}">
										<g:select name="shipmentStatus.id" from="${org.pih.warehouse.shipping.ShipmentStatus.list()}" optionKey="id" value="${shipmentInstance?.shipmentStatus?.id}" noSelection="['0':'']" />
		                            </td>                            
		                        </tr>         
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.shipmentMethod.label" default="Shipment Method" /></label>
		                            </td>        
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentMethod', 'errors')}">
										<g:select name="shipmentMethod.id" from="${org.pih.warehouse.shipping.ShipmentMethod.list()}" optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentMethod?.id}" noSelection="['0':'']" />
		                            </td>
		                        </tr>                    
-->
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.destination.label" default="Ship on" /></label>
		                            </td>
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}">
										<input id="expectedShippingDate" name="expectedShippingDate" type="hidden"/>
										<input id="expectedShippingDatePicker" name="expectedShippingDatePicker" type="text" class="date" width="8" />
		                            </td>                            
		                        </tr>          
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.origin.label" default="Ship from" /></label>
		                            </td>                            
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
					                	<g:select name="origin.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.origin?.id}" noSelection="['0':'']" />
		                            </td>                            
		                        </tr>		                        
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.destination.label" default="Ship to" /></label>
		                            </td>                            
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
		                            	<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.destination?.id}" noSelection="['0':'']" />
		                            </td>                            
		                        </tr>		                        
		                        
		                        <%-- 
		                        <tr class="prop">
		                            <td valign="top" class="name">
		                            	<label><g:message code="shipment.destination.label" default="Deliver on" /></label>
		                            </td>
		                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}">
					                	<input id="expectedDeliveryDate" name="expectedDeliveryDate" type="hidden"/> 
					                	<input id="expectedDeliveryDatePicker" name="expectedDeliveryDatePicker" type="text" class="date"/>
		                            </td>                            
		                        </tr>
		                        --%>

		                        <tr class="prop">		                        
		                        	<td></td>
		                        	<td>
										<div class="buttons" style="text-align: left;">
											<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>
											
											<g:link controller="home" action="index" class="negative">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel
											</g:link>
										</div>
									</td>		                        
		                        </tr>

		                                  
							</tbody>
						</table>	            	        				            	        		                			
			 		</fieldset>
	 			</g:form>	

            </div>
            
            
        </div>
    </body>
</html>
