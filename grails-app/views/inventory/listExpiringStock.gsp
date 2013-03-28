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
			
			<h3><warehouse:message code="inventory.expiringStock.label"/></h3>
			
			<div class="yui-gf">
				<div class="yui-u first">
		            <g:form action="listExpiringStock" method="get">
						<div class="dialog box">
                            <h2>
                                <warehouse:message code="default.filters.label" default="Filters"/>
                            </h2>
			           		<div class="filter-list-item">
		           				<label><warehouse:message code="category.label"/></label>
				           		<g:select name="category"
												from="${categories}"
												optionKey="id" optionValue="${{format.category(category:it)}}" value="${categorySelected?.id}" 
												noSelection="['': warehouse.message(code:'default.all.label')]" />   
							</div>
							<div class="filter-list-item">
		           				<label><warehouse:message code="inventory.expiresWithin.label"/></label>
				           		<g:select name="threshold"
									from="['7': warehouse.message(code:'default.week.oneWeek.label'), '14': warehouse.message(code:'default.week.twoWeeks.label'),
										   '30': warehouse.message(code:'default.month.oneMonth.label'), '60': warehouse.message(code:'default.month.twoMonths.label'), 
										   '90': warehouse.message(code:'default.month.threeMonths.label'), '180': warehouse.message(code:'default.month.sixMonths.label'), 
										   '365': warehouse.message(code:'default.year.oneYear.label'), '730': warehouse.message(code:'default.year.twoYear.label'), '1825': warehouse.message(code:'default.year.fiveYear.label')]"
									optionKey="key" optionValue="value" value="${thresholdSelected}" 
									noSelection="['': warehouse.message(code:'default.all.label')]" />   
				           	</div>
				           	<div class="filter-list-item right">
								<button name="filter">
									<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
							</div>
							<div class="clear"></div>
						</div>
		            </g:form>  
		   		</div>
		   		<div class="yui-u">
		   		
					<div class="box">
                        <h2>
                            <warehouse:message code="default.results.label" default="Results"/>
                        </h2>
                        <div class="">
                            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
                                <table>
                                    <thead>
                                        <tr class="odd">
                                            <th class="center" style="width: 50px; text-align: center;">
                                                <input type="checkbox" id="toggleCheckbox"/>
                                            </th>
                                            <th><warehouse:message code="category.label"/></th>
                                            <th><warehouse:message code="item.label"/></th>
                                            <th><warehouse:message code="inventory.lotNumber.label"/></th>
                                            <th class="center"><warehouse:message code="inventory.expires.label"/></th>
                                            <th class="center"><warehouse:message code="default.qty.label"/></th>
                                            <th class="center"><warehouse:message code="product.uom.label"/></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:set var="counter" value="${0 }" />
                                        <g:set var="anyExpiringStock" value="${false }"/>
                                        <g:each var="inventoryItem" in="${inventoryItems}" status="i">
                                            <g:set var="quantity" value=""/>
                                            <g:set var="anyExpiringStock" value="${true }"/>
                                            <tr class="${(counter++ % 2) == 0 ? 'even' : 'odd'}">
                                                <td class="center">
                                                    <g:checkBox id="${inventoryItem?.id }" name="inventoryItem.id"
                                                        class="checkbox" style="top:0em;" checked="${false }"
                                                            value="${inventoryItem?.id }" />

                                                </td>
                                                <td class="checkable left">
                                                    <span class="fade"><format:category category="${inventoryItem?.product?.category}"/> </span>

                                                </td>
                                                <td class="checkable" >
                                                    <g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
                                                        <format:product product="${inventoryItem?.product}"/>
                                                    </g:link>

                                                </td>
                                                <td class="checkable" >
                                                    <span class="lotNumber">
                                                        ${inventoryItem?.lotNumber }
                                                    </span>
                                                </td>
                                                <td class="checkable center" >
                                                    <span class="fade">
                                                        <g:formatDate date="${inventoryItem?.expirationDate}" format="MMM yyyy"/>
                                                    </span>
                                                </td>
                                                <td class="checkable center">
                                                    ${quantityMap[inventoryItem]}
                                                </td>
                                                <td class="checkable center" >
                                                    ${inventoryItem?.product?.unitOfMeasure?:"EA" }
                                                </td>
                                            </tr>
                                        </g:each>
                                        <g:if test="${!anyExpiringStock }">
                                            <tr>
                                                <td colspan="7">
                                                    <div class="padded center fade">
                                                        <warehouse:message code="inventory.noExpiringStock.label" />
                                                    </div>
                                                </td>
                                            </tr>
                                        </g:if>
                                    </tbody>
                                    <tfoot>
                                        <tr style="border-top: 1px solid lightgrey">
                                            <td colspan="7">
                                                <div>
                                                    <g:render template="./actionsExpiringStock" />
                                                </div>
                                            </td>
                                        </tr>
                                    </tfoot>
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
						//$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().removeClass('checked');
						return false;
					}
				);
				
				$("#toggleCheckbox").click(function(event) {
					$(".checkbox").attr("checked", $(this).attr("checked"));
				});	
			});	
		</script>	
		
	</body>

</html>
