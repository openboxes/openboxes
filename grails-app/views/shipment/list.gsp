<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="messagePrefix" value="${incoming ? 'shipping.shipmentsTo' : 'shipping.shipmentsFrom'}"/>
        <title><warehouse:message code="${messagePrefix}.label" args="[session.warehouse.name]"/></title>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
			<table>            	
            	<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="list" method="post">
			            	<g:if test="${incoming}"><g:hiddenField name="type" value="incoming"/></g:if>
			            	<table>
			            		<tr>
						           	<td class="filter-list-item">
						           		<label class="block"><warehouse:message code="default.type.label"/> </label> 
						           		<g:select name="shipmentType"
														from="${org.pih.warehouse.shipping.ShipmentType.list()}"
														optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${shipmentType}" 
														noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;    
									</td>
									<g:if test="${incoming}">
							           	<td class="filter-list-item">
								           	<label class="block"><warehouse:message code="default.origin.label"/>  </label>
								           	<g:select name="origin" 
								           							from="${org.pih.warehouse.core.Location.list().sort()}"
								           							optionKey="id" optionValue="name" value="${origin}" 
								           							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
										</td>									
									</g:if>
									<g:else>
							           	<td class="filter-list-item">
								           	<label class="block"><warehouse:message code="default.destination.label"/>  </label>
								           	<g:select name="destination" 
								           							from="${org.pih.warehouse.core.Location.list().sort()}"
								           							optionKey="id" optionValue="name" value="${destination}" 
								           							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
										</td>
									</g:else>
						           	<td class="filter-list-item">
							           	<label class="block"><warehouse:message code="default.status.label"/> </label> 
							           	<g:select name="status" 
							           					   from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
							           					   optionKey="name" optionValue="${{format.metadata(obj:it)}}" value="${status}" 
							           					   noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;	
									</td>
						           	<td class="filter-list-item">
							           	<label class="block"><warehouse:message code="default.from.label"/> </label> 
							           	<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
																			value="${statusStartDate}" format="MM/dd/yyyy"/>
									</td>
						           	<td class="filter-list-item">
										<label class="block"><warehouse:message code="default.to.label"/> </label> 
										<g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
																			value="${statusEndDate}" format="MM/dd/yyyy"/>
									</td>
						           	<td class="filter-list-item" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;
											<warehouse:message code="default.button.filter.label"/>  </button>
									</td>
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
            	<tr>
					<td>            
			            <g:if test="${shipments.size()==0}">
			           		<div>
			           			<warehouse:message code="shipping.noShipmentsMatchingConditions.message"/>
			            		&nbsp;
			            		<g:link controller="shipment" action="list"><warehouse:message code="shipping.startOver.label"/></g:link>
			           		</div>
			           	</g:if>
			            
			            <g:else>
				            <div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">   
				                         	<th>${warehouse.message(code: 'default.actions.label')}</th>
				                        	<th>${warehouse.message(code: 'default.type.label')}</th>
				                            <th>${warehouse.message(code: 'shipping.shipment.label')}</th>
				                            <g:if test="${incoming}">
				                           		<th>${warehouse.message(code: 'default.origin.label')}</th>
				                            </g:if>
				                            <g:else>
				                            	<th>${warehouse.message(code: 'default.destination.label')}</th>
				                            </g:else>						
				                        	<th>${warehouse.message(code: 'shipping.expectedShippingDate.label')}</th>
				                         	<th>${warehouse.message(code: 'default.status.label')}</th>
				                         	<th>${warehouse.message(code: 'default.lastUpdated.label')}</th>
				                        </tr>
				                    </thead>
				                   	<tbody>
										<g:each var="shipmentInstance" in="${shipments}" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
												<td>
													<div class="action-menu">
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
														</button>
														<div class="actions" style="position: absolute; display: none;">
															<g:render template="listShippingMenuItems" model="[shipmentInstance:shipmentInstance]"/>															
														</div>
													</div>	
												</td>
												<td width="3%" style="text-align: center">
													<img src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
													alt="${format.metadata(obj:shipmentInstance?.shipmentType)}" style="vertical-align: middle; width: 24px; height: 24px;" />		
												</td>										
												<td>
													<g:link action="showDetails" id="${shipmentInstance.id}">
														${fieldValue(bean: shipmentInstance, field: "name")}
													</g:link>				
												</td>
												<td align="center">
													<g:if test="${incoming}">
														${fieldValue(bean: shipmentInstance, field: "origin.name")}
													</g:if>
													<g:else>
														${fieldValue(bean: shipmentInstance, field: "destination.name")}
													</g:else>
												</td>
												<td align="center">
													<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
												</td>
												<td>												
													<format:metadata obj="${shipmentInstance?.status.code}"/>
													<g:if test="${shipmentInstance?.status.date}">
													 - <format:date obj="${shipmentInstance?.status.date}"/>
													 </g:if>									
												</td>
												<td align="center">
													<format:date obj="${shipmentInstance?.lastUpdated}"/>
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
        
    </body>
</html>
