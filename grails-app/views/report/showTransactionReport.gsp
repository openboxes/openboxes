<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showTransactionReport.label" /></title>    
        <style>
        	.title { text-align: center; padding: 15px; }
        	.total { border-right: 1px solid lightgrey; border-left: 1px solid lightgrey; }
        	.parameters { width:30%; margin-left: auto; margin-right: auto;  }
        </style>
    </head>    
    <body>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${cmd}">
			<div class="errors">
				<g:renderErrors bean="${cmd}" as="list" />
			</div>
		</g:hasErrors>
	   	<g:if test="${!params.print}">
			<div class="form" style="border-bottom: 1px solid lightgrey; border-top: 1px solid lightgrey; padding: 5px">
				<g:form controller="report" action="showTransactionReport" method="GET">
					<span class="filter">
						<label>
							<warehouse:message code="report.category.label"/>
						</label>
						<select id="category.id" name="category.id" class="filter">
							<option value=""></option>
							<g:render template="../category/selectOptions" model="[category:cmd.rootCategory, selected:cmd.category, level: 0]"/>								
						</select>							
					</span>
					<span class="filter">
						<label>
							<warehouse:message code="report.location.label"/>
						</label>
						<g:selectLocation class="filter" name="location.id" noSelection="['null':'']" value="${cmd?.location?.id}"/>
					</span>	
					<span class="filter">
						<label>
							<warehouse:message code="report.startDate.label"/>
						</label>
						<g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${cmd?.startDate }" format="MM/dd/yyyy"/>
					</span>					
					<span class="filter">
						<label>
							<warehouse:message code="report.endDate.label"/>
						</label>
						<g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${cmd?.endDate }" format="MM/dd/yyyy"/>
					</span>
					<span class="filter">
						<g:checkBox name="showTransferBreakdown" value="${params?.showTransferBreakdown}" class="filter"/>
						<label>
							<warehouse:message code="report.showTransferBreakdown.label"/>
						</label>
					</span>
					<span class="filter">
						<button type="submit" class="btn">
							<warehouse:message code="report.runReport.label"/>
						</button>
					</span>
				</g:form>				
			</div>
	    	<div class="left" style="padding: 5px;">
				<label><warehouse:message code="report.exportAs.label"/></label>
	   			<g:link target="_blank" controller="report" action="showTransactionReport" params="[print:'true','location.id':cmd.location?.id,'category.id':cmd?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':params.showTransferBreakdown]">
	   				<warehouse:message code="report.exportAs.html.label"/>
	   			</g:link> 
	   			&nbsp;|&nbsp;
	   			<g:link target="_blank" controller="report" action="downloadTransactionReport" params="[url:request.forwardURI,'location.id':cmd.location?.id,'category.id':cmd?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':params.showTransferBreakdown]">
	   				<warehouse:message code="report.exportAs.pdf.label"/>
	   			</g:link>
			</div>
		</g:if>
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
						${cmd?.location?.name }
					</td>
				</tr>
				<tr>
					<td class="right">
						<label>
							<warehouse:message code="report.category.label"/>
						</label>
					</td>
					<td>
						<format:category category="${cmd?.category?.parentCategory}"/> &#155;
						<format:category category="${cmd?.category}"/>					
					</td>
				</tr>
				<tr>
					<td class="right">
						<label>
							<warehouse:message code="report.startDate.label"/>
						</label>
					</td>
					<td>
						${format.date(obj:cmd?.startDate)}
					</td>
				</tr>
				<tr>
					<td class="right">
						<label>
							<warehouse:message code="report.endDate.label"/>
						</label>
					</td>
					<td>
						${format.date(obj:cmd?.endDate)}
					</td>
				</tr>
			</table>
		</g:else>


		<g:set var="transferInLocations" value="${cmd?.inventoryReportEntryMap.values()*.quantityTransferredInByLocation*.keySet().flatten().unique()}"/>
		<g:set var="transferOutLocations" value="${cmd?.inventoryReportEntryMap.values()*.quantityTransferredOutByLocation*.keySet().flatten().unique()}"/>
		
    	<div class="list">
   			<g:set var="status" value="${0 }"/>
	    	<g:each var="productEntry" in="${cmd?.productsByCategory }" status="i">
	    		<g:set var="category" value="${productEntry.key }"/>
	    		<div style="page-break-after: always">		    		
			    	<table>
			    		<thead>
			    			<tr style="border-top: 1px solid lightgrey;">
			    				<th rowspan="2" class="middle">
									<format:category category="${cmd?.category?.parentCategory}"/> &#155;
									<format:category category="${cmd?.category}"/>					
					    			<g:if test="${!params.print }">
						    			[<g:link controller="report" action="showTransactionReport" params="['location.id':cmd.location?.id,'category.id':category.id,'startDate':cmd.startDate,'endDate':cmd.endDate]" style="display: inline">
											<warehouse:message code="report.showDetails.label"/>
						    			</g:link>]
					    			</g:if>
			    				</th>
			    				<th rowspan="2" class="center middle total">
									<warehouse:message code="report.initialQuantity.label"/>
			    				</th>
			    				<td colspan="${(params.showTransferBreakdown) ? 1 + (transferInLocations?.size?:0) : 2}" class="center total">
			    					<label>
			    						<warehouse:message code="report.incomingQuantity.label"/>
			    					</label>
			    				</td>
			    				<td colspan="${(params.showTransferBreakdown) ? 4 + (transferOutLocations?.size?:0) : 5}" class="center total">
				    				<label>
				    					<warehouse:message code="report.outgoingQuantity.label"/>
				    				</label>
								</td>
								<td colspan="3" class="center total">
									<label>
					    				<warehouse:message code="report.adjustedQuantity.label"/>
									</label>
								</td>	
								<th rowspan="2" class="center middle total">
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
								<th class="center total">
									<warehouse:message code="report.outgoingTotalQuantity.label"/>
								</th>
								<th class="center nowrap">
									<warehouse:message code="report.adjustedInQuantity.label"/>
								</th>	
								<th class="center">
									<warehouse:message code="report.adjustedOutQuantity.label"/>
								</th>
								<th class="center bottom total">
									<warehouse:message code="report.adjustedTotalQuantity.label"/>
								</th>
				    		</tr>
				    	</thead>
				    	<tbody>
					    	<g:each var="product" in="${productEntry.value }" status="j">
					    		<g:set var="entry" value="${cmd.inventoryReportEntryMap[product] }"/>
								<tr class="${status++%2 ? 'even' : 'odd' }">
									<td class="left">
										<g:if test="${!params.print }">
											<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">   	
												<format:product product="${product }"/>
								    		</g:link>
											[
											<g:link controller="report" action="showProductReport" params="['product.id':product?.id,'location.id':session?.warehouse?.id,startDate:params.startDate,endDate:params.endDate]" fragment="inventory">
												<warehouse:message code="report.showDetails.label"/>
											</g:link>
											]
							    		</g:if>
							    		<g:else>
											<format:product product="${product }"/>
							    		</g:else>
						    		</td>
									<td class="center total">	    	
							    		<strong>${entry?.quantityInitial ?: 0}</strong>
						    		</td>
						    		
						    		<g:if test="${params.showTransferBreakdown }">
										<g:each var="location" in="${transferInLocations }">
											<td class="center">${entry.quantityTransferredInByLocation[location]?:0}</td>
										</g:each>
									</g:if>
									<g:else>
										<td class="center">	    	
								    		${entry?.quantityTransferredIn ?: 0}
										</td>
									</g:else>
									<td class="center total">	    	
							    		${(entry?.quantityTotalIn!=0)?'+':''}${entry?.quantityTotalIn ?: 0}
									</td>
									
						    		<g:if test="${params.showTransferBreakdown }">
										<g:each var="location" in="${transferOutLocations }">
											<td class="center">${entry.quantityTransferredOutByLocation[location]?:0}</td>
										</g:each>
									</g:if>
									<g:else>
										<td class="center">	    	
											${entry?.quantityTransferredOut ?: 0}
										</td>
									</g:else>
									<td class="center">	    	
							    		${entry?.quantityExpired ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityConsumed ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityDamaged ?: 0}
									</td>
									<td class="center total">
										${(entry?.quantityTotalOut!=0)?'-':''}${entry?.quantityTotalOut ?: 0 }
									</td>
									<td class="center">	    	
							    		${entry?.quantityFound ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityLost ?: 0}
									</td>
									<td class="center total">	    	
							    		${(entry?.quantityAdjusted>0)?'+':'-'}${entry?.quantityAdjusted ?: 0}
									</td>
									<td class="center total">	    	
							    		<strong>${entry?.quantityFinal ?: 0}</strong>
									</td>
						    	</tr>
					    	</g:each>
						</tbody>
					</table>
				</div>
			</g:each>
    	</div>
	    <script>
			$(document).ready(function() {
				$(".filter").change(function() { 
					$(this).closest("form").submit();
				});
			});
	    </script>
    </body>
</html>