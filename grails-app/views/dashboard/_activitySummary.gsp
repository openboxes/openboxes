<div class="widget-large">
	<div class="widget-header">
		<h2>
			<warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
			&rsaquo;
			<span class="fade">		 					
				<warehouse:message code="dashboard.showRecentActivity.message" args="[activityList?.size()]"/>
			</span>
		
		</h2>
	</div>	    			
	<div class="widget-content" style="padding: 0; margin: 0">	    					    			
		<%-- 	
		<div style="padding: 10px">
			There are ${activityList.size() } recent activities.		
		</div>
		--%>
		<div id="activity-summary" style="max-height: 300px; overflow: auto; padding: 0px;">	
			<table>
				<tbody>
					<g:set var="status" value="${0 }"/>
		 			<g:each var="activity" in="${activityList }" status="i">
		 				<tr class="${status++%2?'odd':'even' } prop">
		 					<td>
								<img src="${createLinkTo(dir:'images/icons/silk',file: activity.type + '.png')}" class="middle"/> 
							</td>
							<td>
		 						<div>${activity.label }</div> 
		 						<span class='fade'>${format.date(obj:activity.lastUpdated,format:'dd MMM hh:mm a')}</span>
		 					
		 					</td>
		 				</tr>
		 			</g:each>
		 			<g:unless test="${activityList }">
						<tr class="prop">
							<td>
								<warehouse:message code="dashboard.noActivityFound.message"/>
							</td>
						</tr>	 			
		 			</g:unless>
		 		</tbody>	 			
			</table>			
		</div>
	</div>
</div>	