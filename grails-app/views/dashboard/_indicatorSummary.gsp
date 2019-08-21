<div class="box">
    <h2><warehouse:message code="dashboard.indicators.label" default="KPIs"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="indicatorSummary">

    		<table>
    			<tbody>
                    <g:each in="${org.pih.warehouse.reporting.Indicator.list()}" var="indicator" status="status">
                        <tr class="${status%2?'odd':'even'}">
                            <td class="center" style="width: 1%">
                                <img src="${createLinkTo(dir:'images/icons/silk', file: 'sum.png')}" class="middle"/>
                            </td>
                            <td>
                                ${indicator.description}
                            </td>
                            <td class="right">
                                <div id="${indicator.id}"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                            </td>
                        </tr>
                        <script type="text/javascript">
                            $(function() {
                                $('#${indicator.id}').load('${request.contextPath}/json/evaluateIndicator?id=${indicator.id}');
                            });
                        </script>
                    </g:each>
                    <g:unless test="${org.pih.warehouse.reporting.Indicator.list()}">
                        <tr>
                            <td >
                                <div class="empty center">
                                    <warehouse:message code="indicators.empty.label" default="No indicators available"/>
                                </div>
                            </td>
                        </tr>

                    </g:unless>
				</tbody>
			</table>
		</div>
	</div>
</div>
