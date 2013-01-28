<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
        
        <g:if test="${productInstance?.id}">
	        <title>${productInstance?.name }</title>
		</g:if>
		<g:else>
	        <title><warehouse:message code="product.add.label" /></title>	
			<content tag="label1"><warehouse:message code="inventory.label"/></content>
		</g:else>

		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>

    </head>    
    <body>        
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>
            
   			<g:if test="${productInstance?.id }">         
				<g:render template="summary" model="[productInstance:productInstance]"/>
			</g:if>
			
			<div style="padding: 10px">
				<div class="tabs">
					<ul>
						<li><a href="#tabs-details"><warehouse:message code="product.details.label"/></a></li>
						<%-- Only show these tabs if the product has been created --%>
						<g:if test="${productInstance?.id }">
							<li><a href="#tabs-packages"><warehouse:message code="packages.label"/></a></li>
							<li><a href="#tabs-status"><warehouse:message code="product.status.label"/></a></li>						
							<li><a href="#tabs-documents"><warehouse:message code="product.documents.label"/></a></li>
						</g:if>
					</ul>		
					<div id="tabs-details" style="padding: 10px;">	
						<g:set var="formAction"><g:if test="${productInstance?.id}">update</g:if><g:else>save</g:else></g:set>			
			            <g:form action="${formAction}" method="post">
							<g:hiddenField name="action" value="save"/>                					
			                <g:hiddenField name="id" value="${productInstance?.id}" />
			                <g:hiddenField name="version" value="${productInstance?.version}" />
			            	<g:hiddenField name="categoryId" value="${params?.category?.id }"/><!--  So we know which category to show on browse page after submit -->
				                <table>
			                      <tbody>               
									<tr class="prop">
										<td valign="top" class="name"><label for="name"><warehouse:message
											code="product.title.label" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
											<%-- 
											<g:textField name="name" value="${productInstance?.name}" size="80" class="medium text" />									
											--%>
											<g:autoSuggestString id="name" name="name" size="60" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" value="${productInstance?.name}"
												placeholder="Product title (e.g. Ibuprofen, 200 mg, tablet)"/>
										</td>
									</tr>
									
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="categories"><warehouse:message code="product.primaryCategory.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
											<%-- 
											<g:selectCategoryMcDropdown id="category" name="category.id" 
												value="${productInstance?.category?.id}"/>									
											--%>

		                                	 <div class="category">
												<select name="category.id">
													<option value="null">Choose product family</option>
													<g:render template="../category/selectOptions" model="[category:rootCategory, selected:productInstance?.category, level: 0]"/>
												</select>	
									       	</div>
									   </td>
									</tr>
									
									<tr class="prop">
										<td valign="top" class="name"><label for="description"><warehouse:message
											code="product.description.label" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: productInstance, field: 'description', 'errors')}">
											<g:textArea name="description" value="${productInstance?.description}" class="medium text" cols="80" rows="6"
												placeholder="Detailed text description (optional)"/>									
										</td>
									</tr>
									
									<%-- 
									<tr class="prop">
										<td valign="top" class="name"></td>
										<td valign="top" class="value">
											<g:autoSuggest_v2 id="product" 
												name="product" 
												jsonUrl="${request.contextPath }/json/findProductByName" 
												styleClass="medium text"
												size="60"
												valueId="${productInstance?.id }" 
												valueName="${productInstance?.name }"/>		
										</td>
									</tr>
									--%>							
									
												
									<%-- 
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="categories"><warehouse:message code="product.otherCategories.label" /></label>
		                                </td>
										<td valign="top" class="value">									
									       	<g:render template="categories" model="['productInstance':productInstance]" />
										</td>
									</tr>
									--%>
									
										
									<%-- 
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="categories"><warehouse:message code="categories.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
											<g:chooseCategory name="category.id" product="${productInstance}"/>
										</td>
									</tr>
									--%>
									<tr class="prop">
										<td valign="top" class="name"><label for="unitOfMeasure"><warehouse:message
											code="product.unitOfMeasure.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
											<%-- 
											<g:textField name="unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="15" class="medium text"/>
											--%>
											<g:autoSuggestString id="unitOfMeasure" name="unitOfMeasure" size="30" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.unitOfMeasure}" placeholder="e.g. each, tablet, tube, vial"/>											
										</td>
									</tr>				
									<tr class="prop">
										<td valign="top" class="name"><label for="coldChain"><warehouse:message
											code="product.coldChain.label" /></label></td>
										<td valign="top" class=" ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
											<g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<h3><warehouse:message
											code="product.tags.label" /></h3>
										</td>
									</tr>										
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="categories"><warehouse:message code="product.tags.label" /></label>
		                                </td>
										<td valign="top" class="value">									
									       	<%-- 
									       	<g:render template="tags" model="['productInstance':productInstance]" />
									       	--%>
									       	
									       	<g:textField id="tags1" class="tags" name="tagsToBeAdded" value="${productInstance?.tagsToString() }"/>
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
										<td colspan="2">
											<h3><warehouse:message
											code="product.identifiers.label" /></h3>
										</td>
									</tr>									
									<tr class="prop">
										<td valign="top" class="name"><label for="productCode"><warehouse:message
											code="product.productCode.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
											<g:textField name="productCode" value="${productInstance?.productCode}" size="50" class="medium text" 
												placeholder="${warehouse.message(code:'product.productCode.placeholder') }"/>
										</td>
									</tr>	
									
									<tr class="prop">
										<td valign="top" class="name"><label for="upc"><warehouse:message
											code="product.upc.label" /></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
											<g:textField name="upc" value="${productInstance?.upc}" size="50" class="medium text"/>
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="ndc"><warehouse:message
											code="product.ndc.label" /></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
											<g:textField name="ndc" value="${productInstance?.ndc}" size="50" class="medium text"
												placeholder="e.g. 0573-0165"/>
										</td>
									</tr>													
									<tr>
										<td colspan="2">
											<h3><warehouse:message
											code="product.manufacturer.label" /></h3>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label for="manufacturer"><warehouse:message
											code="product.manufacturer.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
											<%-- 
											<g:textField name="unitOfMeasure" value="${productInstance?.manufacturer}" size="60" class="medium text"/>
											--%>
											<g:autoSuggestString id="manufacturer" name="manufacturer" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.manufacturer}"
												placeholder="e.g. Pfizer, Beckton Dickson"/>
											
										</td>
									</tr>		
									<tr class="prop">
										<td valign="top" class="name"><label for="brandName"><warehouse:message
											code="product.brandName.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
											<g:autoSuggestString id="brandName" name="brandName" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.brandName}"
												placeholder="e.g. Advil, Tylenol"/>
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="manufacturerCode"><warehouse:message
											code="product.manufacturerCode.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
											<g:autoSuggestString id="manufacturerCode" name="manufacturerCode" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.manufacturerCode}"
												placeholder=""/>
												
										</td>
									</tr>	
									<tr class="prop">
										<td valign="top" class="name"><label for="manufacturerName"><warehouse:message
											code="product.manufacturerName.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturerName', 'errors')}">
											<g:autoSuggestString id="manufacturerName" name="manufacturerName" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.manufacturerName}"
												placeholder=""/>
												
										</td>
									</tr>	
									
															
									<tr class="prop">
										<td valign="top" class="name"><label for="modelNumber"><warehouse:message
											code="product.modelNumber.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'modelNumber', 'errors')}">
											<g:autoSuggestString id="modelNumber" name="modelNumber" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.modelNumber}" promptOnMatch="true"
												placeholder="e.g. Usually only pertains to equipment "/>
										</td>
									</tr>								
									<tr>
										<td colspan="2">
											<h3><warehouse:message
											code="product.vendor.label" /></h3>
										</td>
									</tr>
									
									<tr class="prop">
										<td valign="top" class="name"><label for="vendor"><warehouse:message
											code="product.vendor.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'vendor', 'errors')}">
											<g:autoSuggestString id="vendor" name="vendor" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.vendor}"
												placeholder="e.g. IDA, IMRES, McKesson"/>
											
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="vendorCode"><warehouse:message
											code="product.vendorCode.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'vendorCode', 'errors')}">
											<g:autoSuggestString id="vendorCode" name="vendorCode" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.vendorCode}"
												placeholder=""/>
										</td>
									</tr>	
									<tr class="prop">
										<td valign="top" class="name"><label for="vendorName"><warehouse:message
											code="product.vendorName.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'vendorName', 'errors')}">
											<g:autoSuggestString id="vendorName" name="vendorName" size="50" class="text" 
												jsonUrl="${request.contextPath}/json/autoSuggest" 
												value="${productInstance?.vendorName}"
												placeholder=""/>
												
										</td>
									</tr>													
															
														
									 
									<g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
										<tr class="prop">
											<td valign="top" class="name">
												<label for="attributes"><format:metadata obj="${attribute}"/></label>
											</td>
											<td valign="top" class="value">
												<g:set var="attributeFound" value="f"/>
												<g:if test="${attribute.options}">
													<select name="productAttributes.${attribute?.id}.value" class="attributeValueSelector">
														<option value=""></option>
														<g:each var="option" in="${attribute.options}" status="optionStatus">
															<g:set var="selectedText" value=""/>
															<g:if test="${productInstance?.attributes[status]?.value == option}">
																<g:set var="selectedText" value=" selected"/>
																<g:set var="attributeFound" value="t"/>
															</g:if>
															<option value="${option}"${selectedText}>${option}</option>
														</g:each>
														<g:set var="otherAttVal" value="${productInstance?.attributes[status]?.value != null && attributeFound == 'f'}"/>
														<g:if test="${attribute.allowOther || otherAttVal}">
															<option value="_other"<g:if test="${otherAttVal}"> selected</g:if>>
																<g:message code="product.attribute.value.other" default="Other..." />
															</option>
														</g:if>
													</select>
												</g:if>
												<g:set var="onlyOtherVal" value="${attribute.options.isEmpty() && attribute.allowOther}"/>
												<g:textField class="otherAttributeValue" style="${otherAttVal || onlyOtherVal ? '' : 'display:none;'}" name="productAttributes.${attribute?.id}.otherValue" value="${otherAttVal || onlyOtherVal ? productInstance?.attributes[status]?.value : ''}"/>
											</td>
										</tr>
									</g:each>
									
									
												
									<tr class="prop">
										<td valign="top" class="">
										</td>
										<td>
											<button type="submit" class="button icon approve">
												${warehouse.message(code: 'default.button.save.label', default: 'Save')}
											</button>
											<g:if test="${productInstance.id && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()}">
												<g:link class="button icon trash" action="delete" id="${productInstance.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
										  			${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}
									  			</g:link>
											</g:if>
											&nbsp;
											<g:if test="${productInstance?.id }">
												<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="">			
													${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
												</g:link>  
											</g:if>
											<g:else>
												<g:link controller="inventory" action="browse" class="">
													${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
												</g:link>
											</g:else>
										</td>
									</tr>
						
								</tbody>
							</table>
						</g:form>
					</div>
					<%-- Only show these tabs if the product has been created --%>
					<g:if test="${productInstance?.id }">					
						<div id="tabs-status" style="padding: 10px;">						
				            <g:if test="${flash.message}">
				            	<div class="message">${flash.message}</div>
				            </g:if>
				            <g:hasErrors bean="${inventoryLevelInstance}">
					            <div class="errors">
					                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
					            </div>
				            </g:hasErrors>
							
							<g:form controller="inventoryItem" action="updateInventoryLevel">
								<g:hiddenField name="id" value="${inventoryLevelInstance?.id}"/>
								<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
								<g:hiddenField name="product.id" value="${productInstance?.id}"/>
								<table>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventory.label"/></label></td>
										<td class="value">
											${inventoryInstance?.warehouse?.name }
										</td>
									</tr>
									<g:if test="${productInstance }">
										<tr class="prop">
											<td class="name"><label><warehouse:message code="product.label"/></label></td>
											<td class="value">
												<format:product product="${productInstance}" />
											</td>
										</tr>
									</g:if>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventoryLevel.status.label"/></label></td>
										<td class="value">
								           	<g:select name="status" 
				           					   from="${org.pih.warehouse.inventory.InventoryStatus.list()}"
				           					   optionValue="${{format.metadata(obj:it)}}" value="${inventoryLevelInstance.status}" 
				           					   noSelection="['':warehouse.message(code:'inventoryLevel.chooseStatus.label')]" />&nbsp;&nbsp;	
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventoryLevel.binLocation.label"/></label></td>
										<td class="value">
											<g:textField name="binLocation" value="${inventoryLevelInstance?.binLocation }" size="20" class="text"/>
										</td>
									</tr>
									
									
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventoryLevel.minQuantity.label"/></label></td>
										<td class="value">
											<g:textField name="minQuantity" value="${inventoryLevelInstance?.minQuantity }" size="10" class="text"/>
											<span class="fade">${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}</span>
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventoryLevel.reorderQuantity.label"/></label></td>
										<td class="value">
											<g:textField name="reorderQuantity" value="${inventoryLevelInstance?.reorderQuantity }" size="10" class="text"/>
											<span class="fade">${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}</span>
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="inventoryLevel.maxQuantity.label"/></label></td>
										<td class="value">
											<g:textField name="maxQuantity" value="${inventoryLevelInstance?.maxQuantity }" size="10" class="text"/>
											<span class="fade">${productInstance?.unitOfMeasure?:warehouse.message(code:'default.each.label')}</span>
										</td>
									</tr>
									<tr class="prop">
										<td></td>
										<td>
										
											<button type="submit" class="button icon approve">${warehouse.message(code: 'default.button.save.label', default: 'Save')}
											</button>
											&nbsp;
											<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="">			
												${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}			
											</g:link>  
										
										</td>
									</tr>
								</table>			
								
							</g:form>			
						</div>
					
						<div id="tabs-documents" style="padding: 10px;">					
							<!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->					
							<g:uploadForm controller="product" action="upload">
								<g:hiddenField name="product.id" value="${productInstance?.id}" />
								<g:hiddenField name="document.id" value="${documentInstance?.id}" />
								<table>
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message
											code="document.selectUrl.label" default="Select URL" /></label>
										</td>
										<td valign="top" class="value">
											<g:textField name="url" value="${params.url }" placeholder="http://www.example.com/images/image.gif" class="text medium" size="80"/>
											&nbsp;
											<!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
											<button type="submit" class="button icon approve">
												${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
										</td>
									</tr>						
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message
											code="document.selectFile.label" /></label>
										</td>
										<td valign="top" class="value">
											<input name="fileContents" type="file" />
											&nbsp;
											<!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
											<button type="submit" class="button icon approve">
												${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
										</td>
									</tr>						
									<tr class="prop">
										<td valign="top" class="name"><label for="documents"><warehouse:message
													code="documents.label" /></label></td>
										<td valign="top"
											class="value">
											
											<table class="box">
												<thead>
													<tr>
														<th>
															<!-- Delete -->
														</th>
														<th>
															<warehouse:message code="document.filename.label"/>
														</th>
														<th>
															<warehouse:message code="document.contentType.label"/>
														</th>
														<th>
															<warehouse:message code="default.dateCreated.label"/>
														</th>
														<th>
															<warehouse:message code="default.lastUpdated.label"/>
														</th>
														<th>
														</th>
													</tr>
												</thead>
												<tbody>
													<g:each var="document" in="${productInstance?.documents }" status="i">
														<tr class="prop ${i%2?'even':'odd' }" >
															<td>
																<g:link action="deleteDocument" id="${document?.id}" params="['product.id':productInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
																	<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
																</g:link>												
															</td>
															<td>
																${document.filename }
															</td>	
															<td>
																${document.contentType }
															</td>	
															<td>
																${document.dateCreated }
															</td>
															<td>
																${document.lastUpdated }
															</td>
															<td>
																<img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}" 
																	class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />
															</td>
														</tr>						
													</g:each>
													<g:unless test="${productInstance?.documents }">
														<tr>
															<td colspan="3">
																<div class="padded fade center">													
																	<warehouse:message code="product.hasNoDocuments.message"/>
																</div>
															</td>
														</tr>
													</g:unless>
												</tbody>									
											</table>
										</td>
									</tr>
								</table>
							</g:uploadForm>
						</div>
						
						
						<div id="tabs-packages" style="padding: 10px;">						
				            <%-- <g:if test="${flash.message}">
				            	<div class="message">${flash.message}</div>
				            </g:if>
				            
				            <g:hasErrors bean="${inventoryLevelInstance}">
					            <div class="errors">
					                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
					            </div>
				            </g:hasErrors>
							--%>
							
							<div class="buttonBar">            	
								<span class="linkButton">
									<%--
									<g:link class="create" controller="productPackage" action="create">Add product package</g:link>							
									 --%>
									<a href="javascript:void(0);" class="open-dialog create" dialog-id="createProductPackage"><warehouse:message code="package.add.label"/></a>
								</span>
				            	<span class="linkButton">											
									<a href="javascript:void(0);" class="open-dialog create" dialog-id="uom-dialog">Add UoM</a>
								</span>
								<%-- 
								<span class="linkButton">
									<a href="javascript:void(0);" class="open-dialog create" dialog-id="uom-class-dialog">Add UoM Class</a>
								</span>
								--%>
							</div>
							<div class="box">
								<table>
									<thead>
										<tr>
											<th>
												<warehouse:message code="default.actions.label"/>
											</th>
											<th>
												<warehouse:message code="package.name.label"/>
											</th>
											<th>
												<warehouse:message code="package.uom.label"/>
											</th>
											<th>
												<warehouse:message code="package.description.label"/>
											</th>
											<th>
												<warehouse:message code="package.gtin.label"/>
											</th>
										</tr>
									</thead>
									<tbody>
										<g:each var="pkg" in="${productInstance.packages }">
											<tr>
												<td>
												
													<div class="action-menu">
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>							
														</button>
														<div class="actions">
															<div class="action-menu-item">								
															
																<a href="javascript:void(0);" class="open-dialog create" dialog-id="editProductPackage-${pkg?.id }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"/>&nbsp;
																	<warehouse:message code="default.edit.label" args="[warehouse.message(code:'package.label')]"/>
																</a>					
															</div>	
															<div class="action-menu-item">													
																<g:link controller="product" action="removePackage" id="${pkg.id }" params="['product.id':productInstance.id]" class="actionBtn">	
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}"/>&nbsp;
																	<warehouse:message code="default.delete.label" args="[warehouse.message(code:'package.label')]"/>
																</g:link>
															</div>	
														</div>
													</div>												
												</td>											
												<td>
													${pkg.name }
												</td>
												<td>
													${pkg.uom.name }
												</td>
												<td>
													${pkg.uom.code }/${pkg.quantity }
												</td>
												<td>
													${pkg.gtin }
												</td>
												
											</tr>
										</g:each>
										<g:unless test="${productInstance?.packages }">
											<tr>
												<td colspan="6">
													<warehouse:message code="package.packageNotFound.message"/>
												</td>
											</tr>
										</g:unless>
										
									</tbody>
								</table>
							
							</div>
						</div>
						<div id="uom-class-dialog" class="dialog hidden" title="Add a unit of measure class">
							<g:form controller="unitOfMeasureClass" action="save" method="post">

								<table>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="name">Name</label>
		                                </td>
		                                <td valign="top" class="value ">
											<g:textField name="name" size="10" class="medium text" />
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="code">Type</label>
		                                </td>
		                                <td valign="top" class="value ">
											<g:select name="type" from="${org.pih.warehouse.core.UnitOfMeasureType.list() }" value="${packageInstance?.uom }" noSelection="['null':'']"></g:select>
		                                    <span class="fade"></span>
		                                </td>
		                            </tr>
								
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="code">Code</label>
		                                </td>
		                                <td valign="top" class="value ">
		                                    <g:textField name="code" size="10" class="medium text" />
		                                    <span class="fade"></span>
		                                </td>
		                            </tr>
									<tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="name">Name</label>
		                                </td>
		                                <td valign="top" class="value ">
		                                    <g:textField name="name" size="40" class="medium text" />
		                                    <span class="fade"></span>
		                                </td>
		                            </tr>
		                        
	                            </table>
								<div class="buttons">
				                   <input type="submit" name="create" class="save" value="Create" id="create" />						                   
				                   <a href="#" class="close-dialog" dialog-id="uom-class-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
				                </div>   	                            
		           

						
							</g:form>						
						</div>
							
						<div id="uom-dialog" class="dialog hidden" title="Add a unit of measure">
						
							<g:form controller="unitOfMeasure" action="save" method="post">
								<table>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="uomClass">Uom Class</label>
		                                </td>
		                                <td valign="top" class="value ">		                                
											<g:select name="uomClass.id" from="${org.pih.warehouse.core.UnitOfMeasureClass.list() }" optionValue="name" optionKey="id" value="${pacakageInstance?.uom }" noSelection="['null':'']"></g:select>		                                
											<span class="linkButton">
												<a href="javascript:void(0);" class="open-dialog create" dialog-id="uom-class-dialog">Add UoM Class</a>
											</span>
		                                </td>
		                            </tr>				                        
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="code">Code</label>
		                                </td>
		                                <td valign="top" class="value ">
		                                    <g:textField name="code" size="10" class="medium text" />
		                                </td>
		                            </tr>
									<tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="name">Name</label>
		                                </td>
		                                <td valign="top" class="value ">
		                                    <g:textField name="name" size="40" class="medium text" />
		                                </td>
		                            </tr>
		                        
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="description">Description</label>
		                                </td>
		                                <td valign="top" class="value ">
											<g:textField name="description" size="40" class="medium text" />		                                    
		                                </td>
		                            </tr>
		                        
		                      	</table>
		                      	
								<div class="buttons">
				                   <input type="submit" name="create" class="save" value="Create" id="create" />						                   
				                   <a href="#" class="close-dialog" dialog-id="uom-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
				                </div>   		                      	
		                	</g:form>				
						</div>
						
						<g:each var="packageInstance" in="${productInstance.packages }">
							<g:set var="dialogId" value="${'editProductPackage-' + packageInstance.id}"/>
							<g:render template="productPackageDialog" model="[dialogId:dialogId,productInstance:productInstance,packageInstance:packageInstance]"/>
						</g:each>		
						<g:render template="productPackageDialog" model="[dialogId:'createProductPackage',productInstance:productInstance,packageInstance:packageInstance]"/>
					</g:if>			
				</div>	
			</div>
		</div>
		<g:render template='category' model="['category':null,'i':'_clone','hidden':true]"/>
	
	
		<script type="text/javascript">
	    	$(document).ready(function() {
	    	
	    	
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				); 

				$(".dialog").dialog({ autoOpen: false, modal: true, width: '600px'});	

				$(".open-dialog").click(function() { 
					var id = $(this).attr("dialog-id");
					$("#"+id).dialog('open');
				});
				$(".close-dialog").click(function() { 
					var id = $(this).attr("dialog-id");
					$("#"+id).dialog('close');
				});

				$(".attributeValueSelector").change(function(event) {
					if ($(this).val() == '_other') {
						$(this).parent().find(".otherAttributeValue").show();
					}
					else {
						$(this).parent().find(".otherAttributeValue").val('').hide();
					}
				});

				
			});
		</script> 				
		
	</body>
</html>
