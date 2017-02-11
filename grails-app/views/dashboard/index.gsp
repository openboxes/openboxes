<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.dashboard.label', default: 'Dashboard')}</title>
    </head>
    <body>        
		<div class="body">		
		
			<g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>		
	    	<div id="dashboard">
                <div id="column1" class="column">
                    <div id="requisitionItemSummary" class="dragbox">
                        <g:render template="requisitionItemSummary"/>
                    </div>
                    <div id="requisitionSummary" class="dragbox">
                        <g:render template="requisitionSummary" model="[requisitions:requisitions]"/>
                    </div>
                    <div id="receiptSummary" class="dragbox">
                        <g:render template="receiptSummary"/>
                    </div>
                    <div id="shipmentSummary" class="dragbox">
                        <g:render template="shipmentSummary"/>
                    </div>
                    <div id="indicatorSummary" class="dragbox">
                        <g:render template="indicatorSummary"/>
                    </div>

                </div>
                <div id="column2" class="column">
                    <%--<g:render template="valueSummary"/>--%>
                    <div id="orderSummary" class="dragbox">
                        <g:render template="orderSummary"/>
                    </div>
                    <div id="productSummary" class="dragbox">
                        <g:render template="productSummary"/>
                    </div>
                    <div id="expiringSummary" class="dragbox">
                        <g:render template="expiringSummary"/>
                    </div>

                </div>
                <div id="column3" class="column">
                    <div id="activitySummary" class="dragbox">
                        <g:render template="activitySummary"/>
                    </div>
                    <div id="genericProductSummary" class="dragbox">
                        <g:render template="genericProductSummary"/>
                    </div>

                    <div id="tagSummary" class="dragbox">
                        <g:render template="tagSummary" model="[tags:tags]"/>
                    </div>
                </div>
	    	</div>
		</div>
        <script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script type="text/javascript">

            $(window).load(function(){
                $(".spinner").click(function() {
                    $(this).hide();
                });

                $('.column').sortable({
                    connectWith: '.columng',
                    handle: 'h2',
                    cursor: 'move',
                    placeholder: 'placeholder',
                    forcePlaceholderSize: true,
                    opacity: 0.4,
                    stop: function(event, ui){
                        saveState();
                    }
                }).disableSelection();

            });

            function saveState(){
                var items = [];
                // traverse all column div and fetch its id and its item detail.
                $(".column").each(function(){
                    var columnId = $(this).attr("id");
                    $(".dragbox", this).each(function(i){ // here i is the order, it start from 0 to...
                        var item = {
                            id: $(this).attr("id"),
                            column_no: columnId,
                            order: i
                        };
                        items.push(item);
                    });

                });
                $("#results").html("loading..");
                var sortorder = {items : items};
                $.ajax({
                    url: "/openboxes/dashboard/saveState",
                    async: false,
                    data: sortorder,
                    dataType: "html",
                    type: "POST",
                    success: function(html){
                        $("#results").html(html);
                    }
                });
            }

        </script>
		
    </body>
</html>

