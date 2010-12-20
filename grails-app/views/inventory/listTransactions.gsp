
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>    
    </head>    

	<body>
       <div class="body">
       
			<div class="nav">
				<g:render template="nav"/>
			</div>
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			
			
			<div class="list">
				<table>
                    <thead>
                        <tr>   
							<th>ID</th>
							<th>Date</th>
							<th>Type</th>
							<th>Source</th>
							<th>Destination</th>
							<th>Confirmed?</th>
							<th>Entries</th>
							<th>Actions</th>
                        </tr>
                    </thead>
       	           	<tbody>			
						<g:each var="transactionInstance" in="${transactionInstanceList}" status="i">           
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td>
									${transactionInstance?.id }
								</td>				
								<td>
									${formatDate(date: transactionInstance?.transactionDate, format: 'dd-MMM-yyyy') }
								</td>
								<td>
									${transactionInstance?.transactionType?.name }
								</td>
								<td>
									${transactionInstance?.source?.name }
								</td>
								<td>
									${transactionInstance?.destination?.name }
								</td>
								<td>
									<g:if test="${!transactionInstance?.confirmed}">
										
									</g:if>
									<g:else>
										Confirmed by ${transactionInstance?.confirmedBy?.name } on
										${formatDate(date: transactionInstance?.dateConfirmed, format: 'dd-MMM-yyyy') }
									</g:else>
								</td>
								<td>
									${transactionInstance?.transactionEntries?.size() }
								</td>
								<td>
									<g:link action="showTransaction" id="${transactionInstance?.id }">View</g:link>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
									<g:if test="${transactionInstance?.confirmed }">
										<g:link action="confirmTransaction" id="${transactionInstance?.id }">Reset</g:link>	
									</g:if>
									<g:else>
										<g:link action="confirmTransaction" id="${transactionInstance?.id }">Confirm</g:link>	
									</g:else>
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</body>

</html>
