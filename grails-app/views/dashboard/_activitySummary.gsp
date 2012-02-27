<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/></h2>
	</div>	    			
	<div class="widget-content">	    					    			
		<%-- 	
		<div style="padding: 10px">
			There are ${activityList.size() } recent activities.		
		</div>
		--%>
		<div id="activity-summary" style="max-height: 150px; overflow: auto;">	
			<table>
				<tbody>
					<g:set var="status" value="${0 }"/>
		 			<g:each var="activity" in="${activityList }" status="i">
		 				<tr class="${status++%2?'even':'odd' }">
		 					<td>
		 						<span>										
									<img src="${createLinkTo(dir:'images/icons/silk',file: activity.type + '.png')}" class="middle"/> 
									&nbsp; 					
			 						<span class='fade'>${format.date(obj:activity.lastUpdated,format:'dd MMM hh:mm a')}</span>
			 						${activity.label } 
			 						[<a href="${activity.url}">details</a>]
		 						</span>
		 					</td>
		 				</tr>
		 			</g:each>
		 			<g:unless test="${activityList }">
						<tr>
							<td>
								<warehouse:message code="dashboard.noActivityFound.message"/>
							</td>
						</tr>	 			
		 			</g:unless>
		 		</tbody>	 			
			</table>			
		</div>
		<div>
			<table>
				<tfoot>
		 			<tr>
		 				<td>	
		 					<span class="fade">		 					
				 				<warehouse:message code="dashboard.showRecentActivity.message" args="[activityList?.size()]"/>
			 				</span>
		 				</td>
		 			</tr>
		 		</tfoot>
			</table>
		</div>
	</div>
</div>	