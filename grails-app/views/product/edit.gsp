
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        
        <g:if test="${productInstance?.id}">
	        <title><g:message code="default.edit.label" args="[entityName]" /></title>
			<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
		</g:if>
		<g:else>
	        <title><g:message code="default.create.label" args="[entityName]" /></title>
			<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>		
		</g:else>
    </head>    
    <body>    
        <div class="body">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
        
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<g:set var="formAction"><g:if test="${productInstance?.id}">update</g:if><g:else>save</g:else></g:set>			
            <g:form action="${formAction}" method="post">
				<g:hiddenField name="action" value="save"/>                					
                <g:hiddenField name="id" value="${productInstance?.id}" />
                <g:hiddenField name="version" value="${productInstance?.version}" />

                <div class="dialog">
					<fieldset>	              		
					
						<g:render template="summary"/>
					
		                <table>
	                      <tbody>                        
	                          <tr class="prop">
	                              <td valign="top" class="name">
	                                <label for="productClass"><g:message code="product.productClass.label" default="Class" /></label>
	                              </td>
	                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productClass', 'errors')}">
	                              		<g:if test="${!productInstance?.productClass }">
											<g:select name="productClass" from="${org.pih.warehouse.product.ProductClass.values()}"
												value="${productInstance?.productClass}"
												noSelection="${['null': '- Select -']}"/>
										</g:if>
										<g:else>
							                <g:hiddenField name="productClass" value="${productInstance?.productClass}" />											
											${productInstance?.productClass?.name }
										</g:else>

	                              </td>
	                          </tr>
	                          <tr class="prop">
	                              <td valign="top" class="name">
	                                <label for="productType.id"><g:message code="product.productType.label" default="Type" /></label>
	                              </td>
	                              <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productType', 'errors')}">
	                              
	                              		<%-- 
	                                  <g:select name="productType.id" from="${org.pih.warehouse.product.ProductType.list()}"
	                                  		optionKey="id" optionValue="name" value="${productInstance?.productType?.id}"  />
	                                  		--%>
									<g:autoSuggest id="productType" 
										name="productType" width="150"
										jsonUrl="/warehouse/json/findProductTypeByName" 
										valueId="${productInstance?.productType?.id }" 
										valueName="${productInstance?.productType?.name }"/>
	                              </td>
	                          </tr>	                          
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="name"><g:message code="drugProduct.name.label" default="Display Name" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
		                                    <g:textField name="name" value="${productInstance?.name}" size="30"/>
		                                    
		                                </td>
		                            </tr>
								<g:if test="${productInstance.productClass == org.pih.warehouse.product.ProductClass.DRUG}">	
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="brandName"><g:message code="product.brandName.label" default="Brand Name" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
		                                    <g:textField name="brandName" value="${productInstance?.brandName}" size="30" />
		                                </td>
		                            </tr>						
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="inn"><g:message code="drugProduct.inn.label" default="INN" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'inn', 'errors')}">
		                                    <g:textField name="inn" value="${productInstance?.inn}" size="30"/>
		                                    
		                                </td>
		                            </tr>
		                            
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                    <label for="dosage"><g:message code="drugProduct.dosage.label" default="Dosage" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'dosageStrength', 'errors')}">
		                                
											<table style="width:100px">
												<tr>
													<th>Strength</th>
													<th>Unit of Measure</th>
													<th>Dosage Form</th>
												</tr>
												<tr>
													<td>
					                                    <g:textField name="dosageStrength" value="${productInstance?.dosageStrength}" size="5"/>
													</td>
													<td>
						                                <g:autoSuggest id="unitOfMeasure" 
															name="unitOfMeasure" width="100"
															jsonUrl="/warehouse/json/findUnitOfMeasureByName" 
															valueId="${productInstance?.unitOfMeasure?.id }" 
															valueName="${productInstance?.unitOfMeasure?.name }"/>
													</td>
													<td>
														<g:autoSuggest id="dosageForm" 
															name="dosageForm" width="100"
															jsonUrl="/warehouse/json/findDosageFormByName" 
															valueId="${productInstance?.dosageForm?.id }" 
															valueName="${productInstance?.dosageForm?.name }"/>
													
													</td>
												</tr>
											
											</table>		                                

		                                    
		                                    <%-- 
		                                    <g:select name="dosageForm.id" 
		                                    	from="${org.pih.warehouse.product.DosageForm.list()}" 
		                                     	optionKey="id" optionValue="name"
		                                    	value="${productInstance?.dosageForm?.id}" 
		                                    	noSelection="['0': '-Select form-']" />
		                                    	
		                                    <g:select name="unitOfMeasure.id" 
												from="${org.pih.warehouse.core.UnitOfMeasure.list()}"
												optionKey="id" optionValue="name"
												value="${productInstance?.unitOfMeasure?.id}"
												noSelection="['0': '-Select unit-']">
											</g:select>	
		                                    --%>
		                                </td>
		                            </tr>
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
		                                    <g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
		                                    <label for="coldChain"><g:message code="product.coldChain.label" default="Requires Cold Chain" /></label>
		                                </td>
		                            </tr>								
								</g:if>								
								<g:elseif test="${productInstance.productClass == org.pih.warehouse.product.ProductClass.CONSUMABLE}">
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="brandName"><g:message code="product.brandName.label" default="Brand Name" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
		                                    <g:textField name="brandName" value="${productInstance?.brandName}" />
		                                </td>
		                            </tr>								
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="description"><g:message code="product.description.label" default="Description" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'inn', 'errors')}">
		                                    <g:textField name="inn" value="${productInstance?.inn}" />
		                                </td>
		                            </tr>								
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="coldChain"><g:message code="product.coldChain.label" default="Cold Chain" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')}">
		                                    <g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
		                                </td>
		                            </tr>								
								</g:elseif>
								<g:elseif test="${productInstance.productClass == org.pih.warehouse.product.ProductClass.DURABLE}">
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="make"><g:message code="product.make.label" default="Make" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'make', 'errors')}">
		                                    <g:textField name="make" value="${productInstance?.make}" />
		                                </td>
		                            </tr>								
		                            <tr class="prop">
		                                <td valign="top" class="name">
		                                  <label for="model"><g:message code="product.model.label" default="Model" /></label>
		                                </td>
		                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'model', 'errors')}">
		                                    <g:textField name="model" value="${productInstance?.model}" />
		                                </td>
		                            </tr>								
								</g:elseif>
								<tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="categories"><g:message code="product.categories.label" default="Categories" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}">
	                                	
	                                	<input type="hidden" id="myvalue" name="myvalue">
	                                	<span id="mytext" name="mytext" style="padding:0px;">
	                                		<!-- enter value -->
	                                	</span>
		                                <a href="#" id="showCategories" style="padding-left: 10px">change</a>
	                               	 	<script>
			                               	 $(document).ready(function() {
			                               		$(".selectableCategory").click(function() {
													$('#myvalue').val(this.id);
													$('#mytext').html(this.name);
				                               	 	$('#categories').hide();
			                               	 	});
			                               	 	$("#showCategories").click(function() {
				                               	 	$('#categories').show();
													//$('#showCategories').hide();
				                               	});
			                               	 });                               	 		
	                               	 	</script>
	                               	 	<style>
	                               	 		.parentCategory { 
		                               	 		list-style: square;
		                               	 		padding-left: 25px;
	                               	 		}
	                               	 		.childCategory { 
												list-style: square;	                               	 		
		                               	 		padding-left: 25px;
	                               	 		}
	                               	 	</style>
	                               	 	<div id="categories" style="background-color: #fafafa; display: none; overflow: auto; height: 180px; width: 300px;">
											<g:menu rootNode="${productInstance?.rootCategory}"/>
										</div>
									</td>
								</tr>	
								
								<%-- 							
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="categories"><g:message code="product.categories.label" default="Categories" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'categories', 'errors')}"
	                                	style="padding: 0px;">
	                                	<div style="height: 200px; overflow: auto; padding: 6px;">
                                    		<g:set var="counter" value="${1 }"/>
		                                    <g:each var="category" in="${org.pih.warehouse.product.Category.list().sort() { it?.categories?.size() }.reverse() }" status="status">
		                                    	<g:if test="${!category.parentCategory }">
		                                    		<div>
			                                    		<g:checkBox name="category_${category.id}" value="${productInstance?.categories?.contains(category) }"/> 
			                                    		<g:if test="${!category.parentCategory }"><b>${category.name }</b></g:if>
														<g:else>${category.name }</g:else>
													</div>
													<div style="padding-left: 35px;">									
				                                    	<g:each var="childCategory" in="${category.categories}">														
				                                    		<g:checkBox name="category_${childCategory.id}" value="${productInstance?.categories?.contains(childCategory) }"/> 
				                                    		<g:if test="${!childCategory.parentCategory }"><b>${childCategory.name }</b></g:if>
															<g:else>${childCategory.name }</g:else>
															<br/>
				                                    	</g:each>
			                                    	</div>
		                                    	</g:if>
		                                    </g:each>                                    		
	                                    </div>
	                                </td>
	                            </tr>
	                            --%>
								<g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
									<tr class="prop">
										<td valign="top" class="name"><label for="attributes">
										${attribute.name }
										</label> <g:hiddenField name="attributes[${status }].attribute.id"
											value="${attribute?.id }" /></td>
										<td valign="top" class="value">
											<g:if test="${attribute.options }">
												<g:select
													name="attributes[${status }].value" from="${attribute?.options}"
													value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }" />
												<g:if test="${attribute.allowOther}">
													<g:textField name="attributes[${status }].otherValue" value="" />
												</g:if>
											</g:if>
											<g:else>
												<g:textField name="attributes[${status }].value"
													value="${productInstance?.attributes ? productInstance?.attributes[status ]?.value : '' }"/>
											</g:else>
										</td>
									</tr>
								</g:each>
							<tr class="prop">
								<td valign="top" class="">
								</td>
								<td>
									<div class="buttons">
										<button type="submit" class="positive"><img
											src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
											alt="Save" /> ${message(code: 'default.button.save.label', default: 'Save')}
										</button>
										
										<g:if test="${productInstance?.id}">
											<g:link controller='product' action='show' id='${productInstance.id}' class="negative">			
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> ${message(code: 'default.button.save.label', default: 'Cancel')}			
											</g:link>  
										</g:if>
										<g:else>
											<g:link controller='product' action='browse' class="negative">			
												<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> ${message(code: 'default.button.save.label', default: 'Cancel')}			
											</g:link>  											
										</g:else>
										<!-- 
								           <button type="submit" class="negative" action="delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"> 
								                <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Delete" /> ${message(code: 'default.button.delete.label', default: 'Delete')}</button>
										-->
									</div>
								
								</td>
							</tr>
				
						</tbody>
					</table>
				</div>
			</g:form>
        </div>
    </body>
</html>
