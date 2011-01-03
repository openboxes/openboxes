
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
						<td width="25%">
							<fieldset>	
													
								<h2>Browse by category</h2>								
																
								<div style="text-align: center; padding: 10px;">
									<g:render template="../category/menuTree" model="[root:rootCategory, selected:selectedCategory, level: 0]"/>
								</div>
								<br clear="all"/>
								 
								<div style="text-align: left; padding: 5px; background-color: #f5f5f5;">
									<b>
										<g:render template="../category/breadcrumb" model="[categoryInstance: selectedCategory]"/>
									</b>
								</div>
								<div style="text-align:left;">
									<ul>
										<g:render template="../category/menuTreeOptions" model="[root:selectedCategory, selected:selectedCategory, level: 0]"/>
									</ul>
								</div>
										
												
								
								<%-- 
								<h2>Browse by attribute</h2>							
								<g:each var="attribute" in="${org.pih.warehouse.product.Attribute.list()}" status="status">
									<ul class="treeList">
										<li>
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
											${attribute.name }							
											<g:if test="${attribute.options }">
												<ul class="treeList">
													<g:each var="attributeOption" in="${attribute?.options }">
														<li>
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
															<a href="${createLink(action:'browse',params:["attributeId":attribute.id])}">${(attributeOption)?:'none' }</a>
														</li>
													</g:each>
												</ul>
											</g:if>
										</li>
									</ul>
								</g:each>	
								--%>						
							</fieldset>					
						</td>			
						<td>
							<div>
		            			<table>
			            			<tbody>
					            		<g:each var="key" in="${productsByCategory.keySet() }">
					            			<tr>
					            				<th><h2><g:render template="../category/breadcrumb" model="[categoryInstance: key]"/></h2></th>
					            			</tr>
					            			<g:each var="productInstance" in="${productsByCategory.get(key) }" status="i">
												 <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
													<td>
														<g:link action="edit" id="${productInstance.id}">
															${fieldValue(bean: productInstance, field: "name") }
														</g:link>
													</td>
												</tr>				            				
					            			
					            			</g:each>
					            		</g:each>
			            			</tbody>
		            			</table>
			            		<div style="text-align: right;">
			            			Showing ${productInstanceList?.totalCount } products
			            		</div>
		            		
			            		
			            		<%-- 
					            <g:if test="${productInstanceList}">
					            	<div class="list">						            	
						                <table>
						                    <thead>
						                        <tr>             
						                        	<th>Type</th>
						                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
						                        </tr>
						                    </thead>
						                    <tbody>
							                    <g:each in="${productInstanceList}" status="i" var="productInstance">
							                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
														<td width="5%">
															${productInstance?.category?.name }
														</td>
														<td align="center">
															<g:link action="edit" id="${productInstance.id}">
																${fieldValue(bean: productInstance, field: "name")}
															</g:link>
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
					        	--%>
					        	
					        	<%-- 
								<g:if test="${categoryInstance }">
									<div id="inline">
										<fieldset>
											<g:render template="productForm" model="[productInstance:productInstance]"/>
										</fieldset>
									</div>
								</g:if>
								--%>
				            </div>
						</td>
					</tr>
				</table>
        	</div>
    	</div>    	
    </body>
</html>
