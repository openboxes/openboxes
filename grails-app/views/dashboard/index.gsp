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
                                <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column1.widgets}">
                                    <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                        <g:render template="${widgetName}"/>
                                    </g:if>
                                </g:each>
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
                                <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column2.widgets}">
                                    <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                        <g:render template="${widgetName}"/>
                                    </g:if>
                                </g:each>
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
                                <g:each var="widgetName" in="${grailsApplication.config.openboxes.dashboard.column3.widgets}">
                                    <g:if test='${grailsApplication.config.openboxes.dashboard."$widgetName".enabled}'>
                                        <g:render template="${widgetName}"/>
                                    </g:if>
                                </g:each>
                            </g:if>
                            <g:else>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.newsSummary.enabled}">
                                    <g:render template="newsSummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.activitySummary.enabled}">
                                    <g:render template="activitySummary"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.tagSummary.enabled}">
                                    <g:render template="tagSummary" model="[tags:tags]"/>
                                </g:if>
                                <g:if test="${grailsApplication.config.openboxes.dashboard.catalogsSummary.enabled}">
                                    <g:render template="catalogsSummary" model="[catalogs:catalogs]"/>
                                </g:if>
                            </g:else>
						</td>

					</tr>
				</table>
	    	</div>
		</div>

        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.25.3/moment.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-rss/4.3.0/jquery.rss.min.js"></script>
        <script type="text/javascript">

            $(window).load(function(){
                $(".spinner").click(function() {
                    $(this).hide();
                });

              $(".tagcloud a").tagcloud(
                {
                size: {
                  start:1.0,
                  end: 2.0,
                  unit: 'em'
                },
                color: {
                  start: "#aaa", // "#CDE"
                  end: "#F52"//"#FS2"
                }
              });


            });

        </script>

    </body>
</html>

