
<%@ page import="org.pih.warehouse.core.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'user.label', default: 'User')}" />
<title><warehouse:message code="default.show.label"
		args="[entityName]" /></title>
<content tag="pageTitle">
<warehouse:message code="default.show.label" args="[entityName]" /></content>

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
			<fieldset>
				<div class="dialog">
					<table>
						<thead>
							<tr>
								<td colspan="2"><g:render template="summary" /></td>
							</tr>
						</thead>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><warehouse:message
										code="user.photo.label" />
								</td>
								<td>								
									<img id="thumb"
	            						src="${createLink(controller:'user', action:'viewThumb', id:userInstance.id)}" 
	            						style="vertical-align: middle" />

									<img id="image"
	            						src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" 
	            						style="vertical-align: middle" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"></td>
								<td class="value">
									<div class="buttons left">
										<input class="positive"
											type="submit"
											value="${warehouse.message(code:'default.button.update.label')}" />									
										&nbsp;	
										<g:link class="show" action="show" id="${userInstance?.id}">
											${warehouse.message(code: 'default.button.cancel.label')}
										</g:link>
									</div>
	
								</td>
							</tr>
	
						</tbody>
					</table>
				</div>
			</fieldset>
		</g:form>
		
	</div>
</body>
</html>
