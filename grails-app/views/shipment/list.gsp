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

            <g:if test="${shipments?.size() == params.max}">
                <div class="message">
                    <ul>
                        <li>${g.message(code: 'shipment.limitHasBeenReached.message', args: [params.max])}</li>
                    </ul>
                </div>
            </g:if>

            <div class="yui-gf">
                <div class="yui-u first">
                    <g:render template="filters" model="[]"/>
                </div>
                <div class="yui-u">

                    <g:form id="listForm" name="listForm" method="post">
                        <g:if test="${incoming}">
                            <g:hiddenField name="type" value="incoming"/>
                        </g:if>
                        <g:set var="shipments" value="${shipments.sort { it.status.code }}"/>
                        <g:set var="shipmentMap" value="${shipments.groupBy { it.status.code }}"/>
                        <g:if test="${shipments.size()}">
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
                                    <div id="${shipmentStatusCode}" style="padding: 10px;">
                                        <g:render template="list" model="[incoming:incoming, shipments:shipmentMap[shipmentStatusCode], statusCode:shipmentStatusCode]"/>
                                    </div>
                                </g:each>
                            </div>
                        </g:if>
                        <g:else>

                            <div class="box">

                                <h2>${warehouse.message(code:'shipments.label')}</h2>
                                <div class="center empty">
                                    <warehouse:message
                                            code="shipping.noShipmentsMatchingConditions.message" />

                                </div>
                            </div>
                        </g:else>
                    </g:form>
                </div>
            </div>

        </div>


		<script type="text/javascript">
			$(function() {
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

                $(".bulk-actions .button").click(function(event){
                    event.preventDefault();
                    let action = $(this).data("action")
                    if (action) {
                        $("#listForm").attr("action", action);
                        $("#listForm").submit();
                    }
                });
                $('.dataTable').dataTable({
                    "bJQueryUI": true,
                    "sPaginationType": "full_numbers"
                });

            });

        </script>

    </body>
</html>
