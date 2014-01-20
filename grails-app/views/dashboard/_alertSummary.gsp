<div class="box">
    <h2><warehouse:message code="inventory.alerts.label" default="Alerts"/></h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="alertSummary">	

    		<table class="zebra">
    			<tbody>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/exclamation.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listOutOfStock">
                                <warehouse:message code="inventory.listOutOfStock.label" default="Items that have stocked out"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                <%--
                    <tr>
                        <td class="center" style="width: 1%">

                        </td>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/indent.gif')}" class="middle"/>
                            <g:link controller="inventory" action="listOutOfStock" params="['abcClass':'A']">
                                <warehouse:message code="inventory.classA.label" default="Class A"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCountClassA"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">

                        </td>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/indent.gif')}" class="middle"/>
                            <g:link controller="inventory" action="listOutOfStock" params="['abcClass':'B']">
                                <warehouse:message code="inventory.classB.label" default="Class B"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCountClassB"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">

                        </td>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/indent.gif')}" class="middle"/>
                            <g:link controller="inventory" action="listOutOfStock" params="['abcClass':'C']">
                                <warehouse:message code="inventory.classC.label" default="Class C"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCountClassC"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                --%>
                <%--
                    <tr>
                        <td class="center" style="width: 1%">

                        </td>
                        <td>
                            <img src="${createLinkTo(dir:'images/icons/indent.gif')}" class="middle"/>
                            <g:link controller="inventory" action="listOutOfStock">
                                <warehouse:message code="inventory.classNone.label" default="No class"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="outOfStockCountClassNone"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                --%>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/error.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listLowStock">
                                <warehouse:message code="inventory.listLowStock.label" default="Items that are below minimum level"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="lowStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>

					<tr>
						<td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/creditcards.png')}" class="middle"/>
						</td>
						<td>
                            <g:link controller="inventory" action="listReorderStock">
                                <warehouse:message code="inventory.listReorderStock.label" default="Items that are below reorder level"/>
							</g:link>
						</td>
						<td class="right">
							<div id="reorderStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
						</td>
					</tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/package.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listOverStock">
                                <warehouse:message code="inventory.listOverStock.label" default="Items that are over stocked"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="overStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/box.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listQuantityOnHandZero">
                                <warehouse:message code="inventory.listQuantityOnHandZero.label" default="Items that have QoH equal to zero"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="onHandQuantityZeroCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>

				</tbody>
			</table>
		</div>
	</div>
</div>

