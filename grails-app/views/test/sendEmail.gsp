<html>
	<head>
    	<title>Test Email Service</title>
    	<style>
    		label { display: block; }
    		div, fieldset { padding: 10px; }  
    		
    	</style>
	</head> 
	<body>
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}		
			</div>
		</g:if>
		<a href="${request.contextPath }/test/sendEmail">Refresh</a>
		<g:form action="sendEmail" method="POST">
			<fieldset>
				<legend>Send Simple Email</legend>
				<div> 
					<label for="to">To:</label>
					<g:textField name="to" size="30" />
				</div>
				<div>
					<label for="subject">Subject:</label>
					<g:textField name="subject" size="50" />		
				</div>
				<div>
					<label for="htmlMsg">HTML Message:</label>
					<g:textArea name="htmlMsg" cols="60" rows="5"/>			
				</div>
				<div>
					<g:actionSubmit action="sendEmail" value="Send" />	
				</div>
			</fieldset>
		</g:form>
	</body> 
</html>