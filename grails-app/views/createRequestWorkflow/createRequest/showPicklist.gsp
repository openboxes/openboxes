<%@page import="java.text.Format"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.showPicklistItems.label"/></title>
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
			<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
			<g:render template="header" model="['state':'showPicklist']"/>
			<table>
				<tr>
					<td >						
					
						<div class="center" style="padding: 10px;">
							<g:form action="createRequest" method="post">
								<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
								<input type="text" id="add-item-quantity" name='quantity' value="${requestItem?.quantity }" size="5" class="text" placeholder="${warehouse.message(code:'default.quantity.label')}"/>
								<g:autoSuggest id="item" name="item" jsonUrl="${request.contextPath}/json/findRequestItems" 
									width="400" styleClass="text" valueId="" valueName=""
									placeholder="${warehouse.message(code:'request.addItem.label')}" />															
							
								<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.addItem.label')}"></g:submitButton>
							</g:form>		
						</div>
						
						<div>
							<table>
								<thead>
									<tr class="odd">
										<th class="right">${warehouse.message(code:'default.quantity.label')}</th>
										<th>${warehouse.message(code:'default.item.label')}</th>
										<th class="right">${warehouse.message(code:'default.quantity.label')}</th>
										<th>${warehouse.message(code:'default.item.label')}</th>
									</tr>
								</thead>
								<tbody>
									<g:set var="i" value="${0 }"/>
									<g:each var="requestItem" in="${requestInstance?.requestItems}">
										<tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
											<g:hiddenField name="requestItems[${i }].request.id" value="${requestItem?.request?.id }" size="5"/>
											<td class="right">
												${requestItem?.quantity }
												<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
											</td>
											<td>
												<span class="fade">${requestItem.type}</span>
												<format:metadata obj="${requestItem.displayName()}"/>
												<g:if test="${requestItem?.product }">
													(${requestItem?.product?.unitOfMeasure })
												</g:if>
											</td>
											
										</tr>
									</g:each>
								</tbody>								
							</table>
						</div>
					</td>
					<%-- 
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
					--%>	
				</tr>
			</table>

			<g:form action="createRequest" autocomplete="false">
				<div class="buttons" style="border-top: 1px solid lightgrey;">
					<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
					<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
					<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
				</div>
			</g:form>
				

		</div>

	</div>
	
        <script>

			$(document).ready(function(){

				$("#add-item-quantity").focus();
                
            });
        </script>

</body>
</html>