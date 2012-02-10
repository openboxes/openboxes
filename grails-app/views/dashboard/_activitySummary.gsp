<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="activity.label" args="[session.warehouse.name]"/></h2>
	</div>	    			
	<div class="widget-content" style="margin: 0;">	    					    			
		<%-- 	
		<div style="padding: 10px">
			There are ${activityList.size() } recent activities.		
		</div>
		--%>
		<div id="activity-summary" style="height: 200px; overflow: auto; padding: 0;">	
			<table style="padding: 0;">
				<g:set var="status" value="${0 }"/>
	 			<g:each var="entry" in="${activityList.groupBy { format.date(obj:it.lastUpdated,format:'EEEEE, dd MMMM yyyy') } }" status="i">
	 				<tr>
	 					<td>
	 						<div style="font-weight: bold; padding-left: 15px ">${entry.key }</div>
	 					</td>
	 				</tr>
	 				<g:each var="activity" in="${entry.value }">
		 				<tr class="${status++%2?'even':'odd' }">
		 					<td>
		 						<span style="padding-left: 15px;">
									<img
										src="${createLinkTo(dir:'images/icons/silk',file: activity.type + '.png')}" class="middle"/>
										
			 						<span class='fade'>${format.date(obj:activity.lastUpdated,format:'hh:mm a')}</span>
			 						${activity.label } 
			 						<%--[<a href="${activity.url}">details</a>]--%>
		 						</span>
		 					</td>
		 				</tr>
		 			</g:each>
	 			</g:each>
	 			<g:unless test="${activityList }">
					<tr>
						<td>
							<warehouse:message code="dashboard.noActivityFound.message"/>
						</td>
					</tr>	 			
	 			</g:unless>
	 			
			</table>			
		</div>
		
		
	</div>
	<br clear="all"/>	    			
</div>	