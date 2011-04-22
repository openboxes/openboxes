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
										<div class="fade" >No <b>${commandInstance?.transactionType?.name }</b> transactions between 
											<b><g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${commandInstance?.startDate }"/></b> and
											<b><g:formatDate format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" date="${commandInstance?.endDate }"/></b>.
										</div>
									</td>
								</tr>
							</g:if>
							<g:else>
								<g:set var="totalQuantityChange" value="${0 }"/>							
								<g:each var="transaction" in="${commandInstance?.transactionLogMap?.keySet().sort {it.transactionDate}.reverse() }" status="status">
									<tr class="transaction ${(status%2==0)?'even':'odd' } prop">
										<td>	
											<a id="show-details-${transaction?.id }" class="show-details" href="${createLink(controller: 'inventory', action:'showTransactionDialog', id: transaction.id, params: ['product.id', 'test'])}">
												<g:formatDate
													date="${transaction?.transactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" />																
											</a>
											
											<!--  Transaction Details -->
											<!--  
											<span id="transactionEntries${transaction?.id}" style="text-align: left; padding: 10px; margin: 10px; display: none; position:absolute; padding: 10px; background-color: white; border: 1px dashed black">
												<label>Entries for Transaction #${transaction.id }</label>
												<table width="100%">
													<tr> 
														<th>Description</th>
														<th>Serial/Lot Number</th>
														<th style="text-align: center;">Qty Change</th>
													</tr>
													<g:each var="transactionEntry" in="${commandInstance?.transactionLogMap?.get(transaction) }" status="status2">
														<tr class="${(status2%2==0)?'odd':'even' }">
															<td>
																${transactionEntry?.inventoryItem?.product?.name }
															</td>
															<td>
																${transactionEntry?.inventoryItem?.lotNumber }
															</td>
															<td style="text-align: center;">
																<g:if test="${transactionEntry.quantity<0}">
																	<g:set var="styleClass" value="color: red;"/>																	
																</g:if>
																<span style="${styleClass}">${transactionEntry.quantity }</span> 
															</td>
														</tr>
													</g:each>
													<tfoot>
														<tr style="border-top: 1px solid lightgrey;">
															<th colspan="2">
															</th>
															<th style="text-align: center;">

																<g:set var="quantityChange" value="${transaction?.transactionEntries.findAll{it.inventoryItem?.product == commandInstance?.productInstance}.quantity?.sum() }"/>
																<g:set var="totalQuantityChange" value="${totalQuantityChange + quantityChange}"/>
																																<g:set var="styleClass" value="color:black;"/>														
																<g:if test="${quantityChange<0}">
																	<g:set var="styleClass" value="color: red;"/>																	
																</g:if>
																<span style="${styleClass}">${quantityChange }</span> 
															</th>
														</tr>
													</tfoot>
												</table>
											</span>						
											-->
																				
										</td>
										<td>
											${transaction?.transactionType?.name }&nbsp;
										
										</td>
										<td>
											${transaction?.source?.name }&nbsp;
										</td>
											<td>
											${transaction?.destination?.name }&nbsp;
										
										</td>
										<td style="text-align: center">
											
											<g:set var="styleClass" value="color:black;"/>														
											<g:if test="${quantityChange<0}">
												<g:set var="styleClass" value="color: red;"/>																	
											</g:if>
											<span style="${styleClass}">${quantityChange }</span> 
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
