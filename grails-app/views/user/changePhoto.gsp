
<%@ page import="org.pih.warehouse.core.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'user.label', default: 'User')}" />
<title><warehouse:message code="default.show.label" args="[entityName]" /></title>

</head>
<body>
	<div class="body">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:form controller="user" method="post"
			action="uploadPhoto" enctype="multipart/form-data">
            <g:render template="summary" />
            <div class="dialog box">
				<h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.photo.label" />
							</td>
							<td>
								<input type="hidden" name="id" value="${userInstance.id}" />
								<input type="file" name="photo" />
							</td>
						</tr>
						<tr class="prop">
							<td class="name"></td>
							<td class="value">
								<div class="buttons left">
									<input class="button"
										type="submit"
										value="${warehouse.message(code:'default.button.upload.label')}"/>

									<g:link action="show" id="${userInstance?.id}" class="button">
										${warehouse.message(code: 'default.button.cancel.label')}
									</g:link>
								</div>

							</td>
						</tr>

					</tbody>
				</table>
			</div>
		</g:form>
		
	</div>
</body>
</html>
