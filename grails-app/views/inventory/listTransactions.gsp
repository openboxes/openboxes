<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title><warehouse:message code="transaction.list.label"/></title>    
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
							<th><warehouse:message code="default.actions.label"/></th>
							<th><warehouse:message code="default.date.label"/></th>
							<th><warehouse:message code="transaction.type.label"/></th>
							<th><warehouse:message code="inventory.label"/></th>
							<th><warehouse:message code="default.source.label"/></th>
							<th><warehouse:message code="default.destination.label"/></th>
							<th><warehouse:message code="transaction.entries.label"/></th>
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
														<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;<warehouse:message code="transaction.view.label"/>
													</g:link>
													<g:link action="editTransaction" id="${transactionInstance?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;<warehouse:message code="transaction.edit.label"/>
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
										<format:metadata obj="${transactionInstance?.transactionType}"/>
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
