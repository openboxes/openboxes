<div class="box">
    <h2>
        <div class="action-menu" style="position:absolute;top:5px;right:5px">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${!params.daysToInclude || params.daysToInclude.equals('3')?'selected':''}" params="[daysToInclude:3]">
                        <img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        <warehouse:message code="dashboard.lastThreeDays.label" default="Last 3 days"/></g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${params.daysToInclude.equals('7')?'selected':''}" params="[daysToInclude:7]">
                        <img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        <warehouse:message code="dashboard.lastWeek.label" default="Last week"/></g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${params.daysToInclude.equals('30')?'selected':''}" params="[daysToInclude:30]">
                        <img src="${resource(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        <warehouse:message code="dashboard.lastMonth.label" default="Last month"/>
                    </g:link>

                </div>
            </div>
        </div>


        <warehouse:message code="dashboard.activity.label" args="[session.warehouse.name]"/>
    </h2>

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
								<img src="${resource(dir:'images/icons/silk',file: activity.type + '.png')}" class="middle"/>
							</td>
							<td class="middle">
		 						<div>${raw(activity.label)}</div>
                             </td>
                            <td class="nowrap middle">
                                <div class='fade'>${format.date(obj:activity.lastUpdated,format:'MMM d hh:mma')}</div>
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
			<g:paginate total="${activityListTotal}" params="[daysToInclude:params.daysToInclude]"/>
		</div>
	</div>
</div>	
<script>
	$(function() { 		
		$('.nailthumb-container img').nailthumb({width : 20, height : 20});
	});
</script>
