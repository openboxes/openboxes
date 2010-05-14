<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'user.preferences.label', default: 'User Preferences')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        
        <content tag="localLinks">
			<span class="menuButton">
				<g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link>
			</span>     
        </content>
		<content tag="globalLinks"><!-- Specify global navigation links -->
		</content>
		<content tag="pageTitle"><!-- Specify page title -->
	    	<g:message code="default.list.label" args="[entityName]" />
		</content>
    </head>
    <body>
		<div class="body">
	    	<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
            <div class="list">
                
            </div>
        </div>
    </body>
</html>
