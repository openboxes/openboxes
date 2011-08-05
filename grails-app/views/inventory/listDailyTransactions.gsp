<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title><warehouse:message code="transaction.dailyTransactions.label"/></title>    
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
					                        <%--<th width="2%"><warehouse:message code="default.actions.label"/></th> --%>
					                        <th><warehouse:message code="transaction.id.label"/></th>
											<th><warehouse:message code="transaction.type.label"/></th>
											<th><warehouse:message code="product.label"/></th>
											<th><warehouse:message code="product.lotNumber.label"/></th>
											<th><warehouse:message code="default.quantity.label"/></th>
											<th><warehouse:message code="default.date.label"/></th>
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
										      			<format:metadata obj="${transaction?.transactionType}"/>
										      			<g:if test="${transaction.source }">
											      			from ${transaction?.source?.name }
										      			</g:if>
										      			<g:if test="${transaction.destination }">
											      			to ${transaction?.destination?.name }
										      			</g:if>
										      			
													</td>
										      		
													<td>
														<format:product product="${transactionEntry?.inventoryItem?.product}"/>
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
