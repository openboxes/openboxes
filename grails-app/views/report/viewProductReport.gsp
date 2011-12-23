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
			<g:form controller="report" action="viewProductReport" method="GET">
				<span class="filter">
					<label>Product</label>
					${command?.product?.name }				
					<g:hiddenField name="product.id" value="${command?.product?.id }"/>
				</span>
				<span class="filter">
					<label>Location</label>
					<g:selectLocation name="location.id" noSelection="['null':'']" value="${command?.location?.id}"/>
				</span>	
				<span class="filter">
					<label>Start date</label>
					<g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy"/>
				</span>					
				<span class="filter">
					<label>End date</label>
					<g:jqueryDatePicker id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy"/>
				</span>
				<span class="filter">
					<button type="submit" class="btn">Run Report</button>
				</span>
			</g:form>		
		
		</div>
    
    	<div class="list">
	    	<table>
	    		<thead>
	    			<th>Type</th>
	    			<th>Date</th>	    			
	    			<th>Lot Number</th>
	    			<th>Expires</th>
	    			<th class="center">Quantity</th>
	    			<th class="center">Balance</th>
	    		</thead>
				<tbody>	    	
					<tr>
						<td>Initial inventory</td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td class="center">${command?.quantityInitial }</td>
					</tr>
		    		<g:each var="productReportEntry" in="${command?.productReportEntryList}" status="i">
		    			<g:set var="transactionEntry" value="${productReportEntry?.transactionEntry }"/>
			    		<tr class="${i%2 ? 'even' : 'odd' }">
			    			<td>
			    				${transactionEntry?.transaction?.transactionType?.name }
			    			</td>
			    			<td>
			    				${transactionEntry?.transaction?.transactionDate }
			    			</td>
			    			<td>
			    				${transactionEntry?.inventoryItem?.lotNumber }
			    			</td>
			    			<td>
			    				${transactionEntry?.inventoryItem?.expirationDate }
			    			</td>
			    			<td class="center">
			    				${transactionEntry?.quantity }
			    			</td>
			    			<td class="center">
								${productReportEntry?.balance }			    				
			    			</td>
			    		</tr>
			    	</g:each>
					<tr>
						<td>Current inventory</td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td class="center">${command?.quantityFinal }</td>
					</tr>
			    	
			    	
		    	</tbody>
	    	</table>
    	</div>
    </body>
    
</html>