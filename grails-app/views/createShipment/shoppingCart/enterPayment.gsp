                                                                                                 
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Add Container</title>         
    </head>
    <body>
        <div class="body">

           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
           <g:hasErrors bean="${container}">
                <div class="errors">
                    <g:renderErrors bean="${container}" as="list" />
                </div>
           </g:hasErrors>
           <g:form action="shoppingCart" method="post" >
               <div class="dialog">
               
				<g:each var="container" in="${containers}">
               		${container?.containerType?.name} ${container?.name}
				</g:each>
               
                <table>
                    <tbody>
	                    <tr class='prop'>
	                    	<td valign='top' class='name'>
	                    		<label for='name'>Name:</label>
	                    	</td>
	                    	<td valign='top' class='value ${hasErrors(bean:container,field:'name','errors')}'>
	                    		<input type="text" name='name' value="${container?.name}"/>
	                    	</td>
	                    </tr>
                       
                    </tbody>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
						<g:submitButton name="add" value="Add"></g:submitButton>
						<g:submitButton name="back" value="Back"></g:submitButton>								
						<g:submitButton name="submit" value="Next"></g:submitButton>								
                     </span>
               </div>
            </g:form>
        </div>
    </body>
</html>
