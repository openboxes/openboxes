
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><warehouse:message code="default.browse.label" args="[entityName]" /></title>
		<style>
			.data-table td, .data-table th { vertical-align: middle; }
		</style>
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
					<tr>
						<td style="border-right: 1px solid lightgrey; width: 150px;">	
						
							<g:render template="/common/searchProduct"	/>				
				
						</td>			
						<td>
							<g:set var="attributeList" value="${org.pih.warehouse.product.Attribute.list() }"></g:set>
							<g:if test="${productsByCategory }">
								<div>
		            				<table border="0" >
	          							<thead>
			            					<tr class="odd">
		            							<th>Category</th>
		            							<th>Description</th>
		            						</tr>
		            					</thead>
		            				</table>
		            			</div>
		            			<div style="overflow: auto; height: 600px;">
		            				<table border="0" class="data-table">
				            			<tbody>
											<g:set var="index" value="${0 }"/>
						            		<g:each var="key" in="${productsByCategory.keySet() }">
						            			<g:each var="productInstance" in="${productsByCategory.get(key) }" status="i">
													 <tr class="${(index++ % 2) == 0 ? 'even' : 'odd'}">
				            							<td>
				            								${productInstance?.category?.name }
				            							</td>
														<td>
															<g:link action="edit" id="${productInstance.id}">
																<g:if test="${productInstance?.name }">
																	${fieldValue(bean: productInstance, field: "name") }  
																</g:if>
																<g:else>
																	Untitled Product
																</g:else>
																<span class="fade">${productInstance?.id }</span>
															</g:link>
														</td>
														<%--
				            							<g:each var="attribute" in="${attributeList}">
				            								<td></td>
				            							</g:each>
				            							 --%>
													</tr>							            			
						            			</g:each>
						            		</g:each>
				            			</tbody>
			            			</table>
			            		</div>
		            		</g:if>
		            		<g:else>
			            		
								<table border="0">
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
	       				 

	       				    
						</td>
					</tr>
					
				</table>
        	</div>
    	</div>    	
    </body>
</html>
