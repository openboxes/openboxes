<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	<g:set var="activatedOrDeactivated" value="${userInstance.active ? warehouse.message(code:'user.activated.label'):warehouse.message(code:'user.disabled.label')  }"/>
	${warehouse.message(code: 'email.userAccountActivated.message', args: [userInstance.username,activatedOrDeactivated])}
	
	<g:link controller="user" action="show" id="${userInstance?.id }" absolute="true">
		${warehouse.message(code: 'email.link.label', args: [userInstance?.username])}
	</g:link>
	
</g:applyLayout>
