<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'default.comment.label', default: 'Comment').toLowerCase()}" />
	<title><warehouse:message code="default.add.label" args="[entityName]" /></title>	
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderInstance}">
			<div class="errors">
				<g:renderErrors bean="${orderInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${commentInstance}">
			<div class="errors">
				<g:renderErrors bean="${commentInstance}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">
			<g:render template="summary" model="[orderInstance:orderInstance]" />
			<div class="box">
				<h2><warehouse:message code="default.add.label" args="[entityName]" /></h2>
				<g:form action="saveComment">
					<g:hiddenField name="id" value="${commentInstance?.id}" />
					<g:hiddenField name="order.id" value="${orderInstance?.id}" />
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="comment.recipient.label" /></label></td>
								<td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'recipient', 'errors')}">
									<div style="width:300px">
										<g:select id="recipient.id" class="chzn-select-deselect" name='recipient.id' noSelection="${['':'Select one ...']}"
												  from='${org.pih.warehouse.core.User.list()}' optionKey="id" optionValue="name" value="${commentInstance?.recipient?.id }"></g:select>
									</div>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="comment.sender.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'sender', 'errors')}">
									<g:hiddenField name="sender.id" value="${session.user.id }"/>
									 ${session.user.firstName} ${session.user.lastName} <span class="fade">(${session.user.username})</span>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.comment.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
									<g:textArea name="comment" cols="100" rows="10" value="${commentInstance?.comment }"/>
								</td>
							</tr>
						</tbody>
					</table>
					<div class="buttons">
						<button type="submit" class="button icon approve">
							<warehouse:message code="default.button.save.label"/></button>
						<g:link controller="order" action="show" id="${orderInstance?.id}" class="button icon trash">
							<warehouse:message code="default.button.cancel.label"/></g:link>
					</div>

				</g:form>
			</div>
		</div>
	</div>
</body>
</html>
