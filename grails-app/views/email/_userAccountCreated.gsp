<%@ page contentType="text/html"%>
<g:applyLayout name="email">
	${warehouse.message(code: 'email.userAccountCreated.message', args: [userInstance.username])}
	
	<g:link controller="user" action="show" id="${userInstance?.id }" absolute="true">
		${warehouse.message(code: 'email.link.label', args: [userInstance?.username])}
	</g:link>
	
</g:applyLayout>
