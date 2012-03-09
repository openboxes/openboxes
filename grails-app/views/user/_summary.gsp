				<div class="${(userInstance?.active) ? 'active':'inactive'}">
					<table>
						<tr>
	            			<td class="top" style="width:50px;">
								<g:render template="actions"/>
							</td>
							
							<td>
		            			<span style="font-weight: bold; font-size: 1.2em; color: grey;">
		            				${fieldValue(bean: userInstance, field: "firstName")} 
		            				${fieldValue(bean: userInstance, field: "lastName")}
		            			</span>
		            			
								<div >
									<warehouse:message code="default.status.label"/>:
									<b>${userInstance?.active ? warehouse.message(code: 'user.active.label') : warehouse.message(code: 'user.inactive.label')}</b>
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
								<g:if test="${userInstance.photo}">
									<g:link action="viewPhoto" id="${userInstance?.id }">
		            					<img
		            						src="${createLink(controller:'user', action:'viewThumb', id:userInstance.id)}" 
		            						style="vertical-align: middle" />
	            					</g:link>
	            				</g:if>
	            				<g:else>
	            					<g:if test="${userInstance?.active}">
		            					<img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
		            						style="vertical-align: bottom;" />
		            						
	            					</g:if>
	            					<g:else>
		            					<img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
		            						style="vertical-align: bottom;" />
	            					</g:else>
	            				</g:else>
	            			</td>
							
						</tr>
						
					</table>
				</div>
