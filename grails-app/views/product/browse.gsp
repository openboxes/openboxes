
<%@ page import="org.pih.warehouse.Product" %>
<%@ page import="org.pih.warehouse.ConditionType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>
    </head>    
    <body>
        <div class="body" style="width: 95%">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>


			<div class="listcontainer">
				<ul class="list">
					<g:set var="activeClass"><g:if test="${params.browseBy == 'all' || params.browseBy == ''}">active</g:if></g:set>
					<li class="first ${activeClass}"><g:link action="browse" params="[browseBy:'all']">Show All</g:link></li>						

					<g:set var="activeClass"><g:if test="${params.browseBy == 'type'}">active</g:if></g:set>
					<li class="${activeClass}"><g:link action="browse" params="[browseBy:'type']">Type</g:link></li>

					<g:set var="activeClass"><g:if test="${params.browseBy == 'attribute'}">active</g:if></g:set>
					<li class="${activeClass}"><g:link action="browse" params="[browseBy:'attribute']">Attribute</g:link></li>

					<g:set var="activeClass"><g:if test="${params.browseBy == 'category'}">active</g:if></g:set>
					<li class="${activeClass}"><g:link action="browse" params="[browseBy:'category']">Category</g:link></li>

					<g:set var="activeClass"><g:if test="${params.browseBy == 'condition'}">active</g:if></g:set>
					<li class="${activeClass}"><g:link action="browse" params="[browseBy:'condition']">Condition</g:link></li>
				</ul>				
			</div>		
			<br clear="all"/>
			<table>
				<tr>
					<td rowspan="2" width="25%" style="border-right: 1px solid black;">
						<g:if test="${params.browseBy == 'type'}">	
							<div style="padding-left: 25px">				
								<ul>
									<g:each in="${productTypes}" status="i" var="productType">
										<li class="${i==0?'first':''}">
											<g:link class="browse" action="browse" params="[browseBy:'type', productTypeId:productType.id]">
												<g:if test="${productType?.id==selectedProductType?.id}"><span class="large"><b>${productType.name}</b></span></g:if>	
												<g:else>${productType.name}</g:else></g:link>
											( ? )								
										</li>				
									</g:each>
								</ul>
							</div>
						</g:if>
						<g:if test="${params.browseBy == 'attribute'}">	
							<div style="padding-left: 25px">				
								<ul>
									<g:each in="${attributes}" status="i" var="attribute">
										<li class="${i==0?'first':''}">
											<g:link class="browse" action="browse" params="[browseBy:'attribute', attributeId:attribute.id]">
												<g:if test="${attribute?.id==selectedAttribute?.id}"><span class="large"><b>${attribute.name}</b></span></g:if>	
												<g:else>${attribute.name}</g:else></g:link>
											( ? )								
										</li>				
									</g:each>
								</ul>
							</div>
						</g:if>
						<g:if test="${params.browseBy == 'category'}">
							<div style="padding-left: 25px">	
	           					<g:displayMenu rootNode="${rootCategory}" />		           					
								<%--            					
								<ul>							
									<g:each in="${org.pih.warehouse.Category.list()}" status="i" var="category">
										<li class="${i==0?'first':''}">
											<g:link class="browse" action="browse"  params="[browseBy:'category', categoryId:category.id]">
												<g:if test="${category?.id==selectedCategory?.id}"><span class="large"><b>${category.name}</b></span></g:if>	
												<g:else>${category.name}</g:else>		
											</g:link>
										</li>				
									</g:each>
								</ul>
								--%>
							</div>
						</g:if>						
						<g:if test="${params.browseBy == 'condition'}">					
							<div style="padding-left: 25px">	
								<ul>
									<g:each in="${conditionTypes}" status="i" var="conditionType">
										<li class="${i==0?'first':''}">
											<g:link class="browse" action="browse"  params="[browseBy:'condition', conditionTypeId:conditionType.id]">
												<g:if test="${conditionType?.id==selectedConditionType?.id}"><span class="large"><b>${conditionType.name}</b></span></g:if>	
												<g:else>${conditionType.name}</g:else></g:link>
											( ? )
										</li>				
									</g:each>
								</ul>
							</div>
						</g:if>
						<br clear="all"/>
					</td>
				</tr>
				<tr>					
					<td>
			            <div class="list">
							<div class="notice">
								Returned ${productInstanceList.size} products
				            	<g:if test="${params.browseBy == 'condition'}">
				            		<g:if test="${selectedConditionType}">
					            		where <b>${params.browseBy}</b> = <b>${selectedConditionType}</b>
				            		</g:if>
				            	</g:if>			            	
				            	<g:elseif test="${params.browseBy == 'attribute'}">
				            		<g:if test="${selectedAttribute}">
					            		where <b>${params.browseBy}</b> = <b>${selectedAttribute}</b>
				            		</g:if>
				            	</g:elseif>			            	
				            	<g:elseif test="${params.browseBy == 'type'}">
				            		<g:if test="${selectedProductType}">
					            		where <b>${params.browseBy}</b> = <b>${selectedProductType}</b>
				            		</g:if>
				            	</g:elseif>			            	
				            	<g:elseif test="${params.browseBy == 'category'}">
				            		<g:if test="${selectedCategory}">
					            		where <b>${params.browseBy}</b> = <b>${selectedCategory}</b>
				            		</g:if>
				            	</g:elseif>			            	
				            	<g:else>
				            		<!-- showing all products -->				            		
				            	</g:else>			         
				            	
				            </div>				            
				            
				            
				            <g:if test="${productInstanceList.size > 0}">
				                <table>
				                    <thead>
				                        <tr>                        
				                            <g:sortableColumn property="id" title="${message(code: 'product.id.label', default: 'ID')}" />
				                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
				                            <g:sortableColumn property="productType" title="${message(code: 'product.productType.label', default: 'Product Type')}" />
				                        </tr>
				                    </thead>
				                    <tbody>
					                    <g:each in="${productInstanceList}" status="i" var="productInstance">
					                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
												<td align="center">										
													<g:link action="show" id="${productInstance.id}">${fieldValue(bean: productInstance, field: "id")}</g:link>
												</td>
												<td align="center">
													${fieldValue(bean: productInstance, field: "name")}
												</td>
												<td>${fieldValue(bean: productInstance, field: "productType.name")}</td>					                            
					                        </tr>
					                    </g:each>		                    
				                    </tbody>
				                </table>                
			                </g:if>
			            </div>
			            
									                    	
					
					
					</td>
				</tr>
			
			
			</table>
           	

            
        </div>
    </body>
</html>
