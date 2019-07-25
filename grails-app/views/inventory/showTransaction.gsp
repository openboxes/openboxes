
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title><warehouse:message code="default.view.label" args="[entityName.toLowerCase()]" /></title>    
    </head>

    <body>
        <div class="body">

            <g:render template="../transaction/summary"/>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog">
				<div class="yui-gd">
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
