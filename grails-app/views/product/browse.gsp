
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
    </head>    
    <body>
        <div class="body">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>		
            <div>            
				<table>
				
					<%-- 
					<tr>
						<td colspan="2">
							<div style="text-align: left; padding: 5px; background-color: #fff;">
								<h2>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'map.png') }"/>
				            		You are here: 
								
									<g:render template="../category/breadcrumb" model="[categoryInstance: selectedCategory]"/>
								</h2>
							</div>
						</td>
					</tr>
					--%>
					<tr>
						<td width="25%">	
						
							<g:render template="/common/searchCriteriaVertical"	/>				
							<%-- 		
							<div style="text-align:left; padding-left: 20px;">
								<style>
									.myMenu li { margin: 2px; padding: 2px; }
									.myMenu ul li { margin: 2px; padding: 2px; }
								</style>							
								<ul class="myMenu">
									<b>${selectedCategory?.name }</b>
									<g:render template="../category/menuTreeOptions" model="[root:selectedCategory, selected:selectedCategory, level: 0, recursive: false]"/>
								</ul>
							</div>
							<br clear="all"/><br/>
							<div style="text-align: center; padding: 10px;">
								<g:render template="../category/menuTree" model="[root:rootCategory, selected:selectedCategory, level: 0, recursive: true]"/>
							</div>
							--%>
								 
								
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
						</td>			
						<td>
						
							Showing ${productInstanceList?.size() } products
							<g:set var="attributeList" value="${org.pih.warehouse.product.Attribute.list() }"></g:set>
							<g:if test="${productsByCategory }">
	            				<table border="1" style="border: 1px solid lightgrey">
          							<thead>
		            					<tr>
	            							<th width="5%">ID</th>
	            							<th width="10%">Code</th>
	            							<th>Description</th>
	            							<g:each var="attribute" in="${attributeList}">
	            								<th>${attribute.name }</th>
	            							</g:each>
	            							<th>Cold Chain</th>
	            							<th>Primary Category</th>
	            						</tr>
	            					</thead>
			            			<tbody>
										<g:set var="index" value="${0 }"/>
					            		<g:each var="key" in="${productsByCategory.keySet() }">
					            			<g:each var="productInstance" in="${productsByCategory.get(key) }" status="i">
												 <tr class="${(index++ % 2) == 0 ? 'odd' : 'even'}">
												 	<td>
														${productInstance?.id }													 	
												 	</td>
												 	<td>
												 		${productInstance?.productCode }
												 	</td>
													<td>
														<g:link action="edit" id="${productInstance.id}">
															${fieldValue(bean: productInstance, field: "name") }
														</g:link>
													</td>
			            							<g:each var="attribute" in="${attributeList}">
			            								<td></td>
			            							</g:each>
			            							<td>${(productInstance?.coldChain)?"Yes":"No" }</td>
			            							<td>${productInstance?.category?.name }</td>
												</tr>							            			
					            			</g:each>
					            		</g:each>
			            			</tbody>
		            			</table>
		            		</g:if>
		            		<g:else>
			            		
								<table border="1" style="border: 1px solid lightgrey">
          							<thead>
		            					<tr>
	            							<th width="5%">ID</th>
	            							<th width="10%">Code</th>
	            							<th>Description</th>
	            							<g:each var="attribute" in="${attributeList}">
	            								<th>${attribute.name }</th>
	            							</g:each>
	            							<th>Cold Chain</th>
	            							<th>Primary Category</th>
	            						</tr>
	            					</thead>

			            			<tbody>
			            				<tr class="odd">
			            					<td colspan="${ (attributeList.size()+5) }">
			            						There are no products matching the given criteria.
			            					</td>
			            				</tr>
			            			</tbody>
		            			</table>			            		
			            	</g:else>
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
	       				    <div style="text-align: left; padding: 10px; border-top: 0px solid #f7f7f7;">
	       				    	<table>
	       				    		<tr>
	       				    			<td>
					        				<span class="menuButton">
												<g:link class="new" controller="product" action="create" params="['category.id':params.categoryId]"><g:message code="default.add.label" args="['Product']"/></g:link> 			
				        				    </span>
	       				    			
	       				    			</td>
	       				    			<td style="text-align: right;">
					            			
	       				    			</td>
	       				    		</tr>
	       				    	</table>
	       				    </div>		
	       				    
						</td>
					</tr>
					
				</table>
        	</div>
    	</div>    	
    </body>
</html>
