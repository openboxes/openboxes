<%@page import="org.pih.warehouse.product.Product"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
	<title><warehouse:message code="default.search.label" args="[entityName]" /></title>
</head>
<body>

	<div class="body">
		<h1>Create product from National Drug Code data source</h1>
		<div class="dialog">	
			<g:form action="saveProduct" method="get">
			
				<table>
					<tr>
						<td width="75%">
						
						
							
							<div style="padding: 10px">
								<div class="tabs">
									<ul>
										<li><a href="#tabs-details"><warehouse:message code="product.details.label"/></a></li>
										<%-- Only show these tabs if the product has been created --%>
										<li><a href="#tabs-packages"><warehouse:message code="packages.label"/></a></li>
									</ul>		
									<div id="tabs-details" >													
										<table>				
											<tbody>
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.name.label"/></label>
													</td>
													<td>
														<g:textField name="name" value="${product.drugName }" size="80" class="text"/>
													</td>
												</tr>			
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.name.label"/></label>
													</td>
													<td>
														<g:categorySelect id="category" name="category.id" 
															value="${product?.category?.id}"/>										
													</td>
												</tr>														
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.productNdcCode.label"/></label>
													</td>
													<td>
														<g:textField name="ndc" value="${product.productNdcCode }"  class="text"/>
													</td>
												</tr>	
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.manufacturer.label"/></label>
													</td>
													<td>
														<g:textField name="manufacturer" value="${product.labelerName }" size="60" class="text"/>
														
													</td>
												</tr>
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.unitOfMeasure.label"/></label>
													</td>
													<td>
														<g:textField name="unitOfMeasure" value="${product.dosageForm }" size="40" class="text"/>
														
													</td>
												</tr>
	
											</tbody>
										</table>
									</div>
									<div id="tabs-packages">													
										<table>
											<tbody>						
											
												<tr class="prop">
													<td class="name">
														<label><warehouse:message code="product.packageDescription.label"/></label>
													</td>
													<td class="value">
														
														<g:textField name="packages[0].name" value="${product.packageDescription }" class="text" size="80"/>									
													</td>
												
												</tr>
												<tr class="prop">
													<td class="name">
														<label>
															<warehouse:message code="package.ndc.label"/>
														</label>
													</td>
													<td class="value">
														<g:textField name="packages[0].gtin" value="${product.ndcCode }" class="text"/>
													</td>
												</tr>		
												
												<tr class="prop">
													<td class="name">
														<label>
															<warehouse:message code="package.uom.label"/>
														</label>
													</td>
													<td class="value middle">
														<div class="middle">
															<g:textField name="packages[0].quantity" value="${packageInstance?.quantity }" size="10" class="medium text"/> ${product.dosageForm }
															in
															1 
															<g:select name="packages[0].uom.id" from="${org.pih.warehouse.core.UnitOfMeasure.list() }" optionValue="name" optionKey="id" value="${pacakageInstance?.uom }" noSelection="['null':'']"></g:select>
															
														</div>
													</td>
												</tr>		
											</tbody>
										</table>										
										
									</div>
								</div>			
							</div>
						
							<div class="buttons">
								<g:submitButton name="Save"/>
								<a href="javascript:history.back();">&lsaquo; Back</a>
							</div>
						</td>
						<td>
						
							<h2>Product Details</h2>
							<table class="box">				
								<tbody>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.name.label"/></label>
										</td>
										<td>
											<label>${product.drugName }</label>
										</td>
									</tr>					
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.productNdcCode.label"/></label>
										</td>
										<td>
											${product.productNdcCode }
										</td>
									</tr>	
													
									
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.labelerName.label"/></label>
										</td>
										<td>
											${product.labelerName }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.nonProprietaryName.label"/></label>
										</td>
										<td>
											${product.nonProprietaryName }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.proprietaryName.label"/></label>
										</td>
										<td>
											${product.proprietaryName }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.dosageForm.label"/></label>
										</td>
										<td>	
											${product.dosageForm }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.route.label"/></label>
										</td>
										<td>	
											${product.route }
										</td>
									</tr>
									
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.strengthNumber.label"/></label>
										</td>
										<td>
											${product.strengthNumber }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.strengthUnit.label"/></label>
										</td>
										<td>
											${product.strengthUnit }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.productType.label"/></label>
										</td>
										<td>
											${product.productType }
										</td>
									</tr>				
								</tbody>
							</table>
							
							<h2>Product Packaging</h2>
							
							<table class="box">				
								<tbody>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.packageDescription.label"/></label>
										</td>
										<td>
											<label>${product.packageDescription }</label>
										</td>					
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.ndcCode.label"/></label>
										</td>
										<td>
											${product.ndcCode }
										</td>
									</tr>
									<tr class="">
										<td class="name">
											<label><warehouse:message code="product.labelerName.label"/></label>
										</td>
										<td>
											${product.labelerName }
										</td>
									</tr>
											
								</tbody>
							</table>								
						
						</td>
					</tr>
				
				</table>
						
						
				
				
			</g:form>

		</div>
	</div>
	
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