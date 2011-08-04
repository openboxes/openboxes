<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="shipping.addComment.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="shipping.addComment.label"/></content>
</head>

<body>

	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>
			
		<table>		
			<tr>
				<td>
					<fieldset>
						<g:render template="summary" />

						
							<g:form action="saveComment">
								<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
										<tr class="prop">
				                            <td valign="top" class="name"><label><warehouse:message code="default.to.label" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'recipient', 'errors')}">
												<g:select id="recipientId" name='recipientId' noSelection="['':warehouse.message(code:'default.selectOne.label')]" 
			                                    	from='${org.pih.warehouse.core.User.list()}' optionKey="id" optionValue="username"></g:select>
			                                </td>
				                        </tr>  	          
										<tr class="prop">
				                            <td valign="top" class="name"><label><warehouse:message code="default.from.label" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'sender', 'errors')}">
			                                    ${session.user.username}
			                                </td>
				                        </tr>  	          
										<tr class="prop">
				                            <td valign="top" class="name"><label><warehouse:message code="default.comment.label" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
			                                    <g:textArea name="comment" cols="60" rows="10"/>
			                                </td>
				                        </tr>  	        
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> <warehouse:message code="default.button.add.label"/></button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> <warehouse:message code="default.button.cancel.label"/> </g:link>
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
</body>
</html>
