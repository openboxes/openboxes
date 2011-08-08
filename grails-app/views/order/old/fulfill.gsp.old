
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order')}" />
        <title><warehouse:message code="default.fulfill.label" default="Fulfill {0}" args="[entityName]" /></title>
       
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<g:hasErrors bean="${orderShipment}">
				<div class="errors">
					<g:renderErrors bean="${orderShipment}" as="list" />
				</div>
			</g:hasErrors>            
            <div class="dialog">            
            	<g:form action="addOrderItemToShipment">
            		<g:hiddenField name="id" value="${orderInstance?.id }"/>
            	
	            	<fieldset>
	            		<g:render template="summary" model="[orderInstance:orderInstance]"/>
		                <table>
		                    <tbody>
		                        <g:each var="orderItem" in="${orderInstance?.orderItems }">
									<tr class='prop'>
										<td valign='top'class=''>
											${orderItem.id }
	
										</td>
										<td valign='top'class=''>
											<label>Shipment</label>
											<div class="ui-widget">
												<g:select class="combobox" name="shipment.id" from="${org.pih.warehouse.shipping.Shipment.list()}" 
													optionKey="id" optionValue="name" value="" noSelection="['':'']" />
											</div>									
										</td>
									</tr>
								</g:each>
								<tr>
									<td class="name">
									</td>
									<td>
										<div class="buttons">
											<g:submitButton name="submit"/>
										</div>
									</td>
								</tr>
		                    </tbody>
		                </table>
	               </fieldset>
				</g:form>	               
            </div>
            <%--
            <div class="list">
            	<table>
            		<thead>
            			<tr>	
							<th>ID</th>            			
            			</tr>
            		</thead>
            		<tbody>
            			<g:each var="shipmentItem" in="${org.pih.warehouse.shipping.ShipmentItem.list() }">
							<tr>
								<td>${shipmentItem?.id }</td>						
								<td>${shipmentItem?.shipment?.name }</td>						
								<td>${shipmentItem?.product?.name }</td>						
								<td>${shipmentItem?.quantity }</td>						
							</tr>            		
						</g:each>
            		</tbody>
            	</table>
            </div>
           --%>
             <div class="list">
            	<table>
            		<thead>
            			<tr>	
							<th>ID</th>            			
            			</tr>
            		</thead>
            		<tbody>
            			<g:each var="orderShipment" in="${org.pih.warehouse.order.OrderShipment.list() }">
							<tr>
								<td>${orderShipment.orderItem?.id }							
								<td>${orderShipment.shipmentItem?.id }							
							</tr>            		
						</g:each>
            		</tbody>
            	</table>            
            </div>

        </div>
        <g:comboBox/>
    </body>
</html>
