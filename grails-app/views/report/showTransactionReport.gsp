<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showTransactionReport.label" /></title>    
        <style>
        	.title { text-align: center; padding: 15px; }
        	.total { border-right: 1px solid lightgrey; border-left: 1px solid lightgrey; }
        	.parameters { width:30%; margin-left: auto; margin-right: auto;  }
        	.filter { padding-right: 15px; }
        </style>
    </head>    
    <body>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
		
		<table>
		
		
			<tr>
				<g:if test="${!params.print}">
					<td width="25%">
						<div class="box">
							<g:form controller="report" action="generateTransactionReport" method="GET">
								<table>
									<g:if test="${command?.product }">
										<tr>
											<td>
												<label>
													<warehouse:message code="report.product.label"/>
												</label>
											</td>
										</tr>
										<tr>
											<td class="value">
												<format:product product="${command?.product }"/>
												<g:hiddenField name="product.id" value="${command?.product?.id }"/>
											</td>
										</tr>
									</g:if>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.category.label"/>
											</label>
										</td>
									</tr>
									<tr>
										<td>
											<select id="category.id" name="category.id" class="filter">
												<option value=""></option>
												<g:render template="../category/selectOptions" model="[category:command.rootCategory, selected:command.category, level: 0]"/>								
											</select>							
											<div>
												<g:checkBox name="includeChildren" value="${command?.includeChildren }"/>
												<warehouse:message code="report.includeChildren.label"/>
											</div>
										</td>
									</tr>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.location.label"/>
											</label>
										</td>
									</tr>
									<tr>
										<td>
											<g:selectLocation class="filter" name="location.id" noSelection="['null':'']" maxChars="75" groupBy="locationType" value="${command?.location?.id}"/>
										</td>
									</tr>
									<tr>
										<td>
											<label>
												<warehouse:message code="report.dateRange.label"/>
											</label>
										
										</td>
									</tr>
									<tr>
										<td>
											<g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy"/>
											
											to 
											
											<g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy"/>
											
										</td>
									</tr>
									<tr>
										<td>
										</td>
									</tr>
									<tr>
										<td>
											<label><warehouse:message code="report.options.label"/></label>
										</td>
									</tr>
									<tr>
										<td>
											<div>
												<g:checkBox name="showTransferBreakdown" value="${params?.showTransferBreakdown}" class="filter"/>										
												<warehouse:message code="report.showTransferBreakdown.label"/>
											</div>

										</td>
									</tr>
									<tr>
										<td class="right">
											<button type="submit" class="btn">
												<warehouse:message code="report.runReport.label"/>
											</button>

										</td>
									</tr>
									<tr>
										<td>

										</td>
									</tr>
								</table>

							</g:form>				
						</div>				
						
				    	<div class="left" style="padding: 5px;">
							<label><warehouse:message code="report.exportAs.label"/></label>
				   			<g:link target="_blank" controller="report" action="showTransactionReport" params="[print:'true','product.id':command?.product?.id,'location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':params.showTransferBreakdown]">
				   				<warehouse:message code="report.exportAs.html.label"/>
				   			</g:link> 
				   			&nbsp;|&nbsp;
				   			<g:link target="_blank" controller="report" action="downloadTransactionReport" params="[url:request.forwardURI,'location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':params.showTransferBreakdown]">
				   				<warehouse:message code="report.exportAs.pdf.label"/>
				   			</g:link>
						</div>
						
					
					</td>
				</g:if>
				<%-- 
				<g:else>
					<div class="title">	
						<warehouse:message code="report.transactionReport.title"/>
					</div>		
					<table class="parameters">
						<tr>	
							<td class="right">			
								<label>
									<warehouse:message code="report.location.label"/>
								</label>
							</td>
							<td>
								${command?.location?.name }
							</td>
						</tr>
						<tr>
							<td class="right">
								<label>
									<warehouse:message code="report.category.label"/>
								</label>
							</td>
							<td>
								<format:category category="${command?.category}"/>					
							</td>
						</tr>
						<tr>
							<td class="right">
								<label>
									<warehouse:message code="report.startDate.label"/>
								</label>
							</td>
							<td>
								${format.date(obj:command?.startDate)}
							</td>
						</tr>
						<tr>
							<td class="right">
								<label>
									<warehouse:message code="report.endDate.label"/>
								</label>
							</td>
							<td>
								${format.date(obj:command?.endDate)}
							</td>
						</tr>
					</table>
				</g:else>				
				--%>
				<td>
				
					<g:if test="${command.product }">
						<g:set var="i" value='${0 }'/>
						<g:each var="entry" in="${command?.entries }">
							<g:if test="${command?.product == entry?.value?.product}">									
								<div class="box">
									<table>
										<tr>
											<td>
												<h1 style="display:inline">
													<span class="circle">${entry.value.entries*.value.quantityRunning.sum() }</span>
													&nbsp;
													${entry.key }
												</h1>
												<g:link controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':command?.category.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':false]" style="display: inline">
													<warehouse:message code="report.backToInventoryReport.label"/>
								    			</g:link>	
								    			&nbsp;|&nbsp;
												<g:link controller="report" action="generateTransactionReport" params="['product.id':command?.product?.id,'category.id':command?.category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,showEntireHistory:true]">
													<warehouse:message code="report.showEntireHistory.label"/>
												</g:link>
								    		</td>
								    		<td class="right">
								    			&nbsp;
								    		</td>
					    			</table>
					    			
					    			
					    		</div>
					    		<br/>
								<g:each var="itemEntry" in="${entry.value.entries}">
									<table style="border: 1px solid lightgrey">
										<thead>
											<tr class="${i++%2?'odd':'even' }">
												<th>
													${itemEntry.key.lotNumber ?: 'EMPTY'}
												</th>
												<th>
													<warehouse:message code="report.transactionDate.label"></warehouse:message>
												</th>
												<th>
													<warehouse:message code="report.transactionType.label"></warehouse:message>
												</th>
												<th class="center">
													<warehouse:message code="report.quantityChange.label"></warehouse:message>
												</th>
												<th class="center">
													<warehouse:message code="report.quantityBalance.label"></warehouse:message>
												</th>
											</tr>
										</thead>
										<tbody>
											<g:each var="row" in="${itemEntry.value.transactionEntries}">
												<g:set var="transactionTypeCode" value="${row?.transactionEntry?.transaction?.transactionType?.transactionCode?.toString()?.toLowerCase()}"/>
												<tr class="${i++%2?'odd':'even' }">
													<td></td>
													<td><format:date obj="${row.transactionEntry?.transaction?.transactionDate}"/></td>										
													<td><format:metadata obj="${row.transactionEntry?.transaction?.transactionType}"/></td>										
													<td class="center">
														<span class="${transactionTypeCode}">${row.transactionEntry?.quantity }</span>
													</td>
													<td class="center">${row.balance }</td>
												</tr>								
											</g:each>
										</tbody>
										<tfoot>
											<tr>
												<th colspan="4" class="right">
													
												</th>
												<th class="center">
													${itemEntry.value.quantityRunning }
												</th>
											</tr>
										</tfoot>
									</table>
									<br/>
								</g:each>
							</g:if>	
						</g:each>	
					</g:if>
					<g:else>
						<g:set var="transferInLocations" value="${command?.entries.values()*.quantityTransferredInByLocation*.keySet().flatten().unique()}"/>
						<g:set var="transferOutLocations" value="${command?.entries.values()*.quantityTransferredOutByLocation*.keySet().flatten().unique()}"/>
						
				    	<div class="list">
				   			<g:set var="status" value="${0 }"/>
					    	<g:each var="productEntry" in="${command?.productsByCategory }" status="i">
					    		<g:set var="category" value="${productEntry.key }"/>
					    		<div>		    		
							    	<table>
							    		<thead>
							    			<tr style="border-top: 1px solid lightgrey;">
							    				<th rowspan="2" class="bottom">
													<format:category category="${category}"/>		
															
									    			<g:if test="${!params.print }">
										    			<br/>	
										    			<g:link controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':command?.category?.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':false]" style="display: inline">
															<warehouse:message code="report.showDetails.label"/>
										    			</g:link>
									    			</g:if>
							    				</th>
							    				<th rowspan="2" class="center bottom total start">
													<warehouse:message code="report.initialQuantity.label"/>
							    				</th>
							    				<td colspan="${(params.showTransferBreakdown) ? 2 + (transferInLocations?.size?:0) : 3}" class="center total">
							    					<label>
							    						<warehouse:message code="report.incomingQuantity.label"/>
							    					</label>
							    				</td>
							    				<td colspan="${(params.showTransferBreakdown) ? 5 + (transferOutLocations?.size?:0) : 6}" class="center total">
								    				<label>
								    					<warehouse:message code="report.outgoingQuantity.label"/>
								    				</label>
												</td>
												<th rowspan="2" class="center bottom total end">
													<warehouse:message code="report.finalQuantity.label"/>
												</th>    				
							    			</tr>
							    		
								    		<tr style="border-top: 1px solid lightgrey;">
												<g:if test="${params.showTransferBreakdown }">
													<g:each var="location" in="${transferInLocations }">
														<th class="center bottom">${location.name.substring(0,3) }</th>
													</g:each>
												</g:if>
												<g:else>								
													<th class="center">
														<warehouse:message code="report.incomingTransferQuantity.label"/>
													</th>
												</g:else>	
												<th class="center nowrap">
													<warehouse:message code="report.adjustedInQuantity.label"/>
												</th>	
												<th class="center total">
													<warehouse:message code="report.incomingTotalQuantity.label"/>
												</th>	
												<g:if test="${params.showTransferBreakdown }">
													<g:each var="location" in="${transferOutLocations }">
														<th class="center">${location.name.substring(0,3) }</th>
													</g:each>
												</g:if>
												<g:else>
													<th class="center">
														<warehouse:message code="report.outgoingTransferQuantity.label"/>
													</th>
												</g:else>
												<th class="center">
													<warehouse:message code="report.expiredQuantity.label"/>
												</th>
												<th class="center">
													<warehouse:message code="report.consumedQuantity.label"/>
												</th>
												<th class="center">
													<warehouse:message code="report.damagedQuantity.label"/>
												</th>
												<th class="center">
													<warehouse:message code="report.adjustedOutQuantity.label"/>
												</th>
												<th class="center total">
													<warehouse:message code="report.outgoingTotalQuantity.label"/>
												</th>
								    		</tr>
								    	</thead>
								    	<tbody>
									    	<g:each var="product" in="${productEntry.value }" status="j">
									    		<g:set var="entry" value="${command.entries[product].totals }"/>
												<tr class="${j%2 ? 'even' : 'odd' }">
													<td class="left" style="width: 35%">
														<g:if test="${!params.print }">
															<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">   	
																<format:product product="${product }"/>
												    		</g:link>											    		
												    		
															<g:link controller="report" action="generateTransactionReport" params="['product.id':product?.id,'category.id':command?.category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate]" fragment="inventory">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" class="middle"/>
															</g:link>

											    		</g:if>
											    		<g:else>
															<format:product product="${product }"/>
											    		</g:else>
										    		</td>
													<td class="center total start">	    	
											    		<span class="${(entry?.quantityInitial>=0)?'credit':'debit'}">
											    			${entry?.quantityInitial ?: 0}
											    		</span>
										    		</td>
										    		<g:if test="${params.showTransferBreakdown }">
														<g:each var="location" in="${transferInLocations }">
															<td class="center">
																<span class="${(entry?.quantityTransferredInByLocation[location]>=0)?'credit':'debit'}">
																	${entry.quantityTransferredInByLocation[location]?:0}
																</span>
															</td>
														</g:each>
													</g:if>
													<g:else>
														<td class="center">	    	
												    		<span class="${(entry?.quantityTransferredIn>=0)?'credit':'debit'}">
													    		${entry?.quantityTransferredIn ?: 0}
													    	</span>
														</td>
													</g:else>
													<td class="center">	    	
											    		<span class="${(entry?.quantityFound>=0)?'credit':'debit'}">${entry?.quantityFound ?: 0}</span>
													</td>
													<td class="center total">	    	
											    		<span class="${(entry?.quantityTotalIn>=0)?'credit':'debit'}">
											    			${entry?.quantityTotalIn ?: 0}
											    		</span>
													</td>
													
										    		<g:if test="${params.showTransferBreakdown }">
														<g:each var="location" in="${transferOutLocations }">
															<td class="center">
																<span class="${(entry?.quantityTransferredOutByLocation[location]>0)?'debit':'credit'}">${entry.quantityTransferredOutByLocation[location]?:0}</span>
															</td>
														</g:each>
													</g:if>
													<g:else>
														<td class="center">	    	
															<span class="${(entry?.quantityTransferredOut>0)?'debit':'credit'}">${entry?.quantityTransferredOut?:0}</span>
														</td>
													</g:else>
													<td class="center">	    	
											    		<span class="${(entry?.quantityExpired>0)?'debit':'credit'}">${entry?.quantityExpired ?: 0}</span>
													</td>
													<td class="center">	    	
											    		<span class="${(entry?.quantityConsumed>0)?'debit':'credit'}">${entry?.quantityConsumed ?: 0}</span>
													</td>
													<td class="center">	    	
											    		<span class="${(entry?.quantityDamaged>0)?'debit':'credit'}">${entry?.quantityDamaged ?: 0}</span>
													</td>
													<td class="center">	    	
											    		<span class="${(entry?.quantityLost>=0)?'credit':'debit'}">${entry?.quantityLost ?: 0}</span>
													</td>
													<td class="center total">
														<span class="${(entry?.quantityTotalOut>0)?'debit':'credit'}">${entry?.quantityTotalOut ?: 0 }</span>
													</td>
													<td class="center total end">
														<span class="${(entry?.quantityFinal>=0)?'credit':'debit'}">${entry?.quantityFinal ?: 0}
													</td>
										    	</tr>
									    	</g:each>
										</tbody>
									</table>
								</div>
								<br/>
							</g:each>
				    	</div>			
				    	
				    </g:else>	
				</td>
			</tr>
		</table>
		
		

    	
    	<%--
	    <script>
			$(document).ready(function() {
				$(".filter").change(function() { 
					$(this).closest("form").submit();
				});
			});
	    </script>
	    --%>
    </body>
</html>