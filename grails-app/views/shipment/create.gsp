
<%@ page import="org.pih.warehouse.Shipment" %>
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
            <g:form action="save" method="post">
			    <div class="dialog" >					

					<fieldset> 
						<legend>Overview</legend>
						<div class="value ${hasErrors(bean: shipmentInstance, field: 'source', 'errors')}">

							
							<label for="source"><g:message code="shipment.source.label" default="Supplier" /></label> 
							<input type="hidden" id="source.id" name="source.id" value="${session?.warehouse?.id}"/>
							<span class="large">${session?.warehouse.name}</span>
						</div>						
						<div class="value ${hasErrors(bean: shipmentInstance, field: 'target', 'errors')}">
                              <div>
  	                            <label for="target"><g:message code="shipment.target.label" default="Destination" /></label>
                               <g:select name="target.id" from="${org.pih.warehouse.Warehouse.list()}" 
                               	optionKey="id" value="${shipmentInstance?.target?.id}" 
                               	 />
                              </div>
						</div>
					</fieldset>				



					<fieldset>
						<legend>Shipping Details</legend>
						<div align="left">
							<div valign="top">
								<div class="value ${hasErrors(bean: shipmentInstance, field: 'shippingMethod', 'errors')}">
									<label for="trackingNumber"><g:message
										code="shipment.shippingMethod.label" default="Shipping Method" /></label> 
									<g:select
										name="shippingMethod.id"
										from="${org.pih.warehouse.ShipmentMethod.list()}" optionKey="id"
										value="${shipmentInstance?.shippingMethod?.id}" />
								</div>
								<div class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
									<label for="trackingNumber"><g:message code="shipment.trackingNumber.label" default="Tracking Number" /></label>
	                                <g:textField name="trackingNumber" value="${shipmentInstance?.trackingNumber}" />
	                            </div>
                            	<div>
									<div class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}">
										<label for="expectedShippingDate"><g:message code="shipment.expectedShippingDate.label" default="Expected Ship Date" /></label>
										<g:datePicker name="expectedShippingDate" precision="day" 
											value="${shipmentInstance?.expectedShippingDate}" />
		                            </div>
		                        </div>
                            	<div>
									<div class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}">
										<label for="expectedDeliveryDate"><g:message code="shipment.expectedDeliveryDate.label" default="Expected Delivery Date" /></label>
										<g:datePicker name="expectedDeliveryDate" precision="day" value="${shipmentInstance?.expectedDeliveryDate}" />
		                            </div>
		                        </div>
							</div>	  
							<div valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'status', 'errors')}">
                                <label for="status"><g:message code="shipment.status.label" default="Current Status" /></label>
								<g:select name="status" 
                                	from="${shipmentInstance.constraints.status.inList}" 
                                	value="${shipmentInstance?.status}" valueMessagePrefix="shipment.status" 
                                	 />
							</div>                        	
						</div>
					</fieldset> 
					
					<%-- 
					<fieldset> 
						<legend>Shipping Items</legend> 			
					
						<div class="notice">
							You must save the shipment before adding items.
						</div>
					
					</fieldset>									
					--%>	
				</div>

                <div class="buttons">
                    <span class="button">
                    	<g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />                    	
                    </span>
                </div>
            </g:form>
        </div>
    </body>
</html>
