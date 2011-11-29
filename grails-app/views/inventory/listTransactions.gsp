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
			
			<table style="width:100%; border-collapse: collapse; border-color: black;">
				<tr>
					<td class="filter filterRow ${!transactionTypeSelected ? 'filterSelected' : '' }">
						<g:link controller="inventory" action="listAllTransactions">
							<warehouse:message code="transactionType.all.label"/>
						</g:link>
					</td>
					<g:each var="transactionType" in="${org.pih.warehouse.inventory.TransactionType.list()}">
						<g:set var="numberOfTransactions" value="${transactionMap[transactionType?.id]?.size()?:0}"/> 
						<g:if test="${numberOfTransactions > 0 }">
							<td class="filter filterRow paddingRow"></td>
							<td class="filter filterRow ${transactionType == transactionTypeSelected ? 'filterSelected' : '' }">
								<g:link controller="inventory" action="listAllTransactions" params="['transactionType.id':transactionType?.id]">
									<format:metadata obj="${transactionType }"/>
									(${numberOfTransactions })
								</g:link>
							</td>		
						</g:if>
					</g:each>
					<td class="filter filterRow paddingRow" style="width:100%">&nbsp;</td>
				</tr>
			</table>
			<div style="padding-top: 10px;">
				<table>
                    <thead>
                        <tr class="odd">   
							<th><warehouse:message code="default.actions.label"/></th>
							<th><warehouse:message code="default.date.label"/></th>
							<th><warehouse:message code="transaction.type.label"/></th>
							<th><warehouse:message code="inventory.label"/></th>
							<th><warehouse:message code="default.source.label"/></th>
							<th><warehouse:message code="default.destination.label"/></th>
                        </tr>
                    </thead>
       	           	<tbody>			
						<g:each var="transactionInstance" in="${transactionInstanceList}" status="i">           
							<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">            
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
													<%-- 
													<g:link action="editTransaction" id="${transactionInstance?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;<warehouse:message code="transaction.edit.label"/>
													</g:link>
													--%>
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
									<g:link action="showTransaction" id="${transactionInstance?.id }">
										<span class="${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
											<format:metadata obj="${transactionInstance?.transactionType}"/>
											(${transactionInstance?.transactionEntries?.size() } items)
										</span>
									</g:link>
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
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
			<div class="paginateButtons">
				<g:paginate total="${transactionCount}" params="['transactionType.id':transactionTypeSelected?.id]"/>
			</div>
		</div>
	</body>
</html>
