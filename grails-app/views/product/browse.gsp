
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
		<g:javascript library="prototype" />
    </head>    
    <body>
		<h1>Browse Products</h1>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>


			<%-- 
           	<div>
           		<g:displayCategories categories="${categories}" />
           	</div>
            --%>
            <div class="listcontainer">
            	<div class="list">
					<ul>
						<g:set var="activeClass"><g:if test="${params.browseBy == 'all' || params.browseBy == ''}">active</g:if></g:set>
						<li class="large first ${activeClass}"><g:link action="browse" params="[browseBy:'all']">All</g:link></li>						
						<g:set var="activeClass"><g:if test="${params.browseBy == 'category'}">active</g:if></g:set>
						<li class="large ${activeClass}"><g:link action="browse" params="[browseBy:'category']">Category</g:link></li>
						<g:set var="activeClass"><g:if test="${params.browseBy == 'condition'}">active</g:if></g:set>
						<li class="large ${activeClass}"><g:link action="browse" params="[browseBy:'condition']">Condition</g:link></li>
						<g:set var="activeClass"><g:if test="${params.browseBy == 'type'}">active</g:if></g:set>
						<li class="large ${activeClass}"><g:link action="browse" params="[browseBy:'type']">Type</g:link></li>
					</ul>
					<br clear="all"/>
					<g:if test="${params.browseBy == 'category'}">
						<div style="padding-left: 25px">	
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
						</div>
						<br clear="all"/>
					</g:if>
					<g:elseif test="${params.browseBy == 'condition'}">					
						<div style="padding-left: 25px">	
							<ul>
								<g:each in="${conditionTypes}" status="i" var="conditionType">
									<li class="${i==0?'first':''}">
										<g:link class="browse" action="browse"  params="[browseBy:'condition', conditionTypeId:conditionType.id]">
											<g:if test="${conditionType?.id==selectedConditionType?.id}"><span class="large"><b>${conditionType.name}</b></span></g:if>	
											<g:else>${conditionType.name}</g:else>		
										</g:link>
									</li>				
								</g:each>
							</ul>
						</div>
						<br clear="all"/>
					</g:elseif>
					<g:elseif test="${params.browseBy == 'type'}">	
						<div style="padding-left: 25px">				
							<ul>
								<g:each in="${productTypes}" status="i" var="productType">
									<li class="${i==0?'first':''}">
										<g:link class="browse" action="browse" params="[browseBy:'type', productTypeId:productType.id]">
											<g:if test="${productType?.id==selectedProductType?.id}"><span class="large"><b>${productType.name}</b></span></g:if>	
											<g:else>${productType.name}</g:else>
										</g:link>
									</li>				
								</g:each>
							</ul>
						</div>
						<br clear="all"/>
						<div style="padding-left: 50px">
							<ul>
								<g:each in="${productSubTypes}" status="i" var="productSubType">
									<li class="${i==0?'first':''}">
										<g:link class="browse" action="browse" params="[browseBy:'type', productTypeId:productSubType.parent.id, productSubTypeId:productSubType.id]">
											<g:if test="${productSubType?.id==selectedProductSubType?.id}">
												<span class="large"><b>${productSubType.name}</b></span></g:if>	
											<g:else>${productSubType.name}</g:else>									
										</g:link>
									</li>				
								</g:each>
							</ul>
						</div>
						<br clear="all"/>
					</g:elseif>
					<g:else>
						<br/>					
					</g:else>
				</div>
            </div>
                        
			<br clear="all"/>
            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}" />
                            <g:sortableColumn property="ean" title="${message(code: 'product.ean.label', default: 'UPC')}" />
                            <g:sortableColumn property="ean" title="${message(code: 'product.type.label', default: 'Type')}" />
                            <g:sortableColumn property="ean" title="${message(code: 'product.subtype.label', default: 'Subtype')}" />
                            <g:sortableColumn property="ean" title="${message(code: 'product.category.label', default: 'Categories')}" />
                            <g:sortableColumn property="ean" title="${message(code: 'product.conditionType.label', default: 'Conditions')}" />
						    <th><g:message code="product.details.label" default="Details" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${productInstanceList}" status="i" var="productInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
                            <td>${fieldValue(bean: productInstance, field: "name")}</td>
                            <td>${fieldValue(bean: productInstance, field: "ean")}</td>
                            <td>${fieldValue(bean: productInstance, field: "type.name")}</td>
                            <td>${fieldValue(bean: productInstance, field: "subType.name")}</td>
                            <td>${fieldValue(bean: productInstance, field: "categories")}</td>
                            <td>${fieldValue(bean: productInstance, field: "conditionTypes")}</td>
							<td align="center"><g:link action="show" id="${productInstance.id}">view</g:link></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>                
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
