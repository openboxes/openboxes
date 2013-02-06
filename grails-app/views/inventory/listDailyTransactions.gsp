<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title><warehouse:message code="transaction.dailyTransactions.label"/></title>    
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'scrolltable.css')}" type="text/css" media="screen, projection" />
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			
			
			<%-- 
			<table>
				<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listDailyTransactions" method="get">
			            	<table >
			            		<tr>
			            			<th>Show daily transactions for </th>
			            			<th></th>
			            		</tr>
			            		<tr>
									<td>
						           		<g:select name="threshold"
											from="['1':'one week', '14':'two weeks', '30':'one month', 
												'60':'two months', '90':'three months',
												'180': 'six months', '365':'one year']"
											optionKey="key" optionValue="value" value="${thresholdSelected}" 
											noSelection="['':'--All--']" />&nbsp;&nbsp;    
						           	</td>
									<td class="filter-list-item" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;Filter </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>
			<br/>			
			--%>
			
								
			<fieldset>		
				<div style="padding: 10px;">
					<h1><warehouse:message code="transaction.dailyTransactionsFor.label"/> ${session.warehouse.name} <span class="fade small">${formatDate(date: dateSelected, format: 'EEEEE, MMMMM dd yyyy') }</span></h1>
				</div>
				
				<table>				
					<tr>
						<td width="15%">
							<div class="list">
								<table class="scrollTable">			
									<thead class="fixedHeader">
										<tr>
											<th class="odd"><warehouse:message code="default.dates.label"/></th>
										</tr>
									</thead>
									
									<g:if test="${transactionsByDate }">
										<tbody class="scrollContent">		
											<g:each var="entry" in="${transactionsByDate }" status="i">
												<g:set var="dateParam" value="${formatDate(date: entry.key, format: org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT) }"/>
												<g:set var="dateDisplay" value="${formatDate(date: entry.key, format: 'MMMMM dd') }"/>
												<tr class="prop ${i%2?'odd':'even' }">
													<td class="left">
														<g:link action="listDailyTransactions" params="[date: dateParam]">
															${dateDisplay } 
														</g:link>
														(${entry.value.size() })
													</td>
												</tr>
											</g:each>
										</tbody>
									</g:if>
									<g:else>
										<warehouse:message code="transaction.noTransactions.label"/>
									</g:else>								
								</table>
							</div>
							
						</td>
						<td class="left">
							
							<div class="list">
								<table>
				                    <thead>
				                        <tr class="odd">                           	
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
														<g:link controller="inventory" action="showTransaction" id="${transaction.id }">
														${transaction?.transactionNumber }
														</g:link>
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
