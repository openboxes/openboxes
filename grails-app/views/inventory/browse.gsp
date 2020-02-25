<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="inventory.browse.label" default="Browse inventory"/></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${commandInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <g:render template="summary"/>

            <div class="dialog">

				<g:set var="pageParams" value="${pageScope.variables['params']}"/>
	        	<g:set var="varStatus" value="${0}"/>
	        	<g:set var="totalProducts" value="${0}"/>
                <g:set var="maxResults" value="${(params.max as int)}"/>
                <div class="yui-gf">
					<div class="yui-u first">
                        <g:render template="filters" model="[commandInstance:commandInstance]"/>
                    </div>
					<div class="yui-u">

                        <div class="box">
                            <h2>
                                <g:set var="rangeBegin" value="${Integer.valueOf(params.offset)+1 }"/>
                                <g:set var="rangeEnd" value="${(Integer.valueOf(params.max) + Integer.valueOf(params.offset))}"/>
                                <g:set var="totalResults" value="${commandInstance.totalCount }"/>

                                <g:if test="${totalResults < rangeEnd || rangeEnd < 0}">
                                    <g:set var="rangeEnd" value="${totalResults }"/>
                                </g:if>
                                <g:if test="${totalResults > 0 }">
                                    <warehouse:message code="inventory.browseTab.label" args="[rangeBegin, rangeEnd, totalResults]"/>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="inventory.showingNoResults.label" default="Showing 0 results"/>
                                </g:else>
                                <g:if test="${commandInstance?.searchTerms}">
                                    "${commandInstance.searchTerms }"
                                </g:if>

								<g:link controller="inventory" action="browse" class="button icon reload">
									<warehouse:message code="default.showAll.label" default="Show all"/>
								</g:link>
                            </h2>

                            <div id="tabs-1" style="padding: 0px;">
					            <form id="inventoryBrowserForm" method="POST">
					                <table id="inventoryBrowserTable" border="0">
										<thead>
				           					<tr>
				           						<th>

				           						</th>
%{--				           						<th class="center middle">--}%
%{--                                                   <g:render template="./actions" model="[]"/>--}%
%{--				           						</th>--}%
%{--												<th class="center middle" style="width: 1%">--}%
%{--													<input type="checkbox" id="toggleCheckbox">--}%
%{--												</th>--}%
												<th class="middle">
													<g:message code="product.label"/>
												</th>
                                                <th class="middle">
                                                    <g:message code="category.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="tag.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="productCatalog.label"/>
                                                </th>
												<th class="center middle" style="width: 7%;">
													<g:message code="default.qty.label"/>
												</th>
				           					</tr>
										</thead>
                                        <tbody>
                                            <g:if test="${commandInstance?.searchResults}">
												<g:each var="searchResult" in="${commandInstance?.searchResults}" status="i">
													<tr class="${i%2?'even':'odd' }">
														<td>
															<g:if test="${searchResult?.product?.images }">
															   <div class="nailthumb-container">
																   <g:set var="image" value="${searchResult?.product?.images?.sort()?.first()}"/>
																   <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
															   </div>
														   </g:if>
														   <g:else>
															   <div class="nailthumb-container">
																   <img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />
															   </div>
														   </g:else>
														</td>
														<td>
															<g:link controller="inventoryItem" action="showStockCard" id="${searchResult?.product?.id}" style="color: ${searchResult.color}">
																${searchResult?.product?.productCode}
																${searchResult?.product?.name}
														   	</g:link>
														</td>
														<td>
															<g:link controller="inventory" action="browse" params="${params + ['categoryId':searchResult?.product?.category.id]}">
																${searchResult?.product?.category?.name}
															</g:link>
														</td>
														<td>
															<g:each var="tag" in="${searchResult?.product?.tags}">
																<g:link controller="inventory" action="browse" params="${params + ['tags':tag.id]}">
																	<div class="tag">${tag.tag}</div>
																</g:link>
															</g:each>
														</td>
														<td>
															<g:each var="catalog" in="${searchResult?.product?.productCatalogs}">
																<g:link controller="inventory" action="browse" params="${params + ['catalogs':catalog.id]}">
																	<div class="tag tag-info">${catalog.name}</div>
																</g:link>
															</g:each>
														</td>
														<td class="center">
															${searchResult?.quantityOnHand}
														</td>
													</tr>
												</g:each>
											</g:if>
											<g:unless test="${commandInstance?.searchResults}">
												<tr>
													<td colspan="12" class="even center">
														<div class="fade empty">
                                                            <warehouse:message code="inventory.searchNoMatch.message"
                                                                               args="[commandInstance?.searchTerms?:'',format.metadata(obj:commandInstance?.category)]"/>
														</div>
													</td>
												</tr>
                                            </g:unless>


                                        </tbody>
									</table>

								</form>
							</div>
                            <div class="paginateButtons">

                                <g:paginate total="${commandInstance?.totalCount}"
                                            action="browse" max="${params.max}" params="${pageParams}"/>

                                <div class="right">
                                    <warehouse:message code="inventory.browseResultsPerPage.label"/>:
                                    <g:if test="${params.max != '10'}"><g:link action="browse" params="${pageParams + [max:10]}">10</g:link></g:if><g:else><span class="currentStep">10</span></g:else>
                                    <g:if test="${params.max != '25'}"><g:link action="browse" params="${pageParams + [max:25]}">25</g:link></g:if><g:else><span class="currentStep">25</span></g:else>
                                    <g:if test="${params.max != '50'}"><g:link action="browse" params="${pageParams + [max:50]}">50</g:link></g:if><g:else><span class="currentStep">50</span></g:else>
                                    <g:if test="${params.max != '100'}"><g:link action="browse" params="${pageParams + [max:100]}">100</g:link></g:if><g:else><span class="currentStep">100</span></g:else>
                                </div>
                            </div>
						</div>

					</div>
				</div>
			</div>
		</div>
        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
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

		    	$(".isRelated").hide();
		    	$(".expandable").click(function(event) {
			    	var isVisible = $(".productGroup-"+event.target.id).is(":visible");
			    	if (isVisible) {
				    	$("#productGroup-"+event.target.id).removeClass("showRelated");
				    	$("#productGroup-"+event.target.id).addClass("hideRelated");
				    }
			    	else {
				    	$("#productGroup-"+event.target.id).addClass("showRelated");
				    	$("#productGroup-"+event.target.id).removeClass("hideRelated");
			    	}
		    		$(".productGroup-"+event.target.id).toggle();

		    	});

		    	$('.nailthumb-container').nailthumb({ width : 20, height : 20 });
		    	$('.nailthumb-container-100').nailthumb({ width : 100, height : 100 });

                $("#tagcloud a").tagcloud({
                    size: {
                        start: 10,
                        end: 25,
                        unit: 'px'
                    },
                    color: {
                        start: "#CDE",
                        end: "#FS2"
                    }
                });

		    	function refreshQuantity() {
			    	$.each($(".quantityOnHand"), function(index, value) {
						var productId = $(this).attr("data-product-id");
						$(this).load('${request.contextPath}/json/getQuantityOnHand?product.id='+productId+'&location.id=${session.warehouse.id}');
			    	});

			    	$.each($(".quantityToShip"), function(index, value) {
						var productId = $(this).attr("data-product-id");
						$(this).load('${request.contextPath}/json/getQuantityToShip?product.id='+productId+'&location.id=${session.warehouse.id}');
			    	});

			    	$.each($(".quantityToReceive"), function(index, value) {
						var productId = $(this).attr("data-product-id");
						$(this).load('${request.contextPath}/json/getQuantityToReceive?product.id='+productId+'&location.id=${session.warehouse.id}');
			    	});
		    	}
				<g:if test="${showQuantity}">
					refreshQuantity();
				</g:if>

			});
		</script>
    </body>
</html>
