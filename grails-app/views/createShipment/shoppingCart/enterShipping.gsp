                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Where is the shipment going?</title>         
    </head>
    <body>
        <div class="body">
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
			<g:hasErrors bean="${address}">
	            <div class="errors">
	                <g:renderErrors bean="${address}" as="list" />
	            </div>				
			</g:hasErrors>           
			<g:form action="shoppingCart" method="post" >
               <div class="dialog">
	                <table>
	                    <tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>Address 1:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'address','errors')}'>
									<input type="text" name='address' value="${address?.address?.encodeAsHTML()}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>Address 2:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'address2','errors')}'>
									<input type="text" name='address2' value="${address?.address2?.encodeAsHTML()}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>City:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'city','errors')}'>
									<input type="text" name='city' value="${address?.city?.encodeAsHTML()}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>State/Province:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'stateOrProvince','errors')}'>
									<input type="text" name='stateOrProvince' value="${address?.stateOrProvince?.encodeAsHTML()}" size="2"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>Postal Code:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'postalCode','errors')}'>
									<input type="text" name='postalCode' value="${address?.postalCode?.encodeAsHTML()}" size="5"/>
								</td>
							</tr>                       
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='name'>Country:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:address,field:'country','errors')}'>
									<input type="text" name='country' value="${address?.country?.encodeAsHTML()}"/>
								</td>
							</tr>
	                    </tbody>
	               </table>
               </div>
               <div class="buttons">
					<span class="formButton">
						<g:submitButton name="back" value="Back"></g:submitButton>								
						<g:submitButton name="submit" value="Next"></g:submitButton>
					</span>
               </div>
            </g:form>
        </div>
    </body>
</html>
