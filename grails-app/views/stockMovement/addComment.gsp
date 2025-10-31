<%@ page import="org.pih.warehouse.inventory.StockMovementStatusCode" %>
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
		<g:hasErrors bean="${stockMovement}">
			<div class="errors">
				<g:renderErrors bean="${stockMovement}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${comment}">
			<div class="errors">
				<g:renderErrors bean="${comment}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">
			<g:render template="summary" model="[stockMovement:stockMovement]" />
			<div class="box">
				<h2><warehouse:message code="default.add.label" args="[entityName]" /></h2>

				<g:set var="formAction"  value="saveComment" />
				<g:set var="isRequestRejected" value="${approvalStatus == StockMovementStatusCode.REJECTED}" />
				<g:if test="${comment?.id}">
					<g:set var="formAction"  value="updateComment" />
				</g:if>
				<g:elseif test="${isRequestRejected}">
					<g:set var="formAction" value="updateStatus" />
				</g:elseif>
				<g:form action="${formAction}">
					<g:if test="${isRequestRejected}">
						<g:hiddenField name="status" value="${StockMovementStatusCode.REJECTED}" />
						<g:hiddenField name="id" value="${stockMovement?.id}" />
					</g:if>
					<g:if test="${comment?.id}">
						<g:hiddenField name="id" value="${comment?.id}" />
					</g:if>
					<g:hiddenField name="stockMovementId" value="${stockMovement?.id}" />

					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="comment.recipient.label" /></label></td>
								<td valign="top" class="value ${hasErrors(bean: comment, field: 'recipient', 'errors')}">
									<div style="width:300px">
										<g:selectUser name="recipient.id" value="${comment?.recipient?.id}"
													  noSelection="['null':'']" class="chzn-select-deselect"/>
									</div>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="comment.sender.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: comment, field: 'sender', 'errors')}">
									<g:hiddenField name="sender.id" value="${session.user.id }"/>
									 ${session.user.firstName} ${session.user.lastName} <span class="fade">(${session.user.username})</span>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="default.comment.label"/></label></td>
								<td valign="top" class="value ${hasErrors(bean: comment, field: 'comment', 'errors')}">
									<g:textArea required="true" name="comment" cols="100" rows="10" value="${comment?.comment }"/>
								</td>
							</tr>
						</tbody>
					</table>
					<div class="buttons">
						<button type="submit" class="button icon approve">
							<g:if test="${isRequestRejected}">
								<warehouse:message code="request.confirmReject.label" />
							</g:if>
							<g:else>
								<warehouse:message code="default.button.save.label"/>
							</g:else>
						</button>
						<g:link controller="stockMovement" action="show" id="${stockMovement?.id}" class="button icon trash">
							<warehouse:message code="default.button.cancel.label"/></g:link>
					</div>

				</g:form>
			</div>
		</div>
	</div>
</body>
</html>
