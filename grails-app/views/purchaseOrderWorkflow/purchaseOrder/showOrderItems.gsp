<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add order items</title>
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
				<g:render template="/order/header" model="[orderInstance:order]"/>
				<table>
					<tr>
						<td >
							<div style="margin: 10px">
								<p>There are ${(order?.orderItems)?order?.orderItems?.size():0 } items in this order.</p>
							</div>							
						
							<g:form action="purchaseOrder" autocomplete="false">
								<div style="min-height: 175px">
									<table style="">
										<thead>
											<tr class="odd">
												<g:sortableColumn property="name" title="Name" />
												<g:sortableColumn property="type" title="Type" />
												<g:sortableColumn property="quantity" title="Quantity" />
												<th></th>
											</tr>
										</thead>
										<tbody>
											<g:each var="orderItem" in="${order?.orderItems}" status="i">
												<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
													<g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>
													<td>
														${orderItem?.description?.encodeAsHTML()}
													</td>
													<td>
														${orderItem?.orderItemType }
													</td>
													<td>
														<g:textField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"></g:textField>
													</td>
													<td class="actionButtons">
														<g:if test="${orderItem?.id }">
															<g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}"/>
															</g:link>
														</g:if>
													</td>
												</tr>
											</g:each>
										</tbody>
									</table>
								</div>
								<div class="buttons">
									<g:submitButton name="back" value="Back"></g:submitButton>
									<g:submitButton name="confirmOrder" value="Next"></g:submitButton>
								</div>
							</g:form>
						</td>
						<td style="border-left: 1px solid lightgrey; height: 100%; width: 35%;">
							<div style="margin: 10px">
								<p>Add an order item</p>
							</div>				
							<g:hasErrors bean="${orderItem}">
								<div class="errors">
									<g:renderErrors bean="${orderItem}" as="list" />
								</div>
							</g:hasErrors>														
								<div class="dialog">
									<div class="tabs">
										<ul>
											<li><a href="#tabs-1">Product</a></li>
											<li><a href="#tabs-2">Category</a></li>
											<li><a href="#tabs-3">Unclassified</a></li>
										</ul>
										<div id="tabs-1">
											<g:form action="purchaseOrder" method="post">
												<g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
												<table>
													<tbody>
														<tr class='prop'>
															<td valign='top' class='name'><label for='product.id'>Product:</label></td>
															<td valign='top' class='value' nowrap="nowrap">
																<div class="ui-widget">
																	<g:select class="combobox" name="product.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" optionKey="id" value="" noSelection="['':'']" />
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
																	<g:select class="combobox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
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
									</div>								
								</div>
						</td>			
					</tr>
				</table>

				

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