                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title><warehouse:message code="createProduct.label"/></title>  
    </head>
    <body>
    	<div class="body">
    	
			<g:hasErrors bean="${command}">
				<div class="errors">
					<g:renderErrors bean="${command}" as="list" />
				</div>				
			</g:hasErrors> 			
						
			<g:render template="header" model="['currentState':'search']"/>
						 	
			<g:form action="create" method="post" >				
				<div class="dialog box">					
					<table>
		                <tbody>
		                    <tr>
		                        <td valign="top">
		                        
		                        	<label>Enter UPC/EAN</label>
		                            <input type="text" id="searchTerms" name="searchTerms" 
		                            	value="${command?.searchTerms}" class="text medium" size="80"/>
				                    <g:submitButton class="search" name="search" value="Search" />
		                            
		                        </td>
		                    </tr>
		                </tbody>
	                </table>			
				
				
				</div>			
				<div class="buttons right"></div>
            </g:form>
        </div>        
        
		<script>			
			$(document).ready(function() {
				$("#searchTerms").focus();
				$("#searchTerms").select();
			});
		</script> 				
    </body>
</html>
