
<%@ page import="org.pih.warehouse.product.Product" %>
<%@ page import="org.pih.warehouse.product.ProductClass" %>
<%@ page import="org.pih.warehouse.product.ProductType" %>
<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
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
            <div class="dialog">
	            <g:form action="create" method="post">
                
    	        	<fieldset>
	                    <table>
	                        <tbody>                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><g:message code="product.name.label" default="Add a new product" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                
	                                	<table>
											<tr class="prop">
												<td>
	                                				<img src="${createLinkTo(dir:'images/icons/silk', file:'pill.png') }"/> &nbsp;
													<g:link action="create" params="[productClass: ProductClass.DRUG]">Medicines</g:link>
												</td>
											</tr>
											<tr>
												<td style="padding-left: 50px;">
													<g:each var="productType" in="${ProductType.findAllByProductClass(ProductClass.DRUG) }">
														<g:link action="create" params="['productType.id': productType.id]">${productType?.name }</g:link>
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
													</g:each>
												</td>
											</tr>
											
											<tr class="prop">
												<td>
	                    			            	<img src="${createLinkTo(dir:'images/icons/silk', file:'cup.png') }"/>  &nbsp;
													<g:link action="create" params="[productClass: org.pih.warehouse.product.ProductClass.CONSUMABLE]">Supplies & Consumable</g:link>
												</td>
	                                		</tr>
											<tr>
												<td style="padding-left: 50px;">
													<g:each var="productType" in="${ProductType.findAllByProductClass(ProductClass.CONSUMABLE) }">
														<g:link action="create" params="['productType.id': productType.id]">${productType?.name }</g:link>
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
													</g:each>
												</td>
											</tr>
											<tr class="prop">
												<td>
	                    			            	<img src="${createLinkTo(dir:'images/icons/silk', file:'computer.png') }"/> &nbsp;
													<g:link action="create" params="[productClass: org.pih.warehouse.product.ProductClass.DURABLE]">Equipment & Furniture</g:link>
												</td>
											</tr>
											<tr>
												<td style="padding-left: 50px;">
													<g:each var="productType" in="${ProductType.findAllByProductClass(ProductClass.DURABLE) }">
														<g:link action="create" params="['productType.id': productType.id]">${productType?.name }</g:link>
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
													</g:each>
													<g:link action="createType" params="[productClass: ProductClass.DRUG]">add new  &rsaquo;</g:link>													
												</td>
											</tr>
	                                	</table>
	                                </td>
	                            </tr>
	                        </tbody>
	                    </table>
	                </fieldset>              
	            </g:form>
           	</div>
        </div>
    </body>
</html>
