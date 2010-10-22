  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Personal Details</title>         
    </head>
    <body>
        <div class="body">
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>   
			<g:hasErrors bean="${person}">
	            <div class="errors">
	                <g:renderErrors bean="${person}" as="list" />
	            </div>				
			</g:hasErrors>
           <g:form action="shoppingCart" method="post" >
               <div class="dialog">
                <table>
                    <tbody>
						<tr class='prop'>
							<td valign='top' class='name'><label for='name'>First Name:</label></td>
							<td valign='top' class='value ${hasErrors(bean:person,field:'firstName','errors')}'>
							<input type="text" name='firstName' value="${person?.firstName?.encodeAsHTML()}"/></td>
						</tr>
						<tr class='prop'>
							<td valign='top' class='name'><label for='name'>Last Name:</label></td>
							<td valign='top' class='value ${hasErrors(bean:person,field:'lastName','errors')}'>
							<input type="text" name='lastName' value="${person?.lastName?.encodeAsHTML()}"/></td>
						</tr>
                       
                    </tbody>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
						<g:submitButton name="return" value="Back to Cart"></g:submitButton>							
						<g:submitButton name="submit" value="Next"></g:submitButton>

                     </span>
               </div>
            </g:form>
        </div>
    </body>
</html>
