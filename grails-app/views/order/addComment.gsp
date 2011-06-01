<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>	
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
		<table>		
			<tr>
				<td>
					<fieldset>
						<g:render template="header" model="[orderInstance:orderInstance]" />
							<g:form action="saveComment">
								<g:hiddenField name="id" value="${commentInstance?.id}" />
								<g:hiddenField name="order.id" value="${orderInstance?.id}" />
								<table>
									<tbody>
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.recipient.label" default="To" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'recipient', 'errors')}">
												<g:select id="recipient.id" class="combobox" name='recipient.id' noSelection="${['':'Select one ...']}" 
			                                    	from='${org.pih.warehouse.core.User.list()}' optionKey="id" optionValue="name" value="${commentInstance?.recipient?.id }"></g:select>
			                                </td>
				                        </tr>  	          
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.sender.label" default="From" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'sender', 'errors')}">
				                            	<g:hiddenField name="sender.id" value="${session.user.id }"/>
			                                     ${session.user.firstName} ${session.user.lastName} <span class="fade">(${session.user.username})</span>
			                                </td>
				                        </tr>  	          
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.comment.label" default="Comment" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
			                                    <g:textArea name="comment" cols="60" rows="10" value="${commentInstance?.comment }"/>
			                                </td>
				                        </tr>  	        
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.dateCreated.label" default="Date created" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'dateCreated', 'errors')}">
												${commentInstance?.dateCreated }
			                                </td>
				                        </tr>  	        
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="comment.lastUpdated.label" default="Last updated" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'lastUpdated', 'errors')}">
												${commentInstance?.lastUpdated}
			                                </td>
				                        </tr>  	        
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" 
															alt="Save" />Save</button>
													<g:link controller="order" action="show" id="${orderInstance?.id}" class="negative">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" />Cancel</g:link>
												</div>				
											</td>
										</tr>
								</tbody>
							</table>
						</g:form>
					</fieldset>
				</td>
			</tr>
		</table>
	</div>
	<g:comboBox />
</body>
</html>
