<div class="widget-large">
	<div class="widget-header">
		<h2>
			<warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
		</h2>
	</div>	    			
	<div class="widget-content" style="padding: 0; margin: 0">	    					    			
		<%-- 	
		<div style="padding: 10px">
			There are ${activityList.size() } recent activities.		
		</div>
		--%>
		<div id="activity-summary" >	
		
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<div class="fade">
								<warehouse:message code="dashboard.showing.message" args="[startIndex+1,endIndex+1,activityListTotal,daysToInclude]"/>
							</div>
						</td>
					</tr>
					<g:set var="status" value="${0 }"/>
		 			<g:each var="activity" in="${activityList }" status="i">
		 				<tr class="${status++%2?'odd':'even' } prop">
		 					<td class="center top">
		 						<%--
			 					<div class="nailthumb-container">
									<img src="${resource(dir: 'images', file: 'default-user2.png')}" />		
								</div>
								--%>
								<img src="${createLinkTo(dir:'images/icons/silk',file: activity.type + '.png')}" class="middle"/> 
							</td>
							<td class="top">
		 						<div>${activity.label }</div> 
		 						<span class='fade'>${format.date(obj:activity.lastUpdated,format:'MMMMM dd hh:mm a')}</span>
		 					
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
		<div class="paginateButtons">
			<g:paginate total="${activityListTotal}" />
		</div>
	</div>
</div>	
<script>
	$(function() { 		
		$('.nailthumb-container img').nailthumb({width : 20, height : 20});
	});
</script>
