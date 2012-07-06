<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.addRequestItems.label"/></title>
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
								<p><warehouse:message code="request.itemCount.message" args="[(requestInstance?.requestItems)?requestInstance?.requestItems?.size():0]"/>
							</div>							
						
							
							<div style="max-height: 300px; overflow-y: auto;">
								<table style="">
									<thead>
										<tr class="odd">
											<th><warehouse:message code="default.actions.label"/></th>
											<g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
											<g:sortableColumn property="name" title="${warehouse.message(code:'default.name.label')}" />
											<g:sortableColumn property="type" title="${warehouse.message(code:'request.type.label')}" />
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
														<g:link action="createRequest" id="${requestItem.id}" event="deleteItem" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}"/>
														</g:link>
													</g:if>
												</td>
												<td>
													${requestItem?.quantity }
													<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
												</td>
												<td>
													<format:metadata obj="${requestItem.displayName()}"/>
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
										<li><a href="#Product"><warehouse:message code="product.label"/></a></li>
										<li><a href="#Category"><warehouse:message code="category.label"/></a></li>
										<li><a href="#Unclassified"><warehouse:message code="default.unclassified.label"/></a></li> 
									</ul>
									<div id="Product">
										<g:form action="createRequest" method="post">
											<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
											<g:hiddenField name="requestItem.id" value="${requestItem?.id }"></g:hiddenField>
											<g:hiddenField id="selectedTab" name="selectedTab" value="${requestItem?.type }"></g:hiddenField>
											<table>
												<tbody>
													<tr class='prop'>
														<td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='product.id'><warehouse:message code="product.label"/>:</label></td>
														<td valign='top' class='value' nowrap="nowrap">
															<%-- 
															<div class="ui-widget">
																<g:select class="comboBox" name="product.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" optionKey="id" value="" noSelection="['':'']" />
															</div>
															--%>
															<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath}/json/findProductByName" 
																width="200" valueId="${requestItem?.product?.id }" valueName="${requestItem?.product?.name }"/>															
														</td>
													</tr>
													<tr>
														<td valign="top" class="value" colspan="2">
														
															<div class="buttons">
																<g:if test="${requestItem?.id }">
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.updateItem.label')}"></g:submitButton> 
																	</span>
																</g:if>
																<g:else>
																	<span class="formButton"> 
																		<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.addItem.label')}"></g:submitButton> 
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
														<td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='source'><warehouse:message code="category.label"/>:</label>
														</td>
														<td valign='top' class='value'>													
															<div class="ui-widget"> 
																<g:categorySelect id="category" name="category.id" value="${requestItem?.category?.id }" />
																
																<%--
																<g:select class="comboBox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
																 --%>
															</div>
														</td>
													</tr>
													<tr>
														<td valign="top" colspan="2">
															<div class="buttons">
																<span class="formButton"> 
																	<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.addItem.label')}"></g:submitButton> 
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
														<td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
														<td valign='top' class='value'>
															<input type="text" name='quantity' value="${requestItem?.quantity }" size="5" />
														</td>
													</tr>
													<tr class='prop'>
														<td valign='top' class='name'><label for='description'><warehouse:message code="default.description.label"/>:</label>
														</td>
														<td valign='top' class='value'>
															<input type="text" name='description' value="" size="30" />
														</td>
													</tr>
													<tr>
														<td valign="top" class="value" colspan="2">
															<div class="buttons">
																<span class="formButton"> 
																	<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.addItem.label')}"></g:submitButton> 
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
						<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
						<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
						<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
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