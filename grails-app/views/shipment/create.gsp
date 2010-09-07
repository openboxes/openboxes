<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <g:set var="pageTitle"><g:message code="default.create.label" args="[entityName]" /></g:set>
        <title>${pageTitle}</title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${pageTitle}</content>
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
            
            
            
			<table>
				<tr>
					<td width="70%">
			  			<g:form action="save" method="post">
			                <g:hiddenField name="id" value="${shipmentInstance?.id}" />
			                <g:hiddenField name="version" value="${shipmentInstance?.version}" />
			                <g:hiddenField name="type" value="${params.type}" />
								<fieldset>											
									<g:render template="summary"/>            
		            
				    	        	<div class="dialog" style="clear: both;">             	
						                <table>
						                    <tbody>	
						                    
						                    <%-- 
												<tr class="prop">
													<td valign="top" class="name"><label><g:message
														code="shipment.name.label" default="Shipment Number" /></label></td>
													<td colspan="3" valign="top"
														class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
													<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span></td>
												</tr>
						                    --%>
						                        <tr class="prop">
						                        	<td valign="top" class="name">
						                        		<label><g:message code="shipment.name.label" default="Nickname" /></label>
						                        	</td>
						                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
					                                    <g:textField name="name" size="30" value="${shipmentInstance?.name}" />
						                            </td>                            
						                        </tr>
						                    
												<tr class="prop">
						                            <td valign="top" class="name">
						                            	<label><g:message code="shipment.shipmentType.label" default="Shipment Type" /></label>
						                            </td>                            
						                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
													<%-- 
														<g:select name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
															optionKey="id" 
															optionValue="name"
															value="${shipmentInstance?.shipmentType?.id}" 
															noSelection="['0':'']" />
													--%>	
														<table>
															<tbody>
																<tr>
																	<g:each var="shipmentType" in="${org.pih.warehouse.shipping.ShipmentType.list()}">														
																		<td><input type="radio" name="shipmentType.id"  value="${shipmentType.id}" 
																			${shipmentType.id == shipmentInstance?.shipmentType?.id ? 'checked' : ''}
																		/> ${shipmentType.name}</td>
																	</g:each>															
																</tr>																	
															</tbody>														
														</table>															
						                            </td>                            
						                        </tr>         					
						                        <g:if test="${params.type=='incoming'}">                   
							                        <tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="shipment.origin.label" default="Where is it coming from?" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
							                            	<g:select name="origin.id" from="${warehouses}" optionKey="id" value="${shipmentInstance?.origin?.id}" noSelection="['0':'']" />
							                            	<g:hiddenField name="destination.id" value="${session.warehouse.id}" />
							                            </td>                            
							                        </tr>		     
												</g:if>		                        
						                        
						                        <g:elseif test="${params.type=='outgoing'}">                   
							                        <tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="shipment.destination.label" default="Where is it going?" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
							                            	<%-- <g:select name="destination.id" from="${warehouses}" optionKey="id" value="${shipmentInstance?.destination?.id}" noSelection="['0':'']" />--%>
							                            	<g:autoSuggest name="destination" jsonUrl="/warehouse/shipment/findWarehouseByName" width="300" />
							                            	<g:hiddenField name="origin.id" value="${session.warehouse.id}" />
							                            	
							                            </td>                            
							                        </tr>	
						                        </g:elseif>
						                        <g:else>
							                        <tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="shipment.destination.label" default="Where is it coming from?" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
							                            	<g:select name="origin.id" from="${warehouses}" optionKey="id" value="${shipmentInstance?.origin?.id}" noSelection="['0':'']" />							                            	
							                            </td>                            
							                        </tr>	
							                        <tr class="prop">
							                            <td valign="top" class="name">
							                            	<label><g:message code="shipment.destination.label" default="Where is it going?" /></label>
							                            </td>                            
							                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'origin', 'errors')}">
							                            	<g:select name="destination.id" from="${warehouses}" optionKey="id" value="${shipmentInstance?.destination?.id}" noSelection="['0':'']" />
							                            </td>                            
							                        </tr>	
						                        </g:else>
						                        <tr class="prop">
													<td valign="top" class="name">
						                            	<label><g:message code="shipment.expectedShippingDate.label" default="When is it expected to ship?" /></label>
						                            </td>
						                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}">										
														<g:jqueryDatePicker name="expectedShippingDate" />																			
						                            </td>                            
						                        </tr>          
												<tr class="prop">
						                        	<td valign="top" class="name">
						                        		<label><g:message code="shipment.initialStatus.label" default="What is the status?" /></label>
						                        	</td>
						                            <td valign="top" class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentStatus', 'errors')}">
					                                    <g:select name="eventType.id" from="${eventTypes}" optionKey="id" optionValue="${{it?.description}}" value="" noSelection="['0':'']" />
						                            </td>                            
						                        </tr>	                        	      
						                        <tr class="prop">		                        
						                        	<td></td>
						                        	<td>
														<div class="buttons" style="text-align: left;">
															<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Save</button>											
															<g:link controller="dashboard" action="index" class="negative">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="cancel" /> Cancel
															</g:link>
														</div>
													</td>		                        
						                        </tr>
											</tbody>
										</table>	            	        				            	        		                				 		
									</div>
		            		</fieldset>
	          			</g:form>
          			</td>
          			<td width="20%">
          				
          			
          			</td>
          			
          		</tr>
        	</table>
        </div>
    </body>
</html>
