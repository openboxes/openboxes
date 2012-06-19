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

			<div class="buttonBar">            	
            	<span class="linkButton">
            		<g:link class="list" action="list">
            			<warehouse:message code="default.list.label" args="[warehouse.message(code:'products.label').toLowerCase()]"/>
            		</g:link>
            	</span>
            	<span class="linkButton">
            		<g:link class="showStockCard" controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
            			<warehouse:message code="inventory.showStockCard.label" />
            		</g:link>
            	</span>
           	</div>

			<div class="dialog">

				<div class="tabs">
					<ul>
						<li><a href="#tabs-1"><warehouse:message code="product.details.label"/></a></li>
						<li><a href="#tabs-2"><warehouse:message code="product.documents.label"/></a></li>
					</ul>		
					<div id="tabs-1" style="padding: 0px;">	
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
											code="default.description.label" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
										<g:textField name="name" value="${productInstance?.name}" size="40" />
										</td>
									</tr>
									<tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="categories"><warehouse:message code="categories.label" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
		                                	<%-- <g:render template="../category/chooser"/>--%>
		                                	 <div class="category">
												<select name="category.id">
													<option value="null"></option>
													<g:render template="../category/selectOptions" model="[category:rootCategory, selected:productInstance?.category, level: 0]"/>
												</select>	
									       	</div>
									       	<g:render template="categories" model="['productInstance':productInstance]" />
									   </td>
									</tr>					
									
									<tr class="prop">
										<td valign="top" class="name"><label for="name"><warehouse:message
											code="default.unitOfMeasure.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
										<g:textField name="unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="15" />
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="manufacturer"><warehouse:message
											code="product.manufacturer.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
										<g:textField name="manufacturer" value="${productInstance?.manufacturer}" size="40" />
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="name"><warehouse:message
											code="product.manufacturerCode.label"/></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
										<g:textField name="manufacturerCode" value="${productInstance?.manufacturerCode}" size="15" />
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="upc"><warehouse:message
											code="product.upc.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
										<g:textField name="upc" value="${productInstance?.upc}" size="15" />
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="ndc"><warehouse:message
											code="product.ndc.label" /></label></td>
										<td valign="top"
											class="${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
										<g:textField name="ndc" value="${productInstance?.ndc}" size="15" />
										</td>
									</tr>								
									<tr class="prop">
										<td valign="top" class="name"><label for="coldChain"><warehouse:message
											code="product.coldChain.label" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
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
											<button type="submit" class="positive"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
												alt="Save" /> ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
											</button>
											&nbsp;
											<!-- we only can delete products that 1) exist, and 2) dont have associated transaction entries or shipment items -->
											<g:if test="${productInstance.id && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()}">
											<g:link action="delete" id="${productInstance.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
									                <button type="button" class="negative"><img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="Delete" /> ${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}</button></g:link>
											</g:if>
											&nbsp;
											<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="negative">			
												${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}			
											</g:link>  
										</td>
									</tr>
						
								</tbody>
							</table>
					</g:form>
				</div>
				<div id="tabs-2" style="padding: 0px;">					
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
												</th>
												<th>
													<warehouse:message code="document.contentType.label"/>
												</th>
												<th>
													<warehouse:message code="document.filename.label"/>
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
														${document.contentType }
													</td>	
													<td>
														${document.filename }
													</td>	
												</tr>						
											</g:each>
											<g:unless test="${productInstance?.documents }">
												<tr>
													<td colspan="3" class="left">
														<warehouse:message code="product.hasNoDocuments.message"/>
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
									<button type="submit" class="positive"><img
										src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}"
													alt="save" />${documentInstance?.id ? warehouse.message(code:'default.button.save.label') : warehouse.message(code:'default.button.upload.label')}</button>
								</td>
							</tr>						
						</table>
					</g:uploadForm>
				</div>
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
	    });
       </script>
</body>
</html>
