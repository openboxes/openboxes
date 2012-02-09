				<div class="${(userInstance?.active) ? 'active':'inactive'}">
					<table>
						<tr>
							<td style="width: 128px">
								<div class="left">
									<g:if test="${userInstance.photo}">
			            					<img class="photo" height="128" width="128"
			            						src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" 
			            						style="vertical-align: middle" />
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
	            				</div>
	            			</td>
	            			<td class="top">
	            				<table>
	            					<tr>
	            						<td>
											<span class="action-menu">
												<button class="action-btn">
													<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
													<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
												</button>
												<div class="actions">
													<div class="action-menu-item">		
														<g:link class="list" action="list">
															${warehouse.message(code: 'user.list.label')}
														</g:link>
													</div>
													<div class="action-menu-item">
														<hr/>
													</div>
													<div class="action-menu-item">		
														<g:link class="edit" action="edit" id="${userInstance?.id}">
															${warehouse.message(code: 'default.button.edit.label')}
														</g:link>
													</div>
													<div class="action-menu-item">		
														<g:link class="delete" action="delete" id="${userInstance?.id}"
															onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
															${warehouse.message(code: 'default.button.delete.label')}
														</g:link>
													
													</div>
													<div class="action-menu-item">		
														<g:link action="toggleActivation" id="${userInstance?.id}">
															<g:if test="${userInstance?.active}">
																${warehouse.message(code: 'user.deactivate.label')}
															</g:if>
															<g:else>
																${warehouse.message(code: 'user.activate.label')}
															</g:else>
														</g:link>
													
													</div>
				            						<g:if test="${params.action=='show'}">
														<div class="action-menu-item">
						            						<g:link controller="user" action="changePhoto" id="${userInstance?.id }">
						            							<warehouse:message code="user.changePhoto.label"/>
						            						</g:link>
													
														</div>
													</g:if>
													
												</div>
											</span>
											&nbsp;				
					            			<span style="font-weight: bold; font-size: 1.2em; color: grey;">
					            				${fieldValue(bean: userInstance, field: "firstName")} 
					            				${fieldValue(bean: userInstance, field: "lastName")}
					            			</span>
												            			
					            			
					            			
	            						</td>
	            					
	            					</tr>	            				
	            				</table>
							</td>							
							<td style="text-align: right;">
							
		            			<div>
									<warehouse:message code="default.status.label"/>:
									<b>
									${userInstance?.active ? warehouse.message(code: 'user.active.label') : warehouse.message(code: 'user.inactive.label')}
									</b>
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
						</tr>
					</table>
				</div>
