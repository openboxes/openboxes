<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="report.viewTransactionReport.label" /></title>    
        <style>
        	.filter { padding-right: 30px; border: 0; }
        </style>
    </head>    
    <body>
		<div class="form" style="margin: 15px; padding: 15px;">
			<g:form controller="report" action="viewTransactionReport" method="GET">
				<span class="filter">
					<label>Category</label>
					${cmd.category }				
					<g:hiddenField name="category.id" value="${cmd?.category?.id }"/>
				</span>
				<span class="filter">
					<label>Location</label>
					<g:selectLocation name="location.id" noSelection="['null':'']" value="${cmd?.location?.id}"/>
				</span>	
				<span class="filter">
					<label>Start date</label>
					<g:jqueryDatePicker id="startDate" name="startDate" value="${cmd?.startDate }" format="MM/dd/yyyy"/>
				</span>					
				<span class="filter">
					<label>End date</label>
					<g:jqueryDatePicker id="endDate" name="endDate" value="${cmd?.endDate }" format="MM/dd/yyyy"/>
				</span>
				<span class="filter">
					<button type="submit" class="btn">Run Report</button>
				</span>
			</g:form>		
		
		</div>
    
    	<div class="list">
    
	    	<table>
    			<g:set var="status" value="${0 }"/>
		    	<g:each var="entry" in="${cmd?.productsByCategory }" status="i">
		    		<g:set var="category" value="${entry.key }"/>
		    		<thead>
			    		<tr style="border-top: 1px solid lightgrey;">
							<th>${category?.name }</th>
							<th class="center" style="border-right: 1px solid lightgrey">Start</th>
							<th class="center nowrap">Transfer In</th>	
							<th class="center nowrap">Found</th>	
							<th class="center nowrap" style="border-right: 1px solid lightgrey">Total In</th>	
							<th class="center nowrap">Transfer Out</th>
							<th class="center">Expired</th>
							<th class="center">Consumed</th>
							<th class="center">Damaged</th>
							<th class="center">Lost</th>
							<th class="center" style="border-right: 1px solid lightgrey">Total Out</th>
							<th class="center">End</th>
			    		</tr>
			    	</thead>
			    	<tbody>
				    	<g:each var="product" in="${entry.value }" status="j">
				    		<g:set var="inventoryReportEntry" value="${cmd.inventoryReportEntryMap[product] }"/>
							<tr class="${status++%2 ? 'odd' : 'even' }">
								<td>
									<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">   	
							    		${product?.name }
						    		</g:link>
						    		&nbsp;[
									<g:link controller="report" action="viewProductReport" params="['product.id':product?.id]" fragment="inventory">   	
										details
									</g:link>]
					    		</td>
								<td class="center" style="border-right: 1px solid lightgrey">	    	
						    		<strong>${inventoryReportEntry?.quantityInitial ?: 0}</strong>
					    		</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityTransferredIn ?: 0}
								</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityFound ?: 0}
								</td>
								<td class="center" style="border-right: 1px solid lightgrey">	    	
						    		${inventoryReportEntry?.quantityTotalIn ?: 0}
								</td>
								<td class="center">	    	
									${inventoryReportEntry?.quantityTransferredOut ?: 0}
								</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityExpired ?: 0}
								</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityConsumed ?: 0}
								</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityDamaged ?: 0}
								</td>
								<td class="center">	    	
						    		${inventoryReportEntry?.quantityLost ?: 0}
								</td>
								<td class="center" style="border-right: 1px solid lightgrey">
									${inventoryReportEntry?.quantityTotalOut ?: 0 }
								</td>
								<td class="center">	    	
						    		<strong>${inventoryReportEntry?.quantityFinal ?: 0}</strong>
								</td>
					    	</tr>
				    	</g:each>
					</tbody>
				</g:each>
	    	</table>
    	</div>
    </body>
    
</html>