<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.chooseWarehouse.label', default: 'Choose a warehouse')}</title>
    </head>
    <body>        
		<div class="body">		

	    	<div id="chooseWarehouse">				

				<g:if test="${!session.user}">
					<p>Welcome! Please <a class="home" href="${createLink(uri: '/auth/login')}">login</a> to gain access</p>
				</g:if>
				<g:else>
					<g:if test="${flash.message}">
					    <div class="message">${flash.message}</div>
					</g:if>		


					<fieldset> 		
						<legend>Choose a warehouse</legend>
						<table>
							<tbody>
								<tr>
									<td>
										<g:if test="${session?.user?.warehouse}">
											<span style="width: 100%; text-align: right; color: #aaa">
												Last login: <b>${session?.user?.warehouse}</b> on
												<b><g:formatDate format="dd MMM yyyy hh:mm" date="${session?.user?.lastLoginDate}"/></b> 
											</span>
										</g:if>
									</td>
								</tr>
									<g:each var="warehouse" in="${warehouses}" status="i">								
										<tr>
											<td nowrap="nowrap">
												<div style="padding: 5px; background-color: #F8F7EF; display: block;">												
													<a style="display: block;" class="home" href='${createLink(action:"chooseWarehouse", id: warehouse.id)}'>
														<g:if test="${warehouse.logoUrl}"><img src="${warehouse.logoUrl}" width="24" height="24" style="vertical-align: middle; padding: 5px;"></img></g:if>
														${warehouse.name}
													</a> 
												</div>												
											</td>											
										</tr>																		
									</g:each>							
							</tbody>					
						</table>
					</fieldset>					


				</g:else>
	    	</div>
		</div>
    </body>
</html>

