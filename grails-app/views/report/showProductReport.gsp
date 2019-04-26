<%@ page defaultCodec="html" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="report.showProductReport.label" /></title>
        <style>
        	.filter { }
        	th { text-transform: uppercase; }
        	.product { font-weight: bold; }
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
		<div class="buttonBar">
			<g:link class="list" controller="report" action="showTransactionReport" params="['location.id':command.location?.id,'category.id':command?.product?.category.id,'startDate':command.startDate,'endDate':command.endDate]">
	    		&lsaquo; Back to Transaction Report
	    	</g:link>
	    </div>

		<div class="form box">
			<g:form controller="report" action="showProductReport" method="GET">
				<span class="filter">
					<label>Product</label>
					<span class="product"><format:product product="${command?.product }"/></span>
					<g:hiddenField name="product.id" value="${command?.product?.id }" class="filter"/>
				</span>
				<span class="filter">
					<label>Location</label>
					<g:selectLocation name="location.id" noSelection="['null':'']" value="${command?.location?.id}" class="filter"/>
				</span>
				<span class="filter">
					<label>Start date</label>
					<g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy" class="filter"/>
				</span>
				<span class="filter">
					<label>End date</label>
					<g:jqueryDatePicker id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy" class="filter"/>
				</span>
				<span class="filter">
					<button type="submit" class="btn">Run Report</button>
				</span>
			</g:form>

		</div>

    	<div class="list">
	    	<table>
	    		<thead>
	    			<th>Transaction Date</th>
	    			<th>Transaction Type</th>
	    			<th>Lot Number</th>
	    			<th>Expires</th>
	    			<th class="center">Debit/Credit</th>
	    			<th class="center">Quantity</th>
	    			<th class="center">Balance</th>
	    		</thead>
				<tbody>
					<tr>
						<td></td>
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
			    				<format:date obj="${transactionEntry?.transaction?.transactionDate }"/>

			    			</td>
			    			<td>
			    				<format:metadata obj="${transactionEntry?.transaction?.transactionType}"/>
			    			</td>
			    			<td>
			    				${transactionEntry?.inventoryItem?.lotNumber }
			    			</td>
			    			<td>
			    				<format:expirationDate obj="${transactionEntry?.inventoryItem?.expirationDate }"/>

			    			</td>
			    			<td class="center">
				    			${transactionEntry?.transaction?.transactionType?.transactionCode }
			    			</td>
			    			<td class="center">
			    				<span class="${transactionEntry?.transaction?.transactionType?.transactionCode?.equals("DEBIT")?'debit':'credit' }">
			    					${transactionEntry?.quantity }
			    				</span>
			    			</td>
			    			<td class="center">
			    				<span class="${productReportEntry?.balance>=0?'credit':'debit' }">
									${productReportEntry?.balance }
								</span>
			    			</td>
			    		</tr>
			    	</g:each>
					<tr>
						<td></td>
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
	    <script>
			$(document).ready(function() {
				$(".filter").change(function() {
					$(this).closest("form").submit();
				});
			});
	    </script>
    </body>
</html>
