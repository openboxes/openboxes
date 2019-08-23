<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>Browse inventory</title>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            
			<div>
	            <div class="yui-gf">
					<div class="yui-u first">
						<div class="box">
							<h2><warehouse:message code="default.filters.label"/></h2>

                            <g:form controller="browseInventory" action="list">
                                <label>Search by name</label>
                                <div>
                                    <g:textField name="q" value="${params.q }" class="text" size="30"/>
                                </div>
                                <label>Location(s)</label>
                                <div>
                                    <g:selectDepot name="location.id" class="chzn-select-deselect" value="${location?.id}" noSelection="['':'']"/>
                                </div>
                                <label>Categories</label>
                                <div>
                                    <g:selectCategory name="category.id" class="chzn-select-deselect" value="${category?.id}" noSelection="['':'']"/>
                                </div>
                                <div>
                                    <button class="button icon search">Search</button>

                                </div>
                            </g:form>
						</div>
					</div>
					<div class="yui-u">
						<div class="box">

                            <h2>
                                Showing ${inventorySnapshots.size() } of ${inventorySnapshots.totalCount} records ${location?.name}
                                ${category?.name}
                            </h2>
							<table>
								<thead>
									<tr>
										<th>Product</th>
										<th>Category</th>
										<th>Unit of measure</th>
										<th class="center middle">Inbound quantity</th>
										<th class="center middle">Outbound quantity</th>
										<th class="center middle">On hand quantity</th>
										<th class="center middle">Available to promise</th>
										<th>Last updated</th>
										
									</tr>
								</thead>
								<g:each var="inventorySnapshot" in="${inventorySnapshots }" status="i">
									<tr class="${i%2?'even':'odd'}">
										<td>
											<g:link controller="inventoryItem" action="showStockCard" id="${inventorySnapshot?.product?.id }">
											${inventorySnapshot.product?.name }
											</g:link>
										</td>
										<td>
											<g:link controller="browseInventory" action="list" 
												params="['category.id':inventorySnapshot?.product?.category?.id]">
												${inventorySnapshot.product?.category?.name }
											</g:link>
										</td>
										<td>
											${inventorySnapshot.product.unitOfMeasure }
										</td>
										<td class="center middle">
											${inventorySnapshot.quantityInbound?:0 }
										</td>
										<td class="center middle">
											${inventorySnapshot.quantityOutbound?:0 }
										</td>
										<td class="center middle">
											${inventorySnapshot.quantityOnHand?:0 }
										</td>
										<td class="center middle">
                                            0
										</td>
										<td>
											<g:formatDate date="${inventorySnapshot.lastUpdated }" format="hh:mm:ss a"/>
										</td>
									</tr>							
								</g:each>
							</table>
						</div>
                        <div class="paginateButtons">

                            <g:set var="pageParams"
                                   value="${['location.id': params?.location?.id, 'category.id': params?.category?.id, q: params.q].findAll {it.value}}"/>

                            <g:paginate total="${inventorySnapshots.totalCount}"
                                        action="list" max="${params.max}" offset="${params.offset}" params="${pageParams}"/>

                        </div>
					
				</div>
			</div>
		</div>
	</body>
</html>