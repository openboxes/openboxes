<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
        
        <g:if test="${productInstance?.id}">
	        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
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
												jsonUrl="${request.contextPath}/json/findProductNames" value="${productInstance?.name}"
												placeholder="Product title (e.g. Ibuprofen, 200 mg, tablet)"/>
										</td>
									</tr>
									
									<tr class="prop">
										<td valign="top" class="name"><label for="productCode"><warehouse:message
											code="product.productCode.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
											<g:textField name="productCode" value="${productInstance?.productCode}" size="60" class="medium text" 
												placeholder="Internal product code used to identify the product"/>
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
												jsonUrl="${request.contextPath}/json/findUnitOfMeasures" 
												value="${productInstance?.unitOfMeasure}" placeholder="e.g. each, tablet, tube, vial"/>
											
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
											<g:autoSuggestString id="manufacturer" name="manufacturer" size="30" class="text" 
												jsonUrl="${request.contextPath}/json/findManufacturers" 
												value="${productInstance?.manufacturer}"
												placeholder="e.g. Pfizer, Beckton Dickson"/>
											
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="name"><warehouse:message
											code="product.manufacturerCode.label"/></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
											<g:textField name="manufacturerCode" value="${productInstance?.manufacturerCode}" size="30" 
												class="medium text"/>
										</td>
									</tr>	
															
									<tr class="prop">
										<td valign="top" class="name"><label for="upc"><warehouse:message
											code="product.upc.label" /></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
											<g:textField name="upc" value="${productInstance?.upc}" size="30" class="medium text"/>
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="ndc"><warehouse:message
											code="product.ndc.label" /></label></td>
										<td valign="top" class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
											<g:textField name="ndc" value="${productInstance?.ndc}" size="30" class="medium text"
												placeholder="e.g. 0573-0165"/>
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="coldChain"><warehouse:message
											code="product.coldChain.label" /></label></td>
										<td valign="top" class=" ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
											<g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
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
									
									<script type="text/javascript">
										$(document).ready(function() {
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
												
									<tr class="prop">
										<td valign="top" class="">
										</td>
										<td>
											<button type="submit" class="button"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
												alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
											</button>
											&nbsp;
											<!-- we only can delete products that 1) exist, and 2) dont have associated transaction entries or shipment items -->
											<g:if test="${productInstance.id && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()}">
												<g:link action="delete" id="${productInstance.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
										                <button type="button" class="button"><img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="Delete" /> ${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}</button></g:link>
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
										<td class="name"><label><warehouse:message code="inventoryLevel.minimumQuantity.label"/></label></td>
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
										<td></td>
										<td>
										
											<button type="submit" class="button"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
												alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
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
										<td valign="top" class="name"><label for="documents"><warehouse:message
													code="documents.label" /></label></td>
										<td valign="top"
											class="value">
											
											<table>
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
													</tr>
												</thead>
												<tbody>
													<g:each var="document" in="${productInstance?.documents }">
														<tr>
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
									<tr class="prop">
										<td valign="top" class="name"><label><warehouse:message
											code="document.selectFile.label" /></label>
										</td>
										<td valign="top" class="value">
											<input name="fileContents" type="file" />
											&nbsp;
											<!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
											<button type="submit" class="button"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
															alt="save" />${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
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
									<a href="javascript:void(0);" class="open-dialog create" dialog-id="package-dialog"><warehouse:message code="package.add.label"/></a>
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
												<warehouse:message code="package.gtin.label"/>
											</th>
											<th>
												<warehouse:message code="package.description.label"/>
											</th>
											<th>
												<warehouse:message code="package.uom.label"/>
											</th>
										</tr>
									</thead>
									<tbody>
										<g:each var="pkg" in="${productInstance.packages }">
											<tr>
												<td>
												
													<span class="action-menu">
														<button class="action-btn">
															<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
															<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>							
														</button>
														<span class="actions">
															<span class="action-menu-item">													
																<g:link controller="product" action="removePackage" id="${pkg.id }" params="['product.id':productInstance.id]" class="actionBtn">	
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}"/>
																	&nbsp;<warehouse:message code="default.delete.label" args="[warehouse.message(code:'package.label')]"/>
																</g:link>
															</span>	
														</span>
													</span>												
												</td>											
												<td>
													${pkg.gtin }
												</td>
												<td>
													${pkg.uom } / ${pkg.quantity } ${warehouse.message(code: 'default.units.label') }
												</td>
												
												<td>
													<g:formatNumber number="${pkg.quantity}" format="###,##0" />
												</td>
												<td>
													${pkg.uom }
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
							
						<div id="package-dialog" class="dialog hidden" title="${warehouse.message(code:'package.add.label') }">
							<g:form controller="product" action="savePackage" method="post">
								<g:hiddenField name="id" value="${productInstance?.id}"/>							
								<table>
									<tbody>						
										<g:if test="${productInstance }">
											<tr class="prop">
												<td class="name"><label><warehouse:message code="product.label"/></label></td>
												<td class="value">
													<format:product product="${productInstance}" />
												</td>
											</tr>
										</g:if>
										<tr class="prop">
											<td class="name">
												<label>
													<warehouse:message code="package.gtin.label"/>
												</label>
											</td>
											<td class="value">
												<g:textField name="gtin" value="${packageInstance?.gtin }" class="medium text"/>
											</td>
										</tr>		
										<%-- 
										<tr class="prop">
											<td class="name">
												<label>
													<warehouse:message code="package.quantity.label"/>
												</label>
											</td>
											<td class="value">
												<g:textField name="quantity" value="${packageInstance?.quantity }" size="5" class="medium text"/>
											</td>
										</tr>
										--%>	
										<tr class="prop">
											<td class="name">
												<label>
													<warehouse:message code="package.uom.label"/>
												</label>
											</td>
											<td class="value middle">
												<div class="middle">
													1 
													<g:select name="uom.id" from="${org.pih.warehouse.core.UnitOfMeasure.list() }" optionValue="name" optionKey="id" value="${pacakageInstance?.uom }" noSelection="['null':'']"></g:select>
													= 
													<g:textField name="quantity" value="${packageInstance?.quantity }" size="10" class="medium text"/>
												${productInstance?.unitOfMeasure }	
												</div>
											</td>
										</tr>		
										<%-- 										
										<tr class="prop">
											<td class="name">
												<label>
													<warehouse:message code="package.name.label"/>
												</label>
											</td>
											<td class="value">
												<g:textField name="name" value="${packageInstance?.name }" class="medium text"/>
											</td>
										</tr>	
										<tr class="prop">
											<td class="name">
												<label>
													<warehouse:message code="package.description.label"/>
												</label>
											</td>
											<td class="value">
												<g:textField name="description" value="${packageInstance?.description }" class="medium text"/>
											</td>
										</tr>	
										--%>
										
										
																														
									</tbody>
								</table>
								<div class="buttons">
									<button type="submit" class="button"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" alt="Save" /> 
										${warehouse.message(code: 'default.button.save.label', default: 'Save')}
									</button>
									&nbsp;
									
									<a href="#" class="close-dialog" dialog-id="package-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>
									
												
								</div>
							</g:form>
						</div>						
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
				
			});
		</script> 				
		
	</body>
</html>
