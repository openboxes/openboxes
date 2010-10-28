                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Suitcase Shipment Complete</title>         
    </head>
    <body>
       
        <div class="body">
           
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors">
					<g:renderErrors bean="${shipmentInstance}" as="list" />
				</div>
			</g:hasErrors>
           
			<g:render template="flowHeader"/>						
           
        	<fieldset>
        		<legend>Congratulations</legend>
         		
				<div class="dialog">     
					<p align="center">
						<b>Congratulations, your suitcase shipment has been created!</b>  
						<br/>
						<span class="fade" >										
							<g:link controller='shipment' action='showDetails' id="${shipmentInstance?.id}">click to view details</g:link>
						</span>
					</p>					
				</div>
			</fieldset>
        </div>
    </body>
</html>
