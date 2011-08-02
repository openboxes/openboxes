
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>    
    <body>
    	
    
        <div class="body">
		    <div class="nav">
		    	<g:render template="nav"/>		    
		    </div>
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>            
            <div class="dialog">
	            <g:form action="saveType" method="post">
                
    	        	<fieldset>
	                    <table>
	                        <tbody>                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="productClass"><warehouse:message code="productType.productClass.label" default="Product Class" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: productTypeInstance, field: 'productClass', 'errors')}">
	                                    <g:select name="productClass" from="${org.pih.warehouse.product.ProductClass?.values()}" value="${productTypeInstance?.productClass}" noSelection="['': '']" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
									<td class="name">
	                                    <label for="name"><warehouse:message code="product.name.label" default="Product" /></label>
									</td>
									<td class="value">
										<g:textField name="name" value="${productTypeInstance?.name }"/>
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
