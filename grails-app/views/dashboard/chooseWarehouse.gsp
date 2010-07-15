<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.home.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle">${message(code: 'default.home.label', default: 'Home')}</content>		
		<content tag="pageTitle">${message(code: 'default.home.label', default: 'Home')}</content>
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>
    </head>
    <body>        
		<div class="body">		
	    	<div id="dashboard">				
				<g:if test="${!session.user}">
					<p>Welcome! Please <a class="home" href="${createLink(uri: '/auth/login')}">login</a> to gain access</p>
				</g:if>
				<g:else>
					<p class="large" align="justify">				
						<span class="greeting">Welcome, <b>${session.user.firstName} ${session.user.lastName}</b>!</span>
						You are logged into the <b>${session.warehouse?.name}</b> warehouse as a <b>${session.user.role}</b></b>.  
						This page will be the future home of the system's role-based dashboard.  
						For now, please click on one of the actions below.	
					</p>
					<br/><br/>
					<table class="withoutBorder menu">
						<tbody>
							<tr>
								<th>Please choose your warehouse</th>								
							</tr>							
							<g:each var="warehouse" in="${org.pih.warehouse.inventory.Warehouse.list()}">								
								<tr>
									<td>
										<a class="home" href='${createLink(action:"chooseWarehouse", id: warehouse.id)}'>
											<div style="padding: 15px; background-color: #F8F7EF; display: block;">
												<g:if test="${warehouse.logoUrl}"><img src="${warehouse.logoUrl}" width="24" height="24" style="vertical-align: middle"></img></g:if>
												${warehouse.name} 											
											</div>
										</a>
									</td>
								</tr>
							</g:each>							
						</tbody>					
					</table>
					


				</g:else>
	    	</div>
		</div>
    </body>
</html>

