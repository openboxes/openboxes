
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.browse.label" args="[entityName]" /></content>
    
    	<style>
    		.selected { font-weight: bold; border: 2px solid black; background-color: whitesmoke; padding: 5px; } 
    	
    	</style>
    </head>    


    <body>
    
    
<script type="text/javascript">
	$(function() { 
		$('#productSearch').accordion({active: true, navigation: true, autoheight: false, alwaysOpen: true, clearStyle: true });
	});
</script>
    
        <div class="body" style="width: 95%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
			<table>
				<tr>
					<td>			
						<script type="text/javascript">
							$(function() { $("#productSearchTabs").tabs(); });
						</script>
						<div id="productSearchTabs">
							<ul>
								<li><a href="#tabs-1">Serenic Code</a></li>
								<li><a href="#tabs-2">Categories</a></li>
								<li><a href="#tabs-3">Search</a></li>
							</ul>
							<div id="tabs-1">
							
								<h3>Filter by Serenic Code</h3><br/>
								<table>
									<tr>
										<g:each in="${productTypes}" status="i" var="productType">
											<td>
												<span class="${(productType?.name==selectedProductType?.name)?'selected':''}">
													<a href="${createLink(action:'browse',params:["productTypeId":productType.id])}">${productType.name}</a>
												</span>
											</td>
											<g:if test="${(i+1)%6==0}"></tr><tr></g:if>
										</g:each>
									</tr>
								</table>							
							</div>
							<div id="tabs-2">							
								<h3>Filter by Category</h3><br/>
								<table>
									<tr>
										<g:each in="${categories}" status="i" var="category">
											<td>
												<span class="${(category?.name==selectedCategory?.name)?'selected':''}" style="text-variant: small-caps;">
													<a href="${createLink(action:'browse',params:["categoryId":category.id])}">${category.name}</a>
												</span>
												<div style="padding: 10px;">
												<ul>
													<g:each in="${category.categories}" status="j" var="childCategory">
														<li>
															<span class="${(childCategory?.name==selectedCategory?.name)?'selected':''}">
																<a href="${createLink(action:'browse',params:["categoryId":childCategory.id])}">${childCategory.name}</a>
															</span>
														</li>
													</g:each>
												</ul>
												</div>
											</td>
											<g:if test="${(i+1)%6==0}"></tr><tr></g:if>
											
										</g:each>
									</tr>
								</table>														
							</div>
							<div id="tabs-3">
								<h3>Filter by Name</h3><br/>
								<g:form method="get" action="browse">
									<table>
										<tr class="prop">
											<td class="name">
												<label>Name contains</label>
											</td>
											<td>
												<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
											</td>
											<td colspan="2" valign="top">
												<g:checkBox name="unverified" value="${params.unverified}" /> Show  only invalid products <br/>											 
											</td>										
											<td>
												<span class="buttons">
													<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Filter" /> 
														${message(code: 'default.button.filter.label', default: 'Filter')}</button>
												</span>											
											</td>
											
										</tr>
<%-- 	
									<tr class="prop">
											<td class="name">
												<label>Has product type</label>
											</td>
											<td>
												<g:select multiple="true" size="5" 
													name="productTypeId" 
													from="${productTypes}" 
													value="${selectedProductType}"
													optionKey="id" 
													optionValue="name">
												</g:select>										
											</td>
											<td class="name">
												<label>Has category</label>
											</td>
											<td>
												<g:selectCategory name="categoryId" rootNode="${rootCategory}" />
											</td>
										</tr>
										<tr class="prop">
											<td class="name">
												<label>Has attribute(s)</label>
											</td>
											<td>
												<g:select multiple="true" 
													name="attributeId" 
													from="${org.pih.warehouse.product.Attribute.list()}" 
													value="${selectedAttribute}"
													optionKey="id" 
													optionValue="name">
												</g:select>
											</td>
										</tr>
										<tr class="prop">
											<td class="name">
												<label>Match</label>
											</td>
											<td>
												<g:radio name="match" value="matchAll" checked="true" disabled="true" /> Match all  &nbsp;&nbsp;&nbsp;<br/>
												<g:radio name="match" value="matchAll" disabled="true"/> Match any <i>(not supported)</i>											 
											</td>
											<td>
												<span class="buttons">
													<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Filter" /> 
														${message(code: 'default.button.filter.label', default: 'Filter')}</button>
												</span>											
											</td>
											
										</tr>
--%>
									</table>	
								</g:form>			
							</div>
							
							
						</div>
					</td>					
				</tr>
				
				<tr>
					<td colspan="2">
						<div>
							<fieldset>
			            		<legend>Search results</legend>						
								<div>
									Your search returned ${productInstanceList.size} products.  
					            </div>					            
					            <g:if test="${productInstanceList.size > 0}">
					                <table width="100%">
					                    <thead>
					                        <tr>             
					                        	<%-- 
					                            <g:sortableColumn property="id" title="${message(code: 'product.id.label', default: 'ID')}" />
					                            --%>
					                            <th width="5%" style="text-align: center">${message(code: 'product.type.label', default: 'Type')}</th>
					                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
					                            <g:sortableColumn property="upc" title="${message(code: 'product.upc.label', default: 'UPC')}" />
					                            <g:sortableColumn property="productType" title="${message(code: 'product.productType.label', default: 'Serenic Code')}" />
					                            <g:sortableColumn property="categories" title="${message(code: 'product.categories.label', default: 'Categories')}" />
					                            <g:sortableColumn property="tags" title="${message(code: 'product.tags.label', default: 'Tags')}" />
					                            <g:sortableColumn property="complete" title="${message(code: 'product.unverified.label', default: 'Valid')}" />
					                        </tr>
					                    </thead>
					                    <tbody>
						                    <g:each in="${productInstanceList}" status="i" var="productInstance">
						                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						                        	            
													<td style="text-align: center" width="5%">										
														<%--<g:link action="show" id="${productInstance.id}">${fieldValue(bean: productInstance, field: "id")}</g:link> --%>
														<g:if test="${productInstance.class.simpleName == 'DrugProduct'}">															
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'pill.png')}"/>
														</g:if>
														<g:elseif test="${productInstance.class.simpleName == 'DurableProduct' }">
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'computer.png')}"/>
														</g:elseif>
														<g:else>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'attach.png')}"/>
														</g:else>
													</td>
													<td align="center">
														<g:link action="edit" id="${productInstance.id}">
															${fieldValue(bean: productInstance, field: "name")}
														</g:link>
													</td>
													<td align="center" width="10%">
														${fieldValue(bean: productInstance, field: "upc")}
													</td>
													<td width="10%">
														${fieldValue(bean: productInstance, field: "productType.name")}
													</td>					                            
													<td width="10%">
														${fieldValue(bean: productInstance, field: "categories")}
													</td>					                            
													<td width="10%">
														${fieldValue(bean: productInstance, field: "tags")}
													</td>					                            
													<td valign="top" style="text-align: center" width="5%">
														<g:if test="${productInstance.unverified}">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Invalid" />														
														</g:if>
														<g:else>
															<img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Valid" />														
														</g:else>
													</td>
						                        </tr>
						                    </g:each>		                    
					                    </tbody>
					                </table>       
					        	</g:if>         
							</fieldset>
			            </div>
					</td>
				</tr>
			</table>
        </div>
    </body>
</html>
