<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${message(code: 'default.home.label', default: 'Home')}</title>
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
	    	<div id="home" class="dialog">				
				<g:if test="${!session.user}">
		    		<h2>You are not authorized to access this page.</h2>
		    		<br/>
					<p>
						Please <a class="home" href="${createLink(uri: '/auth/login')}">login</a> to gain access
					</p>
				</g:if>
				This will appear if hudson worked<br/>
				<g:else>
					<h2>Welcome, <b>${session.user.firstName} ${session.user.lastName}</b>!</h2>
					<br/>
					<p class="large" style="width: 65%" align="justify">
						You are logged into the <b>${session.warehouse?.name}</b> warehouse as a <b>${session.user.role}</b></b>.  
						This page will be the future home of the system's role-based dashboard.  
						For now, please click on one of the actions below.	
					</p>
					<br/><br/>
					<ul>
						<li><span class="menuButton"><g:link class="shipment" controller="shipment" action="list"><g:message code="default.list.label"  args="['all Shipment']"/></g:link></span></li>		
						<li>&nbsp;</li>
						<li><span class="menuButton"><g:link class="create" controller="shipment" action="create"><g:message code="default.create.label" args="['a new Shipment']" default="Create a new shipment" /></g:link></span></li>						
					</ul>
				</g:else>
	    	</div>
		</div>
    </body>
</html>

