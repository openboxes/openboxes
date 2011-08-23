<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'orders.label', default: 'Orders').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
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
			            	<table >
			            		<tr>
						           	<td class="filter-list-item">
							           	<label class="block"><warehouse:message code="default.origin.label"/>  </label>
							           	<g:select name="origin" 
							           							from="${suppliers}"
							           							optionKey="id" optionValue="name" value="${origin}" 
							           							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
									</td>
						           	<td class="filter-list-item">
							           	<label class="block"><warehouse:message code="default.status.label"/> </label> 
							           	<g:select name="status" 
							           					   from="${org.pih.warehouse.order.OrderStatus.list()}"
							           					   optionValue="${{format.metadata(obj:it)}}" value="${status}" 
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
			            <g:if test="${orders.size()==0}">
			           		<div>
			           			<warehouse:message code="shipping.noShipmentsMatchingConditions.message"/>
			           		</div>
			           	</g:if>
			            <g:else>
				            <div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">   
				                         	<th>${warehouse.message(code: 'default.actions.label')}</th>
				                        	<th>${warehouse.message(code: 'default.description.label')}</th>
				                            <th>${warehouse.message(code: 'default.origin.label')}</th>					
				                        	<th>${warehouse.message(code: 'order.orderedOn.label')}</th>
				                         	<th>${warehouse.message(code: 'default.status.label')}</th>
				                         	<th>${warehouse.message(code: 'default.lastUpdated.label')}</th>
				                        </tr>
				                    </thead>
				                   	<tbody>
										<g:each var="orderInstance" in="${orders}" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
												<td>
													<div class="action-menu">
														<g:render template="/order/actions" model="[orderInstance:orderInstance,hideDelete:true]"/>
													</div>	
												</td>									
												<td>
													<g:link action="show" id="${orderInstance.id}">
														${fieldValue(bean: orderInstance, field: "description")}
													</g:link>				
												</td>
												<td align="center">
													${fieldValue(bean: orderInstance, field: "origin.name")}
												</td>
												<td align="center">
													<format:date obj="${orderInstance?.dateOrdered}"/>
												</td>
												<td>												
													<format:metadata obj="${orderInstance?.status}"/>								
												</td>
												<td align="center">
													<format:date obj="${orderInstance?.lastUpdated}"/>
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