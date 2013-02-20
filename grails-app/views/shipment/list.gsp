<html>
   <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="messagePrefix" value="${incoming ? 'shipping.shipmentsTo' : 'shipping.shipmentsFrom'}"/>
        <title><warehouse:message code="${messagePrefix}.label" args="[session.warehouse.name]"/></title>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:form action="list" method="post">
            	<g:if test="${incoming}">
            		<g:hiddenField name="type" value="incoming"/>
            	</g:if>
            	
				<g:set var="shipments" value="${shipments.sort { it.status.code }}"/>
				<g:set var="shipmentMap" value="${shipments.groupBy { it.status.code }}"/>
				<div class="tabs">
					<ul>
						<g:each var="shipmentStatusCode" in="${shipmentMap.keySet() }">
							<li>
								<a href="#${shipmentStatusCode }">
									<format:metadata obj="${shipmentStatusCode }"/>
									<span class="fade">(${shipmentMap[shipmentStatusCode]?.size() })</span>
								</a>
							</li>
						</g:each>
					</ul>		
					<g:each var="shipmentStatusCode" in="${shipmentMap.keySet() }">
						<div id="${format.metadata(obj: shipmentStatusCode) }" style="padding: 10px;">	            	
							<g:render template="list" model="[incoming:incoming, shipments:shipmentMap[shipmentStatusCode]]"/>
						</div>
					</g:each>
				</div>
	            
	            <g:if test="${shipments.size()==0}">
					<div class=""><warehouse:message
							code="shipping.noShipmentsMatchingConditions.message" />
					</div>
				</g:if>
			</g:form>
			
        </div>		
		<script type="text/javascript">
			$(function() { 		
				$(".clear-dates").click(function() {
					$('#statusStartDate-datepicker').val('');					
					$('#statusEndDate-datepicker').val('');
					$('#statusStartDate').val('');					
					$('#statusEndDate').val('');
				});			


				$(".filter").change(function() { 
					$(this).closest("form").submit();
				});
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				); 

		    	var index = $('.tabs li a').index($('a[href="#add"]').get(0));
		    	$('.tabs').tabs({selected: index});
				
			});
        </script>
        
    </body>
</html>
