                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Search By Name, Description, GTIN</title>  
    </head>
    <body>
    	<div class="body">
    	
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
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
				<div class="buttons right">
					<%-- 
					<button name="_eventId_back">&lsaquo; <warehouse:message code="default.button.back.label"/></button>	
					<button name="_eventId_next"><warehouse:message code="default.button.next.label"/> &rsaquo;</button>
					<button name="_eventId_save"><warehouse:message code="default.button.saveAndExit.label"/></button>
					<button name="_eventId_cancel"><warehouse:message code="default.button.cancel.label"/></button>					
					--%>
					
					<%-- 
                    <g:submitButton class="back" name="back" value="Back" />
                    <g:submitButton class="cancel" name="cancel" value="Cancel" />
					--%>
				</div>
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
