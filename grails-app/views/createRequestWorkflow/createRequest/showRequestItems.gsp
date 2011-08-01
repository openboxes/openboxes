<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add request items</title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<div class="dialog">
			<fieldset>
				<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
				<table>
					<tr>
						<td >
							<div style="margin: 10px">
								<p>There are ${(requestInstance?.requestItems)?requestInstance?.requestItems?.size():0 } items in this request.</p>
							</div>							
						
							
							<div style="max-height: 300px; overflow-y: auto;">
								<table style="">
									<thead>
										<tr class="odd">
											<th>Actions</th>
											<g:sortableColumn property="quantity" title="Quantity" />
											<g:sortableColumn property="name" title="Name" />
											<g:sortableColumn property="type" title="Type" />
										</tr>
									</thead>
									<tbody>
										<g:set var="i" value="${0 }"/>
										<g:each var="requestItem" in="${requestInstance?.requestItems}">
											<tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
												<g:hiddenField name="requestItems[${i }].request.id" value="${requestItem?.request?.id }" size="5"/>
												<td class="actionButtons">
													<g:if test="${requestItem?.id }">
														<%-- 
														<g:link action="createRequest" id="${requestItem.id}" event="editItem" params="[selectedTab:requestItem.type]">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"/>
														</g:link>
														--%>
														<g:link action="createRequest" id="${requestItem.id}" event="deleteItem" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}"/>
														</g:link>
													</g:if>
												</td>
												<td>
													${requestItem?.quantity }
													<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
												</td>
												<td>
													${requestItem?.description?.encodeAsHTML()}
												</td>
												<td>
													${requestItem?.type }
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</div>
						</td>
						<td style="border-left: 1px solid lightgrey; height: 100%; width: 35%;">
											
							<g:hasErrors bean="${requestItem}">
								<div class="errors">
									<g:renderErrors bean="${requestItem}" as="list" />
								</div>
							</g:hasErrors>														
							<div class="dialog">
								<div class="tabs">
									<ul>
										<li><a href="#Product">Product</a></li>
										<li><a href="#Category">Category</a></li>
										<li><a href="#Unclassified">Unclassified</a></li> 
									</ul>
									<div id="Product">
										<g:form action="createRequest" method="post">
											<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
											<g:hiddenField name="requestItem.id" value="${requestItem?.id }"></g:hiddenField>
											<g:hiddenField id="selectedTab" name="selectedTab" value="${requestItem?.type }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='product.id'>Product:</label></td>
														<td valign='top' class='value' nowrap="nowrap">
															<%-- 
															<div class="ui-widget">
																<g:select class="combobox" name="product.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" optionKey="id" value="" noSelection="['':'']" />
															</div>
															--%>
															<g:autoSuggest id="product" name="product" jsonUrl="/warehouse/json/findProductByName" 
																width="200" valueId="${requestItem?.product?.id }" valueName="${requestItem?.product?.name }"/>															
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'>Quantity:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr>
														<td valign="top" class="value" colspan="2">
														
															<div class="buttons">
																<g:if test="${requestItem?.id }">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="Update Item"></g:submitButton> 
																	</span>
																</g:if>
																<g:else>
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
																	</span>
																</g:else>
															</div>
														</td>
													</tr>
												</tbody>
											</table>
										</g:form>
									</div>
									<div id="Category">
										<g:form action="createRequest" method="post">
											<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='source'>Category:</label>
														</td>
														<td valign='top' class='value'>													
															<div class="ui-widget"> 
																<g:categorySelect id="category" name="category.id" value="${requestItem?.category?.id }" />
																
																<%--
																<g:select class="combobox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
																 --%>
															</div>
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'>Quantity:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
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
									<div id="Unclassified">
										<g:form action="createRequest" method="post">
											<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
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
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
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

				<g:form action="createRequest" autocomplete="false">
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="back" value="Back"></g:submitButton>
						<g:submitButton name="next" value="Next"></g:submitButton>
						<g:link action="createRequest" event="cancel">Cancel</g:link>
					</div>
				</g:form>
				

			</fieldset>
		</div>

	</div>
	
        <script>

			$(document).ready(function(){
				$( ".tabs" ).tabs();

		        var tabId = $("#selectedTab").val();
		        if (tabId) { 
	            	$('.tabs').tabs('select', tabId);
		        }
		        else { 

	            
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
		        }
                
            });
        </script>

</body>
</html>