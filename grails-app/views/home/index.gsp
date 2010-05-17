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
		<content tag="breadcrumb"><g:render template="breadcrumb" model=""/></content>		
    </head>
    <body>        
		<div class="body">		
	    	<div id="home" class="dialog" style="width:50%">
	    	
	    		
	    			    		
				<g:if test="${flash.message}">
				    <div class="message">${flash.message}</div>
				</g:if>
	    	
				<g:if test="${!session.user}">
		    		<h1>You are not authorized to access this page.</h1>
					<p>
						<%-- <g:render template="../common/login"/>--%>
						Please <a class="home" href="${createLink(uri: '/home/index')}">login</a> to gain access
					</p>
				</g:if>
				<g:else>
					<h1>You are logged in</h1>
					<p>
						You are logged as <b>${session.user.username}</b> @ <b>${session.warehouse?.name}</b> as role <b>${session.user.role}</b></b>.  Please select an option from the menu above.  
						This could be potentially be the future home for a role-based dashboard.						
					</p>
				</g:else>
	    	</div>
		</div>
    </body>
</html>

