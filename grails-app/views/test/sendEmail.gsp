<html>
	<head>
    	<title>Test Email Service</title>
		<meta name="layout" content="custom"/>
	</head> 
	<body>
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}		
			</div>
		</g:if>
		<g:form action="sendEmail" method="POST">
			<fieldset>
				<table>
					<tr class="prop">
						<td class="name">
							<label for="to">To:</label>
						</td>
						<td class="value">
							<%-- 
							<g:textField name="to" size="30" />
							--%>
							<g:select name="to" from="${org.pih.warehouse.core.User.list().sort() }" optionKey="email" 
								optionValue="${{it?.name + ' : ' + it?.email }}" value="justin.miranda@gmail.com"/>
							
						</td>
					</tr> 
					<tr class="prop">
						<td class="name">
							<label for="subject">Subject:</label>
						</td>
						<td class="value">
							<g:textField name="subject" size="50" value="Test subject"/>	
						</td>
					</tr> 
					<tr class="prop">
						<td class="name">
							<label for="htmlMsg">HTML Message:</label>
						</td>
						<td class="value">
							<g:textArea name="htmlMsg" cols="60" rows="5"/>		
						</td>
					</tr> 
					<tr class="prop">
						<td></td>
						<td>
							<g:actionSubmit action="sendEmail" value="Send" />	&nbsp;
							<a href="${request.contextPath }/test/sendEmail">Cancel</a>
							
						</td>
					</tr>
				</table>
					
					
				
			</fieldset>
		</g:form>
	</body> 
</html>