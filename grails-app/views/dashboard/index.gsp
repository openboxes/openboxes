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
	    		<table>
					<tr>
						<td width="33%">
                            <g:if test="${grailsApplication.config.openboxes.dashboard.column1.widgets}">
                                <div id="column1" class="column">
                                    <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column1.widgets}">
                                        <div id="${widgetName}" class="dragbox">
                                            <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                                <g:render template="${widgetName}"/>
                                            </g:if>
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>
                            <g:else>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.requisitionItemSummary.enabled}">
                                    <g:render template="requisitionItemSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.requisitionSummary.enabled}">
                                    <g:render template="requisitionSummary" model="[requisitions:requisitions]"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.receiptSummary.enabled}">
                                    <g:render template="receiptSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.shipmentSummary.enabled}">
                                    <g:render template="shipmentSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.indicatorSummary.enabled}">
                                    <g:render template="indicatorSummary"/>
                                </g:if>
                            </g:else>

						</td>
                        <td width="33%">
                            <g:if test="${grailsApplication.config.openboxes.dashboard.column2.widgets}">
                                <div id="column2" class="column">
                                    <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column2.widgets}">
                                        <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                            <div id="${widgetName}" class="dragbox">
                                                <g:render template="${widgetName}"/>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>
                            </g:if>
                            <g:else>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.binLocationSummary.enabled}">
                                    <g:render template="binLocationSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.valueSummary.enabled}">
                                    <g:render template="valueSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.productSummary.enabled}">
                                    <g:render template="productSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.genericProductSummary.enabled}">
                                    <g:render template="genericProductSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.expiringSummary.enabled}">
                                    <g:render template="expiringSummary"/>
                                </g:if>
                            </g:else>
                        </td>
						<td width="33%">
                            <g:if test="${grailsApplication.config.openboxes.dashboard.column3.widgets}">
                                <div id="column3" class="column">
                                    <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column3.widgets}">
                                        <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                            <div id="${widgetName}" class="dragbox">
                                                <g:render template="${widgetName}"/>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>
                            </g:if>
                            <g:else>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.activitySummary.enabled}">
                                    <g:render template="activitySummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.tagSummary.enabled}">
                                    <g:render template="tagSummary" model="[tags:tags]"/>
                                </g:if>
                            </g:else>
						</td>
					
					</tr>
				</table>
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
                        //saveState();
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
                    url: "${request.contextPath}/dashboard/saveState",
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

