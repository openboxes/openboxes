<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><warehouse:message code="inventory.list.label"/></title>
    </head>    

	<body>
		<div class="body">

            <h3>
                <warehouse:message code="${controllerName}.${actionName}.label"/>
            </h3>

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>


			<%--
			<table>
				<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listLowStock" method="get">
			            	<table >
			            		<tr>
			            			<th><warehouse:message code="category.label"/></th>
			            			<th><warehouse:message code="inventory.showUnsupportedProducts.label"/></th>
			            			<th>&nbsp;</th>
			            		</tr>
			            		<tr>
						           	<td class="filter-list-item">
						           		<g:select name="category"
														from="${categories}"
														optionKey="id" optionValue="${{format.category(category:it)}}" value="${categorySelected?.id}" 
														noSelection="['':'--All--']" />   
									</td>			
									 <td>	
						           		<g:checkBox name="showUnsupportedProducts" value="${showUnsupportedProducts }" } />
						           	</td>	           	
									<td class="filter-list-item" style="height: 100%; width: 70%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>
			--%>

            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="box">
                        <h2>
                            <warehouse:message code="default.filters.label" default="Filters"/>
                        </h2>
                        <div class="filter">
                            <g:link controller="inventory" action="listTotalStock" class="${'listTotalStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listTotalStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter">
                            <g:link controller="inventory" action="listInStock" class="${'listInStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listInStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter">
                            <g:link controller="inventory" action="listOutOfStock" class="${'listOutOfStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listOutOfStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter">
                            <g:link controller="inventory" action="listLowStock" class="${'listLowStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listLowStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter">
                            <g:link controller="inventory" action="listReorderStock" class="${'listReorderStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listReorderStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter">
                            <g:link controller="inventory" action="listOverStock" class="${'listOverStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listOverStock.label"/>
                            </g:link>
                        </div>
                    </div>

                </div>
                <div class="yui-u">

                    <div class="box">
                        <h2>
                            <warehouse:message code="default.results.label" default="Results"/> -
                            <warehouse:message code="default.showing.message" args="[quantityMap?.keySet()?.size()]"/>
                        </h2>
                        <table>
                            <tr>
                                <th><warehouse:message code="product.label"/></th>
                                <th colspan="2" class="center"><warehouse:message code="default.quantity.label"/></th>

                            </tr>
                            <g:each var="entry" in="${quantityMap}" status="i">
                                <tr class="${i%2?'odd':'even'}">
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry.key}">
                                            ${entry.key}
                                        </g:link>
                                    </td>
                                    <td class="right">
                                        ${entry.value}
                                    </td>
                                    <td>
                                        ${entry?.key?.unitOfMeasure}
                                    </td>

                                </tr>


                            </g:each>
                            <g:unless test="${quantityMap}">
                                <tr>
                                    <td colspan="3" class="center">
                                        <warehouse:message code="default.emptyResults.message" default="No results"/>
                                    </td>

                                </tr>

                            </g:unless>
                        </table>
                    </div>
                </div>
            </div>
		</div>
		
	</body>

</html>
