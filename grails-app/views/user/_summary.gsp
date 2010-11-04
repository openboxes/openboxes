				<div class="${(userInstance?.active) ? 'active':'inactive'}">
					<table>
						<tr>
							<td>
								<g:if test="${userInstance.photo}">
		            					<img class="photo" width="25" height="25" 
		            						src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" 
		            						style="vertical-align: middle" />
		            				</g:if>
		            				<g:else>
		            					<g:if test="${userInstance?.active}">
			            					<img class="photo" src="${resource(dir: 'images/icons', file: 'profile.png') }"
			            						style="vertical-align: middle;" />
		            					</g:if>
		            					<g:else>
			            					<img class="photo" src="${resource(dir: 'images/icons', file: 'profile.png') }"
			            						style="vertical-align: bottom;" />
		            					</g:else>
		            				</g:else>
			            			<span style="font-weight: bold; font-size: 1.2em;">
			            				${fieldValue(bean: userInstance, field: "firstName")} 
			            				${fieldValue(bean: userInstance, field: "lastName")}</span>
							
							</td>
							<td style="text-align: right;">
								<div style="font-size: 1.2em">
									<g:if test="${userInstance?.active}">
										<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'tick.png') }"
		            						style="vertical-align: bottom;" />
									</g:if>
									<g:else>
		            					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'stop.png') }"
		            						style="vertical-align: bottom;" />
									</g:else>
									<b>${userInstance?.active?'Active':'Inactive'}</b>
								</div>
							</td>
						</tr>
					</table>
				</div>
