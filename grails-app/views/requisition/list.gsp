<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requests.label', default: 'Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
        
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
			<g:set var="requests" value="${requisitions?.sort { it.status }}"/>
			<g:set var="requestMap" value="${requisitions?.groupBy { it.status }}"/>
			<div class="tabs">
				<ul>
					<g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list() }">
						<li>
							<a href="#${format.metadata(obj: status) }">
								<format:metadata obj="${status }"/>
								<span class="fade">(${requestMap[status]?.size()?:0})</span>
							</a>
						</li>
					</g:each>
				</ul>		
				<g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list() }">
					<div id="${format.metadata(obj: status) }">	            	
						<g:render template="list" model="[requestType: requestType, requests:requestMap[status]]"/>
					</div>
				</g:each>
			</div>			
			
        </div>
        
		<script type="text/javascript">
			$(function() { 
		    	$(".tabs").tabs(
	    			{
	    				//cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    				//	expires: 1
	    				//}
	    			}
				); 
				
			});
        </script>        
        
    </body>
</html>
