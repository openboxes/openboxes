<div class="widget-large">
	<div class="widget-header">
		<h2>
			<warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
            <div style="float: right" class="fade">
                <g:link controller="dashboard" action="index" class="${!params.daysToInclude || params.daysToInclude.equals('3')?'selected':''}" params="[daysToInclude:3]">Last 3 days</g:link> |
                <g:link controller="dashboard" action="index" class="${params.daysToInclude.equals('7')?'selected':''}" params="[daysToInclude:7]">Last week</g:link> |
                <g:link controller="dashboard" action="index" class="${params.daysToInclude.equals('30')?'selected':''}" params="[daysToInclude:30]">Last month</g:link>
            </div>
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
                    <g:if test="${activityList}">
                        <tr>
                            <td colspan="2">
                                <div class="fade">
                                    <warehouse:message code="dashboard.showing.message" args="[startIndex+1,endIndex+1,activityListTotal,daysToInclude]"/>
                                </div>
                            </td>
                        </tr>
                    </g:if>
					<g:set var="status" value="${0 }"/>
		 			<g:each var="activity" in="${activityList }" status="i">
		 				<tr class="${status++%2?'even':'odd' } prop">
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
						<tr class="">
							<td class="center">
								<span class="fade"><warehouse:message code="dashboard.noActivityFound.message"/></span>
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
