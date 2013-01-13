
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
		
	</head>
	<body>
		<div class="body">	
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if> 
			<g:hasErrors bean="${command}">
				<div class="errors"><g:renderErrors bean="${command}" as="list" /></div>
			</g:hasErrors>

			<div id="upload-form" class="box">
				<g:uploadForm controller="product" action="uploadCsv">
					<input name="location.id" type="hidden" value="${session.warehouse.id }"/>
					<input name="type" type="hidden" value="product"/>
					<table>
						<tbody>
							<tr >
								<td colspan="2">
									<span class="title">
										<warehouse:message code="product.import.step1.label" default="Step 1"/>.
										<warehouse:message code="product.import.label" default="Upload CSV file"/>
									</span>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label><warehouse:message code="import.file.label" default="File"/></label>
								</td>
								<td class="value">
									<input name="importFile" type="file" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									&nbsp;
								</td>
								<td>
									<button type="submit" class="button">
										${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}
									</button>
									&nbsp;
									<a href="${createLink(controller: "product", action: "importAsCsv")}" >
										<warehouse:message code="default.button.cancel.label"/>
									</a>							
										
								</td>
							</tr>
						</tbody>				
					</table>
				</g:uploadForm>
			</div>
			
			<g:if test="${command?.products && !productsHaveBeenImported}">
				<g:form controller="product" action="importCsv" method="POST">			
					<div id="verify" class="box">					
						<input name="location.id" type="hidden" value="${session.warehouse.id }"/>
						<input name="type" type="hidden" value="product"/>								
						<table>
							<tbody>
								<tr >
									<td colspan="2">
										<span class="title">
											<warehouse:message code="product.import.step2.label" default="Step 2"/>.
											<warehouse:message code="product.verify.label" default="Verify products"/>
										</span>
									</td>
								</tr>
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="import.filename.label" default="Filename"/></label>
									</td>								
									<td class="value">
										${command?.importFile?.originalFilename }
									</td>
								</tr>
								<tr class="prop">
									<td class="name">
										<label><warehouse:message code="import.numOfRecords.label" default="# of Records"/></label>
									</td>								
									<td class="value">
										${command?.products?.size() } ${warehouse.message(code:'import.products.label', default: 'products') }
									</td>
								</tr>
								<tr class="prop">
									<td class="name">
										<label>${warehouse.message(code:'products.label')}</label>
									</td>
									<td class="value">
										<div style="max-height: 300px; overflow: auto;">
											<table>		
												<thead>
													<tr>
														<th>#</th>
														<g:each var="column" in="${columns }">
															<th><warehouse:message code="import.${column.value}.label" default="${column }"/></th>
														</g:each>
														<th><warehouse:message code="default.actions.label"/></th>
													</tr>							
												</thead>
												<tbody>							
													<g:each var="product" in="${command?.products }" status="status">
														<g:set var="existingProduct" value="${existingProductsMap[product] }"/>	
														<g:set var="maxLength" value="${product?.description?.length() }"/>
														<tr class="${status%2?'even':'odd' }">
															<td>${status+1 }</td>
															<td>
																<g:if test="${product?.id }">		
																	<g:if test="${product?.id?.length() == 32 }">													
																		<span title="${product?.id }">${product?.id?.substring(20, 32) }</span>
																	</g:if>
																	<g:else>
																		${product?.id }
																	</g:else>
																</g:if>
																<g:else>
																	<span class="modified">${warehouse.message(code: 'default.new.label') }</span>
																</g:else>
															</td>
															<td class="${product?.productCode!=existingProduct?.productCode?'modified':'' }">
																<span title="${existingProduct?.productCode }">${product?.productCode }</span>
															</td>
															<td class="${product?.name!=existingProduct?.name?'modified':'' }">
																<span title="${existingProduct?.name }">${product?.name }</span>
															</td>
															<td class="${product?.category!=existingProduct?.category?'modified':'' }"><span title="Was: ${existingProduct?.category }">${product?.category }</span></td>
															<td class="${product?.description!=existingProduct?.description?'modified':'' }">
																<g:if test="${maxLength > 15 }">
																	<span title="${product?.description }">...</span>
																</g:if>
																<g:else>
																	${product?.description }
																</g:else>					
															</td>				
															<td class="${product?.unitOfMeasure!=existingProduct?.unitOfMeasure?'modified':'' }">${product?.unitOfMeasure }</td>
															<td class="${product?.manufacturer!=existingProduct?.manufacturer?'modified':'' }">${product?.manufacturer }</td>
															<td class="${product?.manufacturerCode!=existingProduct?.manufacturerCode?'modified':'' }">${product?.manufacturerCode }</td>
															<td class="${product?.coldChain!=existingProduct?.coldChain?'modified':'' }">${product?.coldChain }</td>
															<td class="${product?.upc!=existingProduct?.upc?'modified':'' }">${product?.upc }</td>
															<td class="${product?.ndc!=existingProduct?.ndc?'modified':'' }">${product?.ndc }</td>
															<td class="fade">${product?.dateCreated }</td>
															<td class="fade">${product?.lastUpdated }</td>
															<td>
																<g:if test="${product?.id }">
																	<g:link controller="inventoryItem" action="showStockCard" id="${product.id }">
																		<warehouse:message code="product.import.stockCard.label" default="Stock card &rsaquo;"/>
																	</g:link>
																</g:if>
															</td>
														</tr>
														<g:if test="${product.hasErrors() }">
														<tr>
															<td colspan="13"><div class="errors"><g:renderErrors bean="${product}" as="list" /></div></td>
														</tr>
														</g:if>
													</g:each>
												</tbody>
											</table>	
										</div>																			
									</td>
								</tr>		
							</tbody>
						</table>
					</div>
					
					<div id="import" class="box">
						<table>
							<tbody>
								<tr>
									<td colspan="2">
										<span class="title">
											<warehouse:message code="product.import.step3.label" default="Step 3"/>.
											<warehouse:message code="product.import.label" default="Import products"/>
										</span>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="tags"><warehouse:message code="product.tags.label" /></label>
	                                </td>
									<td valign="top" class="value">									
								       	<g:textField id="tags1" class="tags" name="tagsToBeAdded" value="imported"/>
										<script>
											$(function() { 
												$('#tags1').tagsInput({
													'autocomplete_url':'${createLink(controller: 'json', action: 'findTags')}',
													'width': 'auto',
													'removeWithBackspace' : true,
												}); 
											});
										</script>
									</td>
								</tr>
								<tr>
									<td class="name">
									</td>
									<td class="value">
										<g:hiddenField name="importNow" value="${true }"/>
										<button type="submit" class="button">
											<%-- <img src="${createLinkTo(dir:'images/skin',file:'database_save.png')}" alt="upload" />--%> 
											${warehouse.message(code: 'default.button.import.label', default: 'Import')}</button>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</g:form>
			</g:if>
		</div>
			
	</body>
</html>
