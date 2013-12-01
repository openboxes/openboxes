<div class="box">
	<h2><warehouse:message code="inventory.expiring.label" default="Expiring inventory"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary">	

    		<table class="zebra">
    			<tbody>

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiredStock">
                                <warehouse:message code="inventory.listExpiredStock.label" default="Items that have expired"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="expiredStockCount">
                                <img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/>
                            </div>
                        </td>
                    </tr>

					<tr>
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/clock_red.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiringStock" params="[threshold:30]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[30]" default="Items that will expire within {0} days"/>

							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn30DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						</td>
					</tr>
					<tr>
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk/clock.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiringStock" params="[threshold:90]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[90]" default="Items that will expire within {0} days"/>
							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn90DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						
						</td>
					</tr>
					<tr>
						<td class="center" style="width: 1%">
							<img src="${createLinkTo(dir:'images/icons/silk', file: 'calendar_select_day.png')}" class="middle"/>
						</td>
						<td>
							<g:link controller="inventory" action="listExpiringStock" params="[threshold:180]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[180]" default="Items that will expire within {0} days"/>
							</g:link>
						</td>
						<td class="right">
							<div id="expiringIn180DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
							
						</td>
					</tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/', file: 'calendar_view_month.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listExpiringStock" params="[threshold:365]">
                                <warehouse:message code="inventory.listExpiringStock.label" args="[365]" default="Items that will expire within {0} days"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="expiringIn365DaysStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                </tbody>
			</table>
		</div>
	</div>
</div>