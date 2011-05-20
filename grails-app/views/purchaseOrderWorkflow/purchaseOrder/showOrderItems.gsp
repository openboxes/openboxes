<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add Order Items</title>
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
						<td>
							
							<p>There are ${order?.orderItems?.size() } items in this order.</p>
						
							<g:form action="purchaseOrder">
								<table>
									<thead>
										<tr class="odd">
											<g:sortableColumn property="id" title="ID" />
											<g:sortableColumn property="type" title="Type" />
											<g:sortableColumn property="category" title="Category" />
											<g:sortableColumn property="name" title="Name" />
											<g:sortableColumn property="quantity" title="Quantity" />
											<th></th>
										</tr>
									</thead>
									<tbody>
										<g:each var="orderItem" in="${order.orderItems.sort { it.id} }" status="i">
											<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
												<td>
													${orderItem?.id}
												</td>
												<td>
													<g:if test="${orderItem?.product }">
														Product
													</g:if>
													<g:elseif test="${orderItem?.category }">
														Category
													</g:elseif>							
													<g:else>
														Unclassified											
													</g:else>
												</td>
												<td>
													${orderItem?.category?.name}
												</td>
												<td>
													${orderItem?.description?.encodeAsHTML()}
												</td>
												<td>
													${orderItem?.quantity}
												</td>
												<td class="actionButtons">
													<g:if test="${orderItem?.id }">
														<g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
															${message(code: 'orderItem.delete.label', default: 'Delete')} 
														</g:link>
													</g:if>
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</g:form>
						</td>
						<td style="border-left: 1px solid lightgrey; height: 400px; width: 25%;">
							<div class="demo">
								<div id="tabs">
									<ul>
										<li><a href="#tabs-1">by Product</a></li>
										<li><a href="#tabs-2">by Category</a></li>
										<li><a href="#tabs-3">by Description</a></li>
									</ul>
									<div id="tabs-1">
										
										<g:form action="purchaseOrder" method="post">
											<div class="dialog">
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
																<input type="text" name='quantity' value="" size="5" />
															</td>
														</tr>
														<tr>
															<td valign="top" class="name">
															
															</td>
															<td valign="top" class="value">
																<div class="buttons">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																	</span>
																</div>
															</td>
														</tr>
													</tbody>
												</table>
											</div>
										</g:form>							
								
									</div>
									<div id="tabs-2">
									
										<g:form action="purchaseOrder" method="post">
											<div class="dialog">
												<table>
													<tbody>
														<tr class='prop'>
															<td valign='top' class='name'><label for='source'>Category:</label>
															</td>
															<td valign='top' class='value'>
																<g:jqueryComboBox />
																<div class="ui-widget"> 
																	<g:select class="combobox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
																</div>
															</td>
														</tr>
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
																<input type="text" name='quantity' value="" size="5" />
															</td>
														</tr>
														
														
														<tr>
															<td valign="top" class="name">
															
															</td>
															<td valign="top" class="value">
																<div class="buttons">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																	</span>
																</div>
															</td>
														</tr>
													</tbody>
												</table>
											</div>
										</g:form>					
							
									</div>
									<div id="tabs-3">
							
										<g:form action="purchaseOrder" method="post">
											<div class="dialog">
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
																<input type="text" name='quantity' value="" size="5" />
															</td>
														</tr>
														<tr>
															<td valign="top" class="name">
															
															</td>
															<td valign="top" class="value">
																<div class="buttons">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																	</span>
																</div>
															</td>
														</tr>
													</tbody>
												</table>
											</div>
										</g:form>			
									</div>
								</div>
							</div>					
							
		
						</td>			
					</tr>
				</table>

				<g:form action="purchaseOrder" method="post">
					<div class="buttons">
						<g:submitButton name="back" value="Back"></g:submitButton>
						<g:submitButton name="confirmOrder" value="Next"></g:submitButton>
					</div>
				</g:form>

			</fieldset>
		</div>

	</div>
	
        <script type="text/javascript">
            $( function()
            {
                var cookieName, $tabs, stickyTab;

                cookieName = 'stickyTab';
                $tabs = $( '#tabs' );

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
</body>
</html>