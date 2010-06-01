
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
				<h2><g:message code="shipment.shipment.label" default="Shipment Details" /></h2>
			</div>	

            <div class="dialog" style="clear: both;">             	
	  			<g:form action="save" method="post">
	                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
	                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
	            	<fieldset>
		                <table class="withoutBorder" border="1">
		                    <tbody>	                        
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.origin.label" default="Status" /></label></td>                            
		                            <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
										<g:select name="shipmentStatus.id" from="${org.pih.warehouse.ShipmentStatus.list()}" optionKey="id" value="${shipmentInstance?.shipmentStatus?.id}"  />
		                            </td>                            
		                        </tr>                    
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.origin.label" default="From" /></label></td>                            
		                            <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
					                	<g:select name="origin.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.origin?.id}"  />
		                            </td>                            
		                        </tr>                    
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.destination.label" default="To" /></label></td>
		                            <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'destination', 'errors')}">
					                	<g:select name="destination.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value="${shipmentInstance?.destination?.id}"  />
		                            </td>                            
		                        </tr>	                       
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.expectedShippingDate.label" default="Ship date" /></label></td>                            
	                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}" nowrap="nowrap">
		           						<g:datePicker name="expectedShippingDate" precision="day" value="${shipmentInstance?.expectedShippingDate}" />
	                                </td>
		                        </tr>                    
		                        <tr class="prop">
		                            <td valign="top" class="name"><label><g:message code="shipment.expectedDeliveryDate.label" default="Delivery date" /></label></td>   
	                                <td valign="top" class=" ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}" nowrap="nowrap">
		           						<g:datePicker name="expectedDeliveryDate" precision="day" value="${shipmentInstance?.expectedDeliveryDate}" />
	                                </td>
		                        </tr>	                       
		                        <tr class="prop">
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
	                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
	                </div>
	 			</g:form>	

            </div>
            
            
        </div>
    </body>
</html>
