<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title>Transactions for inventory at ${session.warehouse.name}</title>    
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
						
			<div class="list">
				<table>
                    <thead>
                        <tr>   
							<th>Actions</th>
							<th>Date</th>
							<th>Type</th>
							<th>Inventory</th>
							<th>Source</th>
							<th>Destination</th>
							<th>Entries</th>
                        </tr>
                    </thead>
       	           	<tbody>			
						<g:each var="transactionInstance" in="${transactionInstanceList}" status="i">           
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
								<td align="center">
									
									<!-- Action menu -->
									<div>
										<span class="action-menu">
											<button class="action-btn">
												<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
												<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
											</button>
											<div class="actions">
												<div class="action-menu-item">
													<g:link action="showTransaction" id="${transactionInstance?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;View transaction details
													</g:link>
													<g:link action="editTransaction" id="${transactionInstance?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;Edit transaction details
													</g:link>
													
												</div>
											</div>
										</span>				
									</div>												
								
									<%-- 
									<g:if test="${transactionInstance?.confirmed }">
										<g:link action="confirmTransaction" id="${transactionInstance?.id }">Reset</g:link>	
									</g:if>
									<g:else>
										<g:link action="confirmTransaction" id="${transactionInstance?.id }">Confirm</g:link>	
									</g:else>
									--%>
								</td>
								<td>
									${formatDate(date: transactionInstance?.transactionDate, format: 'dd-MMM-yyyy') }
								</td>
								<td>
									<span class="${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
										${transactionInstance?.transactionType?.name }
									</span>
								</td>
								<td>
									${transactionInstance?.inventory }
								</td>
								<td>
									${transactionInstance?.source?.name }
								</td>
								<td>
									${transactionInstance?.destination?.name }
								</td>
								<td>
									${transactionInstance?.transactionEntries?.size() }
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
		<div class="paginateButtons">
			<g:paginate total="${transactionCount}" />
		</div>
		
	</body>

</html>
