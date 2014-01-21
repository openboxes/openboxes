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
            
            
			<div class="dialog">
                <div id="inventory-summary" class="summary">
                    <table>
                        <tbody>
                            <tr>
                                <td class="top">
                                    <div class="title">
                                        <warehouse:message code="inventory.browse.label" default="Browse inventory"/>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                </div>

				<!-- Inventory Browser -->
	        	<g:set var="varStatus" value="${0}"/>
	        	<g:set var="totalProducts" value="${0}"/> 							        	
        	
	            <div class="yui-gf">
					<div class="yui-u first">
                        <%--
                        <g:render template="filters" model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>
                        --%>

                        <div>
                            <g:form method="GET" controller="inventory" action="browse">
                                <div class="box">
                                    <h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
                                    <table>
                                        <tr>
                                            <td>
                                                <g:hiddenField name="max" value="${params.max?:10 }"/>
                                                <g:textField name="searchTerms"
                                                             value="${commandInstance.searchTerms}"
                                                             placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
                                                             class="text medium" style="width:100%;"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <g:selectCategory id="subcategoryId"
                                                                  name="subcategoryId"
                                                                  class="chzn-select-deselect"
                                                                  noSelection="['null':'']"
                                                                  style="width:100%;"
                                                                  value="${commandInstance?.subcategoryInstance?.id}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <div >
                                                    <div>
                                                        <g:checkBox name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>
                                                        <warehouse:message code="inventory.showHiddenProducts.label"/>
                                                    </div>
                                                    <div>
                                                        <g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>
                                                        <warehouse:message code="inventory.showOutOfStockProducts.label"/>

                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <div class="center">
                                                    <button type="submit" class="button icon search" name="searchPerformed" value="true">
                                                        <warehouse:message code="default.search.label"/>
                                                    </button>


                                                    <g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]" class="button icon reload">
                                                        <warehouse:message code="inventoryBrowser.resetAll.label" default="Reset all"/>
                                                    </g:link>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="box">
                                    <h2><warehouse:message code="inventory.browseByTag.label"/></h2>
                                    <table>
                                        <tr class="">
                                            <td class="middle">
                                                <g:if test="${tags }">
                                                    <div id="tagcloud">
                                                        <g:each in="${tags }" var="tag" status="status">
                                                            <g:set var="selectedTag" value="${params.tag == tag.key.tag }"/>
                                                            <span class="${selectedTag?'selected':'' }">
                                                                <g:link controller="inventory" action="browse" params="['tag':tag.key.tag,'max':params.max]" rel="${tag?.key?.products?.size() }">
                                                                    ${tag?.key?.tag } (${tag?.key?.products?.size() })</g:link>
                                                            </span>
                                                        </g:each>
                                                    </div>
                                                </g:if>
                                                <g:else>
                                                    <div class="empty middle center">
                                                        ${warehouse.message(code: 'tags.empty.label', default:'No public tags') }
                                                    </div>
                                                </g:else>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="box">
                                    <h2><warehouse:message code="inventory.browseByCategory.label"/></h2>
                                    <table>
                                        <g:each var="category" in="${commandInstance?.categoryInstance?.categories}">
                                            <tr class="prop">
                                                <td class="middle">
                                                    <g:link controller="inventory" action="browse" params="['subcategoryId':category?.id,'max':params.max,'searchPerformed':true,'showHiddenProducts':params.showHiddenProducts?:'on','showOutOfStockProducts':params.showOutOfStockProducts?:'on']">
                                                        ${category?.name } (${category?.products?.size() })
                                                    </g:link>
                                                </td>
                                            </tr>
                                        </g:each>
                                    </table>
                                </div>
                            </g:form>
                        </div>
                    </div>
					<div class="yui-u">
									
						<g:set var="showQuantity" value="${(params.max as int) <= 25}"/>	

						<g:if test="${!showQuantity }">
							<div class="notice">
								<warehouse:message code="inventory.tooManyProducts.message"></warehouse:message>
							</div>
						</g:if>
                        <div class="box">
                            <h2>
                                <g:set var="rangeBegin" value="${Integer.valueOf(params.offset)+1 }"/>
                                <g:set var="rangeEnd" value="${(Integer.valueOf(params.max) + Integer.valueOf(params.offset))}"/>
                                <g:set var="totalResults" value="${numProducts }"/>

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
                            </h2>
                            <div id="tabs-1" style="padding: 0px;">
					            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
					                <table id="inventory-browser-table" border="0"> 
										<thead> 
				           					<tr>
				           						<th>
				           						
				           						</th>
				           						<th class="middle">
                                                   <g:render template="./actions" model="[]"/>
				           						</th>
												<th class="center middle" style="width: 1%">
													<input type="checkbox" id="toggleCheckbox">	
												</th>
												<th class="middle" style="width: 1%">
													<warehouse:message code="product.productCode.label"/>
												</th>
												<th class="middle">
													<warehouse:message code="product.name.label"/>
												</th>
												<th class="middle">
													<warehouse:message code="product.manufacturer.label"/>
												</th>
												<th class="middle">
													<warehouse:message code="product.brandName.label"/>
												</th>
												<th class="middle">
													<warehouse:message code="product.manufacturerCode.label"/>
												</th>
												<th class="center" style="width: 7%;">
													<warehouse:message code="inventory.qtyin.label"/>
												</th>
												<th class="center" style="width: 7%;">
													<warehouse:message code="inventory.qtyout.label"/>
												</th>
												<th class="center middle" style="width: 7%;">
													<warehouse:message code="default.qty.label"/>
												</th>
				           					</tr>
										</thead>
                                        <tbody>
                                            <g:if test="${commandInstance?.categoryToProductMap}">
                                                <g:set var="counter" value="${0 }"/>
                                                <g:each var="entry" in="${commandInstance?.categoryToProductMap}" status="i">
                                                   <g:set var="category" value="${entry.key }"/>
                                                   <g:set var="categoryInventoryItems" value="${commandInstance?.categoryToProductMap[entry.key]}"/>
                                                   <%--
                                                    <tr class="category-header">
                                                       <td colspan="11" style="padding:0; margin:0;">
                                                           <span class="fade">
                                                               <h2 style="border-top: 2px solid lightgrey;">
                                                                   <format:category category="${category }"/>
                                                                   (${categoryInventoryItems.size() })

                                                               </h2>
                                                           </span>

                                                       </td>
                                                   </tr>
                                                    --%>
                                                   <g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">
                                                       <g:if test="${inventoryItem.product }">
                                                           <%--<cache:render cachekey="${inventoryItem?.product?.id}" template="browseProduct" model="[counter:counter,inventoryItem:inventoryItem,cssClass:'product',showQuantity:showQuantity]" />--%>
                                                           <%--<g:render template="browseProduct" model="[counter:counter,inventoryItem:inventoryItem,cssClass:'product',showQuantity:showQuantity]" />--%>



                                                           <tr class="${counter%2==0?'even':'odd' } product prop">
                                                               <td>
                                                                   <g:if test="${inventoryItem?.product?.images }">
                                                                       <div class="nailthumb-container">
                                                                           <g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
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
                                                                   <div class="action-menu hover">
                                                                       <button class="action-btn">
                                                                           <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
                                                                                   style="vertical-align: middle" />
                                                                       </button>
                                                                       <div class="actions left">
                                                                           <div class="action-menu-info">
                                                                               <table style="width: 525px">
                                                                                   <tr>
                                                                                       <td style="width: 100px;" class="center middle">
                                                                                           <g:if test="${inventoryItem?.product?.images }">
                                                                                               <div class="nailthumb-container-100">
                                                                                                   <g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
                                                                                                   <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
                                                                                               </div>
                                                                                           </g:if>
                                                                                           <g:else>
                                                                                               <div class="nailthumb-container-100">
                                                                                                   <img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />
                                                                                               </div>
                                                                                           </g:else>

                                                                                       </td>
                                                                                       <td>
                                                                                           <table>
                                                                                               <tr>
                                                                                                   <td>
                                                                                                       <div class="title">
                                                                                                           <span class="fade">#${inventoryItem?.product?.productCode}</span>
                                                                                                           ${inventoryItem?.product?.name}
                                                                                                       </div>

                                                                                                   </td>
                                                                                               </tr>
                                                                                               <g:if test="${inventoryItem?.product?.productGroups }">
                                                                                                   <tr>
                                                                                                       <td>
                                                                                                           <span style="text-transform:uppercase;" class="fade">
                                                                                                               <format:category category="${inventoryItem?.product?.category }"/> &rsaquo;
                                                                                                               ${inventoryItem?.product?.productGroups?.sort()?.first()?.description}
                                                                                                           </span>
                                                                                                       </td>
                                                                                                   </tr>
                                                                                               </g:if>
                                                                                               <tr>
                                                                                                   <td>
                                                                                                       <p>${inventoryItem?.product?.description }</p>
                                                                                                   </td>
                                                                                               </tr>
                                                                                           </table>
                                                                                       </td>
                                                                                   </tr>
                                                                                   <tr>
                                                                                       <td>

                                                                                       </td>
                                                                                       <td >
                                                                                           <div class="button-group">
                                                                                               <g:link class="button" controller="inventoryItem" action="showStockCard" params="['product.id': inventoryItem?.product?.id]">
                                                                                                   <img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}"/>
                                                                                                   <warehouse:message code="inventory.showStockCard.label"/>
                                                                                               </g:link>
                                                                                               <g:link class="button" controller="product" action="edit" id="${inventoryItem?.product?.id }">
                                                                                                   <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
                                                                                                   <warehouse:message code="product.edit.label"/>
                                                                                               </g:link>
                                                                                               <g:link class="button" controller="inventoryItem" action="showTransactionLog" params="['product.id': inventoryItem?.product?.id, 'disableFilter':true]">
                                                                                                   <img src="${resource(dir: 'images/icons/silk', file: 'chart_bar.png')}"/>
                                                                                                   <warehouse:message code="inventory.showTransactionLog.label"/>
                                                                                               </g:link>
                                                                                           </div>
                                                                                       </td>
                                                                                   </tr>
                                                                               </table>

                                                                           </div>
                                                                       </div>
                                                                   </div>
                                                               </td>
                                                               <td class="top center">
                                                                   <g:checkBox id="${inventoryItem?.product?.id }" name="product.id"
                                                                               class="checkbox" style="top:0em;" checked="${false }"
                                                                               value="${inventoryItem?.product?.id }" />
                                                               </td>
                                                               <%--
                                                           <td class="checkable center middle">
                                                               <img src="${resource(dir: 'images/icons/inventoryStatus', file: inventoryItem?.inventoryLevel?.status?.name()?.toLowerCase() + '.png')}"
                                                                   alt="${inventoryItem?.inventoryLevel?.status?.name() }" title="${inventoryItem?.inventoryLevel?.status?.name() }" style="vertical-align: middle;"/>

                                                           </td>
                                                               --%>
                                                               <td class="checkable top">
                                                                   <span class="fade">${inventoryItem?.product?.productCode }</span>
                                                               </td>
                                                               <td class="checkable top">
                                                                   <g:link name="productLink" controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
                                                                       <div title="${inventoryItem?.product?.description }" class="popover-trigger" data-id="${inventoryItem?.product?.id }">
                                                                           <g:if test="${inventoryItem?.product?.name?.trim()}">
                                                                               ${inventoryItem?.product?.name}
                                                                           </g:if>
                                                                           <g:else>
                                                                               <warehouse:message code="product.untitled.label"/>
                                                                           </g:else>
                                                                       </div>
                                                                       <div class="fade">${inventoryItem?.product?.category}</div>

                                                                   </g:link>
                                                               </td>
                                                               <td class="checkable top left">
                                                                   <span class="fade">${inventoryItem?.product?.manufacturer }</span>
                                                               </td>
                                                               <td class="checkable top left">
                                                                   <span class="fade">${inventoryItem?.product?.brandName}</span>
                                                               </td>
                                                               <td class="checkable top left">
                                                                   <span class="fade">${inventoryItem?.product?.manufacturerCode }</span>
                                                               </td>
                                                               <td class="checkable top center" style="width: 7%; border-left: 1px solid lightgrey;">

                                                                   <g:if test="${!showQuantity }">

                                                                   </g:if>
                                                                   <g:elseif test="${inventoryItem?.supported && showQuantity }">
                                                                   <%-- <g:formatNumber number="${inventoryItem?.quantityToReceive?:0}"/>--%>
                                                                       <div data-product-id="${inventoryItem?.product?.id }" class="quantityToReceive"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                                                                   </g:elseif>
                                                                   <g:else>
                                                                       <span class="fade"><warehouse:message code="default.na.label"/></span>
                                                                   </g:else>
                                                               </td>
                                                               <td class="checkable top center" style="width: 7%; border-right: 1px solid lightgrey;">
                                                                   <g:if test="${!showQuantity }">

                                                                   </g:if>
                                                                   <g:elseif test="${inventoryItem?.supported && showQuantity}">
                                                                   <%-- <g:formatNumber number="${inventoryItem?.quantityToShip?:0}"/>--%>
                                                                       <div data-product-id="${inventoryItem?.product?.id }" class="quantityToShip"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                                                                   </g:elseif>
                                                                   <g:else>
                                                                       <span class="fade"><warehouse:message code="default.na.label"/></span>
                                                                   </g:else>
                                                               </td>
                                                               <td class="checkable top center" style="width: 7%;">

                                                                   <g:if test="${!showQuantity }">
                                                                       <g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
                                                                           <warehouse:message code="default.clickToView.label"/>
                                                                       </g:link>
                                                                   </g:if>
                                                                   <g:elseif test="${inventoryItem?.supported && showQuantity}">
                                                                       <g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
                                                                       <%-- <g:formatNumber number="${inventoryItem?.quantityOnHand?:0}"/>--%>
                                                                           <div data-product-id="${inventoryItem?.product?.id }" class="quantityOnHand"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
                                                                       </g:link>
                                                                   </g:elseif>
                                                                   <g:else>
                                                                       <span class="fade"><warehouse:message code="default.na.label"/></span>
                                                                   </g:else>

                                                               <%--
                                                               <div data-product-id="${inventoryItem?.product?.id }" class="quantityOnHand"></div>
                                                               --%>
                                                               </td>
                                                           </tr>

                                                       </g:if>
                                                       <%--
                                                       <g:elseif test="${inventoryItem.productGroup }">
                                                           <g:render template="browseProductGroup" model="[counter:counter,inventoryItem:inventoryItem,cssClass:'productGroup']"/>
                                                       </g:elseif>
                                                       --%>
                                                       <g:set var="counter" value="${counter+1 }"/>

                                                   </g:each>
                                                </g:each>
										    </g:if>

                                            <g:if test="${numProducts == 0}">
												<tr>
													<td colspan="11" class="even center">
														<div class="fade empty">

                                                            <warehouse:message code="inventory.searchNoMatch.message" args="[commandInstance?.searchTerms?:'',format.metadata(obj:commandInstance?.categoryInstance)]"/>
														</div>
													</td>
												</tr>
                                            </g:if>
                                        </tbody>
									</table>

								</form>		
							</div>

						</div>
                        <div class="paginateButtons">

                            <g:set var="pageParams"
                                   value="${[tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}"/>


                            <g:paginate total="${numProducts}"
                                        action="browse" max="${params.max}" params="${[tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}"/>

                            <div class="right">
                                <warehouse:message code="inventory.browseResultsPerPage.label"/>:
                                <g:if test="${params.max != '10'}"><g:link action="browse" params="${[max:10, tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}">10</g:link></g:if><g:else><span class="currentStep">10</span></g:else>
                                <g:if test="${params.max != '25'}"><g:link action="browse" params="${[max:25, tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}">25</g:link></g:if><g:else><span class="currentStep">25</span></g:else>
                                <g:if test="${params.max != '50'}"><g:link action="browse" params="${[max:50, tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}">50</g:link></g:if><g:else><span class="currentStep">50</span></g:else>
                                <g:if test="${params.max != '100'}"><g:link action="browse" params="${[max:100, tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}">100</g:link></g:if><g:else><span class="currentStep">100</span></g:else>
                                <g:if test="${params.max != '-1'}"><g:link action="browse" params="${[max:-1, tag: params.tag, searchTerms: params.searchTerms, subcategoryId: params.subcategoryId].findAll {it.value}}">${warehouse.message(code:'default.all.label') }</g:link></g:if><g:else><span class="currentStep">${warehouse.message(code:'default.all.label') }</span></g:else>
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
						//$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().removeClass('checked');
						return false;
					}
				);

				
				//$(".megamenu").megamenu();
				
				$("#toggleCheckbox").click(function(event) {
                    var checked = ($(this).attr("checked") == 'checked');
		            $(".checkbox").attr("checked", checked);
				});


		    	//$(".tabs").tabs(
	    		//	{
	    		//		cookie: {
	    		//			// store cookie for a day, without, it would be a session cookie
	    		//			expires: 1
	    		//		}
	    		//	}
				//);


		    	$(".isRelated").hide();
		    	$(".expandable").click(function(event) {
			    	//$("#productGroup-"+event.target.id).css('background-color', '#E5ECF9');
			    	var isVisible = $(".productGroup-"+event.target.id).is(":visible");
			    	if (isVisible) { 
				    	$("#productGroup-"+event.target.id).removeClass("showRelated");
				    	$("#productGroup-"+event.target.id).addClass("hideRelated");
				    }
			    	else { 
				    	$("#productGroup-"+event.target.id).addClass("showRelated");
				    	$("#productGroup-"+event.target.id).removeClass("hideRelated");
			    	}
			    	//$("#productGroup-"+event.target.id).removeClass("hideRelated");
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
						//$(this).html('Loading ...');
						var productId = $(this).attr("data-product-id");
						$(this).load('${request.contextPath}/json/getQuantityOnHand?product.id='+productId+'&location.id=${session.warehouse.id}');									    		  
			    	});

			    	$.each($(".quantityToShip"), function(index, value) {
						//$(this).html('Loading ...');
						var productId = $(this).attr("data-product-id");
						$(this).load('${request.contextPath}/json/getQuantityToShip?product.id='+productId+'&location.id=${session.warehouse.id}');									    		  
			    	});

			    	$.each($(".quantityToReceive"), function(index, value) {
						//$(this).html('Loading ...');
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
