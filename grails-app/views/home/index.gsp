<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle">Home</content>		
		<content tag="pageTitle">Home</content>
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>		
    </head>
    <body>        
		<div class="body">		
	    	<div id="dashboard">	    	
				<g:if test="${!session.user}">
					<p>
						<%-- <g:render template="../common/login"/>--%>
						(user is not currently logged in)
					</p>
				</g:if>
				<g:else>
					<p>
						You are logged into ${session.warehouse.name} as ${session.user.username}.  Please select an option from the menu above.  
						This could be potentially be the future home for a role-based dashboard (${session.user.role}).						
					</p>
				</g:else>
	    	</div>
		</div>
    </body>
</html>

