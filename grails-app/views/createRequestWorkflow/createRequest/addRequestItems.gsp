<%@page import="java.text.Format"%>
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
		
		<div class="">
			<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
			<g:render template="header" model="['state':'addRequestItems']"/>
			
			<div class="center box" style="margin-bottom: 5px;">
				<g:form action="createRequest" method="post">
					<label>Quick Add:</label>
					<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
					<input type="text" id="add-item-quantity" name='quantity' value="${requestItem?.quantity }" size="5" class="text" placeholder="${warehouse.message(code:'default.quantity.label')}"/>
					<g:autoSuggest id="item" name="item" jsonUrl="${request.contextPath}/json/findRequestItems" 
						width="400" styleClass="text" valueId="" valueName=""
						placeholder="${warehouse.message(code:'request.addItem.label')}" />															
				
					<g:submitButton name="addItem" value="${warehouse.message(code:'default.button.addItem.label')}"></g:submitButton>
					<span>
						<a href="#" dialog-id="create-request-item-dialog" class="open-dialog">Add detailed item</a>
					</span>
				</g:form>		
			</div>
			
			<div>
				<table>
					<thead>
						<tr class="odd">
							<th width="1%"></th>
							<th width="1%">${warehouse.message(code:'default.quantity.label')}</th>
							<th>${warehouse.message(code:'default.item.label')}</th>
							<th>${warehouse.message(code:'request.requestedBy.label')}</th>
							<th><warehouse:message code="default.actions.label"/></th>
						</tr>
					</thead>
					<tbody>
					
						<g:if test="${requestInstance?.requestItems }">
							<g:set var="i" value="${0 }"/>
							<g:each var="requestItem" in="${requestInstance?.requestItems.sort { it.dateCreated }.reverse() }">
								<tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
									<g:hiddenField name="requestItems[${i }].request.id" value="${requestItem?.request?.id }" size="5"/>
									<td>
										<g:if test="${requestItem?.type }">
											<img src="${createLinkTo(dir:'images/icons/type',file: requestItem?.type + '.png')}"/>
										</g:if>
										<g:else>
											<img src="${createLinkTo(dir:'images/icons/silk',file: 'page_white.png')}"/>
										</g:else>									
									</td>
									<td class="center">
										${requestItem?.quantity }
										<g:hiddenField name="requestItems[${i }].quantity" value="${requestItem?.quantity }" size="5"/>
									</td>
									<td>
										<format:metadata obj="${requestItem.displayName()}"/>
										<g:if test="${requestItem?.product }">
											(${requestItem?.product?.unitOfMeasure })
										</g:if>
									</td>
									<td>
										${requestItem?.requestedBy?.name }
									</td>											
									<td class="actionButtons">
										<g:if test="${requestItem?.id }">
											<%-- 
											<g:link action="createRequest" id="${requestItem.id}" event="editItem" params="[selectedTab:requestItem.type]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"/>
											</g:link>
											--%>
											<g:link action="createRequest" id="${requestItem.id}" event="deleteItem" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}"/>
											</g:link>
										</g:if>
									</td>
								</tr>
							</g:each>
						</g:if>
						<g:else>
							<tr>
								<td colspan="5" class="center">
									<span class="fade">${warehouse.message(code: 'request.noRequestItems.message') }</span>
								</td>
							</tr>
						</g:else>
					</tbody>								
				</table>
			</div>
					
					<%-- 
										
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
					--%>	
			

			<g:form action="createRequest" autocomplete="false">
				<div class="buttons" style="border-top: 1px solid lightgrey;">
					<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton>
					<g:submitButton name="next" value="${warehouse.message(code:'default.button.next.label')}"></g:submitButton>
					<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
				</div>
			</g:form>
				

		</div>
	</div>


	<div class="dialog" id="create-request-item-dialog">
		<g:form action="createRequest" method="post">
			<g:hiddenField name="request.id" value="${requestInstance?.id }"></g:hiddenField>
			<g:hiddenField name="requestItem.id" value="${requestItem?.id }"></g:hiddenField>
			<g:hiddenField id="selectedTab" name="selectedTab" value="${requestItem?.type }"></g:hiddenField>
			<table>
				<tbody>
					<tr class='prop'>
						<td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
						<td valign='top' class='value'>
							<input type="text" name='quantity' value="${requestItem?.quantity }" size="8" class="text"/>
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' class='name'><label for='product.id'><warehouse:message code="product.label"/>:</label></td>
						<td valign='top' class='value' nowrap="nowrap">
							<%-- 
							<g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath}/json/findProductByName" 
								width="200" valueId="${requestItem?.product?.id }" valueName="${requestItem?.product?.name }"/>															
							--%>
							
							<g:autoSuggest id="item-with-details" name="item" jsonUrl="${request.contextPath}/json/findRequestItems" 
								width="400" styleClass="text" valueId="" valueName=""
								placeholder="${warehouse.message(code:'request.addItem.label')}" />															
							
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' class='name'><label for='requestedBy'><warehouse:message code="request.requestedBy.label"/>:</label></td>
						<td valign='top' class='value'>

							<g:autoSuggest id="requestedBy" name="requestedBy" jsonUrl="${request.contextPath }/json/findPersonByName" 
								styleClass="text"
								placeholder="Requested by"
								valueId="${requestedBy?.id}" 
								valueName="${requestedBy?.name}"/>	
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


	<script>
		$(document).ready(function(){
			$("#add-item-quantity").focus();
			$(".dialog").dialog({ autoOpen: false, modal: true, width: '800px', top: 10});	
			$(".open-dialog").click(function() { 
				var id = $(this).attr("dialog-id");
				$("#" + id).dialog('open');
			});
			$(".close-dialog").click(function() { 
				var id = $(this).attr("dialog-id");
				$("#" + id).dialog('close');
			});
         });
     </script>

</body>
</html>