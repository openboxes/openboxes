
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.browse.label" args="[entityName]" /></content>
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
			<table>
				<tr>
					<td>					
						<g:form method="get" action="browse">
			            	<fieldset>
			            		<legend>Filter by</legend>				            
								<table>
									<tr class="prop">
										<td class="name">
											<label>Match</label>
										</td>
										<td colspan="3">
											<g:radio name="match" value="matchAll" checked="true" disabled="true" /> Match all of the following &nbsp;&nbsp;&nbsp;<br/>
											<g:radio name="match" value="matchAll" disabled="true"/> Match any of the following <i>(not supported)</i>
											 
										</td>
										
									</tr>
									<tr class="prop">
										<td class="name">
											<label>Name contains</label>
										</td>
										<td>
											<g:textField name="nameContains" value="${params.nameContains}"/>										
										</td>										
										<td class="name">
											<label>Show only incomplete products</label>
										</td>
										<td>
											<g:checkBox name="unverified" value="${params.unverified}" />
										</td>
									</tr>
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
<!--  
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
-->									
									<tr class="prop">
										<td class="name"></td>
										<td>
											<span class="buttons">
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Filter" /> 
													${message(code: 'default.button.filter.label', default: 'Filter')}</button>
											</span>										
										</td>
									</tr>
								</table>				
							</fieldset>
						</g:form>
					</td>					
				</tr>
				
				<tr>
					<td colspan="2">
						<div>
							<fieldset>
			            		<legend>Search results</legend>						
								<div>
									Returned ${productInstanceList.size} products				            		         				            	
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
					                            <g:sortableColumn property="productType" title="${message(code: 'product.productType.label', default: 'Product Type')}" />
					                            <g:sortableColumn property="complete" title="${message(code: 'product.unverified.label', default: 'Complete')}" />
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
															<img src="${createLinkTo(dir:'images/icons/silk',file: 'page_white.png')}"/>
														</g:else>
													</td>
													<td align="center" width="40%">
														<g:link action="show" id="${productInstance.id}">
															${fieldValue(bean: productInstance, field: "name")}
														</g:link>
													</td>
													<td align="center" width="10%">
														${fieldValue(bean: productInstance, field: "ean")}
													</td>
													<td width="10%">
														${fieldValue(bean: productInstance, field: "productType.name")}
													</td>					                            
													<td valign="top" align="center" width="5%">
														<g:if test="${productInstance.unverified}">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Complete" />														
														</g:if>
														<g:else>
															<img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Complete" />														
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
