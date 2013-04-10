
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title><warehouse:message code="default.view.label" args="[entityName.toLowerCase()]" /></title>    
        <style>
        /*
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
			.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; }         	
        */


        </style>
    </head>    

    <body>
        <div class="body">
     
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog">
				<g:render template="../transaction/summary"/>
				<div class="yui-gf">
					<div class="yui-u first">
						<g:render template="../transaction/details" model="[transactionInstance:transactionInstance]"/>
					</div>
					<div class="yui-u">									
                        <g:render template="../transaction/entries" model="[transactionInstance:transactionInstance]"/>
					</div>		
				</div>
			</div>
		</div>
		
    </body>
</html>
