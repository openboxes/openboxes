<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><g:message code="inventory.expiringStock.label"/></title>    
    </head>    

	<body>
		<div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<table>
				<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listExpiringStock" method="get">
			            	<table >
			            		<tr>
			            			<th><warehouse:message code="category.label"/></th>
			            			<th><warehouse:message code="inventory.expiresWithin.label"/></th>
			            			<th><warehouse:message code="inventory.excludeExpired.label"/></th>
			            		</tr>
			            		<tr>
						           	<td class="filter-list-item">
						           		<g:select name="category"
														from="${categories}"
														optionKey="id" optionValue="${{format.category(category:it)}}" value="${categorySelected?.id}" 
														noSelection="['':'--All--']" />   
									</td>
									<td>
						           		<g:select name="threshhold"
														from="['1':'one week', '14':'two weeks', '30':'one month', 
															'60':'two months', '90':'three months',
															'180': 'six months', '365':'one year']"
														optionKey="key" optionValue="value" value="${threshholdSelected}" 
														noSelection="['':'--All--']" />  
						           	</td>
						           	<td>	
						           		<g:checkBox name="excludeExpired" value="${excludeExpired }" } />
						           	
						           	</td>						           	
									<td class="filter-list-item" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>
			<br/>
			
				
			<table>
				<tr>					
					<td>
						<label>
							<img src="${resource(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: middle"/> 
							<warehouse:message code="inventory.consumption.label"/>
						</label>
						<div class="list box">
							
							<table>
			                    <thead>
			                        <tr>   
										<th><warehouse:message code="item.label"/></th>
										<th><warehouse:message code="inventory.lotNumber.label"/></th>
										<th><warehouse:message code="inventory.expires.label"/></th>
										<th class="center"><warehouse:message code="default.qty.label"/></th>
			                        </tr>
			                    </thead>
			       	           	<tbody>			
			       	     			<g:set var="counter" value="${0 }" />
									<g:each var="transaction" in="${transactions}" status="i">           
										<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
											<td>
												${transaction?.id }

											</td>
										</tr>						
									</g:each>
								</tbody>
							</table>				
						</div>
					</td>
				</tr>			
			</table>			
		</div>
		
	</body>

</html>
