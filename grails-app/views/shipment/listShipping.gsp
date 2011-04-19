
<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipping')}" />
        <title>Shipments originating at <b>${session.warehouse.name}</b></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
			<table>            	
            	<tr>
            		<td style="border-right: 0px solid lightgrey; width: 200px; border-right: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listShipping" method="post">
				           	<div class="filter-list-item">
				           		<label class="block">Type</label> 
				           		<g:select name="shipmentType"
												from="${org.pih.warehouse.shipping.ShipmentType.list()}"
												optionKey="id" optionValue="name" value="${shipmentType}" 
												noSelection="['':'--All--']" />&nbsp;&nbsp;    
							</div>
				           	<div class="filter-list-item">
					           	<label class="block">Destination </label>
					           	<g:select name="destination" 
					           							from="${org.pih.warehouse.core.Location.list().sort()}"
					           							optionKey="id" optionValue="name" value="${destination}" 
					           							noSelection="['':'--All--']" />&nbsp;&nbsp;
							</div>
				           	<div class="filter-list-item">
					           	<label class="block">Status</label> 
					           	<g:select name="status" 
					           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
					           					   optionKey="name" optionValue="name" value="${status}" 
					           					   noSelection="['':'--All--']" />&nbsp;&nbsp;	
							</div>
				           	<div class="filter-list-item">
					           	<label class="block">From</label> 
					           	<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
																	value="${statusStartDate}" format="MM/dd/yyyy"/>
							</div>
				           	<div class="filter-list-item">
								<label class="block">To</label> 
								<g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
																	value="${statusEndDate}" format="MM/dd/yyyy"/>
							</div>
				           	<div class="filter-list-item right">
								<button name="filter">
									<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
									Filter </button>
							</div>
							
			            </g:form>
            		</td>
            	
					<td>            
			            <g:if test="${shipments.size()==0}">
			           		<div>
			           			<g:if test="${shipmentType || destination || status || statusStartDate || statusEndDate}">
			           				There are no shipments matching your conditions.
			           			</g:if>
			           			<g:else>
			   		        		There are no shipments originating at <b>${session.warehouse.name}</b>.
			            		</g:else>
			           		</div>
			           	</g:if>
			            
			            <g:else>
				            <div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">   
				                        	<th>${message(code: 'shipment.shipmentType.label', default: 'Type')}</th>
				                            <th>${message(code: 'shipment.shipment.label', default: 'Shipment')}</th>							
				                            <th>${message(code: 'shipment.destination.label', default: 'Destination')}</th>
				                        	<th>${message(code: 'shipment.expectedShippingDate.label', default: 'Shipping Date')}</th>
				                         	<th>${message(code: 'shipment.status.label', default: 'Status')}</th>
				                        </tr>
				                    </thead>
				                   	<tbody>
										<g:each var="shipmentInstance" in="${shipments}" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
												<td width="3%" style="text-align: center">
													<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
													alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />		
												</td>										
												<td>
													<span class="action-menu">
														<g:link action="showDetails" id="${shipmentInstance.id}">
															${fieldValue(bean: shipmentInstance, field: "name")}
														</g:link>																														
														<span><img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" /></span>
														<div class="actions">
															<g:render template="listShippingMenuItems" model="[shipmentInstance:shipmentInstance]"/>															
															
														</div>
													</span>	
												</td>
												<td align="center">
													${fieldValue(bean: shipmentInstance, field: "destination.name")}
												</td>
												<td align="center">
													<g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.expectedShippingDate}"/>
												</td>
												<td>												
													${shipmentInstance?.status.name}
													<g:if test="${shipmentInstance?.status.date}">
													 - <g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${shipmentInstance?.status.date}"/>
													 </g:if>									
												</td>
					                        </tr>
										</g:each>                    		
				                    </tbody>
								</table>
				            </div>
			            </g:else>
					</td>
				</tr>
			</table>
        </div>		
        
		<script type="text/javascript">
			$(function(){ 
				function show() {
					$(this).children(".actions").show();
				}
				
				function hide() { 
					$(this).children(".actions").hide();
				}
				
				$(".action-menu").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 5,   // number = milliseconds for onMouseOver polling interval
					over: show,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hide       // function = onMouseOut callback (required)
				});  
			});
		</script>	        
    </body>
</html>
