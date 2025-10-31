<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />

        <title><warehouse:message code="inventory.expiringStock.label"/></title>
    </head>

	<body>
		<div class="body">

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="summary">
                <h3><warehouse:message code="inventory.expiringStock.label"/></h3>
            </div>

            <div class="buttons" style="text-align: left">

                <g:link controller="dashboard" action="index" class="button">
                    <img src="${resource(dir:'images/icons/silk',file:'house.png')}" alt="${warehouse.message(code: 'dashboard.label') }" />
                    &nbsp;<warehouse:message code="dashboard.label"/>
                </g:link>

                <g:link controller="inventory" action="browse" class="button">
                    <img src="${resource(dir:'images/icons/silk',file:'application_form_magnify.png')}" alt="${warehouse.message(code: 'inventory.browse.label') }" />
                    &nbsp;<warehouse:message code="inventory.browse.label"/>
                </g:link>

                <div class="right">

                    <div class="button-container">

                        <div class="button-group">

                            <a href="javascript:void(0);" class="button button-action" data-action="${g.createLink(controller: "inventory", action: "createExpired")}">
                                <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                                &nbsp;<warehouse:message code="inventory.inventoryExpired.label"/>
                            </a>

                            <a href="javascript:void(0);" class="button button-action" data-action="${g.createLink(controller: "inventory", action: "createConsumed")}">
                                <img src="${resource(dir:'images/icons/silk',file:'package_white.png')}"/>
                                &nbsp;<warehouse:message code="inventory.inventoryConsumed.label"/>
                            </a>

                            <a href="javascript:void(0);" class="button button-action" data-action="${g.createLink(controller: "inventory", action: "createOutboundTransfer")}">
                                <img src="${resource(dir:'images/icons/silk',file:'package_go.png')}"/>
                                &nbsp;<warehouse:message code="inventory.outgoingTransfer.label"/>
                            </a>

                        </div>

                        <g:link
                            params="[format: 'csv', category: params.category, status: params.status]"
                            controller="inventory"
                            action="listExpiringStock"
                            class="button"
                        >
                            <img src="${resource(dir:'images/icons/silk',file:'disk.png')}" alt="${warehouse.message(code: 'default.button.download.label') }" style="vertical-align: middle"/>
                            &nbsp; <g:message code="default.button.downloadAsCSV.label" default="Download as CSV"/>
                        </g:link>

                        <g:link
                            params="[format: 'csv', category: params.category, status:params.status, withBinLocation: true]"
                            controller="inventory"
                            action="listExpiringStock"
                            class="button"
                        >
                            <img src="${resource(dir:'images/icons/silk',file:'disk.png')}" alt="${warehouse.message(code: 'default.button.download.label') }" style="vertical-align: middle"/>
                            &nbsp; <g:message code="inventoryItems.downloadWithBinLocation.label" default="Download with Bin Locations"/><span class="ml-1">(.csv)</span>
                        </g:link>
                    </div>
                </div>
            </div>


            <div class="yui-gf">
				<div class="yui-u first">
		            <g:form action="listExpiringStock" method="get">
						<div class="dialog box">
                            <h2>
                                <warehouse:message code="default.filters.label" default="Filters"/>
                            </h2>
			           		<div class="filter-list-item">
		           				<label><warehouse:message code="category.label"/></label>
				           		%{-- not sure what the ${{...}} in optionValue means: it may need to be de-escaped. OBPIH-5506 --}%
				           		<g:select name="category"  class="chzn-select-deselect"
												from="${categories}"
												optionKey="id" optionValue="${{format.category(category:it)}}"
                                                value="${command.category?.id}"
												noSelection="['': warehouse.message(code:'default.all.label')]" />
							</div>
							<div class="filter-list-item">
		           				<label><warehouse:message code="inventory.expiresWithin.label"/></label>
				           		<g:select name="status" class="chzn-select-deselect"
									from="[
										    'within30Days':
                                                    warehouse.message(code:'inventory.listExpiringStock.label', args: [30]),
										    'within90Days':
                                                    warehouse.message(code:'inventory.listExpiringStock.label', args: [90]),
                                            'within180Days':
                                                    warehouse.message(code:'inventory.listExpiringStock.label', args: [180]),
                                            'within365Days':
                                                    warehouse.message(code:'inventory.listExpiringStock.label', args: [365]),
                                            'greaterThan365Days':
                                                    warehouse.message(code:'inventory.listGreaterThan365Days.label',
                                                            args: [365], default: 'Expires after {0} days')]"
									optionKey="key" optionValue="value" value="${command.status}"
									noSelection="['': warehouse.message(code:'default.all.label')]" />
				           	</div>
                            <div class="filter-list-item">
                                <label>
                                    <g:message code="report.expiresAfter.label"
                                               default="Expires after"
                                    />
                                </label>
                                <g:jqueryDatePicker id="startDate"
                                                    name="startDate"
                                                    cssClass="filter"
                                                    format="MM/dd/yyyy"
                                                    autocomplete="off"
                                                    value="${command?.startDate}"
                                />
                            </div>
                            <div class="filter-list-item">
                                <label>
                                    <g:message code="report.expiresBefore.label"
                                               default="Expires before"
                                    />
                                </label>
                                <g:jqueryDatePicker id="endDate"
                                                    name="endDate"
                                                    cssClass="filter"
                                                    format="MM/dd/yyyy"
                                                    autocomplete="off"
                                                    value="${command?.endDate}"
                                />
                            </div>
				           	<div class="filter-list-item right">
								<button name="filter" class="button icon search">
									<warehouse:message code="default.button.filter.label"/> </button>


                            </div>
							<div class="clear"></div>
						</div>
		            </g:form>
		   		</div>
		   		<div class="yui-u">

					<div class="box">
                        <h2>
                            <warehouse:message code="inventoryItems.expiring.label" default="Expiring inventory items"/> (${data.size()} <warehouse:message code="default.results.label" default="Results"/>)
                        </h2>
                        <div class="dialog">
                            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
                                <table>
                                    <thead>
                                        <tr class="odd" style="height:50px;">
                                            <th class="center middle">
                                                <input type="checkbox" id="toggleCheckbox" class="middle"/>
                                            </th>
                                            <th><warehouse:message code="product.productCode.label"/></th>
                                            <th><warehouse:message code="product.label"/></th>
                                            <th><warehouse:message code="category.label"/></th>
                                            <th><warehouse:message code="inventory.lotNumber.label"/></th>
                                            <th class="center"><warehouse:message code="inventory.expires.label"/></th>
                                            <th class="center"><warehouse:message code="default.qty.label"/></th>
                                            <th class="center"><warehouse:message code="product.uom.label"/></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:set var="counter" value="${0}" />
                                        <g:each var="dataEntry" in="${data}" status="i">
                                            <tr class="${(counter++ % 2) == 0 ? 'even' : 'odd'}">
                                                <td class="center">
                                                    <g:checkBox id="${dataEntry.inventoryItem?.id }"
                                                                name="inventoryItem.id"
                                                                class="checkbox"
                                                                style="top:0em;"
                                                                checked="${false}"
                                                                value="${dataEntry.inventoryItem?.id}" />

                                                </td>
                                                <td class="checkable" >
                                                    <g:link controller="inventoryItem" action="showStockCard" params="['product.id': dataEntry?.inventoryItem?.product?.id]">
                                                        ${dataEntry.inventoryItem?.product?.productCode}
                                                    </g:link>

                                                </td>
                                                <td class="checkable" >
                                                    <g:link controller="inventoryItem" action="showStockCard" params="['product.id': dataEntry?.inventoryItem?.product?.id]">
                                                        <format:displayName product="${dataEntry.inventoryItem?.product}" showTooltip="${true}" />
                                                    </g:link>

                                                </td>
                                                <td class="checkable left">
                                                    <span class="fade"><format:category category="${dataEntry.inventoryItem?.product?.category}"/> </span>

                                                </td>
                                                <td class="checkable" >
                                                    <span class="lotNumber">
                                                        ${dataEntry.inventoryItem?.lotNumber }
                                                    </span>
                                                </td>
                                                <td class="checkable center" >
                                                    <span class="fade">
                                                        <g:formatDate date="${dataEntry.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                                                    </span>
                                                </td>
                                                <td class="checkable center">
                                                    ${dataEntry.quantity}
                                                </td>
                                                <td class="checkable center" >
                                                    ${dataEntry.inventoryItem?.product?.unitOfMeasure ?: "EA" }
                                                </td>
                                            </tr>
                                        </g:each>
                                        <g:unless test="${data}">
                                            <tr>
                                                <td colspan="8">
                                                    <div class="padded center fade">
                                                        <warehouse:message code="inventory.noExpiringStock.label" />
                                                    </div>
                                                </td>
                                            </tr>
                                        </g:unless>
                                    </tbody>
                                </table>
                            </form>
                        </div>

					</div>
		   		</div>
		   	</div>


		</div>
		<script>
			$(document).ready(function() {
				$(".checkable a").click(function(event) {
					event.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).parent().find('input').click();
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						return false;
					}
				);

				$("#toggleCheckbox").click(function(event) {
                    var checked = ($(this).attr("checked") == 'checked');
                    $(".checkbox").attr("checked", checked);
				});

                $(".button-action").click(function(event) {
                    var numChecked = $("input.checkbox:checked").length;
                    if (numChecked <= 0) {
                        event.stopImmediatePropagation();
                        alert("${warehouse.message(code: 'inventory.selectAtLeastOneProduct.label')}");
                    }
                    else {
                        var form = $("#inventoryActionForm");
                        form.attr("action", $(this).data("action"));
                        form.submit();
                    }
                });
            });


		</script>

	</body>

</html>
