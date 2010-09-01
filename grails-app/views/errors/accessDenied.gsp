<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'accessDenied.label', default: 'Access Denied')}" />
	<title>Access Denied</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Access Denied</content>
	
	
</head>

<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div class="error">
			Access to action <b>${actionName}</b> has not been granted to user <b>${session.user.username}</b>.  
			Please email your system administrator <b>${session.user.manager}</b>
			
		</div>
		
		
		
		
	</div>
</body>