<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title>Daily transactions ${session.warehouse.name}</title>    
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<fieldset>		
						
				<table>				
					<tr>
						<td></td>
						<td>
						</td>
					</tr>
					<tr>
						<td width="15%">
						
							<table>							
								<g:each var="entry" in="${transactionsByDate }">
									<g:set var="dateParam" value="${formatDate(date: entry.key, format: 'MM/dd/yyyy') }"/>
									<g:set var="dateDisplay" value="${formatDate(date: entry.key, format: 'MMM dd') }"/>
									<g:if test="${entry.key.month == new Date().month }">
										<tr>
											<td>
												<g:link action="listDailyTransactions" params="[date: dateParam]">
													${dateDisplay } 
												</g:link>
												
												...................... ${entry.value.size() }
											</td>
										</tr>
									</g:if>						
								</g:each>
							</table>
							
						</td>
						<td class="left">
							
							<div class="list">
								<table>
				                    <thead>
				                        <tr>                           	
					                        <%--<th width="2%">Actions</th> --%>
					                        <th>ID</th>
											<th>Type</th>
											<th>Product</th>
											<th>Lot Number</th>
											<th>Quantity</th>
											<th>Date</th>
				                        </tr>
				                    </thead>
				       	           	<tbody>			
					       	           	<g:set var="counter" value="${0 }" />	
										<g:each var="transaction" in="${transactions}" status="i">     
											<g:each var="transactionEntry" in="${transaction.transactionEntries}" status="j">     
												<tr class="${counter++%2?'odd':'even' }">
													<td>
														${transaction?.transactionNumber() }
													</td>
										      		<td>
										      			${transaction?.transactionType?.name }
										      			<g:if test="${transaction.source }">
											      			from ${transaction?.source?.name }
										      			</g:if>
										      			<g:if test="${transaction.destination }">
											      			to ${transaction?.destination?.name }
										      			</g:if>
										      			
													</td>
										      		
													<td>
														${transactionEntry?.inventoryItem?.product }
													</td>
													<td>
														${transactionEntry?.inventoryItem?.lotNumber }
													</td>
													<td>
														${transactionEntry.quantity }
													</td>
													<td>
										      			${transaction?.dateCreated }
													</td>
												</tr>
											</g:each>
										</g:each>
									</tbody>
								</table>
							</div>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
		
	</body>

</html>
