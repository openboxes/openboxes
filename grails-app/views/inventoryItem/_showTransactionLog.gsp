<div>							
	<fieldset >
		<legend class="fade">Transaction Log</legend>
		<div>
			<g:form method="GET" action="showStockCard">
				<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>

					<!--  Filter -->
					<div style="padding: 10px; ">
						<g:jqueryDatePicker 
							id="startDate" 
							name="startDate" 
							value="${commandInstance?.startDate }" 
							format="MM/dd/yyyy"
							size="8"
							showTrigger="false" />
						to
						<g:jqueryDatePicker 
							id="endDate" 
							name="endDate" 
							value="${commandInstance?.endDate }" 
							format="MM/dd/yyyy"
							size="8"
							showTrigger="false" />
	
						<g:select name="transactionType.id" 
							from="${org.pih.warehouse.inventory.TransactionType.list()}" 
							optionKey="id" optionValue="name" value="${commandInstance?.transactionType?.id }" 
							noSelection="['0': '-- All types --']" /> 
					
						<button  class="" name="filter">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" style="vertical-align:middle"/>
							&nbsp;Filter
						</button>
					</div>			
		
				<div class="list">
					<table >
						<thead>
							<tr class="odd prop">
								<th>
									${message(code: 'transaction.transactionDate.label', default: 'Date')}
								</th>
								<th>
									${message(code: 'transaction.transactionType.label', default: 'Type')}
								</th>
								<th>
									${message(code: 'transaction.source.label', default: 'Source')}
								</th>
								<th>
									${message(code: 'transaction.destination.label', default: 'Destination')}
								</th>
								<th style="text-align: center">
									${message(code: 'transaction.quantityChange.label', default: 'Qty In/Out')}
								</th>
							</tr>

						</thead>
						<!--  Transaction Log -->
						<tbody>			
							<g:if test="${!commandInstance?.transactionLogMap }">
								<tr>
									<td colspan="5" class="even center" style="min-height: 100px;">		
										<div class="fade" >No <b>${commandInstance?.transactionType?.name}</b> transactions between 
											<b><format:date obj="${commandInstance?.startDate}"/></b> and
											<b><format:date obj="${commandInstance?.endDate}"/></b>.
										</div>
									</td>
								</tr>
							</g:if>
							<g:else>
								<g:set var="totalQuantityChange" value="${0 }"/>							
								<g:each var="transaction" in="${commandInstance?.transactionLogMap?.keySet().sort {it.transactionDate}.reverse() }" status="status">
									<tr class="transaction ${(status%2==0)?'even':'odd' } prop">
										<td style="width: 10%; nowrap="nowrap">	
										
											<g:link controller="inventory" action="showTransaction" id="${transaction?.id }" params="['product.id':commandInstance?.productInstance?.id]">
												<format:date obj="${transaction?.transactionDate}"/>																
											</g:link>
										<td>
											<span class="${transaction?.transactionType?.transactionCode?.name()?.toLowerCase()}">
												${transaction?.transactionType?.name }
											</span>
										</td>
										<td>
											${transaction?.source?.name }
										</td>
											<td>
											${transaction?.destination?.name }
										
										</td>
										<td style="text-align: center">
										
											<g:set var="quantityChange" value="${0 }"/>
											<g:each var="transactionEntry" in="${commandInstance?.transactionLogMap?.get(transaction) }" status="status2">
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
				</div>
			</g:form>
		</div>
	</fieldset>
</div>
