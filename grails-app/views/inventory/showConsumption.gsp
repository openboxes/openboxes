<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><g:message code="inventory.consumption.label"/></title>    
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
			--%>
				
			<table>
				<tr>					
					<td>
						<label>
							<img src="${resource(dir:'images/icons/silk',file:'cup.png')}" style="vertical-align: middle"/> 
							<warehouse:message code="inventory.consumption.label"/>
						</label>
						<div class="message">
							<warehouse:message code="inventory.consumption.message"/>
						</div>
						<div class="list box">
							
							<table>
			       	           	<tbody>			
			                        <tr>   
										<th><warehouse:message code="category.label"/></th>
										<th><warehouse:message code="product.label"/></th>
										<th class="center"><warehouse:message code="inventory.consumeQuantity.label"/></th>
			                        </tr>
			                        <g:set var="c" value="${0 }"/>
									<g:each var="entry" in="${productMap}" status="i">           
										<g:each var="product" in="${entry.value}" status="j">
											<tr class="${(c++ % 2) == 0 ? 'odd' : 'even'}">            
												<td>
													${product.category.name }
												</td>
												<td>
													${product.name }
												</td>
												<td class="center">
													${consumptionMap[product] }
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
		</div>
		
	</body>

</html>
