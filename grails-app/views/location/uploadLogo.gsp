
<%@ page import="org.pih.warehouse.core.Location"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'location.label', default: 'Location')}" />
<title><warehouse:message code="default.show.label" args="[entityName]" /></title>

</head>
<body>
	<div class="body">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:form controller="location" method="post"
			action="uploadLogo" enctype="multipart/form-data">
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
								<td valign="top" class="name"><label><warehouse:message
										code="location.currentLogo.label" default="Current logo" /></label>
								</td>
								<td>
									<g:if test="${locationInstance?.logo }">
										<img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:locationInstance?.id)}" class="middle" />
										<g:link controller="location" action="deleteLogo" id="${locationInstance?.id }" class="button icon trash">
											${warehouse.message(code:'default.button.delete.label') }
										</g:link>						
									</g:if>						
									<g:else>										
										<span class="fade"><warehouse:message code="default.none.label"/></span>
									</g:else>
								</td>
							</tr>						
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message
										code="location.logo.label" /></label>
								</td>
								<td>
									<input type="hidden" name="id" value="${locationInstance.id}" />
									<input type="file" name="logo" />
								
						
								</td>
							</tr>
							<tr class="prop">
								<td class="name"></td>
								<td class="value">
									<div class="buttons left">
										<input class="button icon approve"
											type="submit"
											value="${warehouse.message(code:'default.button.upload.label')}" />									
										&nbsp;	
										<g:link class="show" action="show" id="${locationInstance}">
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
