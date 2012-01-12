<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${params.print?'print':'custom' }" />
        <title><warehouse:message code="report.showTransactionReport.label" /></title>    
        <style>
        	.filter { padding-right: 30px; border: 0; border-right: 1px solid lightgrey; }
        	th { text-transform: uppercase; }
        	.title { text-align: center; padding: 15px; }
        </style>
    </head>    
    <body>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:hasErrors bean="${cmd}">
			<div class="errors">
				<g:renderErrors bean="${cmd}" as="list" />
			</div>
		</g:hasErrors>
	   	<g:if test="${!params.print}">
			<div class="form" style="border-bottom: 1px solid lightgrey; border-top: 1px solid lightgrey; padding: 5px">
				<g:form controller="report" action="showTransactionReport" method="POST">
					<span class="filter">
						<label>
							<warehouse:message code="inventory.filterBy.category"/>
						</label>
						<%-- 
						${cmd.category }				
						<g:hiddenField name="category.id" value="${cmd?.category?.id }"/>
						--%>
						<select id="category.id" name="category.id" class="filter">
							<option value=""></option>
							<g:render template="../category/selectOptions" model="[category:cmd.rootCategory, selected:cmd.category, level: 0]"/>								
						</select>							
					</span>
					<span class="filter">
						<label>Location</label>
						<g:selectLocation class="filter" name="location.id" noSelection="['null':'']" value="${cmd?.location?.id}"/>
					</span>	
					<span class="filter">
						<label>Start date</label>
						<g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${cmd?.startDate }" format="MM/dd/yyyy"/>
					</span>					
					<span class="filter">
						<label>End date</label>
						<g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${cmd?.endDate }" format="MM/dd/yyyy"/>
					</span>
					<span class="filter">
						<button type="submit" class="btn">Run Report</button>
					</span>
				</g:form>				
			</div>
	    	<div class="right" style="padding: 5px;">
				<label>Export as</label>
	   			<g:link target="_blank" controller="report" action="showTransactionReport" params="[print:'true','location.id':cmd.location?.id,'category.id':cmd.category.id,'startDate':params.startDate,'endDate':params.endDate]">HTML</g:link> 
	   			&nbsp;|&nbsp;
	   			<g:link target="_blank" controller="report" action="downloadTransactionReport" params="[url:request.forwardURI,'location.id':cmd.location?.id,'category.id':cmd.category.id,'startDate':params.startDate,'endDate':params.endDate]">PDF</g:link>
			</div>
		</g:if>
		<g:else>
			<div class="title">Inventory Transaction Report</div>		
			<div class="left" style="padding: 5px;">
				<div>				
					<label>Location:</label>
					${cmd?.location?.name }
				</div>
				<div>
					<label>Category:</label>
					${cmd?.category?.parentCategory?.name } &#155;
					${cmd?.category?.name }
				</div>
				<div>
					<label>From:</label>
					${format.date(obj:cmd?.startDate)}
				</div>
				<div>
					<label>To:</label>
					${format.date(obj:cmd?.endDate)}
				</div>
			</div>
		</g:else>


		<g:set var="transferInLocations" value="${cmd?.inventoryReportEntryMap.values()*.quantityTransferredInByLocation*.keySet().flatten().unique()}"/>
		<g:set var="transferOutLocations" value="${cmd?.inventoryReportEntryMap.values()*.quantityTransferredOutByLocation*.keySet().flatten().unique()}"/>
		
    	<div class="list">
   			<g:set var="status" value="${0 }"/>
	    	<g:each var="productEntry" in="${cmd?.productsByCategory }" status="i">
	    		<g:set var="category" value="${productEntry.key }"/>
	    		<div style="page-break-after: always">		    		
			    	<table>
			    		<thead>
				    		<tr style="border-top: 1px solid lightgrey;">
								<th class="left">								
									${category?.parentCategory?.name?.encodeAsHTML() } /
				    				${category?.name?.encodeAsHTML() }					    			
					    			<g:if test="${!params.print }">
						    			[<g:link controller="report" action="showTransactionReport" params="['location.id':cmd.location?.id,'category.id':category.id,'startDate':cmd.startDate,'endDate':cmd.endDate]" style="display: inline">show</g:link>]
					    			</g:if>
								</th>
								<th class="center" style="border-right: 1px solid lightgrey">Start</th>
								<g:each var="location" in="${transferInLocations }">
									<th class="center">${location.name.substring(0,3) }</th>
								</g:each>
								<th class="center">Transfer In</th>	
								<th class="center" style="border-right: 1px solid lightgrey">Total In</th>	
								<g:each var="location" in="${transferOutLocations }">
									<th class="center">${location.name.substring(0,3) }</th>
								</g:each>
								<th class="center">Transfer Out</th>
								<th class="center">Expired</th>
								<th class="center">Consumed</th>
								<th class="center">Damaged</th>
								<th class="center" style="border-right: 1px solid lightgrey">Total Out</th>
								<th class="center nowrap">Found</th>	
								<th class="center">Lost</th>
								<th class="center" style="border-right: 1px solid lightgrey">Total Adjusted</th>
								<th class="center">End</th>
				    		</tr>
				    	</thead>
				    	<tbody>
					    	<g:each var="product" in="${productEntry.value }" status="j">
					    		<g:set var="entry" value="${cmd.inventoryReportEntryMap[product] }"/>
								<tr class="${status++%2 ? 'even' : 'odd' }">
									<td class="left">
										<g:if test="${!params.print }">
											<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">   	
									    		${product?.name.encodeAsHTML() }
								    		</g:link>
											[<g:link controller="report" action="showProductReport" params="['product.id':product?.id,'location.id':session?.warehouse?.id,startDate:params.startDate,endDate:params.endDate]" fragment="inventory">details</g:link>]
							    		</g:if>
							    		<g:else>
								    		${product?.name.encodeAsHTML() }
							    		</g:else>
						    		</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		<strong>${entry?.quantityInitial ?: 0}</strong>
						    		</td>
									<g:each var="location" in="${transferInLocations }">
										<td class="center">${entry.quantityTransferredInByLocation[location]?:0}</td>
									</g:each>
									<td class="center">	    	
							    		${entry?.quantityTransferredIn ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		${entry?.quantityTotalIn ?: 0}
									</td>
									<g:each var="location" in="${transferOutLocations }">
										<td class="center">${entry.quantityTransferredOutByLocation[location]?:0}</td>
									</g:each>
									<td class="center">	    	
										${entry?.quantityTransferredOut ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityExpired ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityConsumed ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityDamaged ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">
										${entry?.quantityTotalOut ?: 0 }
									</td>
									<td class="center">	    	
							    		${entry?.quantityFound ?: 0}
									</td>
									<td class="center">	    	
							    		${entry?.quantityLost ?: 0}
									</td>
									<td class="center" style="border-right: 1px solid lightgrey">	    	
							    		${entry?.quantityTotalAdjusted ?: 0}
									</td>
									<td class="center">	    	
							    		<strong>${entry?.quantityFinal ?: 0}</strong>
									</td>
						    	</tr>
					    	</g:each>
						</tbody>
					</table>
				</div>
			</g:each>
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