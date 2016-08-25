				<div class="${(userInstance?.active) ? 'active':'inactive'} summary">
					<table>
						<tr>
	            			<td class="top" width="1%">
								<g:render template="actions"/>
							</td>
							<td width="1%">
                                <div class="nailthumb-container">
                                    <g:userPhoto user="${userInstance}"/>
                                </div>
							</td>
							<td width="10%">
		            			<span style="font-weight: bold; font-size: 1.2em; color: grey;">
		            				${fieldValue(bean: userInstance, field: "firstName")} 
		            				${fieldValue(bean: userInstance, field: "lastName")}
		            			</span>
		            			
								<div class="fade">
									${userInstance?.email}
								</div>
							
								<%-- 
								<span style="font-size: 1.2em">
									<b>${userInstance?.active ? warehouse.message(code: 'user.active.label') : warehouse.message(code: 'user.inactive.label')}</b>
									<g:if test="${userInstance?.active}">
										<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'status_online.png') }"
		            						style="vertical-align: bottom;" />
									</g:if>
									<g:else>
		            					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'status_offline.png') }"
		            						style="vertical-align: bottom;" />
									</g:else>
								</span>
								--%>		            			
							</td>							
						
							<td class="top right">
                                <div class="right">
                                    <span class="tag">
                                        ${userInstance?.active ? warehouse.message(code:'user.active.label') : warehouse.message(code:'user.inactive.label')}
                                    </span>
                                </div>
	            			</td>
							
						</tr>
						
					</table>
				</div>

                <script src="${resource(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
                <script>
                    $(document).ready(function() {
                        $('.nailthumb-container').nailthumb({ width: 24, height: 24 });
                    });
                </script>