  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         
         <title><warehouse:message code="default.systemError.label"/></title>         
    </head>
    <body>
        
        <div class="body">
           <h1><warehouse:message code="default.systemError.label"/></h1>
           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>       

			<warehouse:message code="default.systemError.message"/>
        </div>
    </body>
</html>
