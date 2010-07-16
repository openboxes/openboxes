<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle">${message(code: 'default.dashboard.label', default: 'Dashboard')}</content>		
		<content tag="pageTitle">${message(code: 'default.dashboard.label', default: 'Dashboard')}</content>
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
						You are logged into the <b>${session.warehouse?.name}</b> warehouse as a <b>${session.user.role}</b>. 
					</p>
				</g:else>
	    	</div>
		</div>
    </body>
</html>

