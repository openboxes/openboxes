<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		
		
		
		<div class="dialog">
			<fieldset>
				<g:render template="/order/summary" model="[orderInstance:order]"/>
				<table>
					<tr>
						<td style="padding: 0px;">
							<div style="margin: 10px">
								<p><warehouse:message code="order.itemsInOrder.message" args="[(order?.orderItems)?order?.orderItems?.size():0]"/></p>
							</div>							
						
							
							<div style="max-height: 300px; overflow-y: auto;">
								<table style="">
									<thead>
										<tr class="odd">
											<th><warehouse:message code="default.actions.label"/></th>
											<g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
											<g:sortableColumn property="name" title="${warehouse.message(code:'default.name.label')}" />
											<g:sortableColumn property="type" title="${warehouse.message(code:'default.type.label')}" />
											<g:sortableColumn class="right" property="unitPrice" title="${warehouse.message(code:'order.unitPrice.label')}" />
											<g:sortableColumn class="right" property="totalPrice" title="${warehouse.message(code:'order.totalPrice.label')}" />
										</tr>
									</thead>
									<tbody>
										<g:set var="i" value="${0 }"/>
										<g:each var="orderItem" in="${order?.orderItems}">
											<tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
												<g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>
												<td class="actionButtons">
													<g:if test="${orderItem?.id }">
														<g:link action="purchaseOrder" id="${orderItem.id}" event="editItem">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"/>
														</g:link>
														<g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}"/>
														</g:link>
													</g:if>
												</td>
												<td>
													${orderItem?.quantity }
													<g:hiddenField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"/>
												</td>
												<td>
													${orderItem?.description?.encodeAsHTML()}
												</td>
												<td>
													${orderItem?.orderItemType }
												</td>
												<td class="right">
													<g:formatNumber number="${orderItem?.unitPrice ?: 0.00 }" format="###,##0.00" />
												</td>
												<td class="right">	
													<g:formatNumber number="${orderItem?.totalPrice() }" format="###,##0.00" />													
												</td>
											</tr>
										</g:each>
										<tfoot>
											<tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
												<td colspan="5"></td>
												<td class="right"><g:formatNumber number="${order?.totalPrice() }" format="###,##0.00" /></td>
											</tr>
										</tfoot>											
									</tbody>
								</table>
							</div>
						</td>
						<td style="border-left: 1px solid lightgrey; padding: 0px; height: 100%; width: 35%;">
											
							<g:hasErrors bean="${orderItem}">
								<div class="errors">
									<g:renderErrors bean="${orderItem}" as="list" />
								</div>
							</g:hasErrors>														
							<div class="dialog">
								<div class="tabs">
									<ul>
										<li><a href="#tabs-1"><warehouse:message code="order.addProduct.label"/></a></li>
										<%--
										<li><a href="#tabs-2">Category</a></li>
										<li><a href="#tabs-3">Unclassified</a></li> 
										--%>
									</ul>
									<div id="tabs-1">
										<g:form action="purchaseOrder" method="post">
											<g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
											<g:hiddenField name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='product.id'><warehouse:message code="product.label"/>:</label></td>
														<td valign='top' class='value' nowrap="nowrap">
															<%-- 
															<div class="ui-widget">
																<g:select class="comboBox" name="product.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" optionKey="id" value="" noSelection="['':'']" />
															</div>
															--%>
															<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName" 
																width="200" valueId="${orderItem?.product?.id }" valueName="${orderItem?.product?.name }"/>															
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${orderItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'><warehouse:message code="order.unitPrice.label"/>:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='unitPrice' value="${orderItem?.unitPrice }" size="5" />
														</td>
													</tr>
													<tr>
														<td valign="top" class="value" colspan="2">
														
															<div class="buttons">
																<g:if test="${orderItem?.id }">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="${warehouse.message(code:'order.updateItem.label')}"></g:submitButton> 
																	</span>
																</g:if>
																<g:else>
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="${warehouse.message(code:'order.addItem.label')}"></g:submitButton> 
																	</span>
																</g:else>
															</div>
														</td>
													</tr>
												</tbody>
											</table>
										</g:form>
									</div>
									<%--
									<div id="tabs-2">
										<g:form action="purchaseOrder" method="post">
											<g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='source'>Category:</label>
														</td>
														<td valign='top' class='value'>													
															<div class="ui-widget"> 
																<g:select class="comboBox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
															</div>
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'>Quantity:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${orderItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr>
														<td valign="top" colspan="2">
															<div class="buttons">
																<span class="formButton"> 
																	<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																</span>
															</div>
														</td>
													</tr>
												</tbody>
											</table>
										</g:form>	
									</div>
									<div id="tabs-3">
										<g:form action="purchaseOrder" method="post">
											<g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='description'>Description:</label>
														</td>
														<td valign='top' class='value'>
															<input type="text" name='description' value="" size="30" />
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'>Quantity:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${orderItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr>
														<td valign="top" class="value" colspan="2">
															<div class="buttons">
																<span class="formButton"> 
																	<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																</span>
															</div>
														</td>
													</tr>
												</tbody>
											</table>
										</g:form>							
									</div>
									 --%>
								</div>								
							</div>
						</td>			
					</tr>
				</table>

				<g:form action="purchaseOrder" autocomplete="false">
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="back" value="${warehouse.message(code:'order.backToOrderDetails.label')}"></g:submitButton>
						<g:submitButton name="next" value="${warehouse.message(code:'order.continueToOrderSummary.label')}"></g:submitButton>
						<%-- 
						<g:submitButton name="finish" value="${warehouse.message(code:'default.button.saveAndExit.label')}"></g:submitButton> 
						--%>
						<g:link action="purchaseOrder" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</div>
				</g:form>
				

			</fieldset>
		</div>

	</div>
	<g:comboBox />
        <script type="text/javascript">
            $( function()
            {
                var cookieName, $tabs, stickyTab;

                cookieName = 'stickyTab';
                $tabs = $( '.tabs' );

                $tabs.tabs( {
                    select: function( e, ui )
                    {
                        $.cookies.set( cookieName, ui.index );
                    }
                } );

                stickyTab = $.cookies.get( cookieName );
                if( ! isNaN( stickyTab )  )
                {
                    $tabs.tabs( 'select', stickyTab );
                }
            } );
        </script>
        <%--
        <script>
		$(function() {
			$( ".tabs" ).tabs();
		});
	</script>
 --%>

</body>
</html>