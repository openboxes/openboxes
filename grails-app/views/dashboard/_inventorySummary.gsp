<div class="box">
    <h2><warehouse:message code="inventory.label" args="[session.warehouse.name]"/></h2>
    <div class="widget-content" style="padding:0; margin:0">
        <div id="inventorySummary">
            <%--
			<div style="padding-top:0px;">
				<g:form method="GET" controller="inventory" action="browse">
					<div>
							
						<g:textField id="dashboardSearchBox" name="searchTerms" style="width: 60%" value="${params.searchTerms }"
							class="globalSearch"/>						
						<g:hiddenField name="resetSearch" value="true"/>							
						<g:hiddenField name="categoryId" value="${rootCategory.id }"/>							
						<g:hiddenField name="showHiddenProducts" value="on"/>
						<g:hiddenField name="showOutOfStockProducts" value="on"/>
						<button type="submit" class="button icon search">
							<warehouse:message code="default.search.label"/>
						</button>
							
					</div>
				</g:form>
            </div>
            --%>
            <table class="zebra">
                <tbody>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/time.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listTotalStock">
                                <warehouse:message code="inventory.listTotalStock.label" default="Items that have ever been stocked"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="totalStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>

                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/accept.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listInStock">
                                <warehouse:message code="inventory.listInStock.label" default="Items that are currently stocked"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="inStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>
                    <tr>
                        <td class="center" style="width: 1%">
                            <img src="${createLinkTo(dir:'images/icons/silk/', file: 'bricks.png')}" class="middle"/>
                        </td>
                        <td>
                            <g:link controller="inventory" action="listReconditionedStock">
                                <warehouse:message code="inventory.listReconditionedStock.label" default="Items that need to be reconditioned"/>
                            </g:link>
                        </td>
                        <td class="right">
                            <div id="reconditionedStockCount"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>

                        </td>
                    </tr>

                </tbody>
            </table>
		</div>
	</div>
</div>	