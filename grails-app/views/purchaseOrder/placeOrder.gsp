<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
        <g:hasErrors bean="${orderItem}">
            <div class="errors">
                <g:renderErrors bean="${orderItem}" as="list" />
            </div>
        </g:hasErrors>


        <div class="dialog">

            <g:render template="/order/summary" model="[orderInstance:order,currentState:'placeOrder']"/>

            <div class="box">
                <h2><warehouse:message code="order.wizard.placeOrder.label"/></h2>
                <div class="empty center" style="margin:15px">
                    <div>
                        Are you sure you want to place this purchase order with the supplier?
                        <g:link controller="order" action="print" id="${order?.id}" class="button icon log" target="_blank">
                            ${warehouse.message(code: 'order.print.label', default:"Print order")}
                        </g:link>
                    </div>
                </div>
            </div>
		</div>
	</div>
    <script type="text/javascript">
        $( function() {
            var cookieName, $tabs, stickyTab;

            cookieName = 'stickyTab';
            $tabs = $( '.tabs' );

            $tabs.tabs( {
                select: function( e, ui )
                {
                    $.cookies.set( cookieName, ui.index );
                }
            } );

            stickyTab = $.cookies.get( cookieName );
            if( ! isNaN( stickyTab )  )
            {
                $tabs.tabs( 'select', stickyTab );
            }
        } );


        $(document).ready(function(){

        });

    </script>
</body>
</html>
