<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div>							
	<div>
		<g:form method="GET" action="showTransactionLog">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>

			<!--  Filter -->
			
				<table style="border: 1px solid lightgrey">
					<tr class="odd">
						<td>
							<label><warehouse:message code="transactionLog.from.label"/></label>
						</td>
					</tr>
					<tr class="even"> 
						<td class="middle left">
							<g:jqueryDatePicker 
								id="startDate" 
								name="startDate" 
								value="${commandInstance?.startDate }" 
								format="MM/dd/yyyy"
								size="8"
								showTrigger="false" />
							<warehouse:message code="transactionLog.to.label"/>
							<g:jqueryDatePicker 
								id="endDate" 
								name="endDate" 
								value="${commandInstance?.endDate }" 
								format="MM/dd/yyyy"
								size="8"
								showTrigger="false" />
			
							<g:select name="transactionType.id" 
								from="${org.pih.warehouse.inventory.TransactionType.list()}" 
								optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${commandInstance?.transactionType?.id }" 
								noSelection="['0': warehouse.message(code:'default.all.label')]" /> 
						
							<button  class="" name="filter">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" style="vertical-align:middle"/>
								&nbsp;<warehouse:message code="default.button.filter.label"/>
							</button>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;">					
							<g:set var="enableFilter" value="${!params.disableFilter}"/>
							<table >
								<thead>
									<tr class="odd prop">
										<th>
											${warehouse.message(code: 'default.date.label')}
										</th>
										<th>
											${warehouse.message(code: 'default.type.label')}
										</th>
										<th>
											${warehouse.message(code: 'shipment.label')}
										</th>
										<th>
											${warehouse.message(code: 'transaction.source.label')} /
											${warehouse.message(code: 'transaction.destination.label')}
										</th>
										<th style="text-align: center">
											${warehouse.message(code: 'transaction.quantityChange.label')}
										</th>
									</tr>
			
								</thead>
								<!--  Transaction Log -->
								<tbody>			
									
									<g:set var="transactionMap" value="${commandInstance?.getTransactionLogMap(enableFilter.toBoolean())}"/>
									<g:if test="${!transactionMap }">
										<tr>
											<td colspan="6" class="even center">		
												<div class="fade padded">
													<warehouse:message code="transaction.noTransactions.label"/>
													<%-- 
													<warehouse:message code="transaction.noTransactions.message" args="[format.metadata(obj:commandInstance?.transactionType),commandInstance?.startDate,commandInstance?.endDate]"/>
													--%>
												</div>
											</td>
										</tr>
									</g:if>
									<g:else>
										<g:set var="totalQuantityChange" value="${0 }"/>		
										<g:each var="transaction" in="${transactionMap?.keySet()?.sort {it.transactionDate}.reverse() }" status="status">
											<tr class="transaction ${(status%2==0)?'even':'odd' } prop">
												<td style="width: 10%; nowrap="nowrap">	
														<format:date obj="${transaction?.transactionDate}"/>																
												</td>
												<td>
													<span class="${transaction?.transactionType?.transactionCode?.name()?.toLowerCase()}">
														<g:link controller="inventory" action="showTransaction" id="${transaction?.id }" params="['product.id':commandInstance?.productInstance?.id]">
															<format:metadata obj="${transaction?.transactionType}"/>
														</g:link>
													</span>
												</td>
												<td>
													<g:if test="${transaction?.incomingShipment }">
														<g:link controller="shipment" action="showDetails" id="${transaction?.incomingShipment?.id }">
															${transaction.incomingShipment?.name }
														</g:link>
													</g:if>
													<g:elseif test="${transaction?.outgoingShipment }">
														<g:link controller="shipment" action="showDetails" id="${transaction?.outgoingShipment?.id }">
															${transaction.outgoingShipment?.name }
														</g:link>
													</g:elseif>
												</td>
												<td>
													<g:if test="${transaction?.source }">
														${transaction?.source?.name }
													</g:if>
													<g:elseif test="${transaction?.destination }">
														${transaction?.destination?.name }
													</g:elseif>
												</td>
												<td style="text-align: center">
													<g:set var="quantityChange" value="${0 }"/>
													<g:each var="transactionEntry" in="${commandInstance?.getTransactionLogMap(enableFilter.toBoolean())?.get(transaction) }" status="status2">
														<g:set var="quantityChange" value="${transaction?.transactionEntries.findAll{it?.inventoryItem?.product == commandInstance?.productInstance}.quantity?.sum() }"/>
													</g:each>
													<span class="${transaction?.transactionType?.transactionCode?.name()?.toLowerCase()}">
														${quantityChange }
													</span>
												</td>
											</tr>
										</g:each>
										<%-- 
										<!-- Commented out because it's a little confusing --> 
										<tr class="prop" style="height: 3em;">
											<td colspan="3" style="text-align: right; font-size: 1.5em; vertical-align: middle;">
												Recent changes
											</td>
											<td style="text-align: center; font-size: 1.5em; vertical-align: middle">
												<g:if test="${totalQuantityChange>0}">${totalQuantityChange}</g:if>
												<g:else><span style="color: red;">${totalQuantityChange}</span></g:else>	
											</td>
										</tr>
										--%>															
									</g:else>
								</tbody>
							</table>
						</td>
					</tr>
				</table>
		</g:form>
	</div>
</div>
<div class="left smpad">
	Showing ${transactionMap?.keySet()?.size() } of ${commandInstance?.allTransactionLogMap?.keySet()?.size() }
	transactions
	<g:if test="${commandInstance?.allTransactionLogMap?.keySet()?.size() > transactionMap?.keySet()?.size()}">
		&nbsp;|&nbsp;
		<g:link controller="inventoryItem" action="showTransactionLog" id="${commandInstance?.productInstance?.id }" params="[disableFilter: true]">
			<warehouse:message code="transactionLog.showAll.label"/>
		</g:link>
	</g:if>
	<%-- 
	<g:else>
		<g:link controller="inventoryItem" action="showStockCard" id="${commandInstance?.productInstance?.id }" fragment="transactionLog">
			<warehouse:message code="transactionLog.reset.label"/>
		</g:link>
	</g:else>
	--%>	
</div>
	