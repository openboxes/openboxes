
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
    </head>    
    <body>
        <div class="body" style="width: 95%">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>		
            <div>            
				<table>
					<tr>
						<td>			
							<script type="text/javascript">
								$( function() {
									var cookieName = 'stickyTab';				
									$( '#productSearchTabs' ).tabs( {
										selected: ( $.cookies.get( cookieName ) || 0 ),
										select: function( e, ui ) {
											$.cookies.set( cookieName, ui.index );
										}
									});
								});
							</script>	
							<%-- --%>											
							<div id="productSearchTabs">
								<ul>
									<li><a href="#tabs-1">Type</a></li>
									<li><a href="#tabs-2">Category</a></li>
								</ul>
								<div class="clear"></div>
								<div class="tab_contents_container">
								
									<div id="tabs-1">							
									
										<g:form method="get" action="browse">
											<table>
												<tr class="prop">
													<td class="">
														<label>Search </label>

														<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
														<span class="buttons">
															<button type="submit" class="positive">
																${message(code: 'default.button.go.label', default: 'Go')}</button>
														</span>											
													</td>
												</tr>
											</table>	
										</g:form>			
									
									
									
										<table>
											<tr>
												<td>
													<g:each in="${productTypes}" status="i" var="productType">
														<g:set var="selected" value="${productType?.id==selectedProductType?.id}"/>
														<g:if test="${selected }">
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_go.png')}" style="vertical-align: middle;"/>																			
														</g:if>
														<g:else>
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_white.png')}" style="vertical-align: middle;"/>																			
														</g:else>
														<span class="${(productType?.id==selectedProductType?.id)?'selected':''}">
															<a href="${createLink(action:'browse',params:["productTypeId":productType.id])}">${productType.name}</a>
															<%-- <a href="${request.request.requestURL }?${request.request.queryString }&productTypeId=${productType.id}">${productType.name}</a> --%>
														</span>
													</g:each>
												</td>
											</tr>
										</table>							
									</div>
									<div id="tabs-2">							
									
										<g:form method="get" action="browse">
											<table>
												<tr class="prop">
													<td class="">
														<label>Search </label>

														<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
														<span class="buttons">
															<button type="submit" class="positive">
																${message(code: 'default.button.go.label', default: 'Go')}</button>
														</span>											
													</td>
												</tr>
											</table>	
										</g:form>			
									
										<table>
											<tr>										
												<g:each in="${categories}" status="i" var="category">
													<td>
														<ul id="categories">
															<g:set var="selected" value="${category?.id==selectedCategory?.id}"/>
															<span class="${(category?.id==selectedCategory?.id)?'selected':''}">													
																<li>
																	<g:if test="${selected }">
																		<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_go.png')}" style="vertical-align: middle;"/>																			
																	</g:if>
																	<g:else>
																		<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_white.png')}" style="vertical-align: middle;"/>																			
																	</g:else>
																	<a href="${createLink(action:'browse',params:["categoryId":category.id])}">${category.name}</a>
																</li>
															</span>
															<li>
																<ul>
																	<g:each in="${category.categories}" status="j" var="childCategory">
																		<li>
																			<g:set var="selected" value="${childCategory?.id==selectedCategory?.id}"/>
																			<span class="${(selected)?'selected':''}">
																				<g:if test="${selected }">
																					<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_go.png')}" style="vertical-align: middle;"/>																			
																				</g:if>
																				<g:else>
																					<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_white.png')}" style="vertical-align: middle;"/>																			
																				</g:else>
																				<a href="${createLink(action:'browse',params:["categoryId":childCategory.id])}">${childCategory?.name }</a>
																			</span>																		
																		</li>
																	</g:each>
																</ul>
															</li>
														</ul>
													</td>
												</g:each>
											</tr>													
										</table>														
									</div>
								</div>
							</div>
						</td>			
					</tr>
					<tr>		
						<td colspan="2">
							<div>
								<fieldset>
				            		<legend>Search results</legend>						
						            <g:if test="${productInstanceList}">
						            	<div class="list">						            	
						            		<span>Your search returned ${productInstanceList?.totalCount } results</span>
							                <table>
							                    <thead>
							                        <tr>             
							                        	<%-- 
							                            <g:sortableColumn property="id" title="${message(code: 'product.id.label', default: 'ID')}" />
							                            --%>
							                            <th width="5%" style="text-align: center">${message(code: 'product.type.label', default: 'Type')}</th>
							                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
							                            <g:sortableColumn property="categories" title="${message(code: 'product.categories.label', default: 'Categories')}" />
							                        </tr>
							                    </thead>
							                    <tbody>
								                    <g:each in="${productInstanceList}" status="i" var="productInstance">
								                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								                        	            
															<td style="text-align: center" width="5%">										
																<%--<g:link action="show" id="${productInstance.id}">${fieldValue(bean: productInstance, field: "id")}</g:link> --%>
																<g:if test="${productInstance?.productClass?.name == 'Drug'}">															
																	<img src="${createLinkTo(dir:'images/icons/silk',file: 'pill.png')}"/>
																</g:if>
																<g:elseif test="${productInstance?.productClass?.name == 'Durable' }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file: 'computer.png')}"/>
																</g:elseif>
																<g:elseif test="${productInstance?.productClass?.name == 'Consumable' }">
																	<img src="${createLinkTo(dir:'images/icons/silk',file: 'cup.png')}"/>
																</g:elseif>
																<g:else>
																	<img src="${createLinkTo(dir:'images/icons/silk',file: 'help.png')}"/>
																</g:else>
															</td>
															<td align="center">
																<g:link action="edit" id="${productInstance.id}">
																	${fieldValue(bean: productInstance, field: "name")}
																	<span class="fade">
																	${fieldValue(bean: productInstance, field: "productType.name")}
																	</span>
																</g:link>
															</td>
															<td width="30%">
																${fieldValue(bean: productInstance, field: "categories")}
															</td>						                            
								                        </tr>
								                    </g:each>		                    
							                    </tbody>
							                </table>  
							            </div>
										<div class="paginateButtons">
							                <g:paginate total="${productInstanceTotal}" params="${params }" />
							            </div>		    
						                
						                
						        	</g:if>         
								</fieldset>
				            </div>
								            
				            
						</td>
					</tr>
				</table>
        	</div>
    	</div>    	
    </body>
</html>
